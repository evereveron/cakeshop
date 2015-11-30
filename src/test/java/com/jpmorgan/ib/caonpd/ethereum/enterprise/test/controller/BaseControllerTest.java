package com.jpmorgan.ib.caonpd.ethereum.enterprise.test.controller;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.annotations.BeforeMethod;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.JsonMethodArgumentResolver;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.test.BaseGethRpcTest;

/**
 * Base class for Controller testing. Simply subclass and implement getController() method
 *
 * @author chetan
 *
 */
@WebAppConfiguration
public abstract class BaseControllerTest extends BaseGethRpcTest {

    private static final Logger LOG = LoggerFactory.getLogger(BaseControllerTest.class);

    protected MockMvc mockMvc;

    /**
     * Return the @Controller instance under test. Used in setUp()
     * @return
     */
    public abstract Object getController();

    @BeforeMethod
    public void setupMockMvc() throws Exception {
        mockMvc = standaloneSetup(getController()).setCustomArgumentResolvers(new JsonMethodArgumentResolver()).build();
    }

}
