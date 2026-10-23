import java.util.Random;

public class CSSTask1 {
    static final long STUDENT_ID = 230103367; 

    public static void main(String[] args) {
        System.out.println("Student ID: " + STUDENT_ID);
        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version")
                + " (" + System.getProperty("os.arch") + ")");
        System.out.println("CPU/Platform: " + System.getProperty("os.arch") + ", Java " + System.getProperty("java.version")
                + ", " + System.getProperty("java.vm.name"));
        System.out.println("Logical cores: " + Runtime.getRuntime().availableProcessors());

        final int N = 20_000_000;
        double[] arr = new double[N];
        Random rnd = new Random(STUDENT_ID);
        for (int i = 0; i < N; i++) arr[i] = rnd.nextDouble();

        long start = System.nanoTime();

        double sum = 0.0;
        for (int i = 0; i < N; i++) sum += arr[i];
        double mean = sum / N;

        double sq = 0.0;
        for (int i = 0; i < N; i++) {
            double d = arr[i] - mean;
            sq += d * d;
        }
        double std = Math.sqrt(sq / N);

        long end = System.nanoTime();

        System.out.println("Sum: " + sum);
        System.out.println("Standard deviation: " + std);
        System.out.printf("Execution time: %.3f ms%n", (end - start) / 1e6);
    }
}