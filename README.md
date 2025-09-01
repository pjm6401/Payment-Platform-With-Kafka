# Payment-Platform-With-Kafka

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.0-6DB33F?style=for-the-badge&logo=spring-boot)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-3.6-231F20?style=for-the-badge&logo=apache-kafka)
![Redis](https://img.shields.io/badge/Redis-7.2-DC382D?style=for-the-badge&logo=redis)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?style=for-the-badge&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-26.1-2496ED?style=for-the-badge&logo=docker)
![Java](https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=openjdk)

실시간으로 발생하는 대규모 결제 데이터를 Apache Kafka를 통해 안정적으로 처리하고, 여러 분석 서비스를 통해 부가 가치를 창출하는 이벤트 기반 결제 분석 플랫폼입니다.

---
## 🎯 프로젝트 목표 (Objective)

결제 산업에서 업무를 수행하며, 실시간으로 발생하는 대량의 트랜잭션 데이터를 활용해 고객과 가맹점주 모두에게 즉각적인 가치를 제공할 수 있는 기능에 대한 아이디어가 많았습니다. 이 프로젝트는 당시의 아이디어들을 직접 구현해보고자 시작한 토이 프로젝트입니다.

**Apache Kafka**를 중심으로 한 **이벤트 기반 아키텍처(Event-Driven Architecture)** 를 구축하고 운영하는 경험을 목표로 삼았으며, 이를 통해 아래와 같은 기능들을 구현했습니다.

* **실시간 이상 거래 탐지 (FDS)**: 물리적으로 불가능한 해외 결제 시도를 실시간으로 탐지하고, 사용자에게 SMS 인증을 통해 거래를 직접 승인/거절하게 하는 양방향 FDS
* **단골 고객 분석 (Loyalty Service)**: 고객의 매장별 방문 횟수를 실시간으로 카운트하여, 특정 조건 충족 시 VIP 혜택을 제공하는 단골 관리 기능
* **결제 혜택 안내 (Benefit Guide)**: 고객이 결제를 완료하는 순간, 해당 가맹점에서 더 좋은 혜택을 받을 수 있었던 다른 카드 정보를 '꿀팁'으로 알려주는 개인화된 안내 기능

---
## 🛠️ 프로젝트 설계 (Architecture)

본 프로젝트는 단일 결제 이벤트를 여러 독립적인 서비스가 동시에 구독하여 각자의 역할을 수행하는 **마이크로서비스 아키텍처**를 기반으로 합니다. 서비스 간의 통신은 **Apache Kafka**가 중앙 허브 역할을 하여 모든 결합을 제거(Decoupling)합니다.

#### **전체 아키텍처 다이어그램**
[Image of the real-time payment processing architecture]

#### **데이터 흐름 및 서비스 역할**

1.  **결제 발생**: 클라이언트(`User App/POS`)에서 결제 요청이 발생하면 `Payment API` 서버로 전달됩니다.
2.  **이벤트 발행**: `Payment API`는 결제 정보를 **`payment-stream`** Kafka 토픽으로 발행합니다. `Payment API`의 역할은 이벤트를 안전하게 Kafka에 전달하는 것까지입니다.
3.  **이벤트 동시 구독**: **FDS, Loyalty, Benefit Guide Consumer**는 각자 다른 Consumer Group ID를 가지고 `payment-stream` 토픽을 구독하여, 동일한 결제 이벤트를 동시에 수신합니다.
4.  **실시간 분석**: 각 Consumer는 **Redis**에 저장된 데이터를 초고속으로 조회하여 실시간 분석을 수행합니다. 데이터가 Redis에 없을 경우(Cache Miss), **PostgreSQL** 원장을 조회하여 데이터를 다시 채워 넣는 **Cache-Aside 패턴**을 적용했습니다.
5.  **후속 처리**:
    * **FDS**: 이상 거래 탐지 시 `Verification API`를 통해 사용자 인증을 요청합니다. 인증 성공시 사용자 상태를 갱신합니다.
    * **Loyalty / Benefit Guide**: 분석 결과에 따라 사용자에게 알림을 보내거나, 가맹점주 대시보드 데이터를 업데이트합니다.
    * **FDS (DB 저장)**: 모든 거래의 최종 상태(`COMPLETED`, `PENDING`, `DENIED`)를 PostgreSQL의 `transactions` 테이블에 기록하여 원장 관리 책임을 수행합니다.

---
## 🚀 기술 스택 (Tech Stack)

* **Backend**: `Java 17`, `Spring Boot 3.2.0`, `Spring Data JPA`, `Spring Data Redis`, `Spring for Apache Kafka`
* **Database**: `PostgreSQL` (영구 데이터 저장), `Redis` (실시간 데이터 캐싱 및 분석)
* **Message Broker**: `Apache Kafka`
* **DevOps**: `Docker`, `Docker Compose`, `AWS EC2`
* **Build Tool**: `Gradle`

---
## 👨‍💻 프로그래머 정보 (Developer)

* **Name**: 박정민
* **Email**: pjm1998@gmail.com