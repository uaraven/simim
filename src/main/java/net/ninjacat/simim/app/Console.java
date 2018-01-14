package net.ninjacat.simim.app;

import net.ninjacat.simim.di.DaggerSimimComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class Console {

    private static final Logger LOGGER = LoggerFactory.getLogger(Console.class);

    private Console() {
    }

    public static void main(final String[] argv) {
        final Path path = Paths.get(argv[0]);

        final DuplicateFinder duplicateFinder = DaggerSimimComponent.create().duplicateFinder();

        duplicateFinder.invalidateDatabase();

//        final List<SimImage> images = duplicateFinder.readInMemory(Paths.get("/home/raven/Pictures"), null);
        final List<Duplicates> duplicates = duplicateFinder.findDuplicates();

        duplicates.forEach(it -> LOGGER.info("Duplicates:\n {}\n\n", it));

//        final List<SimImage> duplicates = duplicateFinder.read(
//                path,
//                p -> LOGGER.info(p + "\r"));
//
//        LOGGER.debug("Sucked in {} images", duplicates.size());



//        duplicates.forEach(list -> {
//            System.out.println(list.iterator().next().getSignature());
//            list.forEach(it -> System.out.println(it.getPath()));
//            System.out.println();
//
//        });
    }
}
