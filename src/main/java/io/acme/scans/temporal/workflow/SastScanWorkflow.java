package io.acme.scans.temporal.workflow;

import io.acme.scans.domain.ScanRequest;
import io.acme.scans.domain.ScanResult;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface SastScanWorkflow {
    @WorkflowMethod
    ScanResult run(ScanRequest request);
}

