public class CSSTask8 {
    static int N = 1024;
    static double[][] A = new double[N][N];
    static double[][] B = new double[N][N];
    static double[][] C = new double[N][N];

    static double run(int block, int threads) throws Exception {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                C[i][j] = 0;
            }
        }

        Thread[] t = new Thread[threads];
        int chunk = N / threads;

        long start = System.nanoTime();
        for (int k = 0; k < threads; k++) {
            int rs = k * chunk;
            int re = (k == threads - 1) ? N : rs + chunk;
            t[k] = new Thread(() -> {
                if (block == 0) {
                    for (int i = rs; i < re; i++) {
                        for (int j = 0; j < N; j++) {
                            double sum = 0;
                            for (int x = 0; x < N; x++) {
                                sum += A[i][x] * B[x][j];
                            }
                            C[i][j] = sum;
                        }
                    }
                } else {
                    for (int ii = rs; ii < re; ii += block) {
                        for (int kk = 0; kk < N; kk += block) {
                            for (int jj = 0; jj < N; jj += block) {
                                int iMax = Math.min(ii + block, re);
                                int kMax = Math.min(kk + block, N);
                                int jMax = Math.min(jj + block, N);
                                for (int i = ii; i < iMax; i++) {
                                    for (int x = kk; x < kMax; x++) {
                                        double a = A[i][x];
                                        for (int j = jj; j < jMax; j++) {
                                            C[i][j] += a * B[x][j];
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
            t[k].start();
        }
        for (int k = 0; k < threads; k++) {
            t[k].join();
        }
        long end = System.nanoTime();
        return (end - start) / 1e6;
    }

    public static void main(String[] args) throws Exception {
        int threads = Runtime.getRuntime().availableProcessors();
        System.out.println("Matrix size: " + N + "x" + N);
        System.out.println("Threads: " + threads);

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                A[i][j] = (i + j) % 10 * 0.1;
                B[i][j] = (i - j) % 10 * 0.1;
            }
        }

        run(16, threads);

        double tNon = run(0, threads);
        double t16 = run(16, threads);
        double t64 = run(64, threads);

        System.out.printf("Non-tiled : %.3f ms%n", tNon);
        System.out.printf("Tiled 16x16: %.3f ms%n", t16);
        System.out.printf("Tiled 64x64: %.3f ms%n", t64);
        System.out.printf("Speedup tiled 16 vs non-tiled: %.2fx%n", tNon / t16);
        System.out.printf("Speedup tiled 64 vs non-tiled: %.2fx%n", tNon / t64);
    }
}