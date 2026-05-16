package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Task 5 – Mocks &amp; Stubs (Chapter 6)
 *
 * <p>Target class: {@link OrderProcessor}
 *
 * <p>Use Mockito to mock {@link InventoryService} and {@link PaymentGateway},
 * then test {@link OrderProcessor#process(String, ShoppingCart)} in isolation.
 *
 * <h3>Required scenarios (at least 4)</h3>
 * <ol>
 *   <li><b>Happy path</b> — inventory available, payment succeeds → non-null {@link Order} returned.</li>
 *   <li><b>Inventory failure</b> — {@code isAvailable()} returns {@code false} for at least one item
 *       → method returns {@code null} AND {@code charge()} is <em>never</em> called.</li>
 *   <li><b>Payment failure</b> — inventory OK, {@code charge()} returns {@code false}
 *       → method returns {@code null}.</li>
 *   <li><b>Partial quantity</b> — define the expected behaviour when only some items
 *       pass the inventory check, and write a test for it.</li>
 * </ol>
 *
 * <h3>Verification</h3>
 * Use {@code verify(paymentGateway, never()).charge(...)} to assert that
 * payment is never attempted when inventory is insufficient.
 *
 * <h3>Reflection (add to your report)</h3>
 * Answer: What does mocking allow you to test that you could not test otherwise?
 * What does it prevent you from testing? When is mocking a bad idea?
 */
@ExtendWith(MockitoExtension.class)
class OrderProcessorMockTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private OrderProcessor orderProcessor;

    private ShoppingCart cart;
    private Product widget;
    private Product phone;

    @BeforeEach
    void setUp() {
        cart   = new ShoppingCart();
        widget = new Product("P001", "Widget", 25.0, 100);
        phone = new Product("P002", "Phone", 500.0, 10);
    }

    // -----------------------------------------------------------------------
    // TODO: Write your mock-based tests below.
    //
    // EXAMPLE STRUCTURE — happy path:
    //
    // @Test
    // void process_inventoryOkAndPaymentOk_returnsOrder() {
    //     cart.addItem(widget, 2);
    //
    //     when(inventoryService.isAvailable(widget, 2)).thenReturn(true);
    //     when(paymentGateway.charge("customer-1", 50.0)).thenReturn(true);
    //
    //     Order order = orderProcessor.process("customer-1", cart);
    //
    //     assertThat(order).isNotNull();
    //     assertThat(order.getCustomerId()).isEqualTo("customer-1");
    //     assertThat(order.getTotal()).isEqualTo(50.0);
    //     verify(paymentGateway).charge("customer-1", 50.0);
    // }
    // -----------------------------------------------------------------------

    /**
     * Happy path:
     * Inventory is available and payment succeeds.
     *
     * Expected behavior:
     * A valid Order object should be returned.
     */
    @Test
    void process_inventoryAvailableAndPaymentSuccessful_returnsOrder() {

        cart.addItem(widget, 2);

        when(inventoryService.isAvailable(widget, 2))
                .thenReturn(true);

        when(paymentGateway.charge("customer-1", 50.0))
                .thenReturn(true);

        Order order = orderProcessor.process("customer-1", cart);

        assertThat(order).isNotNull();
        assertThat(order.getCustomerId()).isEqualTo("customer-1");
        assertThat(order.getTotal()).isEqualTo(50.0);

        verify(inventoryService).isAvailable(widget, 2);
        verify(paymentGateway).charge("customer-1", 50.0);
    }

    /**
     * Inventory failure:
     * One product is unavailable.
     *
     * Expected behavior:
     * Processing should stop immediately,
     * return null,
     * and payment should never be attempted.
     */
    @Test
    void process_inventoryUnavailable_returnsNullAndDoesNotCharge() {

        cart.addItem(widget, 3);

        when(inventoryService.isAvailable(widget, 3))
                .thenReturn(false);

        Order order = orderProcessor.process("customer-2", cart);

        assertThat(order).isNull();

        verify(inventoryService).isAvailable(widget, 3);

        verify(paymentGateway, never())
                .charge(anyString(), anyDouble());
    }

    /**
     * Payment failure:
     * Inventory succeeds but payment fails.
     *
     * Expected behavior:
     * No order should be created.
     */
    @Test
    void process_paymentFails_returnsNull() {

        cart.addItem(widget, 2);

        when(inventoryService.isAvailable(widget, 2))
                .thenReturn(true);

        when(paymentGateway.charge("customer-3", 50.0))
                .thenReturn(false);

        Order order = orderProcessor.process("customer-3", cart);

        assertThat(order).isNull();

        verify(inventoryService).isAvailable(widget, 2);
        verify(paymentGateway).charge("customer-3", 50.0);
    }

    /**
     * Partial quantity scenario:
     * One item passes inventory check while another fails.
     *
     * Expected behavior:
     * Entire checkout should fail,
     * because all items must be available.
     */
    @Test
    void process_partialInventoryAvailability_abortsEntireCheckout() {

        cart.addItem(widget, 2);
        cart.addItem(phone, 1);

        when(inventoryService.isAvailable(widget, 2))
                .thenReturn(true);

        when(inventoryService.isAvailable(phone, 1))
                .thenReturn(false);

        Order order = orderProcessor.process("customer-4", cart);

        assertThat(order).isNull();

        verify(inventoryService).isAvailable(widget, 2);
        verify(inventoryService).isAvailable(phone, 1);

        verify(paymentGateway, never())
                .charge(anyString(), anyDouble());
    }

    /**
     * Validation scenario:
     * Empty carts are not allowed.
     *
     * Expected behavior:
     * IllegalArgumentException should be thrown.
     */
    @Test
    void process_emptyCart_throwsException() {

        assertThatThrownBy(() ->
                orderProcessor.process("customer-5", cart)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("cart must not be empty");

        verifyNoInteractions(inventoryService);
        verifyNoInteractions(paymentGateway);
    }

    /**
     * Validation scenario:
     * Blank customer IDs are invalid.
     *
     * Expected behavior:
     * IllegalArgumentException should be thrown.
     */
    @Test
    void process_blankCustomerId_throwsException() {

        cart.addItem(widget, 1);

        assertThatThrownBy(() ->
                orderProcessor.process("   ", cart)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("customerId");

        verifyNoInteractions(inventoryService);
        verifyNoInteractions(paymentGateway);
    }

}
