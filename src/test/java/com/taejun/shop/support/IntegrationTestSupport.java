package com.taejun.shop.support;

import com.taejun.shop.domain.member.repository.MemberRepository;
import com.taejun.shop.domain.order.repository.OrderRepository;
import com.taejun.shop.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(IntegrationTestSupport.MySqlTestConfiguration.class)
public abstract class IntegrationTestSupport {

    @TestConfiguration(proxyBeanMethods = false)
    static class MySqlTestConfiguration {

        @Bean
        @ServiceConnection
        MySQLContainer<?> mysqlContainer() {
            return new MySQLContainer<>("mysql:8.4")
                    .withDatabaseName("shop_test")
                    .withUsername("test")
                    .withPassword("test");
        }
    }

    @Container
    @ServiceConnection
    static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>("mysql:8.4")
                    .withDatabaseName("shop_test")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected MemberRepository memberRepository;

    @BeforeEach
    void cleanDatabase() {
        // 외래키 참조 역순으로 삭제
        orderRepository.deleteAll();
        productRepository.deleteAll();
        memberRepository.deleteAll();
    }

}
