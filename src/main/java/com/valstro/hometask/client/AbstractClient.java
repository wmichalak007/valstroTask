package com.valstro.hometask.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract client implementation that supports default processing of messages and error messages.
 */
public abstract class AbstractClient implements Client {
    private static final Logger _L = LoggerFactory.getLogger(AbstractClient.class);

    /**
     * Default message processor
     * @param event processed event name
     * @param objects event payload
     */
    protected final void defaultProcessor(String event, Object... objects) {
        StringBuilder sb = new StringBuilder("Received: ");
        sb.append(event).append(" = ");
        for(Object o : objects) {
            sb.append(o.toString());
        }
        _L.info(sb.toString());
    }

    /**
     * Default error processor
     * @param event processed event name
     * @param objects event payload
     */
    protected final void errorProcessor(String event, Object... objects) {
        StringBuilder sb = new StringBuilder("Caught: ");
        sb.append(event).append(" = ");
        for(Object o : objects) {
            sb.append(o.toString());
        }
        _L.error(sb.toString());
    }


}
