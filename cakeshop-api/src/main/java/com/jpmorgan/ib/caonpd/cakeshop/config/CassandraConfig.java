/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.cakeshop.config;

import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Account;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Block;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Event;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.LatestBlockNumber;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Peer;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Transaction;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.mapping.BasicCassandraMappingContext;
import org.springframework.data.cassandra.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

/**
 *
 * @author I629630
 */
@Configuration
@EnableCassandraRepositories(
        basePackages = "com.jpmorgan.ib.caonpd.cakeshop.cassandra.repository")
public class CassandraConfig extends AbstractCassandraConfiguration {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CassandraConfig.class);

    @Autowired
    private Environment env;

    @Override
    protected String getKeyspaceName() {
        return env.getProperty("blockchain.cassandra.keyspace", "blockchain");
    }

    @Bean
    @Override
    public CassandraClusterFactoryBean cluster() {
        String user = env.getProperty("blockchain.cassandra.param1", "");
        String passw = env.getProperty("blockchain.cassandra.param2", "");
        String host = env.getProperty("blockchain.cassandra.host");
        Integer coreConnections = Integer.valueOf(env.getProperty("blockchain.cassandra.core.connections", "20"));
        Integer maxConnections = Integer.valueOf(env.getProperty("blockchain.cassandra.max.connections", "50"));
        Integer timeout = Integer.valueOf(env.getProperty("blockchain.cassandra.timeout.ms", "1000"));
        Integer heartbeatInterval = Integer.valueOf(env.getProperty("blockchain.cassandra.heartbeat.sec", "15"));
        Integer port = Integer.valueOf(env.getProperty("blockchain.cassandra.port", "9042"));
        Boolean sslEnabled = Boolean.valueOf(env.getProperty("blockchain.cassandra.ssl.enabled", "false"));
        if (StringUtils.isNotBlank(host)) {
            PoolingOptions poolingOptions = new PoolingOptions()
                .setIdleTimeoutSeconds(timeout / 1000)
                .setPoolTimeoutMillis(timeout)
                .setConnectionsPerHost(HostDistance.LOCAL, coreConnections, maxConnections)
                .setHeartbeatIntervalSeconds(heartbeatInterval);
            CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
            cluster.setContactPoints(host);
            cluster.setPort(port);
            cluster.setPoolingOptions(poolingOptions);
            cluster.setSslEnabled(sslEnabled);
            cluster.setReconnectionPolicy(new ConstantReconnectionPolicy(100L));
            cluster.setRetryPolicy(DefaultRetryPolicy.INSTANCE);
            if (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(passw)) {
                cluster.setUsername(user);
                cluster.setPassword(passw);
            }
            return cluster;
        } else {
            LOG.warn("NO Cassandra settings provided");
            return new CassandraClusterFactoryBean();
        }
    }

    @Bean
    @Override
    public CassandraMappingContext cassandraMapping() throws ClassNotFoundException {
        BasicCassandraMappingContext mapping = new BasicCassandraMappingContext();
        Set<Class<?>> entities = new HashSet();
        entities.add(Account.class);
        entities.add(Block.class);
        entities.add(Event.class);
        entities.add(LatestBlockNumber.class);
        entities.add(Peer.class);
        entities.add(Transaction.class);
        mapping.setInitialEntitySet(entities);
        mapping.afterPropertiesSet();
        return mapping;
    }

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }

    @Override
    public String[] getEntityBasePackages() {
        return new String[]{"com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity"};
    }

}
