package io.github.qe.powerwall;

import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.qe.powerwall.Profiles.Forbidden;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(Forbidden.class)
public class ForbiddenTest {

  @Inject StatsCollector collector;
  @Inject PowerwallService service;

  @Test
  void testLogin() {
    service.login();
    assertFalse(service.getLoggedIn());
  }

  @Test
  void testCollect() {
    collector.collect();
  }
}
