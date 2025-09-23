package hello.springtx.order;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Transactional
@Slf4j
@SpringBootTest
class OrderServiceTest {

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    @Test
    void complete() throws NotEnoughMoneyException {
        Order order = new Order();
        order.setUsername("정상");

        orderService.order(order);

        Order findOrder = orderRepository.findById(order.getId()).get();
        assertThat(findOrder.getPayStatus()).isEqualTo("완료");
    }

    @Test
    void order_예외() throws NotEnoughMoneyException {
        Order order = new Order();
        order.setUsername("예외");

        assertThatThrownBy(() -> orderService.order(order))
            .isInstanceOf(RuntimeException.class);

        Optional<Order> orderOptional = orderRepository.findById(order.getId());
        assertThat(orderOptional).isPresent();
    }

    @Test
    void order_잔고부족() throws NotEnoughMoneyException {
        Order order = new Order();
        order.setUsername("잔고부족");

        assertThatThrownBy(() -> orderService.order(order))
                .isInstanceOf(NotEnoughMoneyException.class);

        Order findOrder = orderRepository.findById(order.getId()).get();
        assertThat(findOrder.getPayStatus()).isEqualTo("대기");
    }

}