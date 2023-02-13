package com.valstro.hometask.client;

import com.valstro.hometask.transaction.Transaction;
import io.socket.emitter.Emitter;

import java.util.Properties;

/**
 * Client interface representing data source. It can use any implementation so long it follows
 * asynchronous, event driven, bidirectional communication. Once <pre>connect()</pre> is called, <pre>Client</pre>
 * allows subscribing to events <pre>subscribeToEvent</pre> as well as <pre>emitEvent</pre> operations.<br>
 * Client by default does keep track of subscribed event so each consumer will be destroyed. Consumers can do that manually
 * by calling <pre>unsubscribe</pre>.
 *
 */
public interface Client {

    /**
     * Connect to the data source.
     */
    void connect();

    /**
     * Disconnects from the server and scraps all listeners that are unsubscribed.
     */
    void disconnect();

    /**
     * Returns client's connection status.
     * @return whether there is an active connection to the server.
     */
    boolean isConnected();

    /**
     * Executes a single transaction.
     * @param t Transaction to be executed
     */
    void executeTransaction(Transaction t);

    /**
     * Emits a single event to the server. Payload must be in JSON format.
     *
     * @param name event name
     * @param payload event payload in JSON format
     */
    void emitEvent(String name, String payload);

    /**
     * Subscribes to an event stream identified by the event name. Each event will trigger a callback function for
     * processing the payload.
     *
     * @param name Name of the event stream to subscribe to
     * @param listener Callback function used to process the event
     */
    void subscribeToEvent(String name, EventWrapper listener);

    /**
     * Unsubscribes from the event stream identified by the event name.
     * @param name
     */
    void unsubscribe(String name);

    /**
     * Callback functional interface for event payload processing.
     */
    interface EventWrapper {

        /**
         * Payload processor.
         * @param objects event payload
         */
        public void call(Object... objects);
    }
}
