package net.ninjacat.simim.core;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

import static javafx.embed.swing.SwingFXUtils.fromFXImage;
import static javafx.embed.swing.SwingFXUtils.toFXImage;

/**
 * Image wrapper used internally when searching for similar images
 */
public class SimImage {
    private static final int THUMBNAIL_DIMENSION = 320;
    private final Path path;
    private final ImageHash signature;
    private final Supplier<Image> thumbnail;

    public static SimImage fromPath(final Path path) {
        try (final InputStream is = new BufferedInputStream(new FileInputStream(path.toFile()))) {
            return new SimImage(is, path);
        } catch (final IOException e) {
            throw new ImageProcessingException("Failed to load image " + path, e);
        }
    }

    @VisibleForTesting
    SimImage(final InputStream inputStream) throws IOException {
        this(inputStream, Paths.get(""));
    }

    SimImage(final InputStream inputStream, final Path path) throws IOException {
        this.path = path;
        try (final InputStream is = new BufferedInputStream(inputStream)) {
            final Image image = load(is);
            this.signature = new ImageHash(image);
            this.thumbnail = Suppliers.memoize(() -> generateThumbnail(image));
        }
    }

    SimImage(final Path path, final ImageHash signature, final InputStream thumbnailStream) throws IOException {
        this.path = path;
        final Image image = load(thumbnailStream);
        this.signature = signature;
        this.thumbnail = () -> image;
    }

    private static Image load(final InputStream input) throws IOException {
        try (final InputStream is = new BufferedInputStream(input)) {
            return new Image(is);
        }
    }

    public Path getPath() {
        return this.path;
    }

    public ImageHash getSignature() {
        return this.signature;
    }

    public Image getThumbnail() {
        return this.thumbnail.get();
    }

    public InputStream getThumbnailBlob() {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(fromFXImage(getThumbnail(), null), "jpg", baos);
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (final IOException e) {
            throw new ImageProcessingException("Failed to convert image to jpg", e);
        }
    }

    private static Image generateThumbnail(final Image image) {
        final double scale = Math.max(image.getWidth(), image.getHeight()) / THUMBNAIL_DIMENSION;
        final int newW = (int) (image.getWidth() / scale);
        final int newH = (int) (image.getHeight() / scale);
        final BufferedImage bimg = toBufferedImage(fromFXImage(image, null)
                .getScaledInstance(newW, newH, java.awt.Image.SCALE_SMOOTH));
        return toFXImage(bimg, null);
    }

    private static BufferedImage toBufferedImage(final java.awt.Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        final BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);

        // Draw the image on to the buffered image
        final Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    @Override
    public String toString() {
        return "SimImage{" +
                "path=" + this.path +
                ", signature=" + this.signature +
                '}';
    }
}
