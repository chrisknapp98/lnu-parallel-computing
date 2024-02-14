import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class problem_1 {
    public static void main(String[] args) {
        int digits = 7;
        int terms = 1000;
        try {
            BigDecimal piApproximation = problem_1.approximatePiThroughBaileyBorweinPlouffeFormula(digits, terms);
            System.out.println("Pi approximation: " + piApproximation);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static BigDecimal approximatePiThroughBaileyBorweinPlouffeFormula(int digits, int terms) {
        if (digits < 0) {
            throw new IllegalArgumentException("The number of digits must be non-negative");
        }
        int calculationPrecision = Math.max(digits + 1, 15);
        MathContext mc = new MathContext(calculationPrecision, RoundingMode.HALF_UP);
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < terms; i++) {
            BigDecimal sixteenPowK = BigDecimal.valueOf(16).pow(i, mc);
            BigDecimal term1 = BigDecimal.valueOf(4).divide(BigDecimal.valueOf(8 * i + 1), mc);
            BigDecimal term2 = BigDecimal.valueOf(2).divide(BigDecimal.valueOf(8 * i + 4), mc);
            BigDecimal term3 = BigDecimal.ONE.divide(BigDecimal.valueOf(8 * i + 5), mc);
            BigDecimal term4 = BigDecimal.ONE.divide(BigDecimal.valueOf(8 * i + 6), mc);

            BigDecimal term = term1.subtract(term2).subtract(term3).subtract(term4);
            term = term.divide(sixteenPowK, mc);
            sum = sum.add(term);
        }
        MathContext roundingMc = new MathContext(digits + 1, RoundingMode.HALF_UP);
        return sum.round(roundingMc);
    }
}
