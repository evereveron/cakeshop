package com.jpmorgan.ib.caonpd.cakeshop.test.controller;

import static org.hamcrest.CoreMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.testng.Assert.*;

import com.jpmorgan.ib.caonpd.cakeshop.controller.BlockController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;


public class BlockControllerTest extends BaseControllerTest {

    @Autowired
    BlockController blockController;

    public BlockControllerTest() {
        super();
    }

    @Override
    public Object getController() {
    	return blockController;
    }

    @Test
    public void testGetBlockByNumber() throws Exception {
        assertNotNull(mockMvc);
        commonTest("{\"number\":0}", 0);
    }

    @Test
    public void testGetBlockByHash() throws Exception {
        commonTest("{\"hash\":\"0x4c5dc2805d90a33fa4e5358346d5335d4f6aeefd7e839952ef4e070e3a8412d2\"}", 0);
    }

    @Test
    public void testGetBlockByInvalidHash() throws Exception {
        String body = "{\"hash\":\"0xb067233bfb768b2d5b7c190b13601f5eb8628e8daf02bb21dd091369c330c25a\"}";
        mockMvc.perform(post("/api/block/get")
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(body))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("\"errors\":")));
    }

    @Test
    public void testGetBlockByTag() throws Exception {
        commonTest("{\"tag\":\"latest\"}", 1);
    }

    private void commonTest(String postBody, int blockNum) throws Exception {
        mockMvc.perform(post("/api/block/get")
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(postBody))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString("\"number\":" + blockNum)));
    }

}