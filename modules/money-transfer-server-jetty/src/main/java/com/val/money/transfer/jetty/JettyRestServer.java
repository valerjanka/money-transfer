package com.val.money.transfer.jetty;

import com.val.money.transfer.rest.RestServer;
import com.val.money.transfer.rest.TransferService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.util.Properties;

public class JettyRestServer implements RestServer {
    private static final String TYPE = "JETTY";
    private Server server;

    /**
     * @param properties
     */
    @Override
    public void init(Properties properties, TransferService transferService) {
        int port = getPort(properties);
        server = new Server(port);
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");
        contextHandler.addServlet(new ServletHolder(new ServletContainer(createResourceConfig(transferService))),
                "/rest/*");
        server.setHandler(contextHandler);
        //ServletHolder servletHolder = contextHandler.addServlet(ServletContainer.class, "/rest/*");
        //servletHolder.setInitParameter("jersey.config.server.provider.packages",
        //        "com.val.money.transfer.rest");
    }

    private ResourceConfig createResourceConfig(TransferService transferService) {
        return new ResourceConfig().register(transferService);
    }

    @Override
    public void start() throws Exception {
        try {
            server.start();
            server.join();
        } finally {
            server.destroy();
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }

    private int getPort(Properties properties) {
        try {
            return Integer.valueOf(properties.getProperty(PORT_PROPERTY_NAME));
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not find port property '" + PORT_PROPERTY_NAME + "' in given properties");
        }
    }

}
