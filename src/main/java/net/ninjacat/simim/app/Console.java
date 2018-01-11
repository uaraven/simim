package net.ninjacat.simim.app;

import net.ninjacat.simim.core.SimImage;
import net.ninjacat.simim.di.DaggerSimimComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

public final class Console {

    private static final Logger LOGGER = LoggerFactory.getLogger(Console.class);

    private Console() {
    }

    public static void main(final String[] argv) {
        final Path path = Paths.get(argv[0]);

        final DuplicateFinder duplicateFinder = DaggerSimimComponent.create().duplicateFinder();

        final List<Collection<SimImage>> duplicates = duplicateFinder.scan(
                path,
                p -> LOGGER.info(p + "\r"));

        duplicates.forEach(list -> {
            System.out.println(list.iterator().next().getSignature());
            list.forEach(it -> System.out.println(it.getPath()));
            System.out.println();

        });
    }
}
