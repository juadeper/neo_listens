package com.maxdemarzi;

import org.neo4j.annotations.service.ServiceProvider;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.kernel.availability.AvailabilityGuard;
import org.neo4j.kernel.extension.ExtensionFactory;
import org.neo4j.kernel.extension.ExtensionType;
import org.neo4j.kernel.extension.context.ExtensionContext;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;
import org.neo4j.logging.internal.LogService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ServiceProvider
public class RegisterTransactionEventListenerExtensionFactory extends ExtensionFactory<RegisterTransactionEventListenerExtensionFactory.Dependencies> {
    @Override
    public Lifecycle newInstance(final ExtensionContext extensionContext, final Dependencies dependencies) {
        final GraphDatabaseAPI db = dependencies.graphdatabaseAPI();
        final LogService log = dependencies.log();
        final DatabaseManagementService databaseManagementService = dependencies.databaseManagementService();
        return new CustomGraphDatabaseLifecycle(log, db, dependencies, databaseManagementService);
    }

    interface Dependencies {
        GraphDatabaseAPI graphdatabaseAPI();

        DatabaseManagementService databaseManagementService();

        AvailabilityGuard availabilityGuard();

        LogService log();
    }

    public static class CustomGraphDatabaseLifecycle extends LifecycleAdapter {
        private final GraphDatabaseAPI db;
        private final LogService log;
        private MyTransactionEventListener transactionEventHandler;
        private final DatabaseManagementService databaseManagementService;

        public CustomGraphDatabaseLifecycle(final LogService log, final GraphDatabaseAPI db, final Dependencies dependencies, final DatabaseManagementService databaseManagementService) {
            this.log = log;
            this.db = db;
            this.databaseManagementService = databaseManagementService;
        }

        @Override
        public void start() {
            if (this.db.databaseName().compareTo("system") != 0) {
                System.out.println("STARTING trigger watcher");
                ExecutorService executor = Executors.newFixedThreadPool(2);
                this.transactionEventHandler = new MyTransactionEventListener(this.db, executor, this.log);
                this.databaseManagementService.registerTransactionEventListener(this.db.databaseName(), this.transactionEventHandler);
            }
        }

        @Override
        public void shutdown() {
            System.out.println("STOPPING trigger watcher");
            this.databaseManagementService.unregisterTransactionEventListener(this.db.databaseName(), this.transactionEventHandler);
        }
    }

    public RegisterTransactionEventListenerExtensionFactory() {
        super(ExtensionType.DATABASE, "neo4JTransactionEventHandler");
    }
}
