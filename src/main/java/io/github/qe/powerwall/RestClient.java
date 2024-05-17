package io.github.qe.powerwall;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.mutiny.ext.web.client.predicate.ResponsePredicate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class RestClient {

  private final WebClient client;
  private static final Duration MAX_WAIT = Duration.ofSeconds(30);

  @ConfigProperty(name = "powerwall.gateway.pw")
  private String password;

  @ConfigProperty(name = "powerwall.gateway.login")
  private String email;

  @ConfigProperty(name = "powerwall.gateway.server")
  @Getter
  private String gatewayAddress;

  private static final ResponsePredicate JSON_OR_TEXT =
      ResponsePredicate.contentType(List.of("application/json", "text/plain"));

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
    LoginObject login = LoginObject.builder().email(email).password(password).build();
    JsonObject jsonPayload = JsonObject.mapFrom(login);
    token =
        client
            .postAbs(uri("login/Basic"))
            .expect(ResponsePredicate.SC_SUCCESS)
            .expect(JSON_OR_TEXT) // this is kinda weird should be .expect(ResponsePredicate.JSON)
            .sendJsonObject(jsonPayload)
            .onItem()
            .transform(r -> r.bodyAsJsonObject().getString("token"))
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
            .expect(ResponsePredicate.SC_SUCCESS)
            .expect(ResponsePredicate.JSON)
            .putHeader("Authorization", "Bearer " + token)
            .send()
            .onItem()
            .transform(r -> r.bodyAsJsonObject().getMap())
            .await()
            .atMost(MAX_WAIT);
    logging(forcedLogging, "Scraped {}", api);
    return result;
  }

  private String uri(String uri) {
    return String.format("%s/api/%s", gatewayAddress, uri);
  }

  private void logging(boolean forced, String msg, Object... args) {
    if (forced) {
      log.info(msg, args);
    } else {
      log.debug(msg, args);
    }
  }

  /*
  {
    "clientInfo": {
      "timezone" : "UTC"
    },
    "email": "{email}",
    "password": "{password}",
    "username": "customer"
  }
   */
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder(builderClassName = "Builder")
  @Getter
  @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private static class LoginObject {
    @Setter private String email;
    @Setter private String password;
    private final String username = "customer";
    private final Map<String, String> clientInfo = Map.of("timezone", "UTC");
  }
}
