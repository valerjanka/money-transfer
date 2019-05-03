package com.val.money.transfer.rest;

import com.val.money.transfer.TransferException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class TransferExceptionMapper implements ExceptionMapper<TransferException> {

    @Override
    public Response toResponse(TransferException exception) {
        ErrorResponse errorResponse = new ErrorResponse(exception.getMessage(),
                Response.Status.BAD_REQUEST.getReasonPhrase());

        return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse)
                .type(MediaType.APPLICATION_JSON).build();
    }
}
