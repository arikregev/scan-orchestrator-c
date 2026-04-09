package io.acme.scans.temporal.workflow;

import io.acme.scans.domain.ScanRequest;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface DtPrescanWorkflow {
    @WorkflowMethod
    void run(ScanRequest request);
}

