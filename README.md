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


---
## KEY Summary

<details>
<summary style="font-size: 16px; font-weight: bold">🛠 예약 대기열 <span style="background-color: red;">동시성</span> 이슈</summary>
<details>
<summary><strong>⚠️ 문제 발견</strong></summary>
    
- **현상**: JMeter로 1,000명의 유저가 동시에 대기열에 등록했을 때, 발권 번호가 중복으로 발생하는 동시성 이슈가 확인되었습니다.

</details>

<details>
<summary><strong>🔍 상황 분석</strong></summary>

- **분석**: 대기열 등록 로직이 발권번호 테이블과 웨이팅 테이블로 구성되어 있으며, 동시에 접근 시 발권번호가 올바르게 증가하지 않고 중복된 번호가 저장되는 문제가 있었습니다.

</details>

<details>
<summary><strong>📝 1차 시도: <code>synchronized</code> 사용</strong></summary>

- **방법**: 발권번호 증가 부분에 `synchronized` 키워드를 적용하여 동시성 제어를 시도했습니다.
- **결과**: 중복이 줄어들었지만 완전히 해결되지는 않았습니다.
    - `@Transactional`로 인해 `synchronized`가 끝난 후 커밋 시점에서 다른 스레드들이 값을 변경하여 발생한 문제임을 확인했습니다.

</details>

<details>
<summary><strong>🔒 2차 시도: 비관적 락(Pessimistic Lock) 적용</strong></summary>

- **방법**: 동시성 충돌 가능성이 높은 환경을 고려해, 비관적 락을 사용하여 발권번호 테이블의 중복 문제를 해결했습니다.
- **결과**: 중복 문제는 해결되었으나, 높은 트래픽 상황에서 데드락이 발생하여 일부 트랜잭션이 롤백되었습니다.
   - `show engine innodb status` 명령어로 데드락 로그를 분석한 결과, 동일한 `storeId`와 `waiting_number` 값을 삽입하려는 트랜잭션 간의 충돌로 데드락이 발생한 것을 확인했습니다.

</details>

<details>
<summary><strong>💡 3차 시도: Redis로 전환</strong></summary>

- **방법**: RDB 대신 **Redis**를 사용하여 싱글 스레드 기반으로 순차적 처리를 유도했습니다. Redis는 메모리 기반이므로 Lock 없이도 효율적으로 동시성을 관리할 수 있었습니다.
- **결과**: 처리율이 약 **43.2%** 개선되었고, 응답 시간도 **약 30.8%** 단축되었습니다.
    - 또한, Redis의 데이터 휘발성 문제를 해결하기 위한 추가적인 처리를 고민하였습니다.
      - 레디스 클러스터 구성 및 AOF 옵션 추가
      - 레디스 서버 다운 시 슬랙 알림 기능
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
<summary style="font-size: 16px; font-weight: bold">⏰ 스프링 배치 도입 </summary>

1. **배경**
   - **스프링 배치 도입**
       - 정산 또는 웨이팅 데이터는 실시간 처리보다는 시스템 부담이 적은 시간대에 일괄적으로 처리할 필요성에 의해 배치 방식을 선택
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
4. **관련 링크** : https://github.com/final17/batch

</details>

---

## 인프라 아키텍처 & 적용 기술

