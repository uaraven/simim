package net.ninjacat.simim.app;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import net.ninjacat.simim.core.ImageDatabase;
import net.ninjacat.simim.core.ImageHash;
import net.ninjacat.simim.core.SimImage;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DuplicateFinder {

    private final ImageDatabase imageDatabase;

    @Inject
    public DuplicateFinder(final ImageDatabase imageDatabase) {
        this.imageDatabase = imageDatabase;
    }

    /**
     * Scans directory for duplicates
     *
     * @param root     Root folder to scan
     * @param callback Optional progress callback
     * @return List of collections of duplicate images
     */
    public List<Collection<SimImage>> scan(final Path root, final Consumer<Path> callback) {
        try {
            final ListMultimap<ImageHash, SimImage> images = Files.walk(root)
                    .filter(DuplicateFinder::isImageFile)
                    .map(path -> getSimImageWithCallbackNoDb(callback, path))
                    .collect(Multimaps.toMultimap(SimImage::getSignature, img -> img, MultimapBuilder.hashKeys().arrayListValues()::build));
            return images.asMap().entrySet().stream()
                    .filter(entry -> entry.getValue().size() > 1)
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());

        } catch (final IOException e) {

        }
        return ImmutableList.of();
    }

    /**
     * Scan directory and check for all known duplicates (in folders that have been scanned before)
     *
     * @param root     Root folder to scan
     * @param callback Optional progress callback
     * @return List of collections of duplicate images
     */
    public List<Collection<SimImage>> scanGlobal(final Path root, final Consumer<Path> callback) {
        try {
            final ListMultimap<ImageHash, SimImage> images = Files.walk(root)
                    .filter(DuplicateFinder::isImageFile)               // only images
                    .filter(path -> !this.imageDatabase.exists(path))   // skip known images
                    .map(path -> getSimImageWithCallback(callback, path))
                    .flatMap(img -> this.imageDatabase.loadByHash(img.getSignature()).stream())
                    .collect(Multimaps.toMultimap(SimImage::getSignature, img -> img, MultimapBuilder.hashKeys().arrayListValues()::build));
            return images.asMap().entrySet().stream()
                    .filter(entry -> entry.getValue().size() > 1)
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());

        } catch (final IOException e) {

        }
        return ImmutableList.of();
    }

    private SimImage getSimImageWithCallback(final Consumer<Path> callback, final Path path) {
        if (callback != null) {
            callback.accept(path);
        }
        final SimImage simImage = SimImage.fromPath(path);
        this.imageDatabase.insertImage(simImage);
        return simImage;
    }

    private SimImage getSimImageWithCallbackNoDb(final Consumer<Path> callback, final Path path) {
        if (callback != null) {
            callback.accept(path);
        }
        return SimImage.fromPath(path);
    }

    private static boolean isImageFile(final Path path) {
        final String s = path.toString().toLowerCase();
        return s.endsWith(".jpg") || s.endsWith(".jpeg") || s.endsWith(".png") || s.endsWith(".gif");
    }
}
