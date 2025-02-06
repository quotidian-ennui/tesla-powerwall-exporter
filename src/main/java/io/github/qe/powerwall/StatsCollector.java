package io.github.qe.powerwall;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.math.NumberUtils.toDouble;
import static org.apache.commons.text.WordUtils.capitalizeFully;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.jbosslog.JBossLog;

@ApplicationScoped
@JBossLog
public class StatsCollector {

  private final MeterRegistry registry;
  private final PowerwallService service;
  private final Map<String, Object> pwStats = new HashMap<>();

  public StatsCollector(MeterRegistry registry, PowerwallService pwSvc) {
    this.registry = registry;
    this.service = pwSvc;
    initMicrometer();
  }

  @Scheduled(every = "${powerwall.scrape.interval}")
  void collect() {
    pwStats.putAll(service.getStats());
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
