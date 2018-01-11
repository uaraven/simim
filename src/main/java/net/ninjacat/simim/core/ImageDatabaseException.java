package net.ninjacat.simim.core;

import java.sql.SQLException;

public class ImageDatabaseException extends RuntimeException {
    public ImageDatabaseException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
