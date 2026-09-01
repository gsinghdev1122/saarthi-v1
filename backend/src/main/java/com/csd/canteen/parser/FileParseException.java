package com.csd.canteen.parser;

/** Raised when an uploaded CIMS export can't be parsed — malformed fixed-width
 *  columns, an unreadable spreadsheet, wrong file type for the declared kind, etc. */
public class FileParseException extends RuntimeException {
    public FileParseException(String message) {
        super(message);
    }
    public FileParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
