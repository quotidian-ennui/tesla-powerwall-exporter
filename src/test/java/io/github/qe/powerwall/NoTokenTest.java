package io.github.qe.powerwall;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.qe.powerwall.Profiles.NoToken;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(NoToken.class)
public class NoTokenTest {

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
    Map<String, Object> result = client.get("system_status/soe", false);
    assertEquals(0, result.size());
  }
}
