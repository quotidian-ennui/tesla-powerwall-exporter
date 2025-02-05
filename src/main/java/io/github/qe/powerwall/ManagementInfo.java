package io.github.qe.powerwall;

import io.quarkus.vertx.http.ManagementInterface;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.jbosslog.JBossLog;

@ApplicationScoped
@JBossLog
@RequiredArgsConstructor
public class ManagementInfo {

  private final PowerwallService service;
  private final Vertx vertx;

  @SuppressWarnings("unused")
  public void registerManagementRoutes(@Observes ManagementInterface mi) {
    mi.router()
        .get("/info/loginStatus")
        .handler(
            rc ->
                rc.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", MediaType.APPLICATION_JSON)
                    .end(service.loggedIn()));
    mi.router()
        .get("/info/networks")
        .handler(
            rc ->
                vertx.executeBlocking(
                    () -> {
                      rc.response()
                          .setStatusCode(200)
                          .putHeader("Content-Type", MediaType.APPLICATION_JSON)
                          .end(service.networkInfo());
                      return null;
                    }));
  }
}
