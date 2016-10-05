/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.cakeshop.cassandra.model;

/**
 *
 * @author I629630
 */
public class Input {

    private String method;
    private Object[] args;

    public Input(String method, Object[] args) {
        this.method = method;
        this.args = args;
    }

    public String getMethod() {
        return method;

    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

}