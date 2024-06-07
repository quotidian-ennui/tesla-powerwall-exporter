package io.github.qe.powerwall;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BasicGauge {

  private enum Metrics {
    battery {},
    site {},
    solar {},
    load {},
    percentage {
      @Override
      public List<String> keys() {
        return List.of("tesla_powerwall_percentage");
      }
    },
    system {
      @Override
      public List<String> keys() {
        return List.of(
            "tesla_powerwall_nominal_energy_remaining", "tesla_powerwall_nominal_full_pack_energy");
      }
    };

    public List<String> keys() {
      return AGGREGATE_STAT_KEYS.stream()
          .map(s -> String.format("tesla_%s_%s", name(), s))
          .collect(Collectors.toList());
    }
  }

  public static final List<String> SYSTEM_KEYS =
      List.of("nominal_energy_remaining", "nominal_full_pack_energy");

  public static final List<String> SOE_KEYS = List.of("percentage");

  public static final List<String> AGGREGATE_STAT_KEYS =
      List.of(
          "instant_power",
          "instant_reactive_power",
          "instant_apparent_power",
          "frequency",
          "energy_exported",
          "energy_imported",
          "instant_average_voltage",
          "instant_average_current",
          "instant_total_current");

  public static Map<String, Object> buildStats(
      String name, Map<String, Object> stats, List<String> keys) {
    Map<String, String> keyMap =
        keys.stream()
            .map(s -> Map.entry(String.format("tesla_%s_%s", name, s), s))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    return keyMap.entrySet().stream()
        .filter(entry -> stats.containsKey(entry.getValue()))
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> stats.get(entry.getValue())));
  }

  public static List<String> micrometerKeys() {
    return Arrays.stream(Metrics.values())
        .flatMap(m -> m.keys().stream())
        .collect(Collectors.toList());
  }
}
