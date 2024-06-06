package io.github.qe.powerwall;

import io.github.qe.powerwall.Profiles.Standard;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(Standard.class)
public class StatsCollectorTest {

  @Inject StatsCollector service;

  @Test
  void testCollect() {
    service.collect();
    service.collect();
  }
}
