package com.val.money.transfer.rest;

/**
 * Returns by server in JSON format
 */
public class ErrorResponse {
    private String errorMessage;
    private String responseType;

    public ErrorResponse() {
    }

    public ErrorResponse(String errorMessage, String responseType) {
        this.errorMessage = errorMessage;
        this.responseType = responseType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }
}
