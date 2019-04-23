package com.val.money.transfer.rest;

import com.val.money.transfer.TransferController;
import com.val.money.transfer.model.Transaction;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/transaction")
@Produces(MediaType.APPLICATION_JSON)
public class TransferService {
    private TransferController transferController;

    @POST
    public Response transfer(Transaction transaction) {
        if (transaction == null) {
            throw new WebApplicationException("transaction can't be null", Response.Status.BAD_REQUEST);
        }
        try {
            transferController.transferFunds(transaction);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    public void setTransferController(TransferController transferController) {
        this.transferController = transferController;
    }
}
