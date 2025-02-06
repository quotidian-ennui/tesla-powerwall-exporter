package io.github.qe.powerwall;

import static io.github.qe.powerwall.BasicGauge.buildStats;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.qe.powerwall.model.Aggregate;
import io.github.qe.powerwall.model.Login;
import io.github.qe.powerwall.model.LoginResponse;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

@ApplicationScoped
@JBossLog
public class PowerwallService {
  private final Logger errorLogger = Logger.getLogger("powerwall.transient.errors");
  private static final String LOGIN_STATUS_JSON =
      """
      {
        "loggedin": %s
      }
      """;

  @Getter(AccessLevel.PRIVATE)
  private final String password;

  @Getter(AccessLevel.PRIVATE)
  private final String email;

  private final PowerwallClient client;
  private final ObjectMapper mapper;
  private final AtomicBoolean infoLogging = new AtomicBoolean(true);

  @Getter(AccessLevel.PRIVATE)
  private String token;

  private final AtomicBoolean loggedIn = new AtomicBoolean(false);

  public PowerwallService(
      ObjectMapper m,
      @RestClient PowerwallClient pwSvc,
      @ConfigProperty(name = "powerwall.gateway.login") String email,
      @ConfigProperty(name = "powerwall.gateway.pw") String pw) {
    this.mapper = m;
    this.client = pwSvc;
    this.email = email;
    this.password = pw;
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> getStats() {
    return Optional.ofNullable(attemptQuietly(this::scrapeStats)).orElse(Collections.EMPTY_MAP);
  }

  private Map<String, Object> scrapeStats() {
    Map<String, Object> pwStats = new HashMap<>();
    Aggregate aggregate = client.getAggregates(getToken());
    pwStats.putAll(buildStats(BasicGauge.Metrics.site, aggregate.getSite()));
    pwStats.putAll(buildStats(BasicGauge.Metrics.load, aggregate.getLoad()));
    pwStats.putAll(buildStats(BasicGauge.Metrics.battery, aggregate.getBattery()));
    pwStats.putAll(buildStats(BasicGauge.Metrics.solar, aggregate.getSolar()));
    pwStats.putAll(buildStats(BasicGauge.Metrics.system, client.getSystemStatus(getToken())));
    pwStats.putAll(
        buildStats(BasicGauge.Metrics.percentage, client.getSystemStatusSOE(getToken())));
    logging("Successfully scraped stats");
    logging("Powerwall stats: %s", pwStats);
    infoLogging.set(false);
    return pwStats;
  }

  public String networkInfo() {
    return Optional.ofNullable(attemptQuietly(() -> client.getNetworkInfo(getToken())))
        .orElse("[]");
  }

  @Scheduled(cron = "${powerwall.login.cron:0 15 09 * * ?}")
  public void login() {
    attemptQuietly(
        () -> {
          tryLogin(true);
          return Void.TYPE;
        });
  }

  public boolean getLoggedIn() {
    return loggedIn.get();
  }

  public String loggedIn() {
    return LOGIN_STATUS_JSON.formatted(getLoggedIn());
  }

  private void tryLogin(boolean forced) throws Exception {
    if (BooleanUtils.or(new boolean[] {forced, !getLoggedIn()})) {
      String response = client.login(Login.builder().email(email).password(password).build());
      LoginResponse loginResponse = mapper.readValue(response, LoginResponse.class);
      token = requireNonNull(loginResponse.getToken());
      loggedIn.set(true);
    }
  }

  private void logging(String msg, Object... args) {
    if (infoLogging.get()) {
      log.infof(msg, args);
    } else {
      log.debugf(msg, args);
    }
  }

  private <T> T attemptQuietly(Action<T> r) {
    try {
      tryLogin(false);
      return r.execute();
    } catch (Exception e) {
      loggedIn.set(false);
      infoLogging.set(true);
      errorLogger.error("Failed to scrape powerwall (recoverable)", e);
    }
    return null;
  }

  @FunctionalInterface
  private interface Action<T> {
    T execute() throws Exception;
  }
}
