package io.github.qe.powerwall;

import io.github.qe.powerwall.Profiles.Forbidden;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(Forbidden.class)
public class ForbiddenTest {

  @Inject StatsCollector service;

  @Test
  void testLogin() {
    service.login();
  }

  @Test
  void testCollect() {
    service.collect();
  }
}
