package io.acme.scans.temporal.worker;

import io.acme.scans.config.TemporalConfig;
import io.acme.scans.temporal.activity.DtPrescanActivitiesImpl;
import io.acme.scans.temporal.activity.SastScanActivitiesImpl;
import io.acme.scans.temporal.activity.ScaManifestScanActivitiesImpl;
import io.acme.scans.temporal.workflow.DtPrescanWorkflowImpl;
import io.acme.scans.temporal.workflow.SastScanWorkflowImpl;
import io.acme.scans.temporal.workflow.ScaManifestScanWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.jboss.logging.Logger;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.ShutdownEvent;

@ApplicationScoped
public class TemporalWorkerRunner {
    private static final Logger LOG = Logger.getLogger(TemporalWorkerRunner.class);

    private final WorkerFactory factory;

    public TemporalWorkerRunner(WorkflowClient workflowClient, TemporalConfig config,
                                DtPrescanActivitiesImpl dtPrescanActivities,
                                SastScanActivitiesImpl sastActivities,
                                ScaManifestScanActivitiesImpl scaManifestActivities) {
        this.factory = WorkerFactory.newInstance(workflowClient);
        Worker worker = factory.newWorker(config.taskQueue());
        worker.registerWorkflowImplementationTypes(
                DtPrescanWorkflowImpl.class,
                SastScanWorkflowImpl.class,
                ScaManifestScanWorkflowImpl.class
        );
        worker.registerActivitiesImplementations(
                dtPrescanActivities,
                sastActivities,
                scaManifestActivities
        );
    }

    void onStart(@Observes StartupEvent ev) {
        LOG.info("Starting Temporal worker");
        factory.start();
    }

    void onStop(@Observes ShutdownEvent ev) {
        LOG.info("Shutting down Temporal worker");
        factory.shutdown();
    }
}

