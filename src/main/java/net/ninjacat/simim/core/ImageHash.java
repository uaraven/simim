package net.ninjacat.simim.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Calculates image perceptual hash using difference hash (see http://www.hackerfactor.com/blog/index.php?/archives/529-Kind-of-Like-That.html)
 */
public class ImageHash {

    private static final int SIZE = 8;

    private final long signature;

    @JsonCreator
    public ImageHash(@JsonProperty("signature") final long signature) {
        this.signature = signature;
    }


    public ImageHash(final Image image) throws IOException {
        final BufferedImage gsImage = SwingFXUtils.fromFXImage(image, null);
        final BufferedImage thumbnail = toBufferedImage(
                gsImage.getScaledInstance(SIZE + 1, SIZE, BufferedImage.SCALE_SMOOTH));
        ImageIO.write(thumbnail, "jpg", new File("/tmp/gs.jpg"));
        long gradient = 0;
        int bit = SIZE * SIZE - 1;
        for (int y = 0; y < SIZE; y++) {
            for (int x = 1; x < SIZE; x++) {
                final int comp = Integer.compareUnsigned(thumbnail.getRGB(x, y), thumbnail.getRGB(x - 1, y));
                final int gb = (comp < 0 ? 0 : 1) << bit;
                gradient |= gb;
                bit -= 1;
            }
        }
        this.signature = gradient;
    }

    /**
     *
     * @return The
     */
    public long getSignature() {
        return this.signature;
    }

    @Override
    public String toString() {
        return Long.toUnsignedString(this.signature, 16);
    }

    /**
     * Calculates similarity between two image signatures. Returns a value in range 0.0 - 1.0 where
     * 1.0 is being the same image and 0.0 is absolutely different images, like black vs white.
     *
     * @param other The other image signature
     * @return similarity index
     */
    public double similarity(final ImageHash other) {
        final long sameBits = IntStream.range(0, SIZE * SIZE)
                .filter(i -> ((this.getSignature() >> i) & 1) == ((other.getSignature() >> i) & 1))
                .count();
        return sameBits / 64.0;
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
