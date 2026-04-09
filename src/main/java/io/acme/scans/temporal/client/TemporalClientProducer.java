package io.acme.scans.temporal.client;

import io.acme.scans.config.TemporalConfig;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class TemporalClientProducer {

    @Produces
    @ApplicationScoped
    public WorkflowServiceStubs workflowServiceStubs(TemporalConfig config) {
        WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder()
                .setTarget(config.target())
                .build();
        return WorkflowServiceStubs.newServiceStubs(options);
    }

    @Produces
    @ApplicationScoped
    public WorkflowClient workflowClient(WorkflowServiceStubs stubs, TemporalConfig config) {
        return WorkflowClient.newInstance(stubs,
                io.temporal.client.WorkflowClientOptions.newBuilder()
                        .setNamespace(config.namespace())
                        .build());
    }
}

