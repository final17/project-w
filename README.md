# 웨이티 프로젝트
## 📖 프로젝트 소개
맛집 예약 서비스는 사용자들이 손쉽게 맛집을 예약할 수 있도록 도와주는 시스템입니다. 
이 서비스를 개발하게 된 이유는, 많은 사람들이 맛집 예약 과정에서 겪는 불편함을 해소하고자 했기 때문입니다. 
이번 프로젝트에서는 다양한 기술을 활용해 직관적이고 편리한 예약 경험을 제공하는 것을 목표로 하고 있습니다.

## ⌚ 프로젝트 핵심 목표

1. **대규모 트래픽 대응**
    - Jmeter를 활용해서 동시성 문제가 생긴 기능들 발견 후 동시성 해결
      대기열 및 트래픽 처리 과정에서 Redis 기반 구조로 전환해 안정적인 성능을 확보.
    

2. **성능 최적화**
    - Redis의 RAtomicLong, RScoreSortedSet, RQueue, RMap 등을 비교하여 성능을 테스트하고 최적의 데이터 구조를 선택해 처리 속도를 개선.
    - Redis 기반 캐싱으로 실시간 상품 조회 성능을 3배 향상.
    - Redisson을 사용하여 CPU 점유율 50% 감소 및 안정적 데이터 처리 구현.


3. **운영 및 배포 효율화**
    - Jenkins, Docker, Github Actions, AWS EC2을 이용한 CI/CD 파이프라인 구축으로 배포 자동화.
    - Slack 알림을 설정하여 배포 상태 및 완료 여부를 실시간으로 팀원들에게 전달해 협업 효율성 강화.
    - 보안 그룹 설정과 SSL 인증서를 적용하여 보안성을 강화한 배포 환경 구축.


4. **대용량 데이터 처리 자동화**
    - Spring Batch와 Scheduler를 통해 집계를 자동화하고 QueryDSL를 통해 효율적인 데이터베이스 접근 및 처리 최적화
    - 처리된 데이터를 다양한 엔티티로 분산 저장하여 후속 분석 및 보고에 최적화된 구조 구현

   
5. **검색 기능**
   - Redis 캐싱을 이용해서 100만건 데이터에서 음식점 이름을 조회할 때 1048.9ms에서 4.32ms로 약 24278% 향상되었습니다.
   - 이후, 엘라스틱 서치를 사용해서 검색 속도 개선 및 검색 정확도 향상


6. **웹 크롤링**
   - 음식점 이름과 지역명 추출 후 "맛집" 키워드 추가 하여 네이버 기반 웹 크롤링
   - Redis 캐시 및 ec2 프록시 ip를 확용하여 네이버 ip밴 가능성 축소

## 📚 기술 스택

