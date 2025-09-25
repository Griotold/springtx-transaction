# 스프링 트랜잭션 전파 - REQUIRES_NEW
- 외부 트랜잭션과 내부 트랜잭션이 각각 별도의 물리 트랜잭션을 가진다.
- 내부 트랜잭션에 REQUIRES_NEW 옵션을 주면 된다.

## 동작 흐름
```
@Test
void inner_rollback_requires_new() {
    log.info("외부 트랜잭션 시작");
    TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
    log.info("outer.isNewTransaction() = " + outer.isNewTransaction());

    log.info("내부 트랜잭션 시작");
    DefaultTransactionAttribute definition = new DefaultTransactionAttribute();
    definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW); // 기존 트랜잭션이 있어도 무시하고 새롭게
    TransactionStatus inner = txManager.getTransaction(definition);
    log.info("inner.isNewTransaction() = " + inner.isNewTransaction()); // true

    log.info("내부 트랜잭션 롤백");
    txManager.rollback(inner); // 롤백
    log.info("외부 트랜잭션 커밋");
    txManager.commit(outer); // 커밋
}
```

## 로그 분석
```
외부 트랜잭션 시작
Creating new transaction with name [null]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
Acquired Connection [HikariProxyConnection@1237927927 wrapping conn0: url=jdbc:h2:mem:04577032-6e64-4798-90a6-d9af62ec6af6 user=SA] for JDBC transaction
Switching JDBC Connection [HikariProxyConnection@1237927927 wrapping conn0: url=jdbc:h2:mem:04577032-6e64-4798-90a6-d9af62ec6af6 user=SA] to manual commit
outer.isNewTransaction() = true
내부 트랜잭션 시작
Suspending current transaction, creating new transaction with name [null]
Acquired Connection [HikariProxyConnection@807542010 wrapping conn1: url=jdbc:h2:mem:04577032-6e64-4798-90a6-d9af62ec6af6 user=SA] for JDBC transaction
Switching JDBC Connection [HikariProxyConnection@807542010 wrapping conn1: url=jdbc:h2:mem:04577032-6e64-4798-90a6-d9af62ec6af6 user=SA] to manual commit
inner.isNewTransaction() = true
내부 트랜잭션 롤백
Initiating transaction rollback
Rolling back JDBC transaction on Connection [HikariProxyConnection@807542010 wrapping conn1: url=jdbc:h2:mem:04577032-6e64-4798-90a6-d9af62ec6af6 user=SA]
Releasing JDBC Connection [HikariProxyConnection@807542010 wrapping conn1: url=jdbc:h2:mem:04577032-6e64-4798-90a6-d9af62ec6af6 user=SA] after transaction
Resuming suspended transaction after completion of inner transaction
외부 트랜잭션 커밋
Initiating transaction commit
Committing JDBC transaction on Connection [HikariProxyConnection@1237927927 wrapping conn0: url=jdbc:h2:mem:04577032-6e64-4798-90a6-d9af62ec6af6 user=SA]
Releasing JDBC Connection [HikariProxyConnection@1237927927 wrapping conn0: url=jdbc:h2:mem:04577032-6e64-4798-90a6-d9af62ec6af6 user=SA] after transaction
Closing JPA EntityManagerFactory for persistence unit 'default'
```

- 내부 트랜잭션이 시작될 때 기존 트랜잭션은 Suspending 되고,
- 커넥션도 새롭게 얻는다. (conn1)
- 아예 새로운 트랜잭션
- 내부 트랜잭션이 끝나면(커밋되던, 롤백되던) Suspending 된 외부 트랜잭션은 Resuming 된다.
- 내부 트랜잭션이 롤백 되어도 외부 트랜잭션은 정상적으로 커밋이 된다.

