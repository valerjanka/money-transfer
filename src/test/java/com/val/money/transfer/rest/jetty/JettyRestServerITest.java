package com.val.money.transfer.rest.jetty;

import com.val.money.transfer.TransferException;
import com.val.money.transfer.controller.AccountController;
import com.val.money.transfer.model.Account;
import com.val.money.transfer.model.UserTransaction;
import com.val.money.transfer.rest.RestServer;
import com.val.money.transfer.rest.TransferExceptionMapper;
import com.val.money.transfer.rest.TransferService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static javax.ws.rs.client.ClientBuilder.newClient;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * Integration test: starts Jetty server and tests HTTP requests
 */
public class JettyRestServerITest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String PORT = "8084";
    private JettyRestServer server;
    private TransferService transferService;
    private AccountController emptyAccountController;
    private AccountController throwsRuntimeExceptionAccountController;
    private List<Account> accounts = Arrays.asList(new Account(1, 1, BigDecimal.ONE, "USD"),
            new Account(2, 2, BigDecimal.TEN, "USD"));


    @Before
    public void before() throws Exception {
        server = new JettyRestServer();
        transferService = new TransferService();
        configureMocks();

        Properties properties = new Properties();
        properties.put(RestServer.PORT_PROPERTY_NAME, PORT);
        server.init(properties, Arrays.asList(transferService, new TransferExceptionMapper()));
        try {
            server.start();
        } catch (Exception e) {
            LOGGER.error("Could not start Jetty server. Error: {}", e.getMessage(), e);
            server.stop();
            throw e;
        }
    }

    private void configureMocks() throws TransferException {
        emptyAccountController = Mockito.mock(AccountController.class);
        Mockito.when(emptyAccountController.getAllAccounts()).thenReturn(accounts);
        throwsRuntimeExceptionAccountController = Mockito.mock(AccountController.class);
        Mockito.doThrow(new RuntimeException()).when(throwsRuntimeExceptionAccountController).getAllAccounts();
        Mockito.doThrow(new RuntimeException()).when(throwsRuntimeExceptionAccountController).transferFunds(Mockito.any());
    }

    @Test
    public void testStartGetAllAccountsOk() throws TransferException {
        transferService.setAccountController(emptyAccountController);
        Response response = invokeTestGet();
        Assert.assertNotNull(response);
        try {
            List result = response.readEntity(List.class);
            System.out.println(result);
            Assert.assertNotNull(result);
            Assert.assertEquals(2, result.size());
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatusInfo().getStatusCode());
            Mockito.verify(emptyAccountController, times(1)).getAllAccounts();
        } finally {
            response.close();
        }
    }

    @Test
    public void testStartGetAllAccountsThrowsInternalException() throws TransferException {
        transferService.setAccountController(throwsRuntimeExceptionAccountController);
        Response response = invokeTestGet();
        System.out.println(response);
        Assert.assertNotNull(response);
        try {
            Mockito.verify(throwsRuntimeExceptionAccountController, times(1)).getAllAccounts();
            Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusInfo().getStatusCode());
        } finally {
            response.close();
        }
    }

    @Test
    public void testTransfer() throws TransferException {
        transferService.setAccountController(emptyAccountController);
        Response response = invokeTransferFunds();
        Assert.assertNotNull(response);
        try {
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatusInfo().getStatusCode());
            Mockito.verify(emptyAccountController, times(1)).transferFunds(Mockito.any());
        } finally {
            response.close();
        }
    }

    @Test
    public void testTransferInternalError() throws TransferException {
        transferService.setAccountController(throwsRuntimeExceptionAccountController);
        Response response = invokeTransferFunds();
        Assert.assertNotNull(response);
        try {
            Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusInfo().getStatusCode());
            Mockito.verify(throwsRuntimeExceptionAccountController, times(1)).transferFunds(Mockito.any());
        } finally {
            response.close();
        }
    }

    private Response invokeTestGet() {
        WebTarget target = newClient().target("http://localhost:" + PORT).path("account/all");
        return target.request().get();
    }

    private Response invokeTransferFunds() {
        WebTarget target = newClient().target("http://localhost:" + PORT).path("account/transfer");
        UserTransaction transaction = new UserTransaction(1, 2, BigDecimal.ONE);
        return target.request().post(Entity.json(transaction));
    }

    @After
    public void after() throws Exception {
        server.stop();
    }
}
