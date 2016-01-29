package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import static com.jpmorgan.ib.caonpd.ethereum.enterprise.util.ProcessUtils.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.MediaType.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.AdminBean;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.GethConfigBean;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.NodeInfo;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.RequestModel;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author I629630
 */
@Service
public class GethHttpServiceImpl implements GethHttpService, ApplicationContextAware {

    public static final String SIMPLE_RESULT = "_result";
    public static final Integer DEFAULT_NETWORK_ID = 1006;

    private static final Logger LOG = LoggerFactory.getLogger(GethHttpServiceImpl.class);

    @Value("${app.path}")
    private String APP_ROOT;

    @Autowired
    private GethConfigBean gethConfig;

    private ApplicationContext applicationContext;

    @Override
    public String executeGethCall(String json) throws APIException {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(json, headers);
            ResponseEntity<String> response = restTemplate.exchange(gethConfig.getRpcUrl(), POST, httpEntity, String.class);
            return response.getBody();
        } catch (RestClientException e) {
            LOG.error("RPC call failed - " + ExceptionUtils.getRootCauseMessage(e));
            throw new APIException("RPC call failed", e);
        }
    }

    @Override
    public Map<String, Object> executeGethCall(String funcName, Object[] args) throws APIException {

        RequestModel request = new RequestModel(GethHttpService.GETH_API_VERSION, funcName, args, GethHttpService.USER_ID);
        String req = new Gson().toJson(request);
        String response = executeGethCall(req);

        if (StringUtils.isEmpty(response)) {
            throw new APIException("Received empty reply from server");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(response.trim());
        }

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data;
        try {
            data = mapper.readValue(response, Map.class);
        } catch (IOException e) {
            throw new APIException("RPC call failed", e);
        }

        if (data.containsKey("error") && data.get("error") != null) {
            String message;
            Map<String, String> error = (Map<String, String>) data.get("error");
            if (error.containsKey("message")) {
                message = error.get("message");
            } else {
                message = "RPC call failed";
            }
            throw new APIException(message);
        }

        Object result = data.get("result");
        if (result == null) {
            return null;
        }

        if (!(result instanceof Map)) {
            // Handle case where a simple value is returned instead of a map (int, bool, or string)
            Map<String, Object> res = new HashMap<>();
            res.put(SIMPLE_RESULT, data.get("result"));
            return res;
        }

        return (Map<String, Object>) data.get("result");
    }

    @Override
    public Boolean stopGeth() {
        try {
            return killProcess(readPidFromFile(gethConfig.getGethPidFilename()), "geth.exe");
        } catch (IOException | InterruptedException ex) {
            LOG.error("Cannot shutdown process " + ex.getMessage());
            return false;
        }
    }

    @Override
    public Boolean deleteEthDatabase(String eth_datadir) {
        // FIXME do we need this param here??
        if (eth_datadir == null) {
            return false;
        }

        try {
            FileUtils.deleteDirectory(new File(eth_datadir));
            return true;
        } catch (IOException ex) {
            LOG.error("Cannot delete directory " + ex.getMessage());
            return false;

        }
    }

    @PostConstruct
    public void autoStart() {
        if (!gethConfig.isAutoStart()) {
            return;
        }
        LOG.info("Autostarting geth node");
        start();
    }

    @Override
    public Boolean deletePid() {
        File pidFile = new File(gethConfig.getGethPidFilename());
        return pidFile.delete();
    }

    @Override
    public void setNodeInfo(String identity, Boolean mining, Integer verbosity, Integer networkId) {
        if (null != networkId) {
            gethConfig.setNetworkId(networkId);
        }

        if (null != mining) {
            gethConfig.setMining(mining);
        }

        if (null != verbosity) {
            gethConfig.setVerbosity(verbosity);
        } else if (null == gethConfig.getVerbosity()) {
            gethConfig.setVerbosity(0);
        }

        if (StringUtils.isNotEmpty(identity)) {
            gethConfig.setIdentity(identity);
        } else if (null == gethConfig.getIdentity()) {
            gethConfig.setIdentity("");
        }
    }

    @Override
    public NodeInfo getNodeInfo() {
        return new NodeInfo(gethConfig.getIdentity(), gethConfig.isMining(),
                gethConfig.getNetworkId(), gethConfig.getVerbosity());
    }

    private String getNodeIdentity() throws APIException {
        Map<String, Object> data = null;

        data = this.executeGethCall(AdminBean.ADMIN_NODE_INFO, new Object[] { null, true });

        if (data != null) {
            return (String) data.get("Name");
        }

        return null;
    }

    @PreDestroy
    protected void autoStop () {
        if (gethConfig.isAutoStop()) {
            stopGeth();
            deletePid();
        }
    }

    @Override
    public Boolean start(String... additionalParams) {

        boolean isStarted = isProcessRunning(readPidFromFile(gethConfig.getGethPidFilename()));

        if (isStarted) {
            LOG.info("Ethereum was already running");
            return true;
        }

        String genesisFile = gethConfig.getGenesisBlockFilename();
        String dataDir = gethConfig.getDataDirPath();

        List<String> commands = Lists.newArrayList(gethConfig.getGethPath(),
                "--port", gethConfig.getGethNodePort(),
                "--datadir", dataDir, "--genesis", genesisFile,
                //"--verbosity", "6",
                //"--mine", "--minerthreads", "1",
                "--solc", gethConfig.getSolcPath(),
                "--nat", "none", "--nodiscover",
                "--unlock", "0 1 2", "--password", gethConfig.getGethPasswordFile(),
                "--rpc", "--rpcaddr", "127.0.0.1", "--rpcport", gethConfig.getRpcPort(), "--rpcapi", gethConfig.getRpcApiList(),
                "--ipcdisable"
                );

        if (null != additionalParams && additionalParams.length > 0) {
            commands.addAll(Lists.newArrayList(additionalParams));
        }

        commands.add("--networkid");
        commands.add(String.valueOf(gethConfig.getNetworkId() == null ? DEFAULT_NETWORK_ID : gethConfig.getNetworkId()));

        commands.add("--verbosity");
        commands.add(String.valueOf(gethConfig.getVerbosity() == null ? "3" : gethConfig.getVerbosity()));

        if (null != gethConfig.isMining() && gethConfig.isMining() == true) {
            commands.add("--mine");
            commands.add("--minerthreads");
            commands.add("1");
        }
        if (StringUtils.isNotEmpty(gethConfig.getIdentity())) {
            commands.add("--identity");
            commands.add(gethConfig.getIdentity());
        }

        ProcessBuilder builder = createProcessBuilder(gethConfig, commands);
        builder.inheritIO();

        Boolean started = false;
        Process process;
        try {
            File dataDirectory = new File(dataDir);
            boolean newGethInstall = false;

            if (!dataDirectory.exists()) {
                dataDirectory.mkdirs();
            }

            File keystoreDir = new File(dataDir + File.separator + "keystore");
            if (!keystoreDir.exists()) {
                String keystoreSrcPath = new File(genesisFile).getParent() + File.separator + "keystore";
                FileUtils.copyDirectory(new File(keystoreSrcPath), new File(dataDir + File.separator + "keystore"));
                newGethInstall = true;
            }

            process = builder.start();

            Integer pid = getProcessPid(process);
            if (pid != null) {
                writePidToFile(pid, gethConfig.getGethPidFilename());
            }

            started = checkGethStarted();

            if (started && newGethInstall) {
                BlockchainInitializerTask init = applicationContext.getBean(BlockchainInitializerTask.class);
                init.run();
            }

            // FIXME add a watcher thread to make sure it doesn't die..

        } catch (IOException ex) {
            LOG.error("Cannot start process: " + ex.getMessage());
            started = false;
        }

        if (started) {
            LOG.info("Ethereum started ...");
        } else {
            LOG.error("Ethereum has NOT been started...");
        }
        return started;
    }

    /**
     * Answer "YES" to the GPL agreement prompt on Geth init
     *
     * NOTE: In our "meth" build, this prompt is complete disabled
     *
     * @param process
     * @throws IOException
     */
    private void answerLegalese(Process process) throws IOException {
        try (Scanner scanner = new Scanner(process.getInputStream())) {
            boolean flag = scanner.hasNext();
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                while (flag) {
                    String readline = scanner.next();
                    if (readline.isEmpty()) {
                        continue;
                    }
                    if (readline.contains("[y/N]")) {
                        writer.write("y");
                        writer.flush();
                        writer.newLine();
                        writer.flush();
                        flag = false;
                    }
                }
            }
        }
    }

    private Boolean checkGethStarted() {

        long timeStart = System.currentTimeMillis();
        Boolean started = false;

        while (true) {
            try {
                if (checkConnection()) {
                    LOG.info("Geth started up successfully");
                    started = true;
                    break;
                }
            } catch (IOException ex) {
                LOG.debug(ex.getMessage());
                if (System.currentTimeMillis() - timeStart >= 10000) {
                    // Something went wrong and RPC did not start within 10
                    // sec
                    LOG.error("Geth did not start within 10 seconds");
                    break;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        return started;
    }

    private Boolean checkConnection() throws IOException {
        Boolean connected = false;
        try {
            URL urlConn = new URL(gethConfig.getRpcUrl());
            HttpURLConnection conn = (HttpURLConnection) urlConn.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            if (conn.getResponseCode() == 200) {
                connected = true;
            }
            conn.disconnect();
        } catch (MalformedURLException ex) {
            LOG.error(ex.getMessage());
        }
        return connected;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
