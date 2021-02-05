package com.maxdemarzi;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventListener;
import org.neo4j.logging.internal.LogService;

import java.util.concurrent.ExecutorService;

public class MyTransactionEventListener implements TransactionEventListener<Object> {

    public static GraphDatabaseService db;
    public static ExecutorService ex;
    public static LogService logsvc;

    public MyTransactionEventListener(GraphDatabaseService graphDatabaseService, ExecutorService executor, LogService logService) {
        db = graphDatabaseService;
        ex = executor;
        logsvc = logService;
    }

    @Override
    public Object beforeCommit(final TransactionData transactionData, final Transaction transaction, final GraphDatabaseService databaseService) throws Exception {
        return null;
    }

    @Override
    public void afterCommit(final TransactionData transactionData, final Object state, final GraphDatabaseService databaseService) {
        ex.submit(new SuspectRunnable(transactionData, db, logsvc));
    }

    @Override
    public void afterRollback(final TransactionData data, final Object state, final GraphDatabaseService databaseService) {

    }
}
