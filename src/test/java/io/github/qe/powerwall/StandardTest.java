package io.github.qe.powerwall;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.qe.powerwall.BasicGauge.Metrics;
import io.github.qe.powerwall.Profiles.Standard;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
// Can't mix and match multiple wiremocks with QuarkusTestResource
// @QuarkusTestResource(PowerwallEndpoint.Standard.class)
@TestProfile(Standard.class)
public class StandardTest {

  @Inject RestClient client;
  @Inject PowerwallStats stats;

  @Test
  void testLogin() {
    assertTrue(client.login(true));
  }

  @Test
  void testSolarMetric() {
    Map<String, Object> result =
        BasicGauge.extract(Metrics.solar, client.get("meters/aggregates", false));
    Metrics.solar.keyMap().keySet().forEach(k -> assertTrue(result.containsKey(k)));
  }

  @Test
  void testSiteMetric() {
    Map<String, Object> result =
        BasicGauge.extract(Metrics.site, client.get("meters/aggregates", false));
    Metrics.site.keyMap().keySet().forEach(k -> assertTrue(result.containsKey(k)));
  }

  @Test
  void testLoadMetric() {
    Map<String, Object> result =
        BasicGauge.extract(Metrics.load, client.get("meters/aggregates", false));
    Metrics.load.keyMap().keySet().forEach(k -> assertTrue(result.containsKey(k)));
  }

  @Test
  void testBatteryMetric() {
    Map<String, Object> result =
        BasicGauge.extract(Metrics.battery, client.get("meters/aggregates", false));
    Metrics.battery.keyMap().keySet().forEach(k -> assertTrue(result.containsKey(k)));
  }

  @Test
  void testSystemStatusMetric() {
    Map<String, Object> result =
        BasicGauge.extract(Metrics.system, client.get("system_status", false));
    Metrics.system.keyMap().keySet().forEach(k -> assertTrue(result.containsKey(k)));
  }

  @Test
  void testPercentageMetric() {
    Map<String, Object> result =
        BasicGauge.extract(Metrics.percentage, client.get("system_status/soe", false));
    Metrics.percentage.keyMap().keySet().forEach(k -> assertTrue(result.containsKey(k)));
  }

  @Test
  void testAppCollect() {
    stats.collect();
  }

  @Test
  void testAppLogin() {
    stats.login();
  }

  @Test
  void testMicrometer() {
    MeterRegistry registry = stats.getRegistry();
    for (Metrics metric : Metrics.values()) {
      for (String key : metric.keyMap().keySet()) {
        assertNotNull(registry.find(key));
        assertNotNull(registry.find(key).gauge());
        assertTrue(registry.find(key).gauge().value() >= 0);
      }
    }
  }
}
