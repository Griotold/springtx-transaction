# 스프링 트랜잭션 전파 - 롤백 상황

## 1. 외부 롤백 (External Rollback)

### 📝 상황 설명
- **내부 트랜잭션**: 정상적으로 커밋 시도
- **외부 트랜잭션**: 문제가 발생하여 롤백
- **결과**: 내부 트랜잭션도 함께 롤백됨

### 🔍 핵심 원칙 재확인
> **"논리 트랜잭션이 하나라도 롤백되면 물리 트랜잭션은 롤백된다"**

내부 트랜잭션이 커밋했어도, 외부 트랜잭션이 롤백하면 **전체가 롤백**됩니다.

### 🧪 테스트 코드 분석
```java
@Test
void outer_rollback() {
    log.info("외부 트랜잭션 시작");
    TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
    // outer.isNewTransaction() = true (신규 트랜잭션)

    log.info("내부 트랜잭션 시작");
    TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
    // inner.isNewTransaction() = false (참여 트랜잭션)
    
    log.info("내부 트랜잭션 커밋");
    txManager.commit(inner); // 논리적 커밋 (물리적 작업 없음)

    log.info("외부 트랜잭션 롤백");
    txManager.rollback(outer); // 물리적 롤백 수행!
}
```

### 📊 실행 로그 분석
```
외부 트랜잭션 시작
Creating new transaction with name [null]
Acquired Connection [conn0] for JDBC transaction
Switching JDBC Connection [conn0] to manual commit

내부 트랜잭션 시작
Participating in existing transaction  // 기존 트랜잭션 참여

내부 트랜잭션 커밋                     // 로그 없음 (논리적 커밋만)

외부 트랜잭션 롤백
Initiating transaction rollback        // 실제 롤백 시작
Rolling back JDBC transaction         // 물리적 롤백 수행
Releasing JDBC Connection [conn0]     // 커넥션 반납
```

### 🎯 동작 흐름 상세 분석

#### 1단계: 외부 트랜잭션 시작
- 새로운 물리 트랜잭션 생성 (`conn0` 획득)
- `outer.isNewTransaction() = true`

#### 2단계: 내부 트랜잭션 시작  
- 기존 트랜잭션에 참여
- `inner.isNewTransaction() = false`
- 동일한 `conn0` 사용

#### 3단계: 내부 트랜잭션 커밋
- **논리적 커밋만 수행**
- 실제 DB 커밋은 발생하지 않음
- 신규 트랜잭션이 아니므로 물리적 작업 없음

#### 4단계: 외부 트랜잭션 롤백
- **물리적 롤백 수행** 
- 실제 DB에서 모든 변경사항 롤백
- 내부 트랜잭션의 작업도 모두 롤백됨

### 🔄 트랜잭션 상태 다이어그램

```
[전체 물리 트랜잭션 - 최종 결과: 롤백]
┌─────────────────────────────────────────────────┐
│                                               │
│  외부 트랜잭션 (신규)     내부 트랜잭션 (참여)     │
│  ┌─────────────────┐    ┌─────────────────┐   │
│  │     로직1       │ →  │     로직2       │   │
│  │ isNew = true   │    │ isNew = false  │   │
│  └─────────────────┘    └─────────────────┘   │
│           ↓                       ↓          │
│      롤백 요청              논리적 커밋         │
│           ↓                                   │
│         물리적 롤백 (모든 변경사항 취소)         │
└─────────────────────────────────────────────────┘
                    ↓
            [전체 트랜잭션 롤백 완료]
```

### ⚠️ 중요 포인트

1. **내부 트랜잭션의 커밋은 무의미**
   - 논리적 커밋만 수행되므로 실제로는 아무 작업도 하지 않음
   - 외부 트랜잭션이 롤백하면 내부 작업도 모두 취소

2. **물리 트랜잭션의 일관성**
   - 하나의 물리 트랜잭션 내에서는 모든 작업이 함께 커밋되거나 롤백
   - **원자성(Atomicity) 보장**

3. **외부 트랜잭션의 책임**
   - 신규 트랜잭션(`isNewTransaction = true`)만이 물리적 커밋/롤백 권한을 가짐
   - 외부 트랜잭션이 전체 트랜잭션의 최종 운명을 결정

### 💡 실무 적용 시나리오

```java
@Service
public class OrderService {
    
    @Transactional
    public void processOrder(Order order) {
        // 외부 트랜잭션 시작
        orderRepository.save(order);        // 주문 저장
        
        try {
            paymentService.processPayment(order); // 내부 트랜잭션 참여
            
            // 주문 처리 중 비즈니스 로직 문제 발생
            if (order.isInvalid()) {
                throw new BusinessException("주문이 유효하지 않습니다");
            }
            
        } catch (BusinessException e) {
            // 외부 트랜잭션 롤백 → 주문과 결제 모두 취소
            throw e;
        }
    }
}
```

**결과**: `paymentService.processPayment()`에서 결제가 성공했더라도, 외부 트랜잭션에서 예외가 발생하면 **주문과 결제 모두 롤백**됩니다.

### 🔗 관련 테스트 코드
- `BasicTxTest.outer_rollback()`: 외부 롤백 상황 재현

---

## 2. 내부 롤백