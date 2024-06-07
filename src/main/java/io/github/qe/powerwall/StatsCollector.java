package io.github.qe.powerwall;

import static io.github.qe.powerwall.BasicGauge.AGGREGATE_STAT_KEYS;
import static io.github.qe.powerwall.BasicGauge.SOE_KEYS;
import static io.github.qe.powerwall.BasicGauge.SYSTEM_KEYS;
import static io.github.qe.powerwall.BasicGauge.buildStats;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.math.NumberUtils.toDouble;
import static org.apache.commons.text.WordUtils.capitalizeFully;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.qe.powerwall.model.Aggregate;
import io.github.qe.powerwall.model.Login;
import io.github.qe.powerwall.model.LoginResponse;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@Slf4j
@ApplicationScoped
public class StatsCollector {

  private static final Marker TRANSIENT = MarkerFactory.getMarker("TRANSIENT_FAILURE");

  @RestClient private PowerwallService pwSvc;

  @ConfigProperty(name = "powerwall.gateway.pw")
  @Getter(AccessLevel.PRIVATE)
  private String password;

  @ConfigProperty(name = "powerwall.gateway.login")
  @Getter(AccessLevel.PRIVATE)
  private String email;

  @Getter(AccessLevel.PRIVATE)
  private String token;

  private final MeterRegistry registry;

  private final ObjectMapper mapper;

  private boolean loggedIn = false;
  private final Map<String, Object> pwStats = new HashMap<>();

  public StatsCollector(ObjectMapper m, MeterRegistry registry) {
    this.mapper = m;
    this.registry = registry;
    initMicrometer();
  }

  @Scheduled(every = "${powerwall.scrape.interval}")
  void collect() {
    try {
      if (!loggedIn) {
        tryLogin();
      }
      Aggregate aggregate = pwSvc.getAggregates(getToken());
      pwStats.putAll(buildStats("site", aggregate.getSite(), AGGREGATE_STAT_KEYS));
      pwStats.putAll(buildStats("load", aggregate.getLoad(), AGGREGATE_STAT_KEYS));
      pwStats.putAll(buildStats("battery", aggregate.getBattery(), AGGREGATE_STAT_KEYS));
      pwStats.putAll(buildStats("solar", aggregate.getSolar(), AGGREGATE_STAT_KEYS));
      pwStats.putAll(buildStats("powerwall", pwSvc.getSystemStatus(getToken()), SYSTEM_KEYS));
      pwStats.putAll(buildStats("powerwall", pwSvc.getSystemStatusSOE(getToken()), SOE_KEYS));
      log.debug("Successfully scraped stats");
      log.debug("Powerwall stats: {}", pwStats);
    } catch (Exception e) {
      loggedIn = false;
      log.info(TRANSIENT, "Failed to scrape powerwall (recoverable)", e);
    }
  }

  @Scheduled(cron = "${powerwall.login.cron:0 15 09 * * ?}")
  void login() {
    try {
      log.info("Scheduled login refresh");
      tryLogin();
      log.info("Scheduled login successful");
    } catch (Exception e) {
      loggedIn = false;
      log.info(TRANSIENT, "Scheduled Login failed (recoverable)", e);
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
}
