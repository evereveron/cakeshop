package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService.CodeType;

import java.util.List;

public interface ContractRegistryService {

    /**
     * Deploy the Contract Registry contract onto the chain.
     *
     * Primarily used during chain initialization.
     *
     * @return
     * @throws APIException
     */
    public boolean deploy() throws APIException;

    /**
     * Register a new contract
     *
     * @param id
     * @param name
     * @param abi
     * @param code
     * @param codeType
     * @param createdDate
     *
     * @return {@link TransactionResult} The id of the registration transaction
     * @throws APIException
     */
    public TransactionResult register(String from, String id, String name, String abi, String code, CodeType codeType, Long createdDate) throws APIException;

    /**
     * Lookup a contract in the registry by ID (address hash)
     *
     * @param id                Address hash of contract
     *
     * @return
     * @throws APIException
     */
    public Contract getById(String id) throws APIException;

    /**
     * Lookup a contract in the registry by name. If multiple instances are
     * stored, will return the latest version (by timestamp).
     *
     * @param name
     * @return
     * @throws APIException
     */
    public Contract getByName(String name) throws APIException;

    /**
     * List all contracts in the registry
     *
     * @return
     * @throws APIException
     */
    public List<Contract> list() throws APIException;

    /**
     * List all contracts registered by the given address
     *
     * @param owner           Address hash to lookup
     * @return
     * @throws APIException
     */
    public List<Contract> listByOwner(String owner) throws APIException;

}
