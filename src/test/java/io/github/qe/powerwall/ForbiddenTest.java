package io.github.qe.powerwall;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.qe.powerwall.Profiles.Forbidden;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.vertx.core.impl.NoStackTraceThrowable;
import jakarta.inject.Inject;
import java.util.concurrent.CompletionException;
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
  void testClientLogin() {
    CompletionException e1 = assertThrows(CompletionException.class, () -> client.login(true));
    assertInstanceOf(NoStackTraceThrowable.class, e1.getCause());
  }

  @Test
  void testClientGet() {
    CompletionException e =
        assertThrows(CompletionException.class, () -> client.get("system_status/soe", false));
    assertInstanceOf(NoStackTraceThrowable.class, e.getCause());
    //    assertThrows(IllegalStateException.class, () -> client.get("system_status/soe", false));
  }
}
