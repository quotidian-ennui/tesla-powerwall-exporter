package io.github.qe.powerwall;

import static org.apache.commons.text.WordUtils.capitalizeFully;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class PowerwallStats {

  @Inject
  private RestClient tesla;
  private final MeterRegistry registry;

  private final Map<String, Object> powerwallStats = new HashMap<>();
  private static final List<String> AGGREGATE_STAT_KEYS = List.of("instant_power",
      "instant_reactive_power", "instant_apparent_power", "frequency", "energy_exported",
      "energy_imported", "instant_average_voltage", "instant_average_current",
      "instant_total_current");

  private enum Metrics {
    battery {},
    site {},
    solar {},
    load {},
    percentage {
      public Map<String, Object> navigateTo(Map<String, Object> stats) {
        return stats;
      }

      @Override
      public Map<String, String> keyMap() {
        return Collections.singletonMap("tesla_powerwall_state_of_charge_percentage", "percentage");
      }
    },
    system {
      public Map<String, Object> navigateTo(Map<String, Object> stats) {
        return stats;
      }
      @Override
      public Map<String, String> keyMap() {
        return Map.ofEntries(
            Map.entry("tesla_powerwall_nominal_energy_remaining", "nominal_energy_remaining"),
            Map.entry("tesla_powerwall_nominal_full_pack_energy", "nominal_full_pack_energy"));
      }
    };

    public Map<String, String> keyMap() {
      return AGGREGATE_STAT_KEYS.stream()
          .map(s -> Map.entry(String.format("tesla_%s_%s", name(), s), s))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> navigateTo(Map<String, Object> stats) {
      return (Map<String, Object>) stats.get(name());
    }
  }

  PowerwallStats(MeterRegistry registry) {
    this.registry = registry;
    initMicrometer();
  }

  @Scheduled(every = "{powerwall.scrape.interval}")
  void collect() {
    Map<String, Object> aggregate = tesla.get("meters/aggregates");
    powerwallStats.putAll(extract(Metrics.site, aggregate));
    powerwallStats.putAll(extract(Metrics.load, aggregate));
    powerwallStats.putAll(extract(Metrics.battery, aggregate));
    powerwallStats.putAll(extract(Metrics.solar, aggregate));
    powerwallStats.putAll(extract(Metrics.percentage, tesla.get("system_status/soe")));
    powerwallStats.putAll(extract(Metrics.system, tesla.get("system_status")));
  }

  @Scheduled(cron = "${powerwall.login.cron:0 15 09 * * ?}")
  void login() {
    tesla.login();
  }

  private void initMicrometer() {
    for (Metrics metric : Metrics.values()) {
      for (String key : metric.keyMap().keySet()) {
        Gauge.builder(key, powerwallStats,
                (stats) -> Double.parseDouble(stats.getOrDefault(key, 0).toString()))
            .description(capitalizeFully(key.replace("_", " ")))
            .register(registry);
      }
    }
  }

  private Map<String, Object> extract(Metrics metrics, Map<String, Object> stats) {
    Map<String, Object> result = new HashMap<>();
    Map<String,Object> item = metrics.navigateTo(stats);
    for (Map.Entry<String, String> entry : metrics.keyMap().entrySet()) {
      if (item.containsKey(entry.getValue())) {
        result.put(entry.getKey(), item.get(entry.getValue()));
      }
    }
    return result;
  }

}
