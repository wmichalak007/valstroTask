package com.valstro.hometask.client;

import com.valstro.hometask.Configuration;
import com.valstro.hometask.transaction.Transaction;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Socket.io implementation of the {@link Client} interface.<br>
 * Test server url:<br>
 * <a href="http://localhost:3000/socket.io/?EIO=4&transport=polling">http://localhost:3000/socket.io/?EIO=4&transport=polling</a>
 * returns:<br>
 * <pre>0{"sid":"e8IM03XHmctoWx7MAAAA","upgrades":["websocket"],"pingInterval":25000,"pingTimeout":20000}</pre>
 */
public class SocketIoClient extends AbstractClient {
    private static final Logger _L = LoggerFactory.getLogger(SocketIoClient.class);
    private static final Map<String, List<String>> CORS_HEADERS;

    static {
        CORS_HEADERS = new HashMap<>();
        List<String> list = new ArrayList<>();
        list.add("*");
        CORS_HEADERS.put("Access-Control-Allow-Origin", list);
    }

    /**
     * Socket.Io handle
     */
    private Socket socket;

    /* Dispatcher required: https://socketio.github.io/socket.io-client-java/faq.html#How_to_properly_close_a_client */
    private Dispatcher dispatcher;

    @Override
    public void connect() {
        try {
            dispatcher = new Dispatcher();

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .dispatcher(dispatcher)
                    .readTimeout(1, TimeUnit.MINUTES) // important for HTTP long-polling
                    .build();

            /* create connection options - can be configured externally */
            IO.Options options = IO.Options.builder()
                    .setReconnection(Configuration.get("socketio.reconnection", true))
                    .setReconnectionAttempts(Configuration.get("socketio.reconnectionAttempts", 60))
                    .setReconnectionDelay(Configuration.get("socketio.reconnectionDelay", 500))
                    .setReconnectionDelayMax(Configuration.get("socketio.reconnectionDelayMax", 1000))
                    .setExtraHeaders(CORS_HEADERS)
                    .build();
            options.callFactory = okHttpClient;
            options.webSocketFactory = okHttpClient;

            /* create socket connection - URL can cen configured externally */
            socket = IO.socket(Configuration.get("socketio.server","http://localhost:3000"), options);
            /* create default events listeners for greater visibility */
            socket.on(Socket.EVENT_CONNECT_ERROR, objs -> errorProcessor(Socket.EVENT_CONNECT_ERROR, objs));
            socket.on(Socket.EVENT_CONNECT, objs -> defaultProcessor(Socket.EVENT_CONNECT, objs));
            socket.on(Socket.EVENT_DISCONNECT, objs -> defaultProcessor(Socket.EVENT_DISCONNECT, objs));
            socket.on(Manager.EVENT_OPEN, objs -> defaultProcessor(Manager.EVENT_OPEN, objs));
            socket.on(Manager.EVENT_CLOSE, objs -> defaultProcessor(Manager.EVENT_CLOSE, objs));
            socket.on(Manager.EVENT_PACKET, objs -> defaultProcessor(Manager.EVENT_PACKET, objs));
            socket.on(Manager.EVENT_ERROR, objs -> errorProcessor(Manager.EVENT_ERROR, objs));
            socket.on(Manager.EVENT_RECONNECT, objs -> defaultProcessor(Manager.EVENT_RECONNECT, objs));
            socket.on(Manager.EVENT_RECONNECT_ERROR, objs -> errorProcessor(Manager.EVENT_RECONNECT_ERROR, objs));
            socket.on(Manager.EVENT_RECONNECT_FAILED, objs -> errorProcessor(Manager.EVENT_RECONNECT_FAILED, objs));
            socket.on(Manager.EVENT_RECONNECT_ATTEMPT, objs -> defaultProcessor(Manager.EVENT_RECONNECT_ATTEMPT, objs));
            socket.on(Manager.EVENT_TRANSPORT, objs -> defaultProcessor(Manager.EVENT_TRANSPORT, objs));
            /* open the socket and wait for completion */
            socket.open();
        } catch (URISyntaxException e) {
            _L.error("Failed to connect to the server.", e);
        }
    }

    @Override
    public void executeTransaction(Transaction t) {
        if(socket != null) {
            t.execute(this);
        } else {
            _L.error("Failed to execute transaction! Socket closed");
        }
    }

    @Override
    public void emitEvent(String name, String payload) {
        if(socket != null) {
            /* if socket is not connected it will queue events until it reconnects as per documentation:
                https://socket.io/docs/v4/client-offline-behavior/
                Could require manual override should queued messages can result in sudden burst if reconnected
                and result with a spike.
             */
            socket.emit(name, payload);
        }
    }

    @Override
    public void subscribeToEvent(String name, EventWrapper listener) {
        if(socket!=null) {
            socket.on(name, objects -> listener.call(objects));
        }
    }

    @Override
    public void unsubscribe(String name) {
        if(socket!= null) {
            socket.off(name);
        }
    }

    @Override
    public void disconnect() {
        /* Remove all listeners created during connection */
        socket.off(Socket.EVENT_CONNECT_ERROR, objs -> errorProcessor(Socket.EVENT_CONNECT_ERROR, objs));
        socket.off(Socket.EVENT_CONNECT, objs -> defaultProcessor(Socket.EVENT_CONNECT, objs));
        socket.off(Socket.EVENT_DISCONNECT, objs -> defaultProcessor(Socket.EVENT_DISCONNECT, objs));
        socket.off(Manager.EVENT_OPEN, objs -> defaultProcessor(Manager.EVENT_OPEN, objs));
        socket.off(Manager.EVENT_CLOSE, objs -> defaultProcessor(Manager.EVENT_CLOSE, objs));
        socket.off(Manager.EVENT_PACKET, objs -> defaultProcessor(Manager.EVENT_PACKET, objs));
        socket.off(Manager.EVENT_ERROR, objs -> errorProcessor(Manager.EVENT_ERROR, objs));
        socket.off(Manager.EVENT_RECONNECT, objs -> defaultProcessor(Manager.EVENT_RECONNECT, objs));
        socket.off(Manager.EVENT_RECONNECT_ERROR, objs -> errorProcessor(Manager.EVENT_RECONNECT_ERROR, objs));
        socket.off(Manager.EVENT_RECONNECT_FAILED, objs -> errorProcessor(Manager.EVENT_RECONNECT_FAILED, objs));
        socket.off(Manager.EVENT_RECONNECT_ATTEMPT, objs -> defaultProcessor(Manager.EVENT_RECONNECT_ATTEMPT, objs));
        socket.off(Manager.EVENT_TRANSPORT, objs -> defaultProcessor(Manager.EVENT_TRANSPORT, objs));
        /* close the socket */
        socket.close();
        /* wait for the socket shutdown */
        long startTs = System.currentTimeMillis();
        while(socket.connected() && socket.isActive()) {
            try {
                if(System.currentTimeMillis()-startTs < 5000) {
                    /* terminate dispatcher as per: https://socketio.github.io/socket.io-client-java/faq.html#How_to_properly_close_a_client */
                    System.out.println("Shutting down the dispatcher");
                    dispatcher.executorService().shutdown();
                }
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isConnected() {
        return socket != null && socket.connected();
    }

}
