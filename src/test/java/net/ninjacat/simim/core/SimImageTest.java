package net.ninjacat.simim.core;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SimImageTest {

    @Test
    public void signaturesForSameImageShouldBeTheSame() throws IOException {
        final SimImage img1 = new SimImage(getClass().getResourceAsStream("/im1-1.jpg"));
        final SimImage img2 = new SimImage(getClass().getResourceAsStream("/im1-1.jpg"));

        final double distance = img1.getSignature().similarity(img2.getSignature());

        assertThat(distance, is(1.0));
    }

    @Test
    public void signaturesForGrayscaleVersionShouldBeTheSame() throws IOException {
        final SimImage img1 = new SimImage(getClass().getResourceAsStream("/im1-1.jpg"));
        final SimImage img2 = new SimImage(getClass().getResourceAsStream("/im1-bright.jpg"));

        final double distance = img1.getSignature().similarity(img2.getSignature());

        assertThat(distance > 0.95, is(true));
    }

    @Test
    public void brightnessShouldNotImpactSimilarity() throws IOException {
        final SimImage img1 = new SimImage(getClass().getResourceAsStream("/im1-1.jpg"));
        final SimImage img2 = new SimImage(getClass().getResourceAsStream("/im1-bright.jpg"));

        final double distance = img1.getSignature().similarity(img2.getSignature());

        assertThat(distance > 0.95, is(true));
    }

    @Test
    public void signaturesForDifferentImageshouldBeTheLargelyDifferent() throws IOException {
        final SimImage img1 = new SimImage(getClass().getResourceAsStream("/im1-1.jpg"));
        final SimImage img2 = new SimImage(getClass().getResourceAsStream("/im2.jpg"));

        final double distance = img1.getSignature().similarity(img2.getSignature());

        assertThat(distance < 0.85, is(true));
    }
}