### 아키텍처 다이어그램
![image](https://github.com/user-attachments/assets/b0234b41-039d-4489-b8be-f7c1e08124e0)

---

<details>
<summary style="font-size: 24px;"><b>📦 적용 기술 상세보기</b></summary>

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
- **사용 이유**: 데이터베이스 관리 작업(백업, 패치, 모니터링 등) 자동화하여 부담을 줄여주고 장애 발생시 자동으로 대체 인스턴스로 전환하여 고가용성을 제공.
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
   - **적용 위치**: 통합 검색, AWS EC2에 모니터링 환경 구축
   - **사용 이유**: 통합 로깅과 모니터링을 위한 플랫폼으로 시스템 로그 수집 및 분석을 통해 빠른 문제 탐지 및 대응. Elasticsearch를 통한 데이터 인덱싱 및 검색, Logstash로 다양한 로그 데이터를 수집 및 변환, Kibana 대시보드를 통한 시각화, Beats로 서버에서의 로그와 메트릭 경량 수집을 통해 시스템의 실시간 상태를 효과적으로 파악.

</details>

---
## 주요 기능

### 결제 API 연동
- 외부 PG사와 결제 API를 연동하여 결제 정보를 처리하고, 결과를 DB에 저장함으로써 결제 정보를 추적
        <details>
        <summary>프로세스 흐름</summary>
        <img src="https://github.com/user-attachments/assets/80652b74-2877-4fa6-baa2-b3b4d2cd2c6a">
        </details>
- Spring의 REST API와 트랜잭션 이벤트 리스너를 활용하여 결제 API와의 연동을 효율적으로 처리

### 정산 Spring Batch
- 실시간 처리 대신, 시스템 부하가 적은 시간대에 배치 방식으로 정산을 수행하며, 별도의 서버 환경을 활용하여 부하를 최소화.
  - **프로세스 흐름**: `결제 완료 데이터 조회` → `수수료 계산` → `정산 결과 저장`.
- 스프링 배치와 스케줄러를 활용하여 정산 작업을 자동화하고, 배치가 시스템 부하가 적은 시간에 실행되도록 설정
- 관련 링크 : https://github.com/final17/batch

### 챗봇 기능
- 고객이 보내는 문의 유형(예약문의,결제문의,회원문의,기타문의)에 따라 적절한 답변을 자동으로 제공
- 문의 유형과 세부 항목을 Enum과 전략 패턴을 활용해 각 문의 유형을 Enum으로 정의하고, 이를 기반으로 각각의 로직을 별도의 전략 클래스로 분리
  - **확장성**: Enum에 새로운 문의 유형을 추가하고, 전략 클래스를 추가하는 방식으로 확장할 수 있어 코드가 비대해지는 것을 방지할 수 있음.
  - **유연성**: 새로운 로직이나 케이스 추가 시 기존 코드 변경 없이 클래스와 Enum만 추가하여 기능을 확장할 수 있음.
---


## 기술적 고도화


<details>
<summary style="font-size: 16px">📝예약부터 결제, 그리고 정산까지: 효율적인 프로세스 구축 여정</summary>
<details>
<summary style="font-size: 16px"><strong>💳 결제 API 연동</strong></summary>

<details>
<summary style="font-size: 14px"><strong>트러블 슈팅</strong></summary>

### [문제 인식]

예약 CRUD 작업 진행 후 결제를 처리하기 위해 토스페이먼트 API를 연동하면서 개발을 진행하였고
결제 시스템을 개발하는 과정에서 토스페이를 통한 결제 API를 구현하는 중에 데이터 파싱 문제에 직면하게 되었습니다.
API로 응답받은 데이터가 작성한 형식과 다르게 반환되었고 이를 적절히 처리하지 못하는 상황이 발생했습니다.
이로 인해 결제 정보가 저장되지 않은 문제가 생겼습니다.

### [해결 방안]

API의 응답 데이터 구조를 다시 파악하기 위해 API 문서를 면밀히 살펴보고 문서에서 제공하는 데이터 형식에 맞춰서
필요한 필드들을 추출하여 검증하고 API 응답을 정확히 처리할 수 있도록 했습니다.

### [해결 완료]

테이블 엔티티의 구조를 재검토하여 각 필드가 데이터 타입과 일치하는지 확인했습니다.
이를 바탕으로 실제 결제와 결제 취소 기능을 통해 흐름을 점검했습니다.
또한 제이미터를 통해 최대한 많이 API를 호출해보며 문제가 없는지 검증했습니다.
이 모든 과정을 통해 최종적으로 결제 시스템의 안정성과 신뢰성을 확보할 수 있었습니다.

</details>

<details>
<summary style="font-size: 14px"><strong>결제 테스트</strong></summary>

### 결제 흐름

<img src="https://github.com/user-attachments/assets/e25e3f8e-bda9-46b5-9ba0-d546afbfab47"/>

---

### Jmeter 테스트 결과

테스트 해야하는 부분은 <span style="color : red;">결제 요청 전 검증</span>부분으로 판단했고 해당 부분에 Jmeter를 걸어서 테스트를 진행해보았습니다.

<img src="https://github.com/user-attachments/assets/a3029ed1-9c3e-4642-aaaa-404eeda7ec09"/>

| 테스트 내용               | 샘플 수 | 평균 응답 시간 (ms) | 최소 응답 시간 (ms) | 최대 응답 시간 (ms) | 오류율 (%) | 처리량 (req/sec) |
|----------------------|---------|---------------------|---------------------|---------------------|------------|------------------|
| 기본                   | 2140    | 502                 | 17                  | 1689                | 4.67       | 189.9            |
| RedissonLock         | 1844    | 1831                | 18                  | 2318                | 5.42       | 53.8             |
| Async                | 1968    | 518                 | 8                   | 1046                | 5.08       | 183.6            |
| RedissonLock + Async | 1852    | 1669                | 16                  | 3743                | 5.40       | 58.9             |

---

<div style="font-size: 14px">여러 테스트로 확인해본 결과 <span style="color : red;">기본 방식</span>이 더 빠르고 데이터도 안정적으로 들어가는것을 확인했습니다.</div>

</details>

</details>
<details>
<summary style="font-size:16px"><strong>⚙️ 정산 처리 자동화</strong></summary>

<img src="https://github.com/user-attachments/assets/c302cc4f-c238-4158-9808-6badbb27b16a"/>

관련 링크 : https://github.com/final17/batch

</details>
</details>
<br/>

<details><summary style="font-size: 16px; font-weight: bold">🔍 음식점 검색 기능 속도 향상 </summary>

**배경**
- 100만건 정도의 큰 데이터를 LIKE를 통해 검색
**문제**
- 검색 속도가 1초 정도로 느려 검색 속도 향상이 필요함

<details><summary>🧾 의사 결정 </summary>

<details><summary> 검색을 고도화 시키는 방법 </summary>

1. 페이지네이션: 검색 결과가 많은 경우 한 번에 모든 데이터를 가져오기 않고, 페이지네이션을 적용하여 필요한 부분만 조회하도록 할 수 있습니다.
2. JPA 쿼리 최적화 및 Indexing: 데이터베이스 테이블에 인덱스를 설정하여 검색 속도를 높일 수 있습니다.
3. Redis 캐싱 적용: 자주 조회하는 음식점 데이터를 Redis에 캐싱하여 데이터베이스 조회 빈도를 줄일 수 있습니다.

이중에서 페이지네이션은 적용을 완료하였고, Indexing을 하는 것이 적합하다고 생각을 하여 Indexing을 적용하였습니다. 그 후에 Redis 캐싱을 적용하였습니다.
</details>

<details><summary> 인덱스 종류 </summary>

1. 단일 인덱스(Single Index): 하나의 컬럼에 인덱스를 설정하여 해당 컬럼에 대한 검색을 최적화 합니다.
2. 복합 인덱스(Composite Index): 여러 컬럼을 조합하여 인덱스를 생성하는 방석으로, 특정 조합에 대한 최적의 성능을 제공
3. 유니크 인덱스(Unique Index): 중복이 허용되지 않도록 하는 인덱스
4. 전문검색 인덱스(Full-Text Index): 텍스트 기반의 검색을 최적화 하기 위한 인덱스

이중에서 Full-Text Inex가 텍스트 기반의 검색으로 적합하다고 생각하여 적용하였습니다.

</details>

<details><summary> 해결 순서 </summary>

1. Full-Text Index 적용
* Full-Text Index를 사용하면 키워드 검색을 지원하지 않습니다.("검색어"만 검색이 가능하고 "%검색어", "검색어%", "%검색어%")
2. Full-Text Index에서 N-Gram을 사용하기
* Full-Text는 위와 같은 문제가 발생하여 N-Gram을 사용하였습니다. N-Gram을 사용하면 위에서 발생한 문제를 해결할 수 있었습니다.
* ![image](https://github.com/user-attachments/assets/3403990d-4d17-4666-b320-683761dbce14)
* 하지만 Like만 사용한 결과와 Full-Text Index N-Gram을 사용한 결과를 확인해보면 "소라", "도깨비"를 검색하면 빠르고, "식당"은 둘이 속도가 비슷하고, "찌개", "국밥"은 N-Gram방식이 느린 것을 확인할 수 있었습니다.
* N-Gram이 느린 이유는 N-Gram은 특정 검색 시 세밀한 일치 검색을 가능하게 하지만 더 느려질 수 있기 때문이었습니다.
* 정화도는 향상되었지만 속도가 많이 느려지기 때문에 적합하지 않다고 생각하였습니다.
3. Single Index 걸어보기
* 처음으로 돌아가 가장 기본적인 index인 Single Index를 걸어보았습니다
* 인덱싱 적용 범위
  * EXPLAIN SELECT * FROM store WHERE business_name LIKE '%도깨비%';
  * ![image](https://github.com/user-attachments/assets/5cf2cf95-cd3e-4a22-b883-321ff7ef07a2)
  * EXPLAIN SELECT * FROM store WHERE business_name LIKE '도깨비%';
  * ![image](https://github.com/user-attachments/assets/26efeff3-c0ec-46db-a4e5-8d94e0081094)
  * EXPLAIN SELECT * FROM store WHERE business_name LIKE '%도깨비';
  * ![image](https://github.com/user-attachments/assets/8023c79a-750b-4cfd-91b9-386963c650ff)
* 인덱싱 없이 LIKE만 사용하여 검색했을 때
* ![image](https://github.com/user-attachments/assets/11a4b11c-b140-4418-b50d-cf3190ecf12d)
* 인덱싱 사용했을 때
* ![image](https://github.com/user-attachments/assets/9544af92-503a-42bf-bc87-ecd71d108621)
4. Redis 캐싱 사용하기
* 음식점 데이터는 자주 조회되고, 변경이 적고 읽기 비율이 높은 데이터라고 생각하여 Redis 캐싱 대상에 적합하다고 생각하였습니다.
* 처음 페이징 처리된 데이터를 redis에 넣으려고 하자 에러가 나오면서 넣을 수 없었습니다.
* ![image](https://github.com/user-attachments/assets/39fc68be-c546-4270-b08f-f2ad0df8ef04)
* 그래서 페이징 처리된 데이터를 dto를 새로 만들어서 해결하였습니다.
* Redis 결과
  * 캐싱을 처리하기 전
  * ![image](https://github.com/user-attachments/assets/3eef3793-ab3b-4e43-a5be-6405dffa75f9)
  * 캐싱 처리 후
  * ![image](https://github.com/user-attachments/assets/c0d25d43-a3f4-4ccc-b745-20716a10fc9e)
* 속도가 <span style="color:red;">1048.90ms → 4.32ms</span>로 빨라진 것을 확인 할 수 있습니다.


</details>
</details>
</details>
<br/>
<details>
<summary style="font-size: 16px; font-weight: bold">⚙️ 조회수, 좋아요 분산락 적용</summary>

#### 왜 동시성 제어 시 여러 선택지가 있는데, 분산락을 사용했을까?
- 분산 시스템 환경에서의 데이터 일관성 보장, 성능, 그리고 확장성 측면에서 가장 적합한 솔루션이기 때문
---
#### 낙관적 락과 비관적 락의 선택지
- **비관적 락**  
  트랜잭션 내에서 행 단위로 락을 걸어 데이터를 보호.
  장점: 데이터베이스 레벨에서 강력한 동시성 제어 가능.
  추가 도구 없이 데이터베이스 기능만으로 구현 가능.
  단점: 락이 데이터베이스에 직접 걸리므로, 트랜잭션이 오래 지속되면 데이터베이스의 성능이 저하됨.
  분산 환경(멀티 인스턴스)에서는 사용할 수 없음.
  락 경합이 심하면 데드락 발생 가능

- **낙관적 락**  
  데이터를 읽은 후 업데이트 시점에 데이터의 버전을 확인하여 동시성 충돌을 감지. 
- 장점: 락을 걸지 않으므로 성능이 뛰어남.
  충돌 발생이 적은 환경에서 적합. 
- 단점: 충돌이 빈번한 경우 성능이 저하됨(여러 번 재시도 필요).
  충돌 감지 후 롤백 및 재시도 로직이 복잡해질 수 있음.
  분산 환경에서 사용하려면 추가적인 구현 필요.

---

#### Redis로 분산락을 채택한 이유
- **분산 환경 지원**: 멀티 노드/멀티 인스턴스에서도 동작.
- **성능**: 메모리 기반 Redis를 사용하여 락 처리 속도가 빠름.
- **데드락 방지**: 자동 TTL로 데드락 가능성을 제거.
- **확장성**: 클러스터 구성을 통해 높은 확장성과 가용성 제공.
- **유연성**: 단일 리소스나 특정 작업 단위로 락 관리가 가능.
</details>

<br/>
<details>
<summary style="font-size: 16px"><b>🔍 Elastic search 통합 검색</b></summary>

# 통합 검색 구현
## DB LIKE 검색

### 구현 아이디어
DB에서 `LIKE` 연산을 통해 키워드 검색을 수행해 보았습니다.

### 조건 및 결과
- 데이터 개수: 100만 건
- 검색 컬럼: 사업명, 사업형태, 주소, 도로명 주소, 메뉴
- 결과: 약 15초 소요
  ![image](https://github.com/user-attachments/assets/b871da24-9e08-4ce0-a66f-c15afbb6a32f)

## 인덱스 적용
### 개선 아이디어
DB 컬럼에 인덱스를 추가해 성능을 개선해보려 했으나, `LIKE` 연산에서 `%키워드%`와 같이 시작과 끝을 와일드카드로 둘러싸면 인덱스가 무용지물이 되는 것을 확인했습니다.

### 결과
* `%키워드%`를 사용한 경우 성능 개선 효과가 없었으며, `키워드%`로 시작하는 경우에만 인덱스를 사용할 수 있었습니다.
  ![image](https://github.com/user-attachments/assets/70114b02-c0d0-4d70-9339-af587afeb803)
  ![image](https://github.com/user-attachments/assets/1f09eb2e-f85c-451b-8c99-6f52911f0710)


* `%키워드%` EXPLAIN
  ![image](https://github.com/user-attachments/assets/ebfac40e-0179-4f57-91c9-9cd826e7169e)


* `키워드%` EXPLAIN
  ![image](https://github.com/user-attachments/assets/a0215a51-da22-440f-ba64-054864f41d25)

## MySQL Full-Text Search
MySQL의 `Full-Text Search`를 사용해 검색을 구현해 보았습니다.

```java
    @Query(value = "SELECT * FROM store WHERE MATCH(business_name, full_location_address, full_road_name_address, business_type) AGAINST(:keyword IN NATURAL LANGUAGE MODE)", nativeQuery = true)
    List<Store> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
```
### 결과
검색 성능이 개선되었으나, 원하는 검색 결과가 정확하게 나오지 않아 통합 검색으로는 부적합하다고 판단했습니다.
![image](https://github.com/user-attachments/assets/3b5c447e-78be-448f-993e-cf25e7841dfc)
![image](https://github.com/user-attachments/assets/f68fd8da-f724-4d5d-9339-13511446c780)

## Elasticsearch 도입
DB에서의 검색 대신 Elasticsearch를 사용하기로 했습니다.
Elasticsearch를 도입한 이유는 다음과 같습니다.
1. 기존 DB 검색의 한계
   - 성능: SQL의 LIKE 검색은 `%키워드%` 구조인 경우 인덱스를 활용할 수 없어 데이터가 많을수록 속도가 느려집니다.
   - **제한된 검색 기능:** Full-Text Search는 사용자 의도를 세밀하게 반영하기 어려워 복합 조건 처리와 검색 정확도에 한계가 있습니다.
2. Elasticsearch의 장점
- **성능:** 역색인 구조로 검색 속도가 매우 빠릅니다.
- **정교한 검색:** 형태소 분석기(Nori)를 통해 한국어 검색 최적화 및 필드별 가중치 설정으로 검색 결과의 품질 높습니다.
- **유연한 검색:** Fuzzy 검색, 부분 매칭, 복합 쿼리 등을 지원해 다양한 사용자 요구를 충족할 수 있습니다.
- **확장성과 실시간성:** 실시간 색인과 수평 확장으로 데이터 증가에도 안정적인 성능을 유지할 수 있습니다.

### 테스트
* 간단히 `multi_match` 쿼리를 통해 필드별 가중치를 주어 검색했을 때, 100만건의 데이터에서 30ms 내에 검색 결과를 얻을 수 있었지만, `서울 초밥` 검색 시 관련이 없어보이는 가게가 검색 결과 상위에 나오는 문제를 발견했습니다.
  ![image](https://github.com/user-attachments/assets/0f676f20-8f35-4b2a-933f-52961ac6fe52)

# 검색 정확도 개선
## 문제
사용자가 `서울시 은평구 초밥집`을 검색하면, 기대하는 결과는 서울 은평구에 위치한 초밥집입니다. 그러나 기존 `multi_match` 쿼리는 다음과 같은 한계가 있습니다.
1. 키워드를 토큰화하여 가게명, 주소, 메뉴 등의 필드에서 일부라도 매칭되는 경우 결과로 반환됩니다.
2. 이로 인해 **서울이 아닌 다른 지역의 초밥집**이 높은 점수를 받아 상위에 노출될 수 있습니다.

예를 들어:
- 기대하는 결과: **서울 은평구 초밥집**
- 실제 결과: 서울이 아닌 다른 지역 초밥집이 상위에 나오는 문제

### 해결 방향
문제를 해결하기 위해 **bool** 쿼리를 사용하여 검색 쿼리를 개선하기로 결정했습니다.
### 선택지
1. **키워드 분석 후 필드별로 가중치를 다르게 설정**
    - 가중치를 조정해 원하는 결과를 도출하려 했으나, 입력된 **도시와 구**가 반드시 포함된다는 보장이 없습니다.

2. **bool 쿼리를 사용해 필수 조건 적용**
    - 키워드를 분석하여 도시와 구, 메뉴를 필터링한 후, 해당 조건을 **must**로 설정해 필수적으로 포함되도록 처리합니다.
### 결론
사용자 기대에 부합하는 검색 결과를 보장하기 위해 **bool 쿼리**를 선택했습니다. 이 접근법은 도시, 구, 메뉴와 같은 입력 조건을 반드시 충족시키는 결과를 반환할 수 있습니다.
## 1차 개선
**키워드를 분석하여 필터링**한 뒤, 도시, 구, 메뉴 이름을 추출하고 이를 활용하여 검색을 개선했습니다.
### 구현 방식

1. **도시, 구, 메뉴 필터링**
    - 키워드에서 도시, 구, 메뉴 정보를 추출.
    - 추출된 정보를 Elasticsearch의 **bool 쿼리**에서 `must` 조건으로 적용하여 **반드시 포함**되도록 설정.

2. **검색 프로세스**
    - 추출된 조건(도시, 구, 메뉴)을 기준으로 문서를 필터링.
        - 이 단계에서 입력된 위치와 메뉴를 만족하는 문서만 남김.
    - 필터링된 결과에 대해 **가게명과 메뉴 필드**를 `match`로 검색.

3. **조건이 없을 경우**
    - 만약 입력된 키워드에서 도시, 구, 메뉴 정보가 추출되지 않는다면, 가게명과 메뉴 필드만으로 검색을 수행.

4. **가게명에 우선순위 부여**
    - 가게명 필드에서 토큰화되지 않은 `keyword` 필드를 사용해 검색.
    - **boost 값을 부여**하여 메뉴보다 가게명이 더 높은 우선순위를 갖도록 설정.

## 2차 개선
1차 개선에서는 여러 필드에 대해 쿼리를 요청했습니다. 그러나 이 방식은 검색 필드가 많아질수록 성능에 부정적인 영향을 미칠 가능성이 있었습니다. 이를 개선하기 위해 **Elasticsearch의 `copy_to` 기능**을 활용하여 검색 성능을 최적화했습니다.

### 개선 방안
1. **검색 텍스트 통합**
    - 검색에 필요한 텍스트 데이터를 엘라스틱 서치의 `copy_to`를 사용해서 하나의 필드에 통합하여 저장
- 예를 들어:
    - 주소: `서울시 은평구`
    - 가게명: `맛집`
    - 메뉴: `햄버거, 피자`
    - 통합된 필드: `서울시 은평구 맛집 햄버거, 피자`

## 더 나은 검색 경험을 위해
쿼리를 여러 차례 수정하면서 사용자가 원하는 결과를 얻기 위한 방법을 고민했습니다. 그러나 검색에 대한 구체적인 비즈니스 요구사항이 없어 적절한 방향을 잡기 어려웠습니다. 이에 **캐치테이블**과 같은 서비스의 검색 방식을 분석하여 개선 아이디어를 얻었습니다.

### 분석 내용
- `소스`를 검색하면 이름에 `소스`가 포함된 가게들이 검색됨.

    ![image](https://github.com/user-attachments/assets/ff15d343-f83f-464f-93b7-5dc824f03122)

- `서울 소스`를 검색하면 서울 지역 필터가 적용되지만, 결과는 `서울 소스`와 관련 없는 가게도 포함됨.

  ![image](https://github.com/user-attachments/assets/c2627af9-7119-4d10-99bc-8907be1c6b38)


- `서울 에치세`는 검색되지 않지만, `서울 에치세로소스`와 같이 더 구체적인 입력에는 검색 결과가 나타남.

  ![image](https://github.com/user-attachments/assets/17dde920-b702-4ac9-91bc-f1cedd198d84)
- `에치세로소스스스`는 검색되지만, `서울없이 에치세로소스스스`는 결과가 나오지 않음.

  ![image](https://github.com/user-attachments/assets/536beaaa-5622-447a-9caf-27a4f4f9d8ad)

  ![image](https://github.com/user-attachments/assets/5442085a-998d-47b3-8d88-1d8fd746c663)


- `에치세르소` 는 결과가 안나온다.

  ![image](https://github.com/user-attachments/assets/22befd6f-ccce-4e04-89f2-12e80973a479)





- `서울 낙시`는 필터에 서울이 체크가 된다.

  ![image](https://github.com/user-attachments/assets/14756ed9-04a8-48db-959b-97ec9053fd5a)


- 실제로 있는 `서울 낙업`을 검색 해보면 서울에 필터 체크가 안된다.

  ![image](https://github.com/user-attachments/assets/d73cbc5b-2c6b-4d3e-83e2-a9f7675e16f3)


이러한 분석을 통해 생각한 검색 구현 방식은 다음과 같습니다.
1. **정확한 매칭 우선**
    - 사용자가 입력한 키워드를 기준으로 **term** 또는 **prefix** 쿼리를 사용해 정확히 일치하는 가게명을 먼저 검색.

2. **통합 검색 조건**
    - 만약 정확한 매칭 결과가 없을 경우, 통합 검색으로 진행.
    - 통합 검색에서는 **copy_to**로 생성한 `full_text` 필드에 쿼리를 실행하여 다양한 필드에서 키워드를 찾음.

3. **카테고리 체크와 필터링**
    - 검색된 키워드와 일치하는 카테고리가 있으면 이를 확인해 프론트엔드에 전달하여, 웹 페이지에서 해당 카테고리가 선택된 상태로 표시되도록 함.
    - `Operator`를 **AND**로 설정해 모든 키워드가 일치하는 문서만 반환하도록 처리.

4. **최적화 고려**
    - 검색 성능을 위해 두 번의 쿼리를 사용하는 방식에 대해 고려 필요.

# 검색 정확도 튜닝
기존에는 **백엔드에서 키워드를 분석**하여 프론트엔드에 필터링된 카테고리를 넘겨주는 방식을 고려했으나, 이를 **프론트엔드에서 필터 안에서 선택된 카테고리를 기준으로 검색하는 방식**으로 변경했습니다.

## 현재 검색 로직

1. **가게 이름 검색**
    - `bool` 쿼리에서 `must`와 `prefix` 쿼리를 사용해 가게 이름을 먼저 검색.

2. **카테고리 필터 적용**
    - 사용자가 요청에 카테고리 필터를 포함했다면, `district_category.keyword` 필드에 대해 `terms` 쿼리를 사용해 카테고리가 하나라도 일치하는 결과만 반환하도록 설정.

3. **검색 결과 확인**
    - 검색 결과가 있다면 해당 결과를 그대로 반환.

4. **검색 결과가 없을 경우 통합 검색 수행**
    - `bool` 쿼리를 이용해 `full_text` 필드를 대상으로 검색하며, `AND` 연산자를 사용해 키워드가 모두 포함된 결과를 반환.
    - `filter` 부분은 기존과 동일하게 유지해 카테고리 조건이 적용된 결과를 반환.

## 최적화와 개선
여러 필드를 각각 두 번의 쿼리로 검색할 필요가 없다는 점을 고려하여, **가게명으로만 먼저 검색**하는 부분을 제거하고 **바로 통합 검색을 수행**하는 방식으로 최적화했습니다.

### 튜닝 테스트
`서울복집`을 검색하며 통합 검색 방식으로의 전환 후 결과를 확인했고, 검색 정확도와 성능이 개선되는 것을 확인했습니다.

## 1차 테스트 및 개선

### 테스트 케이스 1

1. **`서울복집`** 검색 시 기대하는 결과는 서울 지역의 복집들이 최상위에 노출되는 것입니다.
2. **`경상남도 서울복집`** 검색 시 기대하는 결과는 경상남도 지역의 서울복집이 상위에 노출되는 것입니다.

### 기존 쿼리

```java
boolBuilder.must(m -> m.match(x -> x.field("full_text").query(search.keyword()).operator(Operator.And))).boost(4f);
```
* `서울복집` 검색 결과: 기대하는 결과가 나오지 않음.
  ![image](https://github.com/user-attachments/assets/6d67e1ba-343a-47f1-9b01-d91e00bc758b)

* `경상남도 서울복집` 검색 결과: 기대하는 결과가 나옴.
  ![image](https://github.com/user-attachments/assets/5f47f6b4-e1d8-4a0f-b0d4-c8b1e320fee6)

### 문제점 및 개선 방향
* 첫 번째 테스트에서 서울복집이 상위에 나오지 않는 문제가 있어, **가게 이름 필드에 가중치**를 부여하여 `should` 조건을 추가했습니다.
### 개선된 쿼리
```java
boolBuilder.must(m -> m.match(x -> x.field("full_text").query(search.keyword()).operator(Operator.And))).boost(2f)
          .should(m -> m.match(x -> x.field("title").query(search.keyword()).boost(4f)));
```
* `서울복집`과 `경상남도 서울복집` 검색 결과 모두 기대하는대로 나옴.
  ![image](https://github.com/user-attachments/assets/518c0500-c31b-452a-8eb7-f0db653ec9e2)
  ![image](https://github.com/user-attachments/assets/9386050b-aa24-478a-9f70-083f79b2e243)

## 2차 테스트 및 개선

### 테스트 케이스 2

1. **`별`** 검색 시 기대하는 결과는 정확히 "별"이라는 가게가 최상위에 표시되는 것입니다.
2. **`대구 별`** 검색 시 기대하는 결과는 대구 지역에 위치한 "별"이라는 가게가 상위에 표시되는 것입니다.

### 기존 쿼리

```java
boolBuilder.must(m -> m.match(x -> x.field("full_text").query(search.keyword()).operator(Operator.And))).boost(2f)
          .should(m -> m.match(x -> x.field("title").query(search.keyword()).boost(4f)));
```
* `별` 검색 결과: 기대한 결과와 달리 "별삽별"이라는 가게가 최상위에 표시됨.
  ![image](https://github.com/user-attachments/assets/8fdae80e-0744-4bf3-9cae-4adcd36c9802)

* `대구 별` 검색 결과: 어느 정도 원하는 결과가 나왔으나, 완벽하지 않음.
  ![image](https://github.com/user-attachments/assets/fff3481f-8074-49e7-b205-c31d31f85bc8)

### 문제점 및 개선 방향
* 기존 `should` 조건의 `match` 쿼리를 `term` 쿼리로 변경하여, 가게 이름과 정확히 일치하는 경우를 우선으로 처리

### 개선된 쿼리
```java
boolBuilder.must(m -> m.match(x -> x.field("full_text").query(search.keyword()).operator(Operator.And))).boost(2f)
          .should(m -> m.term(x -> x.field("title").value(search.keyword()).boost(4f)));
```
* `별` 검색 결과: 이전과 동일하게 기대한 결과와 일치하지 않음.
  ![image](https://github.com/user-attachments/assets/ab1d27c6-2b47-40b7-bcbf-ff1413dca8d2)

* `대구 별` 검색 결과: 일부 개선이 있었으나 여전히 기대에 미치지 못함.
  ![image](https://github.com/user-attachments/assets/b1598033-6c06-420d-8ab7-24845070bfd6)

### 최종 개선 쿼리
기존 쿼리를 결합하여 다음과 같이 개선
* 키워드가 **가게명과 정확히 일치**하면 가중치 2를 부여.
* 키워드가 가게명과 **부분 일치**하면 가중치 1.5를 부여.
```java
boolBuilder = new BoolQuery.Builder()
        .must(m -> m.match(t -> t
                .field("full_text")
                .query(search.keyword())
                .operator(Operator.And)))
        .should(s -> s.match(t -> t
                .field("title")
                .query(search.keyword())
                .boost(1.5f)))
        .should(s -> s.term(t -> t
                .field("title.keyword")
                .value(search.keyword())
                .boost(2.0f)));
```
## 3차 테스트 및 개선

### 메뉴 검색 기능 추가

기존 검색에서는 가게 이름과 주소를 중심으로 검색 결과를 도출했습니다. 이제 **메뉴 검색** 기능을 추가하며, 메뉴 데이터를 Elasticsearch에 저장하고 검색이 가능하도록 구현했습니다.

---

### Elasticsearch 업데이트 방식

1. **가게 및 메뉴 변경사항에 따른 업데이트**
    - 가게 생성, 삭제, 수정, 변경 시 Elasticsearch에 즉시 반영.
    - 메뉴 생성, 수정, 삭제, 변경 시에도 동일하게 즉시 반영.

2. **현재 데이터 업데이트**
    - 데이터가 적기 때문에 변경사항 발생 시 바로 업데이트를 수행.
    - 추후 데이터가 많아지면, 변경사항을 모아 **Bulk Update** 방식으로 전환할 예정.

3. **`copy_to` 활용**
    - 메뉴 데이터를 `copy_to`로 `full_text` 필드에 저장하여, 기존 쿼리를 수정하지 않고도 검색이 가능하도록 설정.

---

### 저장된 데이터 구조

아래와 같은 방식으로 가게 정보와 메뉴 정보가 Elasticsearch에 저장되어 있습니다.

![image](https://github.com/user-attachments/assets/a8087f61-6764-4a4e-b8ed-5ce794b06c03)

---

### 테스트 케이스

1. **`아메리카노` 검색**
    - 메뉴에 "아메리카노"가 포함된 가게가 검색됨.
    - **결과:** 메뉴 검색이 정상적으로 작동.
      ![image](https://github.com/user-attachments/assets/ea9c37d6-b358-4ffe-a1a9-8e2a4eeec839)

2. **`아이스 아메리카노` 검색**
    - 메뉴에 "아이스 아메리카노"가 포함된 가게가 검색됨.
    - **결과:** 메뉴 검색이 복합 키워드에서도 정상적으로 작동.
      ![image](https://github.com/user-attachments/assets/12fca147-0115-405f-89d7-c31926327d73)

---

### 결론

- 메뉴 데이터를 Elasticsearch에 저장하면서 기존 검색 쿼리를 수정하지 않아도 `full_text` 필드에서 메뉴 검색이 가능해졌습니다.
- 검색 결과가 정확하게 반환되는 것을 확인했습니다.
- 현재는 데이터가 적어 가게 및 메뉴에 대해 update가 있으면 즉시, 엘라스틱 서치에 crud 요청을 보내지만, 추후에 쌓인 데이터가 많아진다면 **Bulk Update** 방식으로의 전환을 고려 중입니다.

---
</details>

<br/>

---
## 역할 분담 및 협업 방식

### **Detail Role**

| 이름   | 포지션 | 담당(개인별 기여점)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    | Github 링크                           |
|------|-------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------|
| 황호진  | 리더  | ▶ **Reservation**<br> - 예약 CRUD<br> - 예약메뉴 redis로 장바구니 담으며 저장시 추출하여 저장<br> - 예약 좌석 제한 및 동시에 접근하는 것을 제어<br> - 트랜잭션이벤트리스너를 통해 결제로 트랜잭션 전파<br> ▶ **Payment**<br> - 결제 CRUD<br> - 토스페이 연동(결제 승인 , 결제 취소)<br> - 결제요청시 10분내로 결제완료하지 않으면 자동취소<br> - 트랜잭션이벤트리스너를 통해 예약으로 트랜잭션 전파<br> ▶ **Settlement**<br> - 정산 로그<br> - 정산 집계<br> - Spring Batch를 통한 로그 , 집계 대용량 데이터 처리<br> ▶ **CI / CD**<br> - Docker + Jenkins + AWS EC2를 이용한 파이프라인 구축<br> ▶ **ELK**<br> - ElasticSearch + LogStash + Kibana + Metricbeat를 이용한 모니터링<br> ▶ **react.js**<br> - 예약 API 연동 및 예약 내역 화면 개발 | [🍁https://github.com/ballqs]                           |
| 정재호  | 부리더 | ▶ **스프링 시큐리티**<br> - jwt 액세스, 리프레쉬 토큰 사용하여 인증/인가<br> ▶ **카테고리**<br> - 카테고리 CRUD<br> ▶**엘라스틱 서치 검색 고도화**<br> - 검색 정확도 향상<br> ▶ **웨이팅 대기열 구현**<br> - 웨이팅 CRUD<br> - 웨이팅 대기열 구현에 Redis Sorted Set 사용<br> - 웨이팅 완료 후 redis pub/sub을 사용하여 이벤트 처리<br> - 웨이팅 대기열 구현에 Redis 분산락 사용<br> ▶ **스프링 배치**<br> - 배치로 웨이팅 시간대별, 일간 통계 자정에 일괄 처리<br> ▶ **CI / CD**<br> - 젠킨스 CI CD 파이프라인 구축<br> - Github action으로 Sonar cloud 사용하여 코드 품질 관리                                                                                                                                                                                                                                                                                                                                                                                                                                                              | [🍁https://github.com/Nameless1004]                           |
| 조수현  | 팀원  | ▶ **Store CRUD**<br> - 음식점 CRUD<br> ▶ **AWS S3 버킷에 이미지 업로드**<br> - 음식점 사진을 S3에 업로드 후 클라우드 프론트로 이미지 조회<br> ▶ **Redisson 분산락**<br> - 음식점 조회수 증가, 좋아요 부분에 분산락 적용<br> ▶ **음식점 좋아요 index**<br> - 음식점 좋아요 테이블에서 유저 아이디로 찾아올 때 인덱스 처리<br> ▶ **음식점 이름 검색**<br> - 음식점 이름 검색에 인덱스, Redis 캐시를 이용해서 검색 속도 향상<br> ▶ **CI / CD**<br> - 젠킨스 CI CD 파이프라인 구축                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   | [🍁https://github.com/SuHyun-git]                           |
| 김윤서  | 팀원  | ▶ **Menu**<br> - 메뉴 CRUD<br> - 메뉴 좋아요, 조회 시 Redis Rlock을 통한 동시성 제어<br> ▶ **Follow**<br> - 팔로잉/팔로워 기능<br> - 팔로우 한 사용자의 가게 좋아요 목록 조회<br> - 팔로워/팔로잉 조회 캐싱 처리<br> ▶ **Chatbot**<br> - 챗봇 기능<br> ▶ **Allergy**<br> - 메뉴 생성/수정 시 알레르기 추가<br> - 모든 알레르기 조회 시 캐싱 처리<br> ▶ **Waiting**<br> - Redis 가중치를 통해 웨이팅 실시간 순위 조회<br> ▶ **소셜 로그인(카카오)**                                                                                                                                                                                                                                                                                                                                                                                                           | [🍁https://github.com/yunseokim119] |
| 길용진  | 팀원  | ▶ **Review**<br> - 메뉴 리뷰 CRUD<br> - 리뷰 좋아요<br> - 이미지 AWS S3버킷에 업로드<br> ▶ **크롤링**<br> - 블로그/뉴스 크롤링<br> - 음식점 이름 키워드 변환 후 + 맛집, 지역 추가하여 키워드화<br> - 프록시 ip 획득 후 크롤러 개발<br> ▶ **REACT활용 프론트엔드 제작**<br> - 모든 기능 기초 기반 제작                                                                                                                                                                                                                                                                                                                                                                                                           | [🍁https://github.com/pumaclass/]   |

## Ground Rule

1. **문제 발생 시 즉시 공유**
- 문제가 발생하면 팀원들에게 빠르게 상황을 공유하여 협력 해결.

2. **일정 무조건 지키기**
- 일정 변동 시 슬랙 공유 및 주말 활용할 것.

3. **사소한 것도 질문하기**
- 궁금한 점이나 막힌 부분은 사소한 것이라도 즉시 물어보고 해결.

4. **스크럼에서 트러블 슈팅 및 구현 사항 설명**
- 매일 스크럼 시간에 구현 진행 상황과 문제 해결 과정을 공유.

5. **1Day, 1Code Review, 1PR 원칙**
- 하루에 하나 이상의 구현한 코드 리뷰 및 PR 생성.

6. **1PR 당 1인 이상 확인 후 머지**
- 각 PR에 대해 최소 1명 이상의 승인을 통해 코드 품질을 개선.

---

## 성과 및 회고

### 잘된 점
- **성능 최적화 성공**
    - Redis와 Kafka를 도입해 대규모 트래픽 환경에서도 안정적인 쿠폰 발급 구현.
    - API 처리량 250req/sec를 초과 달성하며 목표를 상회하는 성과를 기록.

- **효율적인 협업**
    - 팀원 간 역할 분담이 명확했으며, GitHub Actions를 활용한 CI/CD 구축으로 개발-배포 주기를 단축.
    - 매일 스크럼을 통해 문제를 빠르게 공유하고, 적극적으로 해결.

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
