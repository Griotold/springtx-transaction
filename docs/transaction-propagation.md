# transaction propagation

## 스프링 트랜잭션의 기본: REQUIRED(이 메서드는 트랜잭션이 필요해!) 
- 기존 트랜잭션 참여
- 없으면 새로 생성

### 원칙
- 모든 논리 트랜잭션이 커밋되어야 물리 트랜잭션이 커밋된다.
- 하나의 논리 트랜잭션이라도 롤백되면 물리 트랜잭션은 롤백된다.

## REQUIRED 전파의 실제 동작 방식

### 🔍 물리 트랜잭션 vs 논리 트랜잭션
- **물리 트랜잭션**: 실제 데이터베이스에 적용되는 트랜잭션 (실제 커넥션)
- **논리 트랜잭션**: 트랜잭션 매니저를 통해 트랜잭션을 구분하는 단위

### 📊 동작 흐름
```java
// BasicTxTest의 inner_commit() 동작 분석
@Test
void inner_commit() {
    log.info("외부 트랜잭션 시작");
    TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
    log.info("outer.isNewTransaction() = " + outer.isNewTransaction()); // true
    
    log.info("내부 트랜잭션 시작"); 
    TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
    log.info("inner.isNewTransaction() = " + inner.isNewTransaction()); // false
    
    log.info("내부 트랜잭션 커밋");
    txManager.commit(inner); // 논리적 커밋 (실제로는 아무것도 안함)
    
    log.info("외부 트랜잭션 커밋");
    txManager.commit(outer); // 물리적 커밋 (실제 DB 커밋)
}
```

### ⚡ 핵심 포인트

- **isNewTransaction() = true**: 물리 트랜잭션을 실제로 시작한 트랜잭션
- **isNewTransaction() = false**: 기존 물리 트랜잭션에 참여한 논리 트랜잭션
- **논리적 커밋**: 내부 트랜잭션의 commit() → 실제로는 아무 일도 일어나지 않음
- **물리적 커밋**: 최초 트랜잭션의 commit() → 실제 데이터베이스에 반영

## 트랜잭션 참여 메커니즘 상세 분석

### 🔄 트랜잭션 참여란?
내부 트랜잭션이 외부 트랜잭션에 참여한다는 것은:
- 내부 트랜잭션이 **외부 트랜잭션을 그대로 이어받아서** 따르는 것
- 외부에서 시작된 **물리적 트랜잭션의 범위가 내부 트랜잭션까지 넓어지는** 것
- **외부 트랜잭션과 내부 트랜잭션이 하나의 물리 트랜잭션으로 묶이는** 것

### 🤔 커밋 처리의 비밀
```java
txManager.commit(inner); // 논리적 커밋 (아무것도 안함)
txManager.commit(outer); // 물리적 커밋 (실제 DB 반영)
```

**Q: 어떻게 커밋을 두 번 호출했는데 트랜잭션이 정상 동작할까?**

**A: Spring은 신규 트랜잭션 여부에 따라 다르게 동작합니다**

#### 핵심 원리
- **신규 트랜잭션 (`isNewTransaction = true`)**: 실제 물리 커밋/롤백 수행
- **참여 트랜잭션 (`isNewTransaction = false`)**: 논리적 처리만, 물리적 작업은 하지 않음


### 🎯 Spring의 중복 커밋 문제 해결 전략
1. **처음 트랜잭션을 시작한 외부 트랜잭션**이 물리 트랜잭션 관리
2. **내부 트랜잭션**은 논리적 처리만 담당
3. **트랜잭션 동기화 매니저**를 통해 동일한 커넥션 공유
4. **신규 트랜잭션 여부**로 실제 커밋/롤백 결정

### 🔗 관련 테스트 코드
- `BasicTxTest.inner_commit()`: REQUIRED 전파 동작 확인

### 💡 핵심 정리
- 트랜잭션 매니저의 commit() 호출 ≠ 항상 물리 커밋
- **신규 트랜잭션만** 실제 물리 커밋/롤백 수행
- **논리 트랜잭션과 물리 트랜잭션의 분리**로 복잡한 전파 상황 해결