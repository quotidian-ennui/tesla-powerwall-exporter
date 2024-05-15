package io.github.qe.powerwall;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access= AccessLevel.PRIVATE)
public class BasicGauge {
  public enum Metrics {
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

  private static final List<String> AGGREGATE_STAT_KEYS = List.of("instant_power",
    "instant_reactive_power", "instant_apparent_power", "frequency", "energy_exported",
    "energy_imported", "instant_average_voltage", "instant_average_current",
    "instant_total_current");

  public static Map<String, Object> extract(Metrics metrics, Map<String, Object> stats) {
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
