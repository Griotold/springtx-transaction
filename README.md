# Spring Transaction 학습 프로젝트

## 📌 프로젝트 개요
Spring Framework의 `@Transactional` 어노테이션과 AOP 프록시 메커니즘에 대한 학습을 위한 프로젝트입니다.

## 🚨 주요 문제점: Internal Call 트랜잭션 미적용

### 문제 상황
```java
@Slf4j
static class CallService {
    public void external() {
        log.info("call external");
        printTxInfo();
        internal(); // ❌ 트랜잭션이 적용되지 않음
    }

    @Transactional
    public void internal() {
        log.info("call internal");
        printTxInfo();
    }
}
```

### 실행 결과
```
INFO  --- call external
INFO  --- txActive:false
INFO  --- call internal
INFO  --- txActive:false  ← 트랜잭션이 적용되지 않음!
```

### 🔍 원인 분석
- **Spring AOP는 프록시 기반**으로 동작
- `external()` 메서드에서 `internal()` 호출 시 **프록시를 거치지 않고 직접 메서드 호출**
- 따라서 `@Transactional` 어노테이션이 무시됨

```
[Client] → [Proxy] → external() → internal()  (프록시 우회)
                         ↑
                    실제 객체 내부 호출
```

## ✅ 해결방법

### 1. external()에도 @Transactional 추가
```java
@Transactional
public void external() {
    log.info("call external");
    printTxInfo();
    internal();
}

@Transactional
public void internal() {
    log.info("call internal");
    printTxInfo();
}
```
**장점**: 간단한 수정  
**단점**: external()에 불필요한 트랜잭션이 적용될 수 있음

### 2. 별도 클래스로 분리 (권장 ⭐)
```java
@TestConfiguration
static class Config {
    @Bean
    CallService callService() {
        return new CallService();
    }
    
    @Bean
    InternalService internalService() {
        return new InternalService();
    }
}

@Slf4j
static class CallService {
    @Autowired
    private InternalService internalService;
    
    public void external() {
        log.info("call external");
        printTxInfo();
        internalService.internal(); // ✅ 다른 빈의 메서드 호출
    }
}

@Slf4j
static class InternalService {
    @Transactional
    public void internal() {
        log.info("call internal");
        printTxInfo();
    }
}
```
**장점**: 
- 책임 분리
- 명확한 트랜잭션 경계
- 유지보수성 향상

### 3. Self-Injection 사용
```java
@Slf4j
static class CallService {
    @Autowired
    private CallService self; // 자기 자신의 프록시 주입
    
    public void external() {
        log.info("call external");
        printTxInfo();
        self.internal(); // ✅ 프록시를 통한 호출
    }
    
    @Transactional
    public void internal() {
        log.info("call internal");
        printTxInfo();
    }
}
```
**장점**: 기존 구조 유지  
**단점**: Self-injection으로 인한 복잡성

## 📊 해결방법 비교

| 방법 | 구현 난이도 | 유지보수성 | 성능 | 권장도 |
|------|------------|------------|------|--------|
| @Transactional 추가 | ⭐ | ⭐⭐ | ⭐⭐⭐ | ⭐⭐ |
| **클래스 분리** | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | **⭐⭐⭐** |
| Self-Injection | ⭐⭐⭐ | ⭐⭐ | ⭐⭐ | ⭐⭐ |

## 🚨 추가 주의사항

### 메서드 가시성 제한
- **@Transactional은 public 메서드에만 적용됩니다**
- private, protected, package-private 메서드는 **어노테이션이 무시됩니다**
- 컴파일 에러나 경고 없이 조용히 무시되므로 주의 필요

```java
@Slf4j
static class CallService {
    @Transactional
    public void publicMethod() {
        printTxInfo(); // ✅ 트랜잭션 적용됨
    }
    
    @Transactional
    private void privateMethod() {
        printTxInfo(); // ❌ 트랜잭션 적용 안됨 (무시됨)
    }
}
```

### 초기화 메서드와 @Transactional
- **@PostConstruct와 @Transactional을 함께 사용하면 트랜잭션이 적용되지 않습니다**
- 초기화 시점에는 프록시가 완전히 준비되지 않았기 때문
- **해결책**: `ApplicationReadyEvent` 사용

```java
@Slf4j
static class Hello {
    @PostConstruct
    @Transactional
    public void initV1() {
        boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("Hello init @PostConstruct tx active={}", isActive); // ❌ false
    }

    @EventListener(value = ApplicationReadyEvent.class)
    @Transactional
    public void initV2() {
        boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("Hello init ApplicationReadyEvent tx active={}", isActive); // ✅ true
    }
}
```

**이유**: Spring은 빈 생성 → 의존성 주입 → @PostConstruct → 프록시 생성 순서로 동작하므로, @PostConstruct 시점에는 아직 트랜잭션 프록시가 준비되지 않습니다.

## 🔑 핵심 포인트

1. **Spring AOP는 프록시 기반**으로 동작
2. **같은 객체 내부 메서드 호출**은 프록시를 우회
3. **public 메서드에만** @Transactional 적용 가능
4. **초기화 시점**에는 @Transactional이 동작하지 않음
5. **별도 클래스 분리**가 가장 깔끔한 해결책
6. 트랜잭션 경계를 명확히 설계하는 것이 중요

## 📁 프로젝트 구조
```
src/test/java/hello/springtx/apply/
├── InternalCallV1Test.java     # 문제 상황 재현
├── InternalCallV2Test.java     # 해결방법 적용
└── ...
```

## 🔗 참고 자료
- [Spring Framework Documentation - Transaction Management](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#transaction)
- [Spring AOP Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#aop)

---
💡 **기억하기**: Spring의 @Transactional은 프록시를 통해서만 작동합니다!
