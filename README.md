# Spring Transaction 학습 프로젝트

## 📌 프로젝트 개요
Spring Framework의 `@Transactional` 어노테이션과 AOP 프록시 메커니즘에 대한 학습을 위한 프로젝트입니다.

## 📚 학습 문서 목차

### 1. 트랜잭션 프록시 이슈
- **[Internal Call 트랜잭션 미적용 문제](./docs/transaction-proxy-issue.md)**
  - 같은 클래스 내부 메서드 호출 시 `@Transactional`이 적용되지 않는 문제
  - Spring AOP 프록시 메커니즘의 한계점과 해결방법
  - 메서드 가시성 제한과 초기화 시점 주의사항

### 2. 트랜잭션 옵션
- **[@Transactional 어노테이션 옵션들](./docs/transaction-option.md)**
  - 트랜잭션 전파 (Propagation)
  - 트랜잭션 격리 수준 (Isolation Level)
  - 트랜잭션 롤백 조건
  - 읽기 전용 트랜잭션 최적화

## 📁 프로젝트 구조
## 🔗 참고 자료
- [Spring Framework Documentation - Transaction Management](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#transaction)
- [Spring AOP Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#aop)

---
💡 **핵심 학습 목표**: Spring 트랜잭션의 동작 원리와 흔히 발생하는 문제점들을 이해하고 해결할 수 있습니다.
