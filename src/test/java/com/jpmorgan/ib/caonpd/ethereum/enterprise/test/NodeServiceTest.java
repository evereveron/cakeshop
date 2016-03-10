package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.testng.Assert.*;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Node;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.NodeService;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

public class NodeServiceTest extends BaseGethRpcTest {

	@Autowired
	private NodeService nodeService;

	@Test
	public void testGet() throws IOException {
	    Node node = nodeService.get();
	    assertNotNull(node);
	    assertEquals(node.getStatus(), "running");
	}

}
