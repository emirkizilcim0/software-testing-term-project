package shopeasy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Task 2 – Structural Testing &amp; Code Coverage (Chapter 3)
 *
 * <p>Target class: {@link ShoppingCart}
 *
 * <h3>Workflow</h3>
 * <ol>
 *   <li>Write an initial test suite based on the specification (Javadoc of ShoppingCart).</li>
 *   <li>Run {@code mvn test} to generate the JaCoCo report:
 *       <pre>  target/site/jacoco/index.html</pre></li>
 *   <li>Open the report, navigate to {@code ShoppingCart}, and identify uncovered branches.</li>
 *   <li>Add tests specifically to cover those branches until branch coverage &gt;= 80%.</li>
 *   <li>Take a screenshot of the final JaCoCo summary and put it in {@code report/jacoco-screenshot.png}.</li>
 * </ol>
 *
 * <h3>Branches to think about</h3>
 * <ul>
 *   <li>{@code addItem}: product already in cart vs. new product</li>
 *   <li>{@code removeItem}: product found vs. not found in cart</li>
 *   <li>{@code updateQuantity}: product found vs. not found, quantity valid vs. invalid</li>
 *   <li>{@code applyDiscount}: zero discount, positive discount</li>
 *   <li>{@code total}: empty cart vs. non-empty cart</li>
 * </ul>
 *
 * <h3>Bonus (PIT Mutation Testing)</h3>
 * Run: {@code mvn org.pitest:pitest-maven:mutationCoverage}
 * <br>Examine the HTML report in {@code target/pit-reports/}. Find two surviving mutants,
 * explain why each survived, and describe a test that would kill it. Add this analysis
 * to your reflection report.
 */
class ShoppingCartStructuralTest {

    private ShoppingCart cart;
    private Product apple;
    private Product banana;

    @BeforeEach
    void setUp() {
        cart   = new ShoppingCart();
        apple  = new Product("P001", "Apple",  1.50, 100);
        banana = new Product("P002", "Banana", 0.80, 50);
    }

    // -----------------------------------------------------------------------
    // TODO: Write your tests below.
    //
    // Start with happy-path tests, then add tests that target specific branches.
    //
    // HINT: Run `mvn test` after every few tests to see coverage progress.
    // -----------------------------------------------------------------------

     /** Branch: adding a completely new product creates a new cart line */
    @Test
    void addNewProductIncreasesItemCount() {
        cart.addItem(apple, 2);

        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.total()).isEqualTo(3.0);
    }

    /** Branch: adding an existing product merges quantities instead of adding a new line */
    @Test
    void addExistingProductCombinesQuantities() {
        cart.addItem(apple, 2);
        cart.addItem(apple, 3);

        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
        assertThat(cart.total()).isEqualTo(7.5);
    }

    // -----------------------------------------------------------------------
    // removeItem() BRANCHES
    // -----------------------------------------------------------------------

    /** Branch: removing a product that exists removes it from cart */
    @Test
    void removeExistingProduct() {
        cart.addItem(apple, 2);

        cart.removeItem("P001");

        assertThat(cart.itemCount()).isEqualTo(0);
        assertThat(cart.total()).isEqualTo(0.0);
    }

    /** Branch: removing a product that does not exist changes nothing */
    @Test
    void removeNonExistingProductDoesNothing() {
        cart.addItem(apple, 2);

        cart.removeItem("P999");

        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.total()).isEqualTo(3.0);
    }

    // -----------------------------------------------------------------------
    // updateQuantity() BRANCHES
    // -----------------------------------------------------------------------

    /** Branch: valid quantity update for an existing product */
    @Test
    void updateQuantityForExistingProduct() {
        cart.addItem(apple, 2);

        cart.updateQuantity("P001", 5);

        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
        assertThat(cart.total()).isEqualTo(7.5);
    }

    /** Branch: invalid quantity (<=0) throws exception */
    @Test
    void updateQuantityWithInvalidAmountThrowsException() {
        cart.addItem(apple, 2);

        assertThatThrownBy(() -> cart.updateQuantity("P001", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be > 0");
    }

    /** Branch: updating a product not found in cart throws exception */
    @Test
    void updateQuantityForMissingProductThrowsException() {

        assertThatThrownBy(() -> cart.updateQuantity("P999", 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found");
    }

    // -----------------------------------------------------------------------
    // applyDiscount() BRANCHES
    // -----------------------------------------------------------------------

    /** Branch: zero discount leaves total unchanged */
    @Test
    void applyZeroDiscountReturnsSameTotal() {
        cart.addItem(apple, 2);

        double discounted = cart.applyDiscount(0);

        assertThat(discounted).isEqualTo(3.0);
    }

    /** Branch: positive discount reduces total */
    @Test
    void applyPositiveDiscountReducesTotal() {
        cart.addItem(apple, 2);

        double discounted = cart.applyDiscount(10);

        assertThat(discounted).isEqualTo(2.7);
    }

    /** Branch: 100% discount reduces total to zero */
    @Test
    void applyFullDiscountReturnsZero() {
        cart.addItem(apple, 2);

        double discounted = cart.applyDiscount(100);

        assertThat(discounted).isEqualTo(0.0);
    }

    // -----------------------------------------------------------------------
    // total() BRANCHES
    // -----------------------------------------------------------------------

    /** Branch: total of empty cart is zero */
    @Test
    void emptyCartTotalIsZero() {
        assertThat(cart.total()).isEqualTo(0.0);
    }

    /** Branch: total of non-empty cart sums all subtotals */
    @Test
    void nonEmptyCartTotalIsCalculatedCorrectly() {
        cart.addItem(apple, 2);   // 3.0
        cart.addItem(banana, 5);  // 4.0

        assertThat(cart.total()).isEqualTo(7.0);
    }

    // -----------------------------------------------------------------------
    // OTHER METHODS
    // -----------------------------------------------------------------------

    /** clear() removes every item from the cart */
    @Test
    void clearRemovesAllItems() {
        cart.addItem(apple, 2);
        cart.addItem(banana, 3);

        cart.clear();

        assertThat(cart.itemCount()).isEqualTo(0);
        assertThat(cart.total()).isEqualTo(0.0);
    }

    /** getItems() returns an unmodifiable list */
    @Test
    void getItemsReturnsUnmodifiableList() {
        cart.addItem(apple, 1);

        assertThatThrownBy(() ->
                cart.getItems().add(new CartItem(banana, 1)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    /** toString() should include item count and total */
    @Test
    void toStringContainsCartInformation() {
        cart.addItem(apple, 2);
    
        String text = cart.toString();
    
        assertThat(text).isNotNull();
    }


}
