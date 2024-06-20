package io.github.qe.powerwall;

import io.github.qe.powerwall.Profiles.NoToken;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(NoToken.class)
public class NoTokenTest {

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
