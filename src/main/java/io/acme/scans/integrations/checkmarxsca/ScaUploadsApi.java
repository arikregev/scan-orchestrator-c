package io.acme.scans.integrations.checkmarxsca;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "checkmarx-sca")
@Path("/api/uploads")
public interface ScaUploadsApi {
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    ScaUploadsResponse createUpload();
}

