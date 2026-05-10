package shopeasy;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 4 – Property-Based Testing (Chapter 5)
 *
 * <p>Target classes: {@link PriceCalculator}, {@link ShoppingCart}
 *
 * <p>Using jqwik, define and test at least <strong>3 distinct properties</strong>.
 * You must use at least one custom {@code @Provide} method.
 *
 * <h3>Suggested properties (you may use these or design your own)</h3>
 * <ul>
 *   <li><b>Monotonicity</b> – For any fixed base and tax, increasing the discount
 *       rate never increases the final price.</li>
 *   <li><b>Identity</b> – A 0% discount and 0% tax returns exactly the base price.</li>
 *   <li><b>Boundedness</b> – The result is always &gt;= 0.</li>
 *   <li><b>Cart commutativity</b> – Adding product A then B yields the same total
 *       as adding B then A.</li>
 *   <li><b>Discount transitivity</b> – Applying a 10% then another 10% discount via
 *       {@code applyDiscount} is equivalent to a single call with the compounded rate
 *       (think carefully: is this actually true for this implementation?).</li>
 * </ul>
 *
 * <h3>For each property, include a comment that answers:</h3>
 * <ol>
 *   <li>What does this property mean in plain English?</li>
 *   <li>What class of bugs would this property catch?</li>
 * </ol>
 *
 * <h3>If jqwik finds a failing case</h3>
 * Do not just fix the test. Investigate the root cause and explain it in your
 * reflection report (include the counterexample jqwik printed).
 */
class ShopEasyPropertyTest {

    // -----------------------------------------------------------------------
    // TODO: Write your properties below.
    //
    // EXAMPLE STRUCTURE:
    //
    // /**
    //  * Property: The final price is always non-negative.
    //  * Bug class caught: any implementation path that produces a negative result
    //  *                   (e.g., discount > 100 applied to negative base).
    //  */
    // @Property
    // void finalPriceIsNeverNegative(
    //         @ForAll @DoubleRange(min = 0, max = 10_000) double base,
    //         @ForAll @DoubleRange(min = 0, max = 100)   double discount,
    //         @ForAll @DoubleRange(min = 0, max = 100)   double tax) {
    //
    //     PriceCalculator calc = new PriceCalculator();
    //     double result = calc.calculate(base, discount, tax);
    //     assertThat(result).isGreaterThanOrEqualTo(0.0);
    // }
    //
    // // Custom provider example:
    // @Provide
    // Arbitrary<Product> validProducts() {
    //     return Combinators.combine(
    //             Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
    //             Arbitraries.doubles().between(0.01, 500.0)
    //     ).as((name, price) -> new Product("P-" + name, name, price, 100));
    // }
    // -----------------------------------------------------------------------
    @Property
    void increasingDiscountNeverIncreasesPrice(
            @ForAll @DoubleRange(min = 0.0, max = 10000.0) double base,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double tax,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double discount1,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double discount2
    ) {

        Assume.that(discount2 >= discount1);

        PriceCalculator calculator = new PriceCalculator();

        double lowerDiscountPrice =
                calculator.calculate(base, discount1, tax);

        double higherDiscountPrice =
                calculator.calculate(base, discount2, tax);

        assertThat(higherDiscountPrice)
                .isLessThanOrEqualTo(lowerDiscountPrice);
    }

    /**
     * Property: A 0% discount and 0% tax should return
     * exactly the original base price.
     *
     * Meaning:
     * If no modifications are applied, the value must remain unchanged.
     *
     * Bug class caught:
     * Incorrect default calculations, rounding problems,
     * or accidental modifications to the base value.
     */
    @Property
    void zeroDiscountAndZeroTaxReturnOriginalPrice(
            @ForAll @DoubleRange(min = 0.0, max = 100000.0) double base
    ) {

        PriceCalculator calculator = new PriceCalculator();

        double result = calculator.calculate(base, 0, 0);

        assertThat(result).isEqualTo(base);
    }

    /**
     * Property: Final price should always stay within valid bounds.
     *
     * Meaning:
     * The final result can never become negative and can never exceed
     * the maximum possible taxed value.
     *
     * Bug class caught:
     * Overflow issues, negative pricing bugs,
     * incorrect tax/discount formulas.
     */
    @Property
    void finalPriceAlwaysWithinExpectedBounds(
            @ForAll @DoubleRange(min = 0.0, max = 10000.0) double base,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double discount,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double tax
    ) {

        PriceCalculator calculator = new PriceCalculator();

        double result = calculator.calculate(base, discount, tax);

        double maximumPossible = base * (1 + tax / 100.0);

        assertThat(result).isGreaterThanOrEqualTo(0.0);
        assertThat(result).isLessThanOrEqualTo(maximumPossible + 0.0001);
    }

    /**
     * Property: Adding product A then B should produce the same cart total
     * as adding B then A.
     *
     * Meaning:
     * Cart totals should not depend on insertion order.
     *
     * Bug class caught:
     * State corruption, ordering bugs,
     * incorrect subtotal accumulation.
     */
        @Property
        void cartAdditionOrderDoesNotChangeTotal(
                @ForAll("validProducts") Product productA,
                @ForAll("validProducts") Product productB,
                @ForAll @IntRange(min = 1, max = 20) int quantityA,
                @ForAll @IntRange(min = 1, max = 20) int quantityB
        ) {
            Assume.that(!productA.getId().equals(productB.getId()));
            ShoppingCart cart1 = new ShoppingCart();
            ShoppingCart cart2 = new ShoppingCart();
            // A then B
            cart1.addItem(productA, quantityA);
            cart1.addItem(productB, quantityB);
            // B then A
            cart2.addItem(productB, quantityB);
            cart2.addItem(productA, quantityA);
            assertThat(cart1.total())
                    .isCloseTo(cart2.total(), within(0.0001));
        }
        
    /**
     * Property: Applying a discount should never increase
     * the shopping cart total.
     *
     * Meaning:
     * Discounts should either reduce the total or leave it unchanged.
     *
     * Bug class caught:
     * Incorrect discount arithmetic or sign mistakes.
     */
    @Property
    void cartDiscountNeverIncreasesTotal(
            @ForAll("validProducts") Product product,
            @ForAll @IntRange(min = 1, max = 50) int quantity,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double discount
    ) {

        ShoppingCart cart = new ShoppingCart();

        cart.addItem(product, quantity);

        double originalTotal = cart.total();
        double discountedTotal = cart.applyDiscount(discount);

        assertThat(discountedTotal)
                .isLessThanOrEqualTo(originalTotal);
    }

    /**
     * Custom provider for valid Product objects.
     *
     * Generates products with:
     * - non-empty IDs
     * - non-empty names
     * - positive prices
     * - positive stock values
     */
    @Provide
    Arbitrary<Product> validProducts() {

        Arbitrary<String> ids =
                Arbitraries.strings()
                        .alpha()
                        .ofMinLength(3)
                        .ofMaxLength(8);

        Arbitrary<String> names =
                Arbitraries.strings()
                        .alpha()
                        .ofMinLength(3)
                        .ofMaxLength(10);

        Arbitrary<Double> prices =
                Arbitraries.doubles()
                        .between(0.01, 1000.0);

        Arbitrary<Integer> stock =
                Arbitraries.integers()
                        .between(1, 1000);

        return Combinators.combine(ids, names, prices, stock)
                .as((id, name, price, quantity) ->
                        new Product(id, name, price, quantity));
    }


}
