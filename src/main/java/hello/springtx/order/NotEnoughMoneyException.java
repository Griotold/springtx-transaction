package hello.springtx.order;

/**
 * 비지니스 예외
 * - 시스템 문제가 아니라 고객의 잔고가 부족한게 문제
 * 얘가 발생했을 때는 롤백 안하고 싶어.
 * */
public class NotEnoughMoneyException extends Exception {
    public NotEnoughMoneyException(String message) {
        super(message);
    }
}
