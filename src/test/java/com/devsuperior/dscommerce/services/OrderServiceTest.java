package com.devsuperior.dscommerce.services;

import com.devsuperior.dscommerce.dto.OrderDTO;
import com.devsuperior.dscommerce.entities.Order;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.factory.OrderFactory;
import com.devsuperior.dscommerce.factory.ProductFactory;
import com.devsuperior.dscommerce.factory.UserFactory;
import com.devsuperior.dscommerce.repositories.OrderItemRepository;
import com.devsuperior.dscommerce.repositories.OrderRepository;
import com.devsuperior.dscommerce.repositories.ProductRepository;
import com.devsuperior.dscommerce.services.exceptions.ForbiddenException;
import com.devsuperior.dscommerce.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.client.ResourceAccessException;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService service;

    @Mock
    private OrderRepository repository;

    @Mock
    private AuthService authService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private UserService userService;

    private Long existingOrderId, nonExistingOrderId;
    private long existingProductId, nonExistingProductId;
    private Order order;
    private OrderDTO orderDTO;
    private User admin, client;
    private Product product;

    @BeforeEach
    void setUp() {
        existingOrderId = 1L;
        nonExistingOrderId = 2L;

        existingProductId = 1L;
        nonExistingProductId = 2L;

        admin = UserFactory.createCustomAdminUser(1L, "Jef");
        client = UserFactory.createCustomClientUser(2L, "Bob");

        order = OrderFactory.createOrder(client);
        orderDTO = new OrderDTO(order);

        product = ProductFactory.createProduct();

        Mockito.when(repository.findById(existingOrderId)).thenReturn(Optional.of(order));
        Mockito.when(repository.findById(nonExistingOrderId)).thenReturn(Optional.empty());

        Mockito.when(productRepository.getReferenceById(existingProductId)).thenReturn(product);
        Mockito.when(productRepository.getReferenceById(nonExistingProductId)).thenThrow(EntityNotFoundException.class);

        Mockito.when(repository.save(any())).thenReturn(order);
        Mockito.when(orderItemRepository.saveAll(any())).thenReturn(new ArrayList<>(order.getItems()));
    }

    @Test
    public void findByIdShouldReturnOrderDTOWhenIdExistsAndAdminLogged() {

        Mockito.doNothing().when(authService).validateSelfOrAdmin(any());

        OrderDTO result = service.findById(existingOrderId);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), existingOrderId);
    }

    @Test
    public void findByIdShouldReturnOrderDTOWhenIdExistsAndSelfClientLogged() {

        Mockito.doNothing().when(authService).validateSelfOrAdmin(any());

        OrderDTO result = service.findById(existingOrderId);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), existingOrderId);
    }

    @Test
    public void findByIdShouldThrowsForbiddenExceptionWhenIdExistsAndOtherClientLogged() {

        Mockito.doThrow(ForbiddenException.class).when(authService).validateSelfOrAdmin(any());

        Assertions.assertThrows(ForbiddenException.class, () -> {
            OrderDTO result = service.findById(existingOrderId);
        });
    }

    @Test
    public void findByIdShouldThrowsResourceNotFoundExceptionWhenIdDoesNotExist() {

        Mockito.doThrow(ResourceAccessException.class).when(authService).validateSelfOrAdmin(any());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.findById(nonExistingOrderId);
        });
    }

    @Test
    public void insertShouldReturnOrderDTOWhenAdminLogged() {

        Mockito.when(userService.authenticated()).thenReturn(admin);

        OrderDTO result = service.insert(orderDTO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(existingOrderId, result.getId());
    }

    @Test
    public void insertShouldReturnOrderDTOWhenClientLogged() {

        Mockito.when(userService.authenticated()).thenReturn(client);

        OrderDTO result = service.insert(orderDTO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(existingOrderId, result.getId());
    }

    @Test
    public void insertShouldThrowsUsernameNotFoundExceptionWhenUserNotLogged() {

        Mockito.when(userService.authenticated()).thenThrow(UsernameNotFoundException.class);
//        Mockito.doThrow(UsernameNotFoundException.class).when(userService).authenticated();

        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            service.insert(orderDTO);
        });
    }
}
