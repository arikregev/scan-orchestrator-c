package io.acme.scans.integrations.checkmarxone.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "checkmarx-one")
@Path("/api/projects")
public interface CxOneProjectsApi {
    /**
     * Best-effort lookup. The actual query params may differ per tenant/version.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    CxOneProjectLookupResponse list(@QueryParam("name") String name);
}