### 💻 Developers
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=Spring&logoColor=white)
![Spring](https://img.shields.io/badge/Spring%20JPA-6DB33F?style=for-the-badge&logo=Spring&logoColor=white)
![Spring](https://img.shields.io/badge/Spring%20JWT-FBBA00?style=for-the-badge&logo=Spring&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)
![ELK](https://img.shields.io/badge/ELK-%230377CC.svg?style=for-the-badge&)
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)
![AWS EC2](https://img.shields.io/badge/AWS%20EC2-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)
![AWS RDS](https://img.shields.io/badge/AWS%20RDS-%230056B3.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS%20S3-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white)
![YAML](https://img.shields.io/badge/yaml-%23ffffff.svg?style=for-the-badge&logo=yaml&logoColor=151515)

### 🎛️ 환경
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-%23000000.svg?style=for-the-badge&logo=notion&logoColor=white)
![Slack](https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white)
![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)
![macOS](https://img.shields.io/badge/mac%20os-000000?style=for-the-badge&logo=macos&logoColor=F0F0F0)
![Windows](https://img.shields.io/badge/Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white)

## 🏠 멤버 구성 및 기능 구현
|                     황호진                      |                 정재호                  |                   조수현                    |                    길용진                    |                 김윤서                  |
|:--------------------------------------------:|:------------------------------------:|:----------------------------------------:|:-----------------------------------------:|:------------------------------------:|
| [@ballqs](https://github.com/ballqs) | [@Nameless1004](https://github.com/Nameless1004) | [@SuHyun-git](https://github.com/SuHyun-git) | [@pumaclass](https://github.com/pumaclass) | [@yunseokim119](https://github.com/yunseokim119) |

---
## KEY Summary
### 🍁**성능 개선 :**
1. **한 줄 요약**

2. **도입 배경**
  
3. **기술적 선택지**
---


## 🍁 **트러블 슈팅 및 성능 개선 **

<details>
<summary style="font-size: 16px; font-weight: bold">🛠 예약 대기열 <span style="background-color: red;">동시성</span> 이슈</summary>


<details>
<summary><strong>⚠️ 문제 발견</strong></summary>

> - **현상**: JMeter로 1,000명의 유저가 동시에 대기열에 등록했을 때, 발권 번호가 중복으로 발생하는 동시성 이슈가 확인되었습니다.

</details>

<details>
<summary><strong>🔍 상황 분석</strong></summary>

> - **분석**: 대기열 등록 로직이 발권번호 테이블과 웨이팅 테이블로 구성되어 있으며, 동시에 접근 시 발권번호가 올바르게 증가하지 않고 중복된 번호가 저장되는 문제가 있었습니다.

</details>

<details>
<summary><strong>📝 1차 시도: <code>synchronized</code> 사용</strong></summary>

> - **방법**: 발권번호 증가 부분에 `synchronized` 키워드를 적용하여 동시성 제어를 시도했습니다.
> - **결과**: 중복이 줄어들었지만 완전히 해결되지는 않았습니다.
    - `@Transactional`로 인해 `synchronized`가 끝난 후 커밋 시점에서 다른 스레드들이 값을 변경하여 발생한 문제임을 확인했습니다.

</details>

<details>
<summary><strong>🔒 2차 시도: 비관적 락(Pessimistic Lock) 적용</strong></summary>

> - **방법**: 동시성 충돌 가능성이 높은 환경을 고려해, 비관적 락을 사용하여 발권번호 테이블의 중복 문제를 해결했습니다.
> - **결과**: 중복 문제는 해결되었으나, 높은 트래픽 상황에서 데드락이 발생하여 일부 트랜잭션이 롤백되었습니다.
    - `show engine innodb status` 명령어로 데드락 로그를 분석한 결과, 동일한 `storeId`와 `waiting_number` 값을 삽입하려는 트랜잭션 간의 충돌로 데드락이 발생한 것을 확인했습니다.

</details>

<details>
<summary><strong>💡 3차 시도: Redis로 전환</strong></summary>

> - **방법**: RDB 대신 **Redis**를 사용하여 싱글 스레드 기반으로 순차적 처리를 유도했습니다. Redis는 메모리 기반이므로 Lock 없이도 효율적으로 동시성을 관리할 수 있었습니다.
>- **결과**: 처리율이 약 **43.2%** 개선되었고, 응답 시간도 **약 30.8%** 단축되었습니다.
    - 다만, Redis는 메모리 기반이기 때문에 데이터 휘발성 문제를 해결하기 위한 추가적인 처리가 필요합니다.
</details>
<br/>
</details>
<br/>
<details>
<summary style="font-size: 16px; font-weight: bold">🔍 음식점 검색 기능 속도 향상 </summary>

1. **배경**
   - 100만건 정도의 큰 데이터를 LIKE를 통해 검색
2. **문제**
   - 검색 속도가 1초 정도로 느려 검색 속도 향상이 필요함
3. **해결 방안**
   - 검색 속도를 향상 시킬 수 있는 방법
     1. index를 적용한다.
     2. Redis 캐시를 이용한다.
   <details>
   <summary>1차 시도: index를 적용</summary>

     - Single Index: 하나의 컬럼에 인덱스를 설정하여 해당 컬럼에 대한 검색을 최적화   
       - 인덱스를 타고 검색된 내용은 0.843 sec -> 0.031 sec로 속도가 향상됨   
       - Single Index를 타고 검색된 내용은 속도가 빨라졌지만 인덱스를 타지 않는 내용은 속도향상이 안됨   
       - Single Index 특성상 "검색어%"만 인덱스를 타고, "%검색어" 또는 "%검색어%"는 인덱스를 타지 않는 문제가 발생함
     - Full text index: 전문 검색 인덱스로 텍스트 기반의 검색을 최적화 하기 위한 인덱스
       - full text index 또한 "검색어%"만 인덱스를 타는 문제가 발생
     - Full text index N-Gram: n-gram 방식은 full text index와 single index의 문제를 해결할 수 있을것 같아 도입   
       - full text index로 인해 검색의 정확도는 Like보다 높아졌지만 속도는 더 느려지는 현상이 발생   
       - 위 현상의 이유를 찾아본 결과 full text index의 n-gram의 경우 접두어 검색에서는 like보다 빠른 성능을 보여주었지만 접미어 경우에는 like보다 느린 속도를 보여주어 일관된 검색속도가 나오지 않는 문제가 발생   
       - 가끔 like보다 10배 정도 느린 문제가 발생
   </details>
   <details>
      <summary>2차 시도: Redis 캐싱 적용</summary>

      - Redis 캐싱 전략에 맞는지? 음식점의 내용과 이름은 변경이 적고, 검색이 매우 많기 때문에 Redis 캐싱에 적합하다고 생각
      - Redis에는 페이징 처리된 결과를 넣을 수 없어 dto를 따로 만들어서 10건씩 저장
      - 결과: 1048ms -> 4.32ms로 속도가 향상됨
   </details>
4. **결과**
   - 검색 속도가 1048.90ms에서 4.32ms로 향상되었다. 
   
   

</details>
<br/>
<details>
<summary style="font-size: 16px; font-weight: bold"> ⚙️ 동시성 문제 해결 </summary>

1. **배경**
- **가게, 메뉴, 리뷰 좋아요 기능**

2. **문제**
- **여러 사용자가 동시에 동일한 가게, 메뉴, 혹은 리뷰에 대해 좋아요를 누르거나 취소하는 요청을 보낼 경우 문제가 발생**
    - 좋아요 중복 저장: 두 개의 "좋아요"가 중복으로 저장되어 실제 상태와 데이터베이스 상태가 불일치하게 됨.
    - 중복 취소: 이미 취소된 좋아요를 다시 취소하려고 시도하는 상황이 발생하여 데이터 무결성을 해침.
    - 좋아요 수의 불일치: 요청이 경합하는 동안 좋아요 추가와 취소가 혼재되어 최종적인 좋아요 수가 비정상적으로 기록됨.

3. **해결 방안**
- **Redisson의 분산 락 RLock을 도입**
    - 락 생성 및 관리:
      Redis의 RLock을 사용하여 리소스별로 고유한 락 키를 생성합니다.
      예: lock:like:<entity_id> 형태의 키를 사용해 특정 가게, 메뉴, 리뷰에 대한 락을 개별적으로 관리.
    - 락 획득:
      lock.tryLock 메서드를 통해 락을 획득합니다.
      대기 시간 동안 락을 시도하며, 락을 획득하면 지정된 시간 동안 다른 스레드가 해당 리소스에 접근하지 못하도록 보장.
    - 동시성 제어:
      락을 통해 한 번에 하나의 스레드만 toggleLike 메서드를 실행하도록 보장합니다.
    - 락 해제:
      작업이 완료되면 락을 해제하여 다른 요청이 해당 리소스를 처리할 수 있도록 허용.
      
4. **결과**
- **좋아요 중복 방지**:
    동일한 리소스에 대해 동시 접근이 직렬화되어, 두 개의 "좋아요"가 중복으로 저장되는 문제를 방지.
- **좋아요 취소 중복 방지**:
    이미 취소된 좋아요에 대해 추가적인 취소 요청이 무시됨.
- **데이터 일관성 보장**:
    좋아요 추가 및 취소가 교차되는 상황에서도 최종적인 좋아요 수가 정확히 반영됨.
- **성능 저하 최소화**:
    Redis의 메모리 기반 처리로 인해 락을 사용하는 작업에서도 성능 손실이 최소화됨.


</details>
<br/>

<details>
<summary style="font-size: 16px; font-weight: bold">💳 결제 API 연동 </summary>

1. **배경**
    - **결제 API 도입**
        - 기존 예약 시스템에 결제 기능의 필요성을 느껴 PG(Payment Gateway) 서비스를 조사한 후, 최적의 결제 흐름을 구성해 시스템을 재구축.
        - 결제 API의 안정적인 통합을 위해 데이터 흐름을 구체화하고 전체 프로세스를 체계적으로 설계.
2. **문제**
    - **데이터 파싱 오류 및 API 문서 오해**
        - 결제 API 통합 중 데이터 파싱에서 문제가 발생하고, 문서의 특정 내용을 잘못 해석하여 불필요한 시간 소모.
        - 초기 문서 분석 부족으로 인해 예상치 못한 장애 발생.
3. **해결 방안**
    - **API 문서 정밀 검토**
        - 문제 해결을 위해 다양한 블로그와 참고 자료를 활용해 결제 API 문서를 더 꼼꼼하게 분석.
        - 결제 처리 로직을 개선하고 파싱 과정의 정확성을 높이기 위해 관련 자료를 지속적으로 학습하고 보강.

</details>
<br/>

<details>
<summary style="font-size: 16px; font-weight: bold">⏰ 스프링 배치 도입 </summary>

1. **배경**
   - **스프링 배치 도입**
       - 정산은 실시간 처리보다는 시스템 부담이 적은 시간대에 일괄적으로 처리할 필요성에 의해 배치 방식을 선택
       - 안정적인 운영을 위해 메인 DB와 배치 메타데이터 DB를 분리하여 독립적인 데이터 관리 구조를 구성
   - **배치 메타데이터 테이블 생성 필수화**
       - 메타데이터 전용 DB를 나누는 구조로 전환
   - **멀티 DataSource 구성**
       - 메인배치 MetaDBConfig과 DataDBConfig로 데이터베이스 모듈 구분
2. **문제**
   - **DB 분리 설정 에러**
       - application.yml에 설정 에러로 인한 메타 테이블 생성 불가
   - **카멜문법 -> 스네이크문법 변환 에러**
       - DB에 접근하여 Query로 변환시 문법 변환이 안되는 에러 발생 
3. **해결 방안**
   - **DB 분리 해결 방법**
       - DB에 맞는 스키마를 찾아서 추가 설정
   - **카멜문법 -> 스네이크문법 변환 해결 방법**
       - CamelCaseToUnderscoresNamingStrategy 추가 설정
       - SpringImplicitNamingStrategy 추가 설정

</details>

## 인프라 아키텍처 & 적용 기술

### 아키텍처 다이어그램
![image](https://github.com/user-attachments/assets/1e48d4bb-690c-44ff-85b1-e9bffaf198d9)

---

<details>
<summary style="font-size: 16px;"><b>📦 적용 기술 상세보기</b></summary>

### 💾 **데이터베이스 및 캐싱**
1. **Redis**
  - **적용 위치**: 캐시 서버, 대기열, 좋아요 기능, 조회 수
  - **사용 이유**: 동시성 제어와 캐싱을 위해 레디스를 사용하였습니다.
2. **Redis Cluster**
- **적용 위치**: 레디스, Docker-compose로 구성
  - **사용 이유**: 고가용성을 위해 레디스 클러스터 구성 및 AOF 옵션을 적용하였습니다.
  <details>
  <summary>docker-compose.yml</summary>
  
    ```yml
    services:
      redis-master-1:
        container_name: redis-master-1
        image: redis:latest
        command: >
          sh -c 'echo "bind 0.0.0.0" > /etc/redis.conf &&
                echo "port 7001" >> /etc/redis.conf &&
                echo "cluster-enabled yes" >> /etc/redis.conf &&
                echo "cluster-config-file node.conf" >> /etc/redis.conf &&
                echo "cluster-node-timeout 5000" >> /etc/redis.conf &&
                echo "cluster-announce-port 7001" >> /etc/redis.conf &&
                echo "cluster-announce-ip ${HOST_IP}" >> /etc/redis.conf &&
                echo "cluster-announce-bus-port 17001" >> /etc/redis.conf &&
                echo "protected-mode no" >> /etc/redis.conf &&
                redis-server /etc/redis.conf'
        ports:
          - "7001:7001"
          - "17001:17001"
        networks:
          - redis-cluster-network
    
      redis-master-2:
        container_name: redis-master-2
        image: redis:latest
        command: >
          sh -c 'echo "bind 0.0.0.0" > /etc/redis.conf &&
                echo "port 7002" >> /etc/redis.conf &&
                echo "cluster-enabled yes" >> /etc/redis.conf &&
                echo "cluster-config-file node.conf" >> /etc/redis.conf &&
                echo "cluster-node-timeout 5000" >> /etc/redis.conf &&
                echo "cluster-announce-port 7002" >> /etc/redis.conf &&
                echo "cluster-announce-ip ${HOST_IP}" >> /etc/redis.conf &&
                echo "cluster-announce-bus-port 17002" >> /etc/redis.conf &&
                echo "protected-mode no" >> /etc/redis.conf &&
                redis-server /etc/redis.conf'
        ports:
          - "7002:7002"
          - "17002:17002"
        networks:
          - redis-cluster-network
    
      redis-master-3:
        container_name: redis-master-3
        image: redis:latest
        command: >
          sh -c 'echo "bind 0.0.0.0" > /etc/redis.conf &&
                echo "port 7003" >> /etc/redis.conf &&
                echo "cluster-enabled yes" >> /etc/redis.conf &&
                echo "cluster-config-file node.conf" >> /etc/redis.conf &&
                echo "cluster-node-timeout 5000" >> /etc/redis.conf &&
                echo "cluster-announce-port 7003" >> /etc/redis.conf &&
                echo "cluster-announce-ip ${HOST_IP}" >> /etc/redis.conf &&
                echo "cluster-announce-bus-port 17003" >> /etc/redis.conf &&
                echo "protected-mode no" >> /etc/redis.conf &&
                redis-server /etc/redis.conf'
        ports:
          - "7003:7003"
          - "17003:17003"
        networks:
          - redis-cluster-network
    
      redis-replica-1:
        container_name: redis-replica-1
        image: redis:latest
        command: >
          sh -c 'echo "bind 0.0.0.0" > /etc/redis.conf &&
                echo "port 7004" >> /etc/redis.conf &&
                echo "cluster-enabled yes" >> /etc/redis.conf &&
                echo "cluster-config-file node.conf" >> /etc/redis.conf &&
                echo "cluster-node-timeout 5000" >> /etc/redis.conf &&
                echo "cluster-announce-port 7004" >> /etc/redis.conf &&
                echo "cluster-announce-ip ${HOST_IP}" >> /etc/redis.conf &&
                echo "cluster-announce-bus-port 17004" >> /etc/redis.conf &&
                echo "protected-mode no" >> /etc/redis.conf &&
                redis-server /etc/redis.conf'
        ports:
          - "7004:7004"
          - "17004:17004"
        networks:
          - redis-cluster-network
    
      redis-replica-2:
        container_name: redis-replica-2
        image: redis:latest
        command: >
          sh -c 'echo "bind 0.0.0.0" > /etc/redis.conf &&
                echo "port 7005" >> /etc/redis.conf &&
                echo "cluster-enabled yes" >> /etc/redis.conf &&
                echo "cluster-config-file node.conf" >> /etc/redis.conf &&
                echo "cluster-node-timeout 5000" >> /etc/redis.conf &&
                echo "cluster-announce-port 7005" >> /etc/redis.conf &&
                echo "cluster-announce-ip ${HOST_IP}" >> /etc/redis.conf &&
                echo "cluster-announce-bus-port 17005" >> /etc/redis.conf &&
                echo "protected-mode no" >> /etc/redis.conf &&
                redis-server /etc/redis.conf'
        ports:
          - "7005:7005"
          - "17005:17005"
        networks:
          - redis-cluster-network
    
      redis-replica-3:
        container_name: redis-replica-3
        image: redis:latest
        command: >
          sh -c 'echo "bind 0.0.0.0" > /etc/redis.conf &&
                echo "port 7006" >> /etc/redis.conf &&
                echo "cluster-enabled yes" >> /etc/redis.conf &&
                echo "cluster-config-file node.conf" >> /etc/redis.conf &&
                echo "cluster-node-timeout 5000" >> /etc/redis.conf &&
                echo "cluster-announce-port 7006" >> /etc/redis.conf &&
                echo "cluster-announce-ip ${HOST_IP}" >> /etc/redis.conf &&
                echo "cluster-announce-bus-port 17006" >> /etc/redis.conf &&
                echo "protected-mode no" >> /etc/redis.conf &&
                redis-server /etc/redis.conf'
        ports:
          - "7006:7006"
          - "17006:17006"
        networks:
          - redis-cluster-network
    
      redis-cluster-entry:
        container_name: redis-cluster-entry
        image: redis:latest
        command: redis-cli --cluster create redis-master-1:7001 redis-master-2:7002 redis-master-3:7003 redis-replica-1:7004 redis-replica-2:7005 redis-replica-3:7006 --cluster-replicas 1 --cluster-yes
        depends_on:
          - redis-master-1
          - redis-master-2
          - redis-master-3
          - redis-replica-1
          - redis-replica-2
          - redis-replica-3
        networks:
          - redis-cluster-network
    
    networks:
      redis-cluster-network:
        driver: bridge
    
    ```
  </details>

3. **MYSQL**
- **적용 위치**: AWS RDS
- **사용 이유**: 고가용성, 백업 및 보안에 유리
---

### 🌐 **인프라 및 배포**

1. **Jenkins**
   - **적용 위치**: AWS EC2에 배포
     - **사용 이유**: 자동화된 빌드와 배포 파이프라인 관리로 일관된 배포 프로세스 구현.
        <details>
          <summary>Main Project Jenkins 구성</summary>
       
             pipeline {
                 agent any
                 environment {
                     AWS_PUBLIC_IP = 'ubuntu@3.36.56.171'
                     DOCKER_HUB = 'aysel0413'
                     DOCKER_IMAGE = 'project-w'
                     DOCKER_TAG = '0.1'
                 }
                 stages {
                    stage('start') {
                        steps {
                            slackSend (
                                channel: '#jenkins',
                                color: '#FFFF00',
                                message: "START: Job ${env.JOB_NAME} [${env.BUILD_NUMBER}]"
                            )
                        }
                    }
                    stage('Git Clone') {
                        steps {
                            git branch: 'dev', 
                            credentialsId: 'github-access-token', 
                            url: 'https://github.com/final17/project-w.git'
                        }
                    }
                    stage('Build') {
                        steps {
                            dir('./') {
                                sh 'chmod +x ./gradlew'
                                sh './gradlew clean bootJar'
                            }
                        }
                    }
                    stage('Test') {
                        steps {
                            // 테스트 단계 실행
                            dir('./') {
                                sh './gradlew test'
                            }
                        }
                    }
                    stage('DockerFile Build') {
                        steps{
                            dir("./") {
                                script {
                                    dockerimage = docker.build("${DOCKER_HUB}/${DOCKER_IMAGE}:${DOCKER_TAG}")
                                }
                            }   
                        }
                    }
                    stage('Docker Image Push') {
                        steps {
                            script {
                                withDockerRegistry(credentialsId:"docker-access-token") {
                                    dockerimage.push()
                                }
                            }
                        }
                    }
                    stage('Deploy') {
                        steps {
                            sshagent(credentials: ['aws_key']) {
                                sh '''
                                    ssh -o StrictHostKeyChecking=no ${AWS_PUBLIC_IP} << EOF
                                    docker container stop app
                                    docker image prune -f
                                    docker pull ${DOCKER_HUB}/${DOCKER_IMAGE}:${DOCKER_TAG}
                                    docker run -d --rm -p 8080:8080 --env-file ./config/.env --name app ${DOCKER_HUB}/${DOCKER_IMAGE}:${DOCKER_TAG}
                                    exit
                                    EOF
                                '''
                               }
                           }
                       }
                   }
                   post {
                       success {
                            slackSend (
                                channel: '#jenkins',
                                color: '#00FF00',
                                message: """
                                        SUCCESS: Job ${env.JOB_NAME} [${env.BUILD_NUMBER}]
                                        """
                            )
                       }
                       failure {
                            slackSend (
                                channel: '#jenkins',
                                color: '#FF0000',
                                message: "FAIL: Job ${env.JOB_NAME} [${env.BUILD_NUMBER}]"
                            )
                       }
                   }
              }
        
        </details>

2. **Docker**
   - **적용 위치**: 모든 서비스 컨테이너화
   - **사용 이유**: 환경 이식성을 높여 운영 환경 간의 일관성을 보장하고 배포 속도 개선. 컨테이너를 통해 서비스 간의 의존성을 격리하여 안정적인 운영 환경 구축.

3. **Github Actions**
   - **적용 위치**: CI/CD 파이프라인
   - **사용 이유**: 코드 푸시에 따른 자동화된 코드 품질 검사와 빌드, 테스트, 배포 과정을 통해 개발 속도와 품질을 동시에 유지.

4. **ELKB**
   - **적용 위치**: AWS EC2에 분산 로그 수집 환경
   - **사용 이유**: 통합 로깅과 모니터링을 위한 플랫폼으로 시스템 로그 수집 및 분석을 통해 빠른 문제 탐지 및 대응. Elasticsearch를 통한 데이터 인덱싱 및 검색, Logstash로 다양한 로그 데이터를 수집 및 변환, Kibana 대시보드를 통한 시각화, Beats로 서버에서의 로그와 메트릭 경량 수집을 통해 시스템의 실시간 상태를 효과적으로 파악.

</details>

## 주요 기능

### 🍁 **결제 API 연동**
- 외부 PG사와 결제 API를 연동하여 결제 정보를 처리하고, 결제 상태를 추적
        <details>
        <summary>프로세스 흐름</summary>
        <img src="https://github.com/user-attachments/assets/80652b74-2877-4fa6-baa2-b3b4d2cd2c6a">
        </details>
- Spring의 REST API와 트랜잭션 이벤트 리스너를 활용하여 결제 API와의 연동을 효율적으로 처리

---

### 🍁 **정산 Spring Batch**
- 실시간 처리 대신, 시스템 부하가 적은 시간대에 배치 방식으로 정산을 수행하며, 별도의 서버 환경을 활용하여 부하를 최소화.
  - **프로세스 흐름**: `결제 완료 데이터 조회` → `수수료 계산` → `정산 결과 저장`.
- 스프링 배치와 스케줄러를 활용하여 정산 작업을 자동화하고, 배치가 시스템 부하가 적은 시간에 실행되도록 설정

## 기술적 고도화

<details>
<summary><b>🍁 분산락 Redisson 도입으로 CPU 점유율 2배 개선</b></summary>

### 왜 동시성 제어 시 여러 선택지가 있는데, 분산락을 사용했을까요?

---

#### 낙관적 락과 비관적 락의 선택지

분산락을 채택하기 이전에는 비관적 락으로 동시성 제어를 선택했습니다.

- **비관적 락**  
  비관적 락으로 데이터를 조회하면 해당 트랜잭션이 끝나기 전까지는 데이터에 대한 Insert 작업이 불가능합니다.
    - 단점: 트래픽이 많은 경우 성능 저하 발생 및 타임아웃 문제.

- **낙관적 락**  
  낙관적 락은 충돌 발생 시 롤백 처리를 요구하며, 충돌 비용이 높습니다.
    - 단점: CPU 점유율이 상승하고, 예상치 못한 오류 발생 가능.

---

#### Redis로 분산락을 채택한 이유
<h1 style="font-size: 50px">김윤서</h1>
1. **Lettuce의 문제점**  
   Lettuce는 스핀락 방식을 사용하여 락이 풀릴 때까지 계속 Redis에 요청을 보냅니다.
    - 결과적으로 Redis CPU 점유율이 높아지는 문제가 발생.

2. **Redisson의 장점**  
   Redisson은 Pub-Sub 구조로 락이 종료될 때 이벤트를 발행하며, 락 요청을 효율적으로 처리합니다.
    - 결과적으로 Redis CPU 점유율이 낮아집니다.

---

### 적용 후

- **CPU 점유율:** 기존 60% → 30% 감소
- **TPS:** 기존 1400 → 2500으로 향상

</details>

<br/>
<details>
<summary style="font-size: 16px"><b>🔍 Elastic search 검색 정확도 향상</b></summary>

---
<details>
<summary><strong>🛠 문제 정의</strong></summary>

> 사용자가 `서울시 은평구 초밥집`을 검색하면, 서울시 은평구에 위치한 초밥 가게를 찾고자 한다고 추정할 수 있습니다. 하지만 현재 Elasticsearch 멀티 매치 쿼리에서는 단순히 키워드를 분리하여 가게명, 주소, 메뉴 중 매칭되는 항목이 있으면 결과로 표시됩니다. 이로 인해 검색 결과가 원하는 위치와 일치하지 않는 다른 지역의 초밥집이 더 높은 점수로 노출되는 문제가 발생했습니다.
</details>

<details>
<summary><strong>💡 해결 접근 방식</strong></summary>

> 1. **가중치 방식**: 키워드를 분석해 필드마다 가중치를 다르게 적용
> 2. **bool 쿼리 방식**: 필터링된 키워드를 bool 쿼리로 처리해 도시, 구, 메뉴 필드가 정확히 일치하는 결과를 우선 표시

> 2번 방식을 선택하여, 사용자가 입력한 위치와 메뉴가 일치하는 가게를 우선 표시하도록 설계했습니다.

</details>


<details>
<summary><strong>⚙️ 1차 해결</strong></summary>

> **키워드 필터링**을 통해 도시, 구, 메뉴를 추출하고 Elasticsearch의 **bool 쿼리**로 필터링합니다.
> 1. 도시, 구, 메뉴가 **반드시 포함되도록** `must` 조건으로 필터링
>    - 필터링 후 가게명과 메뉴 필드에 `match` 쿼리로 추가 검색
>    - 도시, 구, 메뉴가 없으면 `가게명과 메뉴`만으로 검색
>    - 가게명 필드에는 keyword 필드와 `boost` 값을 적용해 메뉴보다 높은 우선순위 부여

</details>


<details>
<summary><strong>⚙️ 2차 해결</strong></summary>

> **성능 개선**: 여러 필드 쿼리로 인한 성능 저하를 해결하기 위해 **copy_to**를 사용하여 필요한 텍스트를 하나의 필드에 저장.
> 
> 예시) 주소가 `서울시 은평구`, 가게명 `맛집`, 메뉴 `햄버거, 피자`라면 `서울시 은평구 맛집 햄버거, 피자`로 저장.

</details>


<details>
<summary><strong>🔍 캐치테이블 검색 분석</strong></summary>

> 더 나은 검색 경험을 위해 다른 서비스들의 검색 기능을 분석을 진행하였습니다.
> 
> **분석 결과**:
> - 단일 키워드 검색은 정확히 일치하는 가게명을 우선으로 검색 (term 또는 prefix 방식 추정)
>   - 일치하는 가게가 없을 경우, copy_to된 full-text 필드에 대해 통합 검색 수행
>   - 통합 검색 시 **Operator를 AND**로 설정하여 필터와 일치하는 단어가 포함된 경우 필터를 적용해 프론트에 전달
> 
> 그러나 단일 검색에 두 번의 쿼리 요청이 필요하다는 점을 고려할 필요가 있었습니다.

</details>

<details>
<summary><strong>🔧 검색 정확도 튜닝</strong></summary>

> **현재 검색 로직**
> 1. 가게 이름을 `must / prefix` 쿼리로 검색
>       - 카테고리 필터가 있으면 `district_category.keyword`에 `terms` 쿼리를 사용해 하나라도 일치하는 결과만 반환
>      - 검색 결과가 없다면 통합검색 실행
>      - 통합검색은 bool 쿼리를 사용하고 `filter`와 `full_text` 필드 검색에 `AND` 연산자 적용
> 쿼리를 단순화하여 가게명 검색 단계를 생략하고 바로 통합 검색으로 변경했습니다.

</details>

<details>
<summary><strong>🔍 테스트 케이스: 서울복집 검색</strong></summary>

> - **쿼리 예시**
>
>    ```java
>    boolBuilder.must(m -> m.match(x -> x.field("full_text").query(search.keyword()).operator(Operator.And))).boost(4f);
>   ```
> - **결과**
>      - `서울복집` 검색 시 서울 지역의 복집 가게가 상위에 노출됨
>      - `경상남도 서울복집` 검색 시 경상남도 지역의 복집 가게가 정확히 노출됨
> 
> 첫 번째 결과에서 원하는 서울복집이 상위로 나오지 않아 개선이 필요했습니다.

</details>


<details>
<summary><strong>✅ 최종 쿼리 개선</strong></summary>

> 1. `must` 조건에 `full_text` 필드, `should` 조건에 `title` 필드로 `match` 쿼리 사용
> 2. `title.keyword` 필드에는 `term` 쿼리를 적용해 가게명이 정확히 일치하는 경우 가중치를 2로 설정
> ```java
> boolBuilder = new BoolQuery.Builder()
>        .must(m -> m.match(t -> t.field("full_text").query(search.keyword()).operator(Operator.And)))
>        .should(s -> s.match(t -> t.field("title").query(search.keyword()).boost(1.5f)))
>        .should(s -> s.term(t -> t.field("title.keyword").value(search.keyword()).boost(2.0f)))
>        .minimumShouldMatch("1");

</details>

---
</details>

<br/>

## 역할 분담 및 협업 방식

### **Detail Role**

| 이름   | 포지션 | 담당(개인별 기여점)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    | Github 링크                           |
|------|-------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------|
| 황호진  | 리더  | ▶ **Reservation**<br> - 예약 CRUD<br> - 예약메뉴 redis로 장바구니 담으며 저장시 추출하여 저장<br> - 예약 좌석 제한 및 동시에 접근하는 것을 제어<br> - 트랜잭션이벤트리스너를 통해 결제로 트랜잭션 전파<br> ▶ **Payment**<br> - 결제 CRUD<br> - 토스페이 연동(결제 승인 , 결제 취소)<br> - 결제요청시 10분내로 결제완료하지 않으면 자동취소<br> - 트랜잭션이벤트리스너를 통해 예약으로 트랜잭션 전파<br> ▶ **Settlement**<br> - 정산 로그<br> - 정산 집계<br> - Spring Batch를 통한 로그 , 집계 대용량 데이터 처리<br> ▶ **CI / CD**<br> - Docker + Jenkins + AWS EC2를 이용한 파이프라인 구축<br> ▶ **ELK**<br> - ElasticSearch + LogStash + Kibana + Metricbeat를 이용한 모니터링<br> ▶ **react.js**<br> - 예약 API 연동 및 예약 내역 화면 개발 | [🍁https://github.com/ballqs]                           |
| 정재호  | 부리더 | ▶ **스프링 시큐리티**<br> - jwt 액세스, 리프레쉬 토큰 사용하여 인증/인가<br> ▶ **카테고리**<br> - 카테고리 CRUD<br> ▶**엘라스틱 서치 검색 고도화**<br> - 검색 정확도 향상<br> ▶ **웨이팅 대기열 구현**<br> - 웨이팅 CRUD<br> - 웨이팅 대기열 구현에 Redis Sorted Set 사용<br> - 웨이팅 완료 후 redis pub/sub을 사용하여 이벤트 처리<br> - 웨이팅 대기열 구현에 Redis 분산락 사용<br> ▶ **스프링 배치**<br> - 배치로 웨이팅 시간대별, 일간 통계 자정에 일괄 처리<br> ▶ **CI / CD**<br> - 젠킨스 CI CD 파이프라인 구축<br> - Github action으로 Sonar cloud 사용하여 코드 품질 관리                                                                                                                                                                                                                                                                                                                                                                                                                                                              | [🍁https://github.com/Nameless1004]                           |
| 조수현  | 팀원  | ▶ **Store CRUD**<br> - 음식점 CRUD<br> ▶ **AWS S3 버킷에 이미지 업로드**<br> - 음식점 사진을 S3에 업로드 후 클라우드 프론트로 이미지 조회<br> ▶ **Redisson 분산락**<br> - 음식점 조회수 증가, 좋아요 부분에 분산락 적용<br> ▶ **음식점 좋아요 index**<br> - 음식점 좋아요 테이블에서 유저 아이디로 찾아올 때 인덱스 처리<br> ▶ **음식점 이름 검색**<br> - 음식점 이름 검색에 인덱스, Redis 캐시를 이용해서 검색 속도 향상<br> ▶ **CI / CD**<br> - 젠킨스 CI CD 파이프라인 구축                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   | [🍁https://github.com/SuHyun-git]                           |
| 김윤서  | 팀원  | ▶ **Menu**<br> - 메뉴 CRUD<br> - 메뉴 좋아요, 조회 시 Redis Rlock을 통한 동시성 제어<br> ▶ **Follow**<br> - 팔로잉/팔로워 기능<br> - 팔로우 한 사용자의 가게 좋아요 목록 조회<br> - 팔로워/팔로잉 조회 캐싱 처리<br> ▶ **Chatbot**<br> - 챗봇 기능<br> ▶ **Allergy**<br> - 메뉴 생성/수정 시 알레르기 추가<br> - 모든 알레르기 조회 시 캐싱 처리<br> ▶ **Waiting**<br> - Redis 가중치를 통해 웨이팅 실시간 순위 조회<br> ▶ **소셜 로그인(카카오)**                                                                                                                                                                                                                                                                                                                                                                                                           | [🍁https://github.com/yunseokim119] |
| 길용진  | 팀원  | ▶ **Review**<br> - 메뉴 리뷰 CRUD<br> - 리뷰 좋아요<br> - 이미지 AWS S3버킷에 업로드<br> ▶ **크롤링**<br> - 블로그/뉴스 크롤링<br> - 음식점 이름 키워드 변환 후 + 맛집, 지역 추가하여 키워드화<br> - 프록시 ip 획득 후 크롤러 개발<br> ▶ **REACT활용 프론트엔드 제작**<br> - 모든 기능 기초 기반 제작                                                                                                                                                                                                                                                                                                                                                                                                           | [🍁https://github.com/pumaclass/]   |
---

### **Ground Rule**

🍁 **문제 발생 시 즉시 공유**
- 문제가 발생하면 팀원들에게 빠르게 상황을 공유하여 협력 해결.

🍁 **일정 무조건 지키기**
- 일정 변동 시 슬랙 공유 및 주말 활용할 것.

🍁 **사소한 것도 질문하기**
- 궁금한 점이나 막힌 부분은 사소한 것이라도 즉시 물어보고 해결.

🍁 **스크럼에서 트러블 슈팅 및 구현 사항 설명**
- 매일 스크럼 시간에 구현 진행 상황과 문제 해결 과정을 공유.

🍁 **1Day, 1Code Review, 1PR 원칙**
- 하루에 하나 이상의 구현한 코드 리뷰 및 PR 생성.

🍁 **1PR 당 1인 이상 확인 후 머지**
- 각 PR에 대해 최소 1명 이상의 승인을 통해 코드 품질을 개선.
## 성과 및 회고

### 잘된 점
- **성능 최적화 성공**
    - Redis와 Kafka를 도입해 대규모 트래픽 환경에서도 안정적인 쿠폰 발급 구현.
    - API 처리량 250req/sec를 초과 달성하며 목표를 상회하는 성과를 기록.

- **효율적인 협업**
    - 팀원 간 역할 분담이 명확했으며, GitHub Actions를 활용한 CI/CD 구축으로 개발-배포 주기를 단축.
    - 매일 스크럼을 통해 문제를 빠르게 공유하고, 적극적으로 해결.

---

### 아쉬운 점
- **프로젝트 초기 설계 부족**
    - 도메인 설계 및 서비스 분리 단계에서 충분한 검토가 이루어지지 않아 일부 마이크로서비스 간 의존성 증가.

- **시간 부족으로 일부 기능 미완성**
    - 사용자 피드백 시스템과 추가 정산 기능 개발이 지연되어 구현하지 못함.

---

### 향후 계획
- **기술적 고도화**
    - Java 21의 가상 스레드를 도입하여 병렬 처리 성능을 개선하고, 무중단 배포를 위한 추가적인 CI/CD 개선 계획.

- **추가 기능 개발**
    - 사용자 피드백 시스템 도입으로 서비스 품질을 지속적으로 향상.
    - 데이터 분석 기능을 추가해 쿠폰 발급 및 사용 데이터를 기반으로 한 비즈니스 인사이트 제공.

- **테스트 자동화 강화**
    - 기존 단위 테스트 외에 통합 테스트 및 부하 테스트를 추가하여 안정성을 더욱 강화.


## ☁ 와이어프레임

## ☁ ERD 다이어그램

## 📑 API 명세서
