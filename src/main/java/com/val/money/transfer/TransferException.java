package com.val.money.transfer;

/**
 * General Transfer Service Exception, which could be internal or business validation exception
 */
public class TransferException extends Exception {
    public TransferException() {
        super();
    }

    public TransferException(String message) {
        super(message);
    }

    public TransferException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransferException(Throwable cause) {
        super(cause);
    }
}
