package io.github.qe.powerwall;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.qe.powerwall.Profiles.NotFound;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(NotFound.class)
public class NotFoundTest {

  @Inject StatsCollector collector;
  @Inject PowerwallService service;

  @Test
  void testLogin() {
    service.login();
    assertTrue(service.getLoggedIn());
  }

  @Test
  void testCollect() {
    collector.collect();
  }
}
