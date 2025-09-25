# Spring Transaction 학습 프로젝트

## 📌 프로젝트 개요
Spring Framework의 `@Transactional` 어노테이션과 AOP 프록시 메커니즘에 대한 학습을 위한 프로젝트입니다.

## 📚 학습 문서 목차

### 1. 트랜잭션 프록시 이슈
- **[Internal Call 트랜잭션 미적용 문제](./docs/transaction-proxy-issue.md)**
  - 같은 클래스 내부 메서드 호출 시 `@Transactional`이 적용되지 않는 문제
  - Spring AOP 프록시 메커니즘의 한계점과 해결방법
  - 메서드 가시성 제한과 초기화 시점 주의사항

### 2. 트랜잭션 롤백
- **[트랜잭션 롤백 정책과 예외 처리](./docs/transaction-rollback.md)**
  - Spring의 기본 롤백 규칙 (RuntimeException vs Checked Exception)
  - rollbackFor와 noRollbackFor 옵션 활용
  - 비즈니스 예외 vs 시스템 예외 구분 전략
  - 실무 활용 팁과 예제

### 3. 트랜잭션 전파 기본
- **[트랜잭션 전파 (Propagation)](./docs/transaction-propagation.md)**
  - 트랜잭션 전파의 개념과 종류

### 4. 트랜잭션 전파 - 롤백 상황
- **[트랜잭션 전파 - 롤백 상황](./docs/transaction-propagation-rollback.md)**
  - 외부 트랜잭션 롤백 시 내부 트랜잭션에 미치는 영향
  - 내부 트랜잭션 롤백 시 발생하는 문제와 해결방안
  - 논리 트랜잭션과 물리 트랜잭션의 롤백 메커니즘


## 📁 프로젝트 구조
## 🔗 참고 자료
- [Spring Framework Documentation - Transaction Management](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#transaction)
- [Spring AOP Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#aop)

---
💡 **핵심 학습 목표**: Spring 트랜잭션의 동작 원리와 흔히 발생하는 문제점들을 이해하고 해결할 수 있습니다.
