package com.projectw.domain.reservation.service;

import com.projectw.common.annotations.RedisListener;
import com.projectw.common.enums.ResponseCode;
import com.projectw.common.exceptions.ForbiddenException;
import com.projectw.common.exceptions.NotFoundException;
import com.projectw.common.utils.Scheduler;
import com.projectw.domain.menu.entity.Menu;
import com.projectw.domain.menu.repository.MenuRepository;
import com.projectw.domain.payment.event.PaymentCancelEvent;
import com.projectw.domain.payment.event.PaymentTimeoutCancelEvent;
import com.projectw.domain.reservation.component.ReservationCheckService;
import com.projectw.domain.reservation.dto.ReserveRequest;
import com.projectw.domain.reservation.dto.ReserveResponse;
import com.projectw.domain.reservation.entity.Reservation;
import com.projectw.domain.reservation.entity.ReservationMenu;
import com.projectw.domain.reservation.enums.ReservationStatus;
import com.projectw.domain.reservation.enums.ReservationType;
import com.projectw.domain.reservation.exception.InvalidCartException;
import com.projectw.domain.reservation.repository.ReservationMenuRepository;
import com.projectw.domain.reservation.repository.ReservationRepository;
import com.projectw.domain.store.entity.Store;
import com.projectw.domain.store.repository.StoreRepository;
import com.projectw.domain.user.entity.User;
import com.projectw.domain.user.repository.UserRepository;
import com.projectw.domain.waiting.dto.WaitingPoll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMenuRepository reservationMenuRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;

    private final ReservationCheckService reservationCheckService;

    private final ApplicationEventPublisher eventPublisher;
    private final Scheduler scheduler;
    private final RedisTemplate<String , Object> redisTemplate;

    @Transactional
    public void prepareReservation(ReserveRequest.InsertReservation insertReservation) {
        // 예약번호 채번하기
        LocalDate now = LocalDate.now();
        Long reservationNo = reservationRepository.findMaxReservationDate(ReservationType.RESERVATION , now);

        // 장바구니에 메뉴가 담겨있는지 검증
        String key = assembleCartRedisKey(insertReservation.user().getId() , insertReservation.store().getId());
        Map<Object, Object> redisMenus = redisTemplate.opsForHash().entries(key);
        List<Menu> menus = getMenus(redisMenus);
        boolean menuYN = !menus.isEmpty();

        // 예약 Entity 만들기
        Reservation reservation = Reservation.builder()
                .orderId(insertReservation.orderId())
                .status(ReservationStatus.RESERVATION)      // 예약 단계!!(승인 안된 상태!)
                .type(ReservationType.RESERVATION)          // 웨이팅 , 예약 중 예약이라는 의미
                .reservationDate(insertReservation.reservationDate())
                .reservationTime(insertReservation.reservationTime())
                .numberPeople(insertReservation.numberPeople())
                .reservationNo(reservationNo)
                .menuYN(menuYN)
                .paymentYN(false)
                .paymentAmt(insertReservation.paymentAmt())
                .user(insertReservation.user())
                .store(insertReservation.store())
                .build();

        Reservation saveReservation = reservationRepository.save(reservation);

        // menu 값이 있을때
        if (menuYN) {
            List<ReservationMenu> reservationMenus = new ArrayList<>();
            for (Menu menu : menus) {
                Map<String, Object> info = (Map<String, Object>) redisMenus.get(String.valueOf(menu.getId()));
                Long cnt = (Long) info.get("cnt");
                ReservationMenu reservationMenu = new ReservationMenu(menu , menu.getName() , (long) menu.getPrice(), cnt , saveReservation);
                reservationMenus.add(reservationMenu);
                redisTemplate.opsForHash().delete(key, String.valueOf(menu.getId()));
            }
            reservationMenuRepository.saveAll(reservationMenus);
        }

        // 지정한 시간 후에 자동 실행!!
        scheduler.scheduleOnceAfterDelay(10 , TimeUnit.MINUTES , this::autoCancelMethod, saveReservation.getId());
    }

    // 결제 완료
    @Transactional
    public void successReservation(String orderId) {
        Reservation reservation = reservationRepository.findByOrderId(orderId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_RESERVATION));
        reservation.updatePaymentYN(true);
    }

    @Transactional
    public void cancelReservation(Long userId , Long storeId , Long reservationId , ReserveRequest.Cancel cancel) {
        // 예약 어떤지?
        Reservation reservation = reservationRepository.findByIdAndStoreId(reservationId , storeId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_RESERVATION));

        // 본인 예약건인지?
        reservationCheckService.isUserReservation(userId , reservation);

        // 예약을 취소할수 있는 타입인지 검증
        reservationCheckService.canChangeReservationType(reservation , ReservationType.RESERVATION);

        switch (reservation.getStatus()) {
            case RESERVATION:
            case APPLY:
                reservation.updateStatus(ReservationStatus.CANCEL);

                if (reservation.isPaymentYN()) {
                    // PaymentEventListener 결제취소
                    PaymentCancelEvent paymentCancelEvent = new PaymentCancelEvent(reservation.getOrderId() , cancel.cancelReason());
                    eventPublisher.publishEvent(paymentCancelEvent);
                }
                break;
            default:
                throw new ForbiddenException(ResponseCode.CANCEL_FORBIDDEN);
        }
    }

    public Page<ReserveResponse.Infos> getUserReservations(Long userId , ReserveRequest.Parameter parameter) {
        Pageable pageable = PageRequest.of(parameter.page() - 1, parameter.size());
        return reservationRepository.getUserReservations(userId , parameter , pageable);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void autoCancelMethod(Long reservationId) {
        log.info("autoCancelMethod 접근!!");
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_RESERVATION));

        if (!reservation.isPaymentYN()) {
            // 예약건 취소
            reservation.updateStatus(ReservationStatus.CANCEL);
            reservationRepository.save(reservation);
            // 결제건 취소
            eventPublisher.publishEvent(new PaymentTimeoutCancelEvent(reservation.getOrderId()));
        }
    }

    public void addCartItem(Long userId , Long storeId , ReserveRequest.AddCart addCart) {
        // 들어온 데이터가 메뉴의 id 길이와 cnt의 길이가 다를 경우 예외처리!!
        if (addCart.menuIds().size() != addCart.menuCnt().size()) {
            throw new InvalidCartException(ResponseCode.INVALID_CART);
        }
        // key 생성!
        String key = assembleCartRedisKey(userId , storeId);

        List<String> menuIds = addCart.menuIds();
        List<Long> menuCnt = addCart.menuCnt();

        // redis에 담기
        for (int i = 0; i < menuIds.size(); i++) {
            Map<String, Object> newMenu = new HashMap<>();
            newMenu.put("cnt" , menuCnt.get(i));
            redisTemplate.opsForHash().put(key, menuIds.get(i), newMenu);
        }
        redisTemplate.expire(key , 24 , TimeUnit.HOURS);
    }

    public void updateCartItem(Long userId , Long storeId , ReserveRequest.UpdateCart updateCart) {
        // key 생성!
        String key = assembleCartRedisKey(userId , storeId);
        if (redisTemplate.opsForHash().hasKey(key, updateCart.menuId())) {
            Map<String, Object> newMenu = new HashMap<>();
            newMenu.put("cnt" , updateCart.menuCnt());
            redisTemplate.opsForHash().put(key, updateCart.menuId(), newMenu);
        }
    }

    public void removeCartItem(Long userId , Long storeId , ReserveRequest.RemoveCart removeCart) {
        String key = assembleCartRedisKey(userId , storeId);
        // redis에 있는지 검증
        if (redisTemplate.opsForHash().hasKey(key, removeCart.menuId())) {
            // 삭제
            redisTemplate.opsForHash().delete(key, removeCart.menuId());
        }
    }

    public List<ReserveResponse.Carts> getCartItems(Long userId , Long storeId) {
        String key = assembleCartRedisKey(userId , storeId);
        // redis에 key값 조회
        Map<Object, Object> redisMenus = redisTemplate.opsForHash().entries(key);
        // menus 조회
        List<Menu> menus = getMenus(redisMenus);

        List<ReserveResponse.Carts> carts = new ArrayList<>();
        for (Menu menu : menus) {
            Map<String, Object> info = (Map<String, Object>) redisMenus.get(String.valueOf(menu.getId()));
            Long cnt = (Long) info.get("cnt");
            ReserveResponse.Carts cart = new ReserveResponse.Carts(menu.getId() , menu.getName() , menu.getPrice() , cnt);
            carts.add(cart);
        }
        return carts;
    }

    private List<Menu> getMenus(Map<Object, Object> redisMenus) {
        List<Long> menuIds = new ArrayList<>();

        for (Object menuId : redisMenus.keySet()) {
            menuIds.add(Long.parseLong((String) menuId));
        }

        return menuRepository.getMenus(menuIds);
    }

    private String assembleCartRedisKey(Long userId , Long storeId) {
        return "user:"+userId+":store:"+storeId;
    }

    @Transactional
    @RedisListener(topic = "waiting-poll")
    public void onWaitingPoll(WaitingPoll waitingPoll){
        LocalDateTime at = waitingPoll.createdAt();
        Long userId = waitingPoll.userId();
        Long storeId = waitingPoll.storeId();
        Long num = waitingPoll.waitingNum();

        User user = userRepository.findById(userId).orElseThrow(()-> new NotFoundException(ResponseCode.NOT_FOUND_USER));
        Store store = storeRepository.findById(storeId).orElseThrow(()-> new NotFoundException(ResponseCode.NOT_FOUND_STORE));

        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.APPLY)
                .type(ReservationType.WAIT)
                .user(user)
                .store(store)
                .reservationDate(at.toLocalDate())
                .reservationTime(at.toLocalTime().truncatedTo(TimeUnit.SECONDS.toChronoUnit()))
                .numberPeople(1L)
                .reservationNo(num)
                .menuYN(false)
                .build();

        reservationRepository.save(reservation);
    }
}
