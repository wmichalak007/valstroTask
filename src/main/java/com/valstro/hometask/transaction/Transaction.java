package com.valstro.hometask.transaction;

import com.valstro.hometask.client.Client;
import io.socket.client.Socket;

/**
 * Transactions are used for data exchange between application and the server. Transactions have unique UUID to
 * differentiate one transaction form another.
 */
public interface Transaction {

    /**
     * Execute a transaction using provided client. The client must be connected in order for transaction to send
     * and receive the data.
     * @param client client to be used
     */
    void execute(final Client client);

    /**
     * Returns result once transaction has completed successfully and result compiled
     * @return complete result or null if transaction failed
     */
    String getResult();

    /**
     * Checks transaction status
     * @return true if transaction has completed or failed, otherwise false
     */
    boolean isComplete();

    /**
     * Unique transaction ID
     * @return transaction id
     */
    String getId();

    /**
     * Returns cause for transactions failure
     * @return cause or null if transaction completed successfully
     */
    String getError();

    /**
     * Current transaction status
     * @return status
     */
    Status getStatus();

    /**
     * Transaction status:<br>
     *<ul>
     *     <li>NEW - new transaction that is executed</li>
     *     <li>COMPLETE - transaction has completed successfully and results are ready</li>
     *     <li>FAILED - transaction has failed and did not complete</li>
     *</ul>
     */
    static enum Status { NEW, COMPLETE, FAILED}
}
