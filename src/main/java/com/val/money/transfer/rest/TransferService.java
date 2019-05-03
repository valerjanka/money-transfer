package com.val.money.transfer.rest;

import com.val.money.transfer.controller.TransferValidationException;
import com.val.money.transfer.controller.AccountController;
import com.val.money.transfer.model.Account;
import com.val.money.transfer.model.UserTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * Main Transfer Service, which handles HTTP request/response for transferring funds
 */
@Path("/account")
@Produces(MediaType.APPLICATION_JSON)
public class TransferService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private AccountController accountController;

    /**
     * Get all accounts - just for testing purposes
     * @return all existed accounts
     *
     * @throws TransferValidationException if request does not pass validation
     */
    @Path("all")
    @GET
    public List<Account> getAll() throws TransferValidationException {
        try {
            return accountController.getAllAccounts();
        } catch (TransferValidationException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Got internal server error: '{}'", e.getMessage(), e);
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    /**
     * Transfer funds by specified in user transaction data
     *
     * @param userTransaction includes source, destination and transfer amount
     *
     * @return HTTP response 200 if no errors
     *
     * @throws TransferValidationException if transaction does not pass validation
     */
    @Path("transfer")
    @POST
    public Response transfer(UserTransaction userTransaction) throws TransferValidationException {
        LOGGER.debug("Received transfer request: {}", userTransaction);
        if (userTransaction == null) {
            LOGGER.error("userTransaction can't be null");
            throw new BadRequestException("userTransaction can't be null");
        }
        try {
            accountController.transferFunds(userTransaction);
            return Response.status(Response.Status.OK).build();
        } catch (TransferValidationException e) {
            LOGGER.error("Got validation exception during Funds transfer: '{}'", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Got internal server error: '{}'", e.getMessage(), e);
            throw new InternalServerErrorException("Got internal server error during funds transfer");
        }
    }

    public void setAccountController(AccountController accountController) {
        this.accountController = accountController;
    }
}
