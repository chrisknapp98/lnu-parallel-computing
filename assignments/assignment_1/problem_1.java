import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class problem_1 {
    public static void main(String[] args) {
        int digits = 7;
        int terms = 1000000;
        int numberOfThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        try {
            // BigDecimal piApproximation = problem_1.approximatePi(digits, terms);
            BigDecimal piApproximation = problem_1.approximatePiParallel(digits, terms, executor);
            System.out.println("Pi approximation: " + piApproximation);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    public static BigDecimal approximatePi(int digits, int terms) {
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

    public static BigDecimal approximatePiParallel(int digits, int terms, ExecutorService executor) throws Exception {
        if (digits < 0) {
            throw new IllegalArgumentException("The number of digits must be non-negative");
        }

        List<Future<BigDecimal>> futures = new ArrayList<>();
        int termsPerThread = terms / Runtime.getRuntime().availableProcessors();
        int remainingTerms = terms % Runtime.getRuntime().availableProcessors();

        int calculationPrecision = Math.max(digits + 1, 15);
        MathContext mc = new MathContext(calculationPrecision, RoundingMode.HALF_UP);

        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            final int start = i * termsPerThread;
            int end = (i + 1) * termsPerThread;
            if (i == Runtime.getRuntime().availableProcessors() - 1) {
                end += remainingTerms;
            }
            final int finalEnd = end;

            Callable<BigDecimal> task = () -> {
                BigDecimal sum = BigDecimal.ZERO;
                for (int k = start; k < finalEnd; k++) {
                    BigDecimal sixteenPowK = BigDecimal.valueOf(16).pow(k, mc);
                    BigDecimal term1 = BigDecimal.valueOf(4).divide(BigDecimal.valueOf(8 * k + 1), mc);
                    BigDecimal term2 = BigDecimal.valueOf(2).divide(BigDecimal.valueOf(8 * k + 4), mc);
                    BigDecimal term3 = BigDecimal.ONE.divide(BigDecimal.valueOf(8 * k + 5), mc);
                    BigDecimal term4 = BigDecimal.ONE.divide(BigDecimal.valueOf(8 * k + 6), mc);

                    BigDecimal term = term1.subtract(term2).subtract(term3).subtract(term4);
                    term = term.divide(sixteenPowK, mc);
                    sum = sum.add(term);
                }
                return sum;
            };
            futures.add(executor.submit(task));
        }

        BigDecimal totalSum = BigDecimal.ZERO;
        for (Future<BigDecimal> future : futures) {
            totalSum = totalSum.add(future.get(), mc);
        }

        MathContext roundingMc = new MathContext(digits + 1, RoundingMode.HALF_UP);
        return totalSum.round(roundingMc);
    }

}
