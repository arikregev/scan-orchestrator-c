package io.acme.scans.ingress.kafka;

import io.acme.scans.domain.ScanRequest;
import io.acme.scans.domain.ScanTool;
import io.acme.scans.ingress.ocsf.proto.ScanActivity;
import io.acme.scans.temporal.client.ScanWorkflowSubmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class KafkaScanActivityConsumer {
    private static final Logger LOG = Logger.getLogger(KafkaScanActivityConsumer.class);

    private final OcsfScanActivityMapper mapper;
    private final Validator validator;
    private final ScanToolRouter router;
    private final ScanWorkflowSubmitter submitter;

    public KafkaScanActivityConsumer(
            OcsfScanActivityMapper mapper,
            Validator validator,
            ScanToolRouter router,
            ScanWorkflowSubmitter submitter
    ) {
        this.mapper = mapper;
        this.validator = validator;
        this.router = router;
        this.submitter = submitter;
    }

    @Incoming("ocsf-scan-activity")
    public void onMessage(ScanActivity payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Empty OCSF ScanActivity message");
        }
        ScanRequest request = mapper.fromProto(payload);

        Set<ConstraintViolation<ScanRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String msg = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Invalid ScanRequest: " + msg);
        }

        ScanTool tool = router.resolveTool(request);
        LOG.infov("Submitting scan workflow. tool={0}, appId={1}, component={2}, buildId={3}, originalEventUid={4}",
                tool, request.appId(), request.componentName(), request.buildId(), request.originalEventUid());

        submitter.submit(request);
    }
}

