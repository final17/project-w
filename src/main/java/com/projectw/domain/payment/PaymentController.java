package com.projectw.domain.payment;


//
//@Controller
//@RequiredArgsConstructor
//@RequestMapping("/payments")
//public class PaymentController {

//    private final PaymentService paymentService;
//    @GetMapping
//    public String page() {
//        return "paytest";
//    }
//
//    @PostMapping
//    public ResponseEntity<SuccessResponse<TempOrderResponse.Create>> createTempOrder(String orderName, long totalAmount) {
//        return ResponseEntity.ok(SuccessResponse.of(paymentService.createTempOrder(orderName, totalAmount)));
//    }
//
//    @ResponseBody
//    @GetMapping("/success")
//    public TossPaymentResponse paySuccess(HttpServletRequest httpServletRequest,
//                                          @RequestParam(value = "orderId") String orderId,
//                                          @RequestParam(value = "amount") Integer amount,
//                                          @RequestParam(value = "paymentKey") String paymentKey) throws Exception {
//        return paymentService.onSuccessPay(orderId, amount, paymentKey);
//    }
//
//    @ResponseBody
//    @GetMapping("/fail")
//    public void payFaile(
//            @RequestParam("message") String message,
//            @RequestParam("code") int code
//    ) {
//        System.out.println("fail : " + "message : " + message + " code : " + code);
//    }
//}
