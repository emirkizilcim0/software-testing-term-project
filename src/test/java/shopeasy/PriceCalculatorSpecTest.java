package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 1 – Specification-Based Testing (Chapter 2)
 *
 * <p>Target class: {@link PriceCalculator}
 *
 * <p>Your goal is to test {@code PriceCalculator.calculate(basePrice, discountRate, taxRate)}
 * using the domain testing technique from Chapter 2:
 * <ol>
 *   <li>Identify equivalence partitions for each input dimension.</li>
 *   <li>Identify boundary values between partitions (on-point / off-point).</li>
 *   <li>Write at least 10 meaningful test cases that cover both partitions and boundaries.</li>
 *   <li>Use {@code @ParameterizedTest} with {@code @CsvSource} for tests that share structure.</li>
 *   <li>Add a comment above each test method explaining which partition or boundary it covers.</li>
 * </ol>
 *
 * <h3>Input dimensions to consider</h3>
 * <ul>
 *   <li><b>basePrice</b>  – zero, positive, very large</li>
 *   <li><b>discountRate</b> – 0 (no discount), (0,100) typical, 100 (full discount)</li>
 *   <li><b>taxRate</b>    – 0 (no tax), (0,100) typical, 100 (100% tax)</li>
 * </ul>
 */
class PriceCalculatorSpecTest {

    private PriceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriceCalculator();
    }

    // -----------------------------------------------------------------------
    // TODO: Write your tests below.
    //
    // EXAMPLE STRUCTURE (replace with real cases):
    //
    // /** Partition: zero base price — result must always be 0 regardless of rates */
    // @Test
    // void zeroPriceAlwaysReturnsZero() {
    //     assertThat(calculator.calculate(0, 20, 10)).isEqualTo(0.0);
    // }
    //
    // /** Boundary: discountRate at lower bound (0%) — no reduction applied */
    // @Test
    // void discountRateZeroMeansNoDiscount() {
    //     double result = calculator.calculate(100, 0, 0);
    //     assertThat(result).isEqualTo(100.0);
    // }
    //
    // /** Boundary: discountRate at upper bound (100%) — full discount wipes price to 0 */
    // @Test
    // void discountRateHundredMeansFullDiscount() {
    //     double result = calculator.calculate(100, 100, 0);
    //     assertThat(result).isEqualTo(0.0);
    // }
    //
    // /** Partition: typical values — check formula correctness */
    // @ParameterizedTest(name = "base={0}, disc={1}%, tax={2}% => {3}")
    // @CsvSource({
    //     "100.0, 10.0, 20.0, 108.0",
    //     "200.0,  0.0, 10.0, 220.0",
    // })
    // void typicalValues(double base, double disc, double tax, double expected) {
    //     assertThat(calculator.calculate(base, disc, tax)).isCloseTo(expected, within(0.001));
    // }
    // -----------------------------------------------------------------------
    /** Partition: zero base price — result must always be 0 regardless of rates */
    @Test
    void zeroBasePriceReturnsZero() {
        double result = calculator.calculate(0, 20, 15);

        assertThat(result).isEqualTo(0.0);
    }

    /** Partition: typical valid values for all dimensions */
    @ParameterizedTest(name = "base={0}, disc={1}, tax={2} => {3}")
    @CsvSource({
            "100, 10, 20, 108",
            "200, 25, 10, 165",
            "50, 50, 50, 37.5"
    })
    void typicalValidInputs(double base,
                            double discount,
                            double tax,
                            double expected) {

        assertThat(calculator.calculate(base, discount, tax))
                .isCloseTo(expected, within(0.001));
    }

    /** Partition: no discount applied */
    @Test
    void zeroDiscountMeansOriginalPriceWithTaxOnly() {
        double result = calculator.calculate(100, 0, 20);

        assertThat(result).isEqualTo(120.0);
    }

    /** Partition: no tax applied */
    @Test
    void zeroTaxMeansDiscountOnly() {
        double result = calculator.calculate(100, 20, 0);

        assertThat(result).isEqualTo(80.0);
    }

    /** Partition: full discount removes all price */
    @Test
    void hundredPercentDiscountReturnsZero() {
        double result = calculator.calculate(100, 100, 20);

        assertThat(result).isEqualTo(0.0);
    }

    /** Partition: 100% tax doubles discounted value */
    @Test
    void hundredPercentTaxDoublesDiscountedPrice() {
        double result = calculator.calculate(100, 20, 100);

        assertThat(result).isEqualTo(160.0);
    }

    /** Partition: very large base price */
    @Test
    void veryLargeBasePriceHandledCorrectly() {
        double result = calculator.calculate(1_000_000, 10, 20);

        assertThat(result).isEqualTo(1_080_000.0);
    }

    // -----------------------------------------------------------------------
    // BOUNDARY VALUE ANALYSIS
    // -----------------------------------------------------------------------

    /** Boundary: discount rate exactly at lower bound (0%) */
    @Test
    void discountAtLowerBoundary() {
        double result = calculator.calculate(100, 0, 0);

        assertThat(result).isEqualTo(100.0);
    }

    /** Boundary: discount rate exactly at upper bound (100%) */
    @Test
    void discountAtUpperBoundary() {
        double result = calculator.calculate(100, 100, 0);

        assertThat(result).isEqualTo(0.0);
    }

    /** Boundary: tax rate exactly at lower bound (0%) */
    @Test
    void taxAtLowerBoundary() {
        double result = calculator.calculate(100, 10, 0);

        assertThat(result).isEqualTo(90.0);
    }

    /** Boundary: tax rate exactly at upper bound (100%) */
    @Test
    void taxAtUpperBoundary() {
        double result = calculator.calculate(100, 0, 100);

        assertThat(result).isEqualTo(200.0);
    }

    // -----------------------------------------------------------------------
    // OFF-POINT / INVALID INPUTS
    // -----------------------------------------------------------------------

    /** Off-point boundary: negative base price violates pre-condition */
    @Test
    void negativeBasePriceShouldThrowAssertionError() {
    
        assertThatThrownBy(() ->
                calculator.calculate(-100, 10, 10))
                .isInstanceOf(AssertionError.class);
    }
    
    /** Off-point boundary: negative discount violates pre-condition */
    @Test
    void negativeDiscountRateShouldThrowAssertionError() {
    
        assertThatThrownBy(() ->
                calculator.calculate(100, -1, 0))
                .isInstanceOf(AssertionError.class);
    }

    /** Off-point boundary: discount above 100 violates pre-condition */
    @Test
    void discountAboveHundredShouldThrowAssertionError() {

        assertThatThrownBy(() ->
                calculator.calculate(100, 150, 0))
                .isInstanceOf(AssertionError.class);
    }

    /** Off-point boundary: negative tax violates pre-condition */
    @Test
    void negativeTaxRateShouldThrowAssertionError() {

        assertThatThrownBy(() ->
                calculator.calculate(100, 0, -10))
                .isInstanceOf(AssertionError.class);
    }

    /** Off-point boundary: tax above 100 violates pre-condition */
    @Test
    void taxAboveHundredShouldThrowAssertionError() {

        assertThatThrownBy(() ->
                calculator.calculate(100, 0, 150))
                .isInstanceOf(AssertionError.class);
    }


}
