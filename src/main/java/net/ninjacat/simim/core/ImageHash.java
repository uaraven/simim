package net.ninjacat.simim.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.scene.image.Image;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.Objects;
import java.util.stream.IntStream;

import static net.ninjacat.simim.core.HashAlgorithm.DHash;

/**
 * Calculates image perceptual hash using difference hash (see http://www.hackerfactor.com/blog/index.php?/archives/529-Kind-of-Like-That.html)
 */
public class ImageHash {

    private final BitSet signature;

    /**
     * Constructor for JSON deserialization
     * @param signature Signature JSON field
     */
    @SuppressWarnings("WeakerAccess")
    @JsonCreator
    public ImageHash(@JsonProperty("signature") final BigInteger signature) {
        this.signature = BitSet.valueOf(signature.toByteArray());
    }


    ImageHash(final Image image) {
        this.signature = DHash.signature(image);
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
        final long sameBits = IntStream.range(0, DHash.hashSize())
                .filter(i -> this.signature.get(i) == other.signature.get(i))
                .count();
        return (double) sameBits / DHash.hashSize();
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ImageHash imageHash = (ImageHash) o;
        return this.signature.equals(imageHash.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.signature);
    }
}
