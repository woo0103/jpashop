package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import jpabook.jpashop.domain.item.Book;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private EntityManager em;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;

    @Test
    void 상품주문() {
        //when
        Member member = createMember();

        Book book = createBook("시골 JPA", 10000, 10);

        //when
        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order findOrder = orderRepository.findOne(orderId);
        Assertions.assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.ORDER);
        Assertions.assertThat(findOrder.getOrderItems().size()).isEqualTo(1);
        Assertions.assertThat(findOrder.getTotalPrice()).isEqualTo(2 * 10000);
        Assertions.assertThat(book.getStockQuantity()).isEqualTo(10 - 2);

    }

    @Test
    void 주문취소() {
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order order = orderRepository.findOne(orderId);
        Assertions.assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCEL);
        Assertions.assertThat(book.getStockQuantity()).isEqualTo(10);
    }

    @Test
    void 상품주문_재고수량초과() {
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 11;

        //when
        //then
        Assertions.assertThatThrownBy(() -> orderService.order(member.getId(), book.getId(), orderCount))
                .isInstanceOf(NotEnoughStockException.class);

    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강가", "123-123"));
        em.persist(member);
        return member;
    }

    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }
}