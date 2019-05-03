package com.val.money.transfer.controller;

import com.val.money.transfer.TransferException;

/**
 * For any business validation logic for transfer funds service.
 * Could be processed by REST server exception mapper to send correct error code
 */
public class TransferValidationException extends TransferException {
    public TransferValidationException() {
    }

    public TransferValidationException(String message) {
        super(message);
    }

    public TransferValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
