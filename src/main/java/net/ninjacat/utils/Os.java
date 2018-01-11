package net.ninjacat.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public enum Os {
    Windows {
        @Override
        public Path getAppData(final String appName) {
            return Paths.get(System.getenv("APPDATA")).resolve(appName);
        }
    },
    Linux {
        @Override
        public Path getAppData(final String appName) {
            return Paths.get(Optional.ofNullable(System.getenv("XDG_DATA_HOME"))
                    .orElse(HOME.resolve(".local/share").resolve(appName).toString()));
        }
    },
    MacOs {
        @Override
        public Path getAppData(String appName) {
            return HOME.resolve("Library").resolve(appName);
        }
    };

    private static final Path HOME = Paths.get(System.getProperty("user.home"));


    public static Os current() {
        final String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return Windows;
        } else if (osName.contains("mac")) {
            return MacOs;
        } else {
            // there is no solaris anymore
            return Linux;
        }
    }

    public Path getHome() {
        return HOME;
    }

    public abstract Path getAppData(final String appName);
}
