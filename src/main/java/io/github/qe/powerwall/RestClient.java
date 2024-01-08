package io.github.qe.powerwall;

import io.quarkus.qute.Qute;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class RestClient {

  private final WebClient client;
  private static final Duration MAX_WAIT = Duration.ofSeconds(30);
  private static final String loginJson = """
      {
        "clientInfo": {
          "timezone" : "UTC"
        },
        "email": "{email}",
        "password": "{password}",
        "username": "customer"
      }""";

  @ConfigProperty(name = "TESLA_PASSWORD")
  private String password;
  @ConfigProperty(name = "TESLA_EMAIL")
  private String email;
  @ConfigProperty(name = "TESLA_ADDR")
  @Getter
  private String gatewayAddress;

  private String token;
  private boolean loggedIn = false;

  @Inject
  RestClient(Vertx vertx) {
    WebClientOptions opt = new WebClientOptions().setDefaultPort(443).setSsl(true).setTrustAll(true)
        .setVerifyHost(false);
    this.client = WebClient.create(vertx, opt);
  }

  public void login() {
    log.debug("Login to {}", gatewayAddress);
    Buffer buffer = Buffer.buffer(
        Qute.fmt(loginJson).data("email", email).data("password", password)
            .render(), "UTF-8");
    token = client.postAbs(uri("login/Basic")).sendBuffer(buffer)
        .onItem().transform(r -> {
          assertStatus(r.statusCode());
          return r.bodyAsJsonObject().getString("token");
        }).await().atMost(MAX_WAIT);
    loggedIn = true;
  }

  public Map<String, Object> get(String api) {
    if (!loggedIn) login();
    log.debug("Scraping {}", api);
    return client.getAbs(uri(api)).putHeader("Authorization", "Bearer " + token).send().onItem()
        .transform(r -> {
          assertStatus(r.statusCode());
          return r.bodyAsJsonObject().getMap();
        }).await().atMost(MAX_WAIT);
  }

  private String uri(String uri) {
    return String.format("https://%s/api/%s", gatewayAddress, uri);
  }

  private void assertStatus(int httpCode) {
    switch (httpCode) {
      case 403, 401 -> loggedIn = false;
      case 200 -> { }
      default -> throw new IllegalStateException("Status code " + httpCode);
    }
  }

}
