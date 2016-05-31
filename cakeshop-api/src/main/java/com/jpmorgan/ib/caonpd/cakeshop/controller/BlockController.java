package com.jpmorgan.ib.caonpd.cakeshop.controller;

import com.jpmorgan.ib.caonpd.cakeshop.config.JsonMethodArgumentResolver.JsonBodyParam;
import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
import com.jpmorgan.ib.caonpd.cakeshop.model.APIError;
import com.jpmorgan.ib.caonpd.cakeshop.model.APIResponse;
import com.jpmorgan.ib.caonpd.cakeshop.model.Block;
import com.jpmorgan.ib.caonpd.cakeshop.service.BlockService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/block",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
public class BlockController extends BaseController {

    @Autowired
    BlockService blockService;

    @RequestMapping("/get")
    public ResponseEntity<APIResponse> getBlock(
            @JsonBodyParam(required=false) String id,
            @JsonBodyParam(required=false) String hash,
            @JsonBodyParam(required=false) Long number,
            @JsonBodyParam(required=false) String tag) throws APIException {

        if (id == null && hash != null) {
            id = hash; // backwards compat
        }

        Block block = blockService.get(id, number, tag);
        //System.out.println(block);

        APIResponse res = new APIResponse();

        if (block != null) {
            res.setData(block.toAPIData());
            return new ResponseEntity<APIResponse>(res, HttpStatus.OK);
        }

        APIError err = new APIError();
        err.setStatus("404");
        err.setTitle("Block not found");
        res.addError(err);
        return new ResponseEntity<APIResponse>(res, HttpStatus.NOT_FOUND);
    }

}