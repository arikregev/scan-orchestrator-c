package io.acme.scans.integrations.checkmarxone.api;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "checkmarx-one")
@Path("/api/uploads")
public interface CxOneUploadsApi {
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    CxOneUploadLinkResponse createUploadLink();
}

