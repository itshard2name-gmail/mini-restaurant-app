package com.example.order;

import com.example.order.dto.CreateOrderRequest;
import com.example.order.dto.OrderItemRequest;
import com.example.order.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.TestPropertySource(properties = {
        "app.jwt.secret=very-long-test-secret-key-for-jwt-signing-and-verification-purposes-1234567890",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.example.order.repository.MenuRepository menuRepository;

    @Test
    @WithMockUser(username = "customer", roles = "USER")
    public void testCreateOrderFlow() throws Exception {
        // Seed Data
        com.example.order.entity.Menu menu = new com.example.order.entity.Menu();
        menu.setName("Test Burger");
        menu.setPrice(new java.math.BigDecimal("10.00"));
        menu.setDescription("Delicious");
        menu.setImageUrl("http://img.com/burger.png");
        com.example.order.entity.Menu savedMenu = menuRepository.save(menu);

        // Prepare Request
        CreateOrderRequest request = new CreateOrderRequest();
        OrderItemRequest item = new OrderItemRequest();
        item.setMenuId(savedMenu.getId()); // Use dynamic ID
        item.setQuantity(2);
        request.setItems(Collections.singletonList(item));

        // Execute Post
        mockMvc.perform(post("/orders")
                .header("X-User-Id", "123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify DB (Optional, or check size increased)
        // assert(orderRepository.count() > 0);
    }
}
