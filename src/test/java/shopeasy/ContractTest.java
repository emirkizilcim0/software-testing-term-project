package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 3 – Design by Contract (Chapter 4)
 *
 * <p>This task has two parts:
 *
 * <h3>Part A – Add contracts to production code</h3>
 * Open {@link ShoppingCart} and {@link PriceCalculator} and add {@code assert}
 * statements for the pre-conditions and post-conditions described in their Javadoc.
 * Note: assertions are enabled via {@code -ea} in Maven Surefire (already configured
 * in {@code pom.xml}).
 *
 * <p>Contracts to implement:
 * <ul>
 *   <li><b>ShoppingCart.addItem</b>: pre — {@code product != null}, {@code quantity > 0};
 *       post — {@code itemCount()} increased or product quantity updated.</li>
 *   <li><b>ShoppingCart.applyDiscount</b>: pre — {@code 0 <= discountRate <= 100};
 *       post — result &lt;= {@code total()} when {@code discountRate > 0}.</li>
 *   <li><b>PriceCalculator.calculate</b>: pre — {@code basePrice >= 0},
 *       {@code 0 <= discountRate <= 100}, {@code 0 <= taxRate <= 100};
 *       post — result {@code >= 0}.</li>
 *   <li><b>ShoppingCart invariant</b>: {@code total() >= 0} after any operation.</li>
 * </ul>
 *
 * <h3>Part B – Write contract tests</h3>
 * Write tests below that:
 * <ol>
 *   <li>Verify contracts hold for valid inputs (positive tests).</li>
 *   <li>Verify contracts are violated ({@code AssertionError}) for invalid inputs (negative tests).</li>
 * </ol>
 *
 * <p>Use {@code assertThatThrownBy(...).isInstanceOf(AssertionError.class)} to test violations.
 */
class ContractTest {

    private ShoppingCart cart;
    private PriceCalculator calculator;
    private Product product;

    @BeforeEach
    void setUp() {
        cart       = new ShoppingCart();
        calculator = new PriceCalculator();
        product    = new Product("P001", "Widget", 10.0, 50);
    }

    // -----------------------------------------------------------------------
    // TODO: Write your contract tests below.
    //
    // EXAMPLE — pre-condition violation (fill in the correct assertion):
    //
    // @Test
    // void addItem_nullProduct_shouldViolatePreCondition() {
    //     assertThatThrownBy(() -> cart.addItem(null, 1))
    //             .isInstanceOf(AssertionError.class);
    // }
    //
    // EXAMPLE — pre-condition holds (valid input):
    //
    // @Test
    // void addItem_validInput_shouldNotThrow() {
    //     assertThatCode(() -> cart.addItem(product, 3)).doesNotThrowAnyException();
    // }
    // -----------------------------------------------------------------------

     /** Valid addItem input should satisfy contracts */
    @Test
    void addItem_validInput_shouldNotThrow() {
        assertThatCode(() ->
                cart.addItem(product, 2))
                .doesNotThrowAnyException();

        assertThat(cart.itemCount()).isEqualTo(1);
    }

    /** Pre-condition: product must not be null */
    @Test
    void addItem_nullProduct_shouldViolatePreCondition() {

        assertThatThrownBy(() ->
                cart.addItem(null, 1))
                .isInstanceOf(AssertionError.class);
    }

    /** Pre-condition: quantity must be > 0 */
    @Test
    void addItem_zeroQuantity_shouldViolatePreCondition() {

        assertThatThrownBy(() ->
                cart.addItem(product, 0))
                .isInstanceOf(AssertionError.class);
    }

    /** Invariant: total should always remain non-negative */
    @Test
    void shoppingCartInvariant_totalShouldNeverBeNegative() {

        cart.addItem(product, 2);

        assertThat(cart.total()).isGreaterThanOrEqualTo(0);
    }

    // -----------------------------------------------------------------------
    // ShoppingCart.applyDiscount() CONTRACTS
    // -----------------------------------------------------------------------

    /** Valid discount should satisfy contracts */
    @Test
    void applyDiscount_validRate_shouldNotThrow() {

        cart.addItem(product, 2);

        assertThatCode(() ->
                cart.applyDiscount(10))
                .doesNotThrowAnyException();
    }

    /** Pre-condition: discount cannot be negative */
    @Test
    void applyDiscount_negativeRate_shouldViolatePreCondition() {

        assertThatThrownBy(() ->
                cart.applyDiscount(-5))
                .isInstanceOf(AssertionError.class);
    }

    /** Pre-condition: discount cannot exceed 100 */
    @Test
    void applyDiscount_rateAboveHundred_shouldViolatePreCondition() {

        assertThatThrownBy(() ->
                cart.applyDiscount(150))
                .isInstanceOf(AssertionError.class);
    }

    /** Post-condition: discounted value must not exceed original total */
    @Test
    void applyDiscount_shouldReduceOrMaintainTotal() {

        cart.addItem(product, 2);

        double original = cart.total();
        double discounted = cart.applyDiscount(20);

        assertThat(discounted).isLessThanOrEqualTo(original);
    }

    // -----------------------------------------------------------------------
    // PriceCalculator.calculate() CONTRACTS
    // -----------------------------------------------------------------------

    /** Valid calculation should satisfy contracts */
    @Test
    void calculate_validInputs_shouldNotThrow() {

        assertThatCode(() ->
                calculator.calculate(100, 10, 20))
                .doesNotThrowAnyException();
    }

    /** Pre-condition: base price must be non-negative */
    @Test
    void calculate_negativeBase_shouldViolatePreCondition() {

        assertThatThrownBy(() ->
                calculator.calculate(-100, 10, 10))
                .isInstanceOf(AssertionError.class);
    }

    /** Pre-condition: discount must be within [0,100] */
    @Test
    void calculate_invalidDiscount_shouldViolatePreCondition() {

        assertThatThrownBy(() ->
                calculator.calculate(100, 120, 10))
                .isInstanceOf(AssertionError.class);
    }

    /** Pre-condition: tax must be within [0,100] */
    @Test
    void calculate_invalidTax_shouldViolatePreCondition() {

        assertThatThrownBy(() ->
                calculator.calculate(100, 10, -5))
                .isInstanceOf(AssertionError.class);
    }

    /** Post-condition: result must always be non-negative */
    @Test
    void calculate_resultShouldNeverBeNegative() {

        double result = calculator.calculate(100, 20, 10);

        assertThat(result).isGreaterThanOrEqualTo(0);
    }

}
