package io.github.qe.powerwall;

import static io.github.qe.powerwall.BasicGauge.buildStats;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.math.NumberUtils.toDouble;
import static org.apache.commons.text.WordUtils.capitalizeFully;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.qe.powerwall.BasicGauge.Metrics;
import io.github.qe.powerwall.model.Aggregate;
import io.github.qe.powerwall.model.Login;
import io.github.qe.powerwall.model.LoginResponse;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.vertx.http.ManagementInterface;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

@ApplicationScoped
@JBossLog
public class StatsCollector {

  private final Logger errorLogger = Logger.getLogger("powerwall.transient.errors");
  private static final String LOGIN_INFO =
      """
  {"loggedin": "%s", "token": "%s"}""";

  @Getter(AccessLevel.PACKAGE)
  private final String password;

  @Getter(AccessLevel.PACKAGE)
  private final String email;

  private final MeterRegistry registry;
  private final PowerwallService pwSvc;
  private final ObjectMapper mapper;
  private final Vertx vertx;

  @Getter(AccessLevel.PRIVATE)
  private String token;

  private boolean loggedIn = false;
  private final Map<String, Object> pwStats = new HashMap<>();
  private final AtomicBoolean infoLogging = new AtomicBoolean(true);

  public StatsCollector(
      ObjectMapper m,
      MeterRegistry registry,
      @RestClient PowerwallService pwSvc,
      @ConfigProperty(name = "powerwall.gateway.login") String email,
      @ConfigProperty(name = "powerwall.gateway.pw") String pw,
      Vertx vertx) {
    this.mapper = m;
    this.registry = registry;
    this.pwSvc = pwSvc;
    this.email = email;
    this.password = pw;
    this.vertx = vertx;
    initMicrometer();
  }

  @Scheduled(every = "${powerwall.scrape.interval}")
  void collect() {
    try {
      if (!loggedIn) {
        tryLogin();
      }
      Aggregate aggregate = pwSvc.getAggregates(getToken());
      pwStats.putAll(buildStats(Metrics.site, aggregate.getSite()));
      pwStats.putAll(buildStats(Metrics.load, aggregate.getLoad()));
      pwStats.putAll(buildStats(Metrics.battery, aggregate.getBattery()));
      pwStats.putAll(buildStats(Metrics.solar, aggregate.getSolar()));
      pwStats.putAll(buildStats(Metrics.system, pwSvc.getSystemStatus(getToken())));
      pwStats.putAll(buildStats(Metrics.percentage, pwSvc.getSystemStatusSOE(getToken())));
      logging("Successfully scraped stats");
      logging("Powerwall stats: %s", pwStats);
      infoLogging.set(false);
    } catch (Exception e) {
      loggedIn = false;
      infoLogging.set(true);
      errorLogger.error("Failed to scrape powerwall (recoverable)", e);
    }
  }

  @Scheduled(cron = "${powerwall.login.cron:0 15 09 * * ?}")
  void login() {
    try {
      log.info("Scheduled login refresh");
      tryLogin();
      log.info("Scheduled login successful");
      infoLogging.set(false);
    } catch (Exception e) {
      loggedIn = false;
      infoLogging.set(true);
      errorLogger.error("Scheduled Login failed (recoverable)", e);
    }
  }

  private void tryLogin() throws Exception {
    String response =
        pwSvc.login(Login.builder().email(getEmail()).password(getPassword()).build());
    LoginResponse loginResponse = mapper.readValue(response, LoginResponse.class);
    token = requireNonNull(loginResponse.getToken());
    loggedIn = true;
  }

  private void initMicrometer() {
    for (String k : BasicGauge.micrometerKeys()) {
      Gauge.builder(k, pwStats, stats -> toDouble(stats.getOrDefault(k, 0).toString(), 0))
          .description(capitalizeFully(k.replace("_", " ")))
          .register(registry);
    }
    for (String key : BasicGauge.micrometerKeys()) {
      requireNonNull(registry.find(key).gauge()).value();
    }
  }

  private void logging(String msg, Object... args) {
    if (infoLogging.get()) {
      log.infof(msg, args);
    } else {
      log.debugf(msg, args);
    }
  }

  @SuppressWarnings("unused")
  public void registerManagementRoutes(@Observes ManagementInterface mi) {
    // VertX router, so we can't block
    mi.router()
        .get("/info/loginStatus")
        .handler(
            rc ->
                rc.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", MediaType.APPLICATION_JSON)
                    .end(String.format(LOGIN_INFO, loggedIn, getToken())));
    mi.router()
        .get("/info/networks")
        .handler(
            rc ->
                vertx.executeBlocking(
                    () -> {
                      rc.response()
                          .setStatusCode(200)
                          .putHeader("Content-Type", MediaType.APPLICATION_JSON)
                          .end(pwSvc.getNetworkInfo(getToken()));
                      return null;
                    }));
  }
}
