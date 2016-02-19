package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl.BlockScannerImpl;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service("test-scanner")
@Scope("prototype")
public class TestBlockScanner extends BlockScannerImpl {

    @Override
    public void run() {
    }

    public void manualRun() {
        this.backfillBlocks();
    }

    @Override
    public void shutdown() {
    }

    @Override
    public synchronized void start() {
    }

}
