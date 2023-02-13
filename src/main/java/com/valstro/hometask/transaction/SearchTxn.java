package com.valstro.hometask.transaction;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.valstro.hometask.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Search transaction implementation. Implements transactional interface to be used within the {@link Client}.<br>
 * Transactions execute synchronously and block the console application until they complete or fail.
 */
public class SearchTxn implements Transaction {

    private static final Logger _L = LoggerFactory.getLogger(SearchTxn.class);

    private static final Gson GSON = new Gson();
    private static final String EVENT_NAME = "search";

    private final String key;
    private final String uuid;
    private Status status = Status.NEW;

    private int count = 0, maxCount = 0;
    private long timeout;
    List<QueryResponse> responses = new ArrayList<>();

    public SearchTxn(String key) {
        this.key = key;
        timeout = 30000;
        uuid = UUID.randomUUID().toString();
    }

    public SearchTxn(String key, long timeout) {
        this(key);
        this.timeout = timeout;
    }

    @Override
    public void execute(final Client c) {
        c.subscribeToEvent(EVENT_NAME, (args) -> {
            _L.info("Got response");
            try {
                QueryResponse object = GSON.fromJson(args[0].toString(), QueryResponse.class);
                if (object.resultCount == -1) {
                    _L.error("Server returned error: "+object.error);
                    responses.add(object);
                    status = Status.FAILED;
                }
                if (object.page >= 0 && object.page == count + 1) {
                    count++;
                    responses.add(object);
                    if (object.page == object.resultCount) {
                        status = Status.COMPLETE;
                    }
                }
            } catch (IllegalStateException | JsonSyntaxException e) {
                responses.add(new QueryResponse(e.getMessage()));
                status = Status.FAILED;
                _L.error("Search request failed. Payload: "+args[0], e);
            }
        });
        c.emitEvent(EVENT_NAME, GSON.toJson(new Query(key)));
        long startTs = System.currentTimeMillis();
        while(status == Status.NEW) {
            try {
                if(System.currentTimeMillis()-startTs>timeout) {
                    responses.add(new QueryResponse("Transaction timed out."));
                    status = Status.FAILED;
                } else {
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        c.unsubscribe(EVENT_NAME);
    }

    @Override
    public String getResult() {
        if(status != Status.COMPLETE) {
            return null;
        }
        StringBuilder sb = new StringBuilder(String.format("[Txn:%s] Results found:\n", uuid));
        for(QueryResponse q : responses) {
            sb.append(q.name).append(" featured in\n    ").append(q.films).append("\n");
        }
        return sb.toString();
    }

    @Override
    public boolean isComplete() {
        return status != Status.NEW;
    }

    @Override
    public String getError() {
        if(status == Status.FAILED) {
            for (QueryResponse r : responses) {
                if (r.error != null && !"".equals(r.error)) {
                    return r.error;
                }
            }
        }
        return null;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public String getId() {
        return uuid;
    }

    /**
     * Query structure describing search request payload.
     */
    private static class Query {
        private String query;

        public Query(String s) { query=s; }
        public String getQuery() { return query; }

    }

    /**
     * Query result structure describing search response payload.
     */
    private static class QueryResponse {
        private int page;
        private int resultCount;
        private String error;
        private String name;
        private String films;

        public QueryResponse(int page, int resultCount, String name, String films) {
            this.page = page;
            this.resultCount = resultCount;
            this.name = name;
            this.films = films;
        }

        public QueryResponse(String error) {
            page = -1;
            resultCount = -1;
            this.error = error;
        }

        public int getPage() { return page; }
        public int getResultCount() { return resultCount; }
        public String getError() { return error; }
        public String getName() { return name; }
        public String getFilms() { return films; }
    }

}
