package io.github.qe.powerwall;

import static io.github.qe.powerwall.BasicGauge.extract;
import static org.apache.commons.lang3.math.NumberUtils.toDouble;
import static org.apache.commons.text.WordUtils.capitalizeFully;

import io.github.qe.powerwall.BasicGauge.Metrics;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@ApplicationScoped
@Slf4j
public class PowerwallStats {

  private static final Marker TRANSIENT = MarkerFactory.getMarker("TRANSIENT_FAILURE");

  @Inject private RestClient tesla;

  @Getter(AccessLevel.PACKAGE)
  private final MeterRegistry registry;

  private final AtomicBoolean lastFailed = new AtomicBoolean(false);
  private final Map<String, Object> powerwallStats = new HashMap<>();

  PowerwallStats(MeterRegistry registry) {
    this.registry = registry;
    initMicrometer();
  }

  @Scheduled(every = "${powerwall.scrape.interval}")
  void collect() {
    try {
      Map<String, Object> aggregate = tesla.get("meters/aggregates", lastFailed.get());
      Stream.of(Metrics.site, Metrics.load, Metrics.battery, Metrics.solar)
          .map(metrics -> extract(metrics, aggregate))
          .forEach(powerwallStats::putAll);
      powerwallStats.putAll(
          extract(Metrics.percentage, tesla.get("system_status/soe", lastFailed.get())));
      powerwallStats.putAll(extract(Metrics.system, tesla.get("system_status", lastFailed.get())));
      lastFailed.set(false);
    } catch (Exception e) {
      lastFailed.set(true);
      log.info(TRANSIENT, "Failed to scrape powerwall (recoverable)", e);
    }
  }

  @Scheduled(cron = "${powerwall.login.cron:0 15 09 * * ?}")
  void login() {
    try {
      log.info("Scheduled login refresh [{}]", tesla.getGatewayAddress());
      tesla.login(lastFailed.get());
      lastFailed.set(false);
    } catch (Exception e) {
      lastFailed.set(true);
      log.info(TRANSIENT, "Scheduled Login failed (recoverable)", e);
    }
  }

  // Introduced commons-lang3 as a formal dependency to get rid of this
  // CodeQL; it's like left pad
  // @SuppressWarnings({"codeql[java/uncaught-number-format-exception]"})
  private void initMicrometer() {
    for (Metrics metric : Metrics.values()) {
      for (String key : metric.keyMap().keySet()) {
        Gauge.builder(
                key, powerwallStats, stats -> toDouble(stats.getOrDefault(key, 0).toString(), 0))
            .description(capitalizeFully(key.replace("_", " ")))
            .register(registry);
      }
    }
  }
}
