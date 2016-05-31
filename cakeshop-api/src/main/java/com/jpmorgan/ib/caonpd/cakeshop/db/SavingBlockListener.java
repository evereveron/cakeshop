package com.jpmorgan.ib.caonpd.cakeshop.db;

import com.google.common.collect.Lists;
import com.jpmorgan.ib.caonpd.cakeshop.bean.GethConfigBean;
import com.jpmorgan.ib.caonpd.cakeshop.dao.BlockDAO;
import com.jpmorgan.ib.caonpd.cakeshop.dao.TransactionDAO;
import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
import com.jpmorgan.ib.caonpd.cakeshop.model.Block;
import com.jpmorgan.ib.caonpd.cakeshop.model.Transaction;
import com.jpmorgan.ib.caonpd.cakeshop.service.TransactionService;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
@Profile("container")
public class SavingBlockListener implements BlockListener {

    private class BlockSaverThread extends Thread {
        public boolean running = true;

        public BlockSaverThread() {
            setName("BlockSaver");
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Block block = blockQueue.poll(500, TimeUnit.MILLISECONDS);
                    if (block != null) {
                        saveBlock(block);
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(SavingBlockListener.class);

    @Autowired
    private BlockDAO blockDAO;

    @Autowired
    private TransactionDAO txDAO;

    @Autowired
    private TransactionService txService;

    @Autowired
    private GethConfigBean gethConfig;

    private final ArrayBlockingQueue<Block> blockQueue;

    private final BlockSaverThread blockSaver;

    public SavingBlockListener() {
        blockQueue = new ArrayBlockingQueue<>(1000);
        blockSaver = new BlockSaverThread();
        blockSaver.start();
    }

    @PreDestroy
    @Override
    public void shutdown() {
        LOG.info("shutdown");
        blockSaver.running = false;
        blockSaver.interrupt();
    }

    protected void saveBlock(Block block) {
        if (!gethConfig.isDbEnabled()) {
            return;
        }
        LOG.debug("Persisting block #" + block.getNumber());
        blockDAO.save(block);
        if (!block.getTransactions().isEmpty()) {
            List<String> transactions = block.getTransactions();
            List<List<String>> txnChunks = Lists.partition(transactions, 256);
            for (List<String> txnChunk : txnChunks) {
                try {
                    List<Transaction> txns = txService.get(txnChunk);
                    txDAO.save(txns);
                } catch (APIException e) {
                    LOG.warn("Failed to load transaction details for tx", e);
                }
            }
        }
    }

    @Override
    public void blockCreated(Block block) {
        try {
            blockQueue.put(block);
        } catch (InterruptedException e) {
            return;
        }
    }

}