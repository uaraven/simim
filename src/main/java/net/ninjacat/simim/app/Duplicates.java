package net.ninjacat.simim.app;

import net.ninjacat.simim.core.ImageHash;
import net.ninjacat.simim.core.SimImage;

import java.util.Collection;
import java.util.stream.Collectors;

public class Duplicates {

    private final ImageHash originalHash;
    private final Collection<SimImage> images;

    public Duplicates(final ImageHash originalHash, final Collection<SimImage> images) {
        this.originalHash = originalHash;
        this.images = images;
    }

    public ImageHash getOriginalHash() {
        return this.originalHash;
    }

    public Collection<SimImage> getImages() {
        return this.images;
    }

    @Override
    public String toString() {
        return this.images.stream()
                .map(im -> im.getSignature().similarity(this.originalHash) + " => " + im.getPath())
                .collect(Collectors.joining("\n"));
    }
}
