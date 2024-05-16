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
  private static final String loginJson =
      """
      {
        "clientInfo": {
          "timezone" : "UTC"
        },
        "email": "{email}",
        "password": "{password}",
        "username": "customer"
      }""";

  @ConfigProperty(name = "powerwall.gateway.pw")
  private String password;

  @ConfigProperty(name = "powerwall.gateway.login")
  private String email;

  @ConfigProperty(name = "powerwall.gateway.server")
  @Getter
  private String gatewayAddress;

  private String token;
  private boolean loggedIn = false;

  @Inject
  RestClient(Vertx vertx) {
    WebClientOptions opt =
        new WebClientOptions()
            .setDefaultPort(443)
            .setSsl(true)
            .setTrustAll(true)
            .setVerifyHost(false);
    this.client = WebClient.create(vertx, opt);
  }

  public boolean login(boolean forcedLogging) {
    logging(forcedLogging, "Login to {}", gatewayAddress);
    Buffer buffer =
        Buffer.buffer(
            Qute.fmt(loginJson).data("email", email).data("password", password).render(), "UTF-8");
    token =
        client
            .postAbs(uri("login/Basic"))
            .sendBuffer(buffer)
            .onItem()
            .transform(
                r -> {
                  assertStatus(r.statusCode(), true);
                  return r.bodyAsJsonObject().getString("token");
                })
            .await()
            .atMost(MAX_WAIT);
    loggedIn = token != null;
    logging(
        forcedLogging,
        "Login {}",
        loggedIn ? "[got a token, so success]" : "[did not find a token]");
    return loggedIn;
  }

  public Map<String, Object> get(String api, boolean forcedLogging) {
    if (!loggedIn) login(forcedLogging);
    logging(forcedLogging, "Scraping {}", api);
    // In the event that login doesn't fail (because it returns a 200 + valid json that doesn't
    // contain a token) we continue blithely on and would get a 401/403 at this point
    // or even duff HTML/JSON which might then throw an exception.
    // This isn't correct behaviour but it coincidentally doesn't matter.
    Map<String, Object> result =
        client
            .getAbs(uri(api))
            .putHeader("Authorization", "Bearer " + token)
            .send()
            .onItem()
            .transform(
                r -> {
                  assertStatus(r.statusCode(), false);
                  return r.bodyAsJsonObject().getMap();
                })
            .await()
            .atMost(MAX_WAIT);
    logging(forcedLogging, "Scraped {}", api);
    return result;
  }

  private String uri(String uri) {
    return String.format("%s/api/%s", gatewayAddress, uri);
  }

  private void assertStatus(int httpCode, boolean failOn400) {
    switch (httpCode) {
      case 403, 401 -> {
        loggedIn = false;
        if (failOn400) {
          throw new IllegalStateException("Forbidden " + httpCode);
        }
      }
      case 200 -> {}
      default -> {
        loggedIn = false;
        throw new IllegalStateException("Unexpected Status code " + httpCode);
      }
    }
  }

  private void logging(boolean forced, String msg, Object... args) {
    if (forced) {
      log.info(msg, args);
    } else {
      log.debug(msg, args);
    }
  }
}
