package net.ninjacat.simim.core;

public class Dct {

    private static final double INV_SQ = 1 / Math.sqrt(2);

    public static double[][] forward(final int[][] image) {
        final int maxV = image.length;
        final int maxU = image[0].length;

        final double[][] result = new double[maxU][maxV];
        for (int u = 0; u < maxU; u++) {
            for (int v = 0; v < maxV; v++) {

                result[u][v] = calculateFor(u, v, image);

            }
        }

        return result;
    }

    private static double calculateFor(final int u, final int v, final int[][] image) {
        double sum = 0;
        for (int y = 0; y < image.length; y++) {
            for (int x = 0; x < image[0].length; x++) {
                sum += image[x][y] * Math.cos((2 * x + 1) * u * Math.PI / 16) * Math.cos((2 * y + 1) * v * Math.PI / 16);
            }
        }
        return 0.25 * alpha(u) * alpha(v) * sum;
    }

    private static double alpha(final int u) {
        if (u == 0) {
            return INV_SQ;
        } else {
            return 1;
        }
    }
}
