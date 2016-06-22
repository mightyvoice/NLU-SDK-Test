package com.example.lj.asrttstest.dialog;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * The Interface IRdomain.
 *
 * Copyright (c) 2015 Nuance Communications. All rights reserved.
 */
public interface IRdomain {

    /** The rdomain parameters. */
    HashMap<String, Object> mParams = new HashMap<String, Object>();

    /**
     * Initializes the rdomain parameters.
     */
    void initParams();

    /**
     * Gets the value.
     *
     * @param paramName the param name
     * @return the value
     */
    Object getValue(String paramName);

    /**
     * Put value.
     *
     * @param paramName the param name
     * @param value the value
     */
    void putValue(String paramName, Object value);

    /**
     * Process nlu results.
     *
     * @param results the results
     */
    void processNluResults(JSONObject results);
}
