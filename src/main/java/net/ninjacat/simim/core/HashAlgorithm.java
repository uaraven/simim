package net.ninjacat.simim.core;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.BitSet;

public enum  HashAlgorithm {
    DHash {
        private static final int SIZE = 8;

        @Override
        public BitSet signature(final Image image) {
            final BufferedImage gsImage = SwingFXUtils.fromFXImage(image, null);
            final BufferedImage thumbnail = toBufferedImage(
                    gsImage.getScaledInstance(SIZE + 1, SIZE, BufferedImage.SCALE_SMOOTH));
            final BitSet gradient = new BitSet(hashSize());
            int bit = SIZE * SIZE;
            for (int a = 0; a < SIZE; a++) {
                for (int b = 1; b < SIZE; b++) {
                    final int phrz = Integer.compareUnsigned(thumbnail.getRGB(b, a), thumbnail.getRGB(b - 1, a));
                    final int pvrt = Integer.compareUnsigned(thumbnail.getRGB(a, b), thumbnail.getRGB(a, b - 1));
                    gradient.set(bit, phrz >= 0);
                    gradient.set(bit * 2, pvrt >= 0);
                    bit -= 1;
                }
            }
            return gradient;
        }

        @Override
        public int hashSize() {
            return 128;
        }
    },
    PHash {

        private static final int THUMBNAIL_SIZE = 32;
        private static final int SIZE = 8;

        @Override
        public BitSet signature(final Image image) {
            final BufferedImage gsImage = SwingFXUtils.fromFXImage(image, null);
            final BufferedImage thumbnail = toBufferedImage(
                    gsImage.getScaledInstance(THUMBNAIL_SIZE, THUMBNAIL_SIZE, BufferedImage.SCALE_SMOOTH));
            final int[][] pixels = new int[THUMBNAIL_SIZE][THUMBNAIL_SIZE];
            for (int y = 0; y < THUMBNAIL_SIZE; y++) {
                for (int x = 0; x < THUMBNAIL_SIZE; x++) {
                    pixels[x][y] = thumbnail.getRGB(x, y);
                }
            }

            final BitSet gradient = new BitSet(hashSize());
            int bit = SIZE * SIZE;
            for (int a = 0; a < SIZE; a++) {
                for (int b = 1; b < SIZE; b++) {
                    final int phrz = Integer.compareUnsigned(pixels[b][a], pixels[b - 1][a]);
                    gradient.set(bit, phrz >= 0);
                    bit -= 1;
                }
            }
            return gradient;
        }

        @Override
        public int hashSize() {
            return 64;
        }
    };

    public abstract BitSet signature(final Image image);

    /**
     * @return Hash size in bits
     */
    public abstract int hashSize();


    /**
     * Converts a given Image into a BufferedImage
     *
     * @param img The Image to be converted
     * @return The converted BufferedImage
     */
    public static BufferedImage toBufferedImage(final java.awt.Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        final BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_BYTE_GRAY);

        // Draw the image on to the buffered image
        final Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }
}
