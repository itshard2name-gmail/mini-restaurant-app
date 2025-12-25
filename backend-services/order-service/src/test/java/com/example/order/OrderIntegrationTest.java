package com.example.order;

import com.example.order.dto.CreateOrderRequest;
import com.example.order.dto.OrderItemRequest;
import com.example.order.entity.Order;
import com.example.order.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

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

        private Long createMenu() {
                com.example.order.entity.Menu menu = new com.example.order.entity.Menu();
                menu.setName("Test Burger");
                menu.setPrice(new java.math.BigDecimal("10.00"));
                menu.setDescription("Delicious");
                menu.setImageUrl("http://img.com/burger.png");
                return menuRepository.save(menu).getId();
        }

        private OrderItemRequest createItemRequest(Long menuId, int quantity) {
                OrderItemRequest item = new OrderItemRequest();
                item.setMenuId(menuId);
                item.setQuantity(quantity);
                return item;
        }

        @Test
        @WithMockUser(username = "customer", roles = "USER")
        public void testCreateOrderFlow() throws Exception {
                Long menuId = createMenu();

                CreateOrderRequest request = new CreateOrderRequest();
                request.setItems(Collections.singletonList(createItemRequest(menuId, 2)));

                mockMvc.perform(post("/orders")
                                .header("X-User-Id", "123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());
        }

        @Test
        public void testCreateGuestOrderSuccess() throws Exception {
                Long menuId = createMenu();

                CreateOrderRequest request = new CreateOrderRequest();
                request.setOrderType("DINE_IN");
                request.setTableNumber("T5");
                request.setItems(Collections.singletonList(createItemRequest(menuId, 1)));

                mockMvc.perform(post("/orders")
                                // No X-User-Id
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.guestToken", notNullValue()));
        }

        @Test
        public void testCreateGuestOrderFail_Takeout() {
                Long menuId = createMenu();

                CreateOrderRequest request = new CreateOrderRequest();
                request.setOrderType("TAKEOUT"); // Fail: Guest cannot do Takeout
                request.setItems(Collections.singletonList(createItemRequest(menuId, 1)));

                // Expect Exception because no ExceptionHandler is defined to swallow it
                org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
                        mockMvc.perform(post("/orders")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)));
                });
        }

        @Test
        public void testCreateGuestOrderFail_NoTable() {
                Long menuId = createMenu();

                CreateOrderRequest request = new CreateOrderRequest();
                request.setOrderType("DINE_IN");
                // No Table Number
                request.setItems(Collections.singletonList(createItemRequest(menuId, 1)));

                org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
                        mockMvc.perform(post("/orders")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)));
                });
        }

        @Test
        public void testGetGuestOrderSuccess() throws Exception {
                // 1. Create Guest Order
                Long menuId = createMenu();
                CreateOrderRequest request = new CreateOrderRequest();
                request.setOrderType("DINE_IN");
                request.setTableNumber("T5");
                request.setItems(Collections.singletonList(createItemRequest(menuId, 1)));

                MvcResult result = mockMvc.perform(post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andReturn();

                String responseStr = result.getResponse().getContentAsString();
                Order createdOrder = objectMapper.readValue(responseStr, Order.class);
                String token = createdOrder.getGuestToken();
                Long orderId = createdOrder.getId();

                // 2. Get Order with Token
                mockMvc.perform(get("/orders/" + orderId)
                                .param("token", token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", is(orderId.intValue())));
        }

        @Test
        public void testGetGuestOrderFail_WrongToken() throws Exception {
                // 1. Create Guest Order
                Long menuId = createMenu();
                CreateOrderRequest request = new CreateOrderRequest();
                request.setOrderType("DINE_IN");
                request.setTableNumber("T5");
                request.setItems(Collections.singletonList(createItemRequest(menuId, 1)));

                MvcResult result = mockMvc.perform(post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andReturn();

                String responseStr = result.getResponse().getContentAsString();
                Order createdOrder = objectMapper.readValue(responseStr, Order.class);
                Long orderId = createdOrder.getId();

                // 2. Get Order with WRONG Token
                org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
                        mockMvc.perform(get("/orders/" + orderId)
                                        .param("token", "BAD_TOKEN"));
                });
        }

        @Test
        public void testAddItemsToGuestOrder() throws Exception {
                // 1. Create Guest Order
                Long menuId = createMenu();
                CreateOrderRequest request = new CreateOrderRequest();
                request.setOrderType("DINE_IN");
                request.setTableNumber("T5");
                request.setItems(Collections.singletonList(createItemRequest(menuId, 1)));

                MvcResult result = mockMvc.perform(post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andReturn();

                String responseStr = result.getResponse().getContentAsString();
                Order createdOrder = objectMapper.readValue(responseStr, Order.class);
                String token = createdOrder.getGuestToken();
                Long orderId = createdOrder.getId();

                // 2. Add Item to Order
                mockMvc.perform(post("/orders/" + orderId + "/items")
                                .param("token", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                Collections.singletonList(createItemRequest(menuId, 2)))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalPrice", is(30.0))); // 10.0 + (10.0 * 2) = 30.0
        }

        @Test
        @WithMockUser(username = "customer", roles = "USER")
        public void testAddItemsToUserOrder() throws Exception {
                // 1. Create User Order
                Long menuId = createMenu();
                CreateOrderRequest request = new CreateOrderRequest();
                request.setItems(Collections.singletonList(createItemRequest(menuId, 1)));

                MvcResult result = mockMvc.perform(post("/orders")
                                .header("X-User-Id", "123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andReturn();

                String responseStr = result.getResponse().getContentAsString();
                Order createdOrder = objectMapper.readValue(responseStr, Order.class);
                Long orderId = createdOrder.getId();

                // 2. Add Item to Order
                mockMvc.perform(post("/orders/" + orderId + "/items")
                                .header("X-User-Id", "123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                Collections.singletonList(createItemRequest(menuId, 1)))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalPrice", is(20.0))); // 10.0 + 10.0 = 20.0
        }
}
