/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.bean;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author n631539
 */
public class AdminBean {
    
    public static final String ADMIN_ADD_PEER_KEY = "add_peer";
    public static final String ADMIN_ADD_PEER = "admin_addPeer";
    public static final String ADMIN_PEERS_KEY = "peers";
    public static final String ADMIN_PEERS = "admin_peers";
    public static final String ADMIN_NODE_INFO_KEY = "get";
    public static final String ADMIN_NODE_INFO = "admin_nodeInfo";
    public static final String ADMIN_VERBOSITY_KEY = "verbosity";
    public static final String ADMIN_VERBOSITY = "admin_verbosity";
    public static final String ADMIN_DATADIR_KEY = "datadir";
    public static final String ADMIN_DATADIR = "admin_datadir";
    
    private Map<String,String> functionNames = new HashMap();
    
    public AdminBean(){
        functionNames.put(ADMIN_ADD_PEER_KEY,ADMIN_ADD_PEER);
        functionNames.put(ADMIN_PEERS_KEY,ADMIN_PEERS);
        functionNames.put(ADMIN_NODE_INFO_KEY,ADMIN_NODE_INFO);
        functionNames.put(ADMIN_VERBOSITY_KEY,ADMIN_VERBOSITY);
        functionNames.put(ADMIN_DATADIR_KEY,ADMIN_DATADIR);
    }

    /**
     * @return the functionNames
     */
    public Map<String,String> getFunctionNames() {
        return functionNames;
    }

    /**
     * @param functionNames the functionNames to set
     */
    public void setFunctionNames(Map<String,String> functionNames) {
        this.functionNames = functionNames;
    }
    
}