package io.github.qe.powerwall;

import io.github.qe.powerwall.model.Aggregate;
import io.github.qe.powerwall.model.Login;
import io.quarkus.rest.client.reactive.NotBody;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(baseUri = "stork://powerwall-api")
@Path("/api")
public interface PowerwallClient {

  // Uses string because I can't seem to get rest client to convert
  // the response to a POJO since the response is marked as text/plain
  @Path("/login/Basic")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
  String login(Login login);

  @Path("/meters/aggregates")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @ClientHeaderParam(name = "Authorization", value = "Bearer {token}")
  Aggregate getAggregates(@NotBody String token);

  @Path("/system_status")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @ClientHeaderParam(name = "Authorization", value = "Bearer {token}")
  Map<String, Object> getSystemStatus(@NotBody String token);

  @Path("/system_status/soe")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @ClientHeaderParam(name = "Authorization", value = "Bearer {token}")
  Map<String, Object> getSystemStatusSOE(@NotBody String token);

  // Uses string because the response is marked as text/plain
  @Path("/networks")
  @GET
  @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
  @ClientHeaderParam(name = "Authorization", value = "Bearer {token}")
  String getNetworkInfo(@NotBody String token);
}
