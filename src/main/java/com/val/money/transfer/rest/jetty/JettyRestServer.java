package com.val.money.transfer.rest.jetty;

import com.val.money.transfer.rest.RestServer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Properties;

public class JettyRestServer implements RestServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String TYPE = "JETTY";
    private static final String PATH_SPEC = "/*";
    private Server server;


    /**
     * Initializes Jetty server by specified properties and rest services
     *
     * @param properties   with port specified
     * @param restServices list of rest services to map and handle
     */
    @Override
    public void init(Properties properties, List restServices) {
        int port = getPort(properties);
        server = new Server(port);
        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setContextPath("");
        contextHandler.addServlet(new ServletHolder(new ServletContainer(createResourceConfig(restServices))),
                PATH_SPEC);
        server.setHandler(contextHandler);
    }

    private ResourceConfig createResourceConfig(List restServices) {
        ResourceConfig resourceConfig = new ResourceConfig();
        for (Object service : restServices) {
            resourceConfig.register(service);
        }
        return resourceConfig;
    }

    @Override
    public void start() throws Exception {
        try {
            server.start();
        } catch (Exception e) {
            LOGGER.error("Could not start jetty server. Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void join() throws InterruptedException {
        try {
            server.join();
        } catch (InterruptedException e) {
            LOGGER.error("Could not join jetty server. Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void stop() throws Exception {
        try {
            server.stop();
        } catch (Exception e) {
            LOGGER.error("Could not stop the server", e);
            throw e;
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
            throw new IllegalArgumentException("Could not find port property '" + PORT_PROPERTY_NAME + "' in given properties", e);
        }
    }

}
