/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterperise.service;

/**
 *
 * @author I629630
 */
public interface GethHttpService {
    
    public static final String startXCommand = "bin/linux/geth";
    public static final String startWinCommand = "bin/win/geth.exe";
    public static final String startMacCommand = "bin/mac/geth";
    
    public String executeGethCall(String json);
    public void startGeth(String command, String genesisDir);
    
}
