package io.github.qe.powerwall;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.qe.powerwall.Profiles.Forbidden;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(Forbidden.class)
public class ForbiddenTest {

  @Inject RestClient client;
  @Inject PowerwallStats stats;

  @Test
  void testAppCollect() {
    stats.collect();
  }

  @Test
  void testAppLogin() {
    stats.login();
  }

  @Test
  void testClient() {
    assertAll(
        () -> assertThrows(IllegalStateException.class, () -> client.login(true)),
        () ->
            assertThrows(
                IllegalStateException.class, () -> client.get("system_status/soe", false)));
  }
}
