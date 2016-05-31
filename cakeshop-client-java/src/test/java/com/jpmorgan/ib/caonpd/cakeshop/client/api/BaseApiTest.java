package com.jpmorgan.ib.caonpd.cakeshop.client.api;

import com.jpmorgan.ib.caonpd.cakeshop.client.ApiClient;

import java.io.IOException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import feign.Logger.Level;
import feign.slf4j.Slf4jLogger;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.QueueDispatcher;

public class BaseApiTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
            .getLogger(BaseApiTest.class);

    protected MockWebServer mockWebServer;

    protected ApiClient apiClient;

    @BeforeClass
    public void setupMockWebserver() throws IOException {
        if (mockWebServer != null) {
            return;
        }
        mockWebServer = new MockWebServer();
        QueueDispatcher dispatcher = new QueueDispatcher();
        dispatcher.setFailFast(true);
        mockWebServer.setDispatcher(dispatcher);
        mockWebServer.start();
    }

    @AfterClass(alwaysRun=true)
    public void stopMockWebserver() {
        if (mockWebServer != null) {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                LOG.debug("MockWebServer shutdown failed", e);
            }
        }
    }

    public String getTestUri() {
        return "http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort() + "/cakeshop/api";
    }

    @BeforeMethod
    public void createApiClient() {
        this.apiClient = new ApiClient().setBasePath(getTestUri());
        this.apiClient.getFeignBuilder().logger(new Slf4jLogger()).logLevel(Level.FULL); // set logger to debug
    }

}
