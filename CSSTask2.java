import java.util.Random;

public class CSSTask2 {
    static final long STUDENT_ID = 230103367; 
    static final int N = 20_000_000;
    static double[] arr = new double[N];

    static double parallelSum(int workers) throws InterruptedException {
        double[] partial = new double[workers];
        Thread[] ts = new Thread[workers];
        int chunk = N / workers;
        for (int t = 0; t < workers; t++) {
            final int id = t;
            final int s = t * chunk;
            final int e = (t == workers - 1) ? N : s + chunk;
            ts[t] = new Thread(() -> {
                double local = 0.0;
                for (int i = s; i < e; i++) local += arr[i];
                partial[id] = local;
            });
        }
        for (Thread t : ts) t.start();
        for (Thread t : ts) t.join();
        double sum = 0.0;
        for (double p : partial) sum += p;
        return sum;
    }

    static double timeMs(int workers) throws InterruptedException {
        double best = Double.MAX_VALUE;
        for (int rep = 0; rep < 5; rep++) {
            long start = System.nanoTime();
            parallelSum(workers);
            best = Math.min(best, (System.nanoTime() - start) / 1e6);
        }
        return best;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Student ID: " + STUDENT_ID);
        int max = Runtime.getRuntime().availableProcessors();
        System.out.println("Logical cores: " + max);

        Random rnd = new Random(STUDENT_ID);
        for (int i = 0; i < N; i++) arr[i] = rnd.nextDouble();

        int[] counts = {1, 2, 4, max, max * 2};
        final double P = 0.95;

        parallelSum(max); 
        double t1 = timeMs(1);

        System.out.println("-------------------------------------------------------");
        System.out.printf("%-8s | %-12s | %-9s | %-18s%n", "Cores", "Wall Time", "Speedup", "Amdahl Upper Bound");
        System.out.println("-------------------------------------------------------");
        for (int p : counts) {
            double tp = (p == 1) ? t1 : timeMs(p);
            double speedup = t1 / tp;
            double amdahl = 1.0 / ((1.0 - P) + P / p);
            System.out.printf("%-8d | %9.3f ms | %8.2fx | %17.2fx%n", p, tp, speedup, amdahl);
        }
        System.out.println("-------------------------------------------------------");
    }
}
