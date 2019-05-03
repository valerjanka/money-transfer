package com.val.money.transfer.controller;

import com.val.money.transfer.TransferException;

/**
 * For any internal errors, which were caused not by business validation logic.
 * Could be processed by REST server exception mapper to send correct error code
 */
public class TransferInternalException extends TransferException {
    public TransferInternalException() {
        super();
    }

    public TransferInternalException(String message) {
        super(message);
    }

    public TransferInternalException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransferInternalException(Throwable cause) {
        super(cause);
    }
}
