package net.ninjacat.simim.app;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import net.ninjacat.simim.core.ImageDatabase;
import net.ninjacat.simim.core.ImageHash;
import net.ninjacat.simim.core.SimImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DuplicateFinder {

    private static final double SIMILARITY_THRESHOLD = 0.9;
    private static final Logger LOGGER = LoggerFactory.getLogger("simim");

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
    public static List<Collection<SimImage>> scan(final Path root, final Consumer<Path> callback) {
        try {
            final ListMultimap<ImageHash, SimImage> images = Files.walk(root)
                    .filter(DuplicateFinder::isImageFile)
                    .map(path -> getSimImageWithCallback(callback, path))
                    .collect(Multimaps.toMultimap(SimImage::getSignature, img -> img, MultimapBuilder.hashKeys().arrayListValues()::build));
            return images.asMap().entrySet().stream()
                    .filter(entry -> entry.getValue().size() > 1)
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());

        } catch (final IOException e) {
            LOGGER.error("Failed to scan " + root, e);
        }
        return ImmutableList.of();
    }

    public void invalidateDatabase() {
        this.imageDatabase.loadPaths().stream()
                .filter(path -> !Files.exists(path))
                .peek(path -> LOGGER.debug("Path {} does not exist, removing from database", path))
                .forEach(this.imageDatabase::delete);
    }

    /**
     * Finds duplicates in a database
     *
     * @return List of collections of duplicate images
     */
    public List<Duplicates> findDuplicates() {
        final Collection<ImageHash> hashes = this.imageDatabase.loadHashes();

        final Set<ImageHash> processed = ConcurrentHashMap.newKeySet();

        final ImmutableList.Builder<Duplicates> resultBuilder = ImmutableList.builder();

        for (final ImageHash first : hashes) {
            if (!processed.contains(first)) {
                final List<SimImage> images =
                        Stream.concat(this.imageDatabase.loadByHash(first).stream(),
                                hashes.stream()
                                        .filter(h -> !processed.contains(h) && h != first)
                                        .filter(h -> first.similarity(h) > SIMILARITY_THRESHOLD)
                                        .flatMap(h -> this.imageDatabase.loadByHash(h).stream()))
                                .collect(Collectors.toList());
                processed.addAll(images.stream().map(SimImage::getSignature).collect(Collectors.toSet()));
                if (images.size() > 1) {
                    resultBuilder.add(new Duplicates(first, images));
                }
            }
        }
        return resultBuilder.build();
    }

    public static List<Duplicates> findDuplicates(final List<SimImage> images) {
        final Map<ImageHash, SimImage> imMap = images.stream().collect(Collectors.toMap(SimImage::getSignature, it -> it));

        final Set<ImageHash> processed = ConcurrentHashMap.newKeySet();

        final ImmutableList.Builder<Duplicates> resultBuilder = ImmutableList.builder();

        for (final ImageHash first : imMap.keySet()) {
            if (!processed.contains(first)) {
                final List<SimImage> duplicateImages =
                        Stream.concat(Stream.of(imMap.get(first)),
                                imMap.keySet().stream()
                                        .filter(h -> !processed.contains(h) && h != first)
                                        .filter(h -> first.similarity(h) > SIMILARITY_THRESHOLD)
                                        .map(imMap::get))
                                .collect(Collectors.toList());
                processed.addAll(duplicateImages.stream().map(SimImage::getSignature).collect(Collectors.toSet()));
                if (duplicateImages.size() > 1) {
                    resultBuilder.add(new Duplicates(first, duplicateImages));
                }
            }
        }
        return resultBuilder.build();
    }

    /**
     * Scan directory and store all images to database
     *
     * @param root     Root folder to scan
     * @param callback Optional progress callback
     * @return List of collections of duplicate images
     */
    public static List<SimImage> readInMemory(final Path root, final Consumer<Path> callback) {
        try {
            return Files.walk(root)
                    .filter(DuplicateFinder::isImageFile)               // only images
                    .map(path -> getSimImageWithCallback(callback, path))
                    .collect(Collectors.toList());


        } catch (final IOException e) {
            LOGGER.error("Failed to scan " + root, e);
        }
        return ImmutableList.of();
    }

    public List<SimImage> readIntoDb(final Path root, final Consumer<Path> callback) {
        try {
            return Files.walk(root).parallel()
                    .filter(DuplicateFinder::isImageFile)               // only images
                    .filter(path -> !this.imageDatabase.exists(path))   // skip known images
                    .map(path -> getSimImageWithCallback(callback, path))
                    .peek(this.imageDatabase::insertImage)
                    .collect(Collectors.toList());


        } catch (final IOException e) {
            LOGGER.error("Failed to scan " + root, e);
        }
        return ImmutableList.of();
    }

    private static SimImage getSimImageWithCallback(final Consumer<Path> callback, final Path path) {
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
