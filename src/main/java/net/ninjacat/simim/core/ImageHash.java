package net.ninjacat.simim.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigInteger;
import java.util.BitSet;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Calculates image perceptual hash using difference hash (see http://www.hackerfactor.com/blog/index.php?/archives/529-Kind-of-Like-That.html)
 */
public class ImageHash {

    private static final int SIZE = 8;
    private static final int NBITS = 128;

    private final BitSet signature;

    @JsonCreator
    public ImageHash(@JsonProperty("signature") final BigInteger signature) {
        this.signature = BitSet.valueOf(signature.toByteArray());
    }


    public ImageHash(final Image image) {
        final BufferedImage gsImage = SwingFXUtils.fromFXImage(image, null);
        final BufferedImage thumbnail = toBufferedImage(
                gsImage.getScaledInstance(SIZE + 1, SIZE, BufferedImage.SCALE_SMOOTH));
        final BitSet gradient = new BitSet(NBITS);
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
        this.signature = gradient;
    }

    /**
     * @return The
     */
    public BigInteger getSignature() {
        return new BigInteger(this.signature.toByteArray());
    }

    @Override
    public String toString() {
        return this.signature.toString();
    }

    /**
     * Calculates similarity between two image signatures. Returns a value in range 0.0 - 1.0 where
     * 1.0 is being the same image and 0.0 is absolutely different images, like black vs white.
     *
     * @param other The other image signature
     * @return similarity index
     */
    public double similarity(final ImageHash other) {
        final long sameBits = IntStream.range(0, NBITS)
                .filter(i -> this.signature.get(i) == other.signature.get(i))
                .count();
        return (double) sameBits / NBITS;
    }


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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ImageHash imageHash = (ImageHash) o;
        return this.signature == imageHash.signature;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.signature);
    }
}
