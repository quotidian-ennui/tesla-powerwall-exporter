package io.github.qe.powerwall;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.github.qe.powerwall.BasicGauge.Metrics;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(MockPowerwallEndpoint.class)
public class PowerwallTest {

  @Inject
  RestClient client;
  @Inject
  PowerwallStats stats;

  @Test
  void testLogin() {
    assertTrue(client.login(true));
  }

  @Test
  void testSolarMetric() {
    Map<String, Object> result = BasicGauge.extract(Metrics.solar, client.get("meters/aggregates", false));
    Metrics.solar.keyMap().keySet().forEach(
      k -> assertTrue(result.containsKey(k))
    );
  }

  @Test
  void testSiteMetric() {
    Map<String, Object> result = BasicGauge.extract(Metrics.site, client.get("meters/aggregates", false));
    Metrics.site.keyMap().keySet().forEach(
      k -> assertTrue(result.containsKey(k))
    );
  }

  @Test
  void testLoadMetric() {
    Map<String, Object> result = BasicGauge.extract(Metrics.load, client.get("meters/aggregates", false));
    Metrics.load.keyMap().keySet().forEach(
      k -> assertTrue(result.containsKey(k))
    );
  }

  @Test
  void testBatteryMetric() {
    Map<String, Object> result = BasicGauge.extract(Metrics.battery, client.get("meters/aggregates", false));
    Metrics.battery.keyMap().keySet().forEach(
      k -> assertTrue(result.containsKey(k))
    );
  }


  @Test
  void testSystemStatusMetric() {
    Map<String, Object> result = BasicGauge.extract(Metrics.system, client.get("system_status", false));
    Metrics.system.keyMap().keySet().forEach(
      k -> assertTrue(result.containsKey(k))
    );
  }


  @Test
  void testPercentageMetric() {
    Map<String, Object> result = BasicGauge.extract(Metrics.percentage, client.get("system_status/soe", false));
    Metrics.percentage.keyMap().keySet().forEach(
      k -> assertTrue(result.containsKey(k))
    );
  }

  @Test
  void testAppCollect() {
    stats.collect();
  }

  @Test
  void testAppLogin() {
    stats.login();
  }


}
