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
import io.quarkus.arc.log.LoggerName;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

@ApplicationScoped
@Slf4j
public class StatsCollector {

  // This is the easy way to get stop log.error() from being
  // emitted to the console when we run gradle test since we
  // can turn off this logging in the %test profile.
  @Inject
  @LoggerName("powerwall.transient.errors")
  private Logger elog;

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
  private final AtomicBoolean infoLogging = new AtomicBoolean(true);

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
      pwStats.putAll(buildStats(Metrics.site, aggregate.getSite()));
      pwStats.putAll(buildStats(Metrics.load, aggregate.getLoad()));
      pwStats.putAll(buildStats(Metrics.battery, aggregate.getBattery()));
      pwStats.putAll(buildStats(Metrics.solar, aggregate.getSolar()));
      pwStats.putAll(buildStats(Metrics.system, pwSvc.getSystemStatus(getToken())));
      pwStats.putAll(buildStats(Metrics.percentage, pwSvc.getSystemStatusSOE(getToken())));
      logging("Successfully scraped stats");
      logging("Powerwall stats: {}", pwStats);
      infoLogging.set(false);
    } catch (Exception e) {
      loggedIn = false;
      infoLogging.set(true);
      elog.error("Failed to scrape powerwall (recoverable)", e);
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
      elog.error("Scheduled Login failed (recoverable)", e);
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
      log.info(msg, args);
    } else {
      log.debug(msg, args);
    }
  }
}
