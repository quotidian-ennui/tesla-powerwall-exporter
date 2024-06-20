package io.github.qe.powerwall;

import io.github.qe.powerwall.Profiles.NotFound;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(NotFound.class)
public class NotFoundTest {

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
