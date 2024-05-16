package io.github.qe.powerwall;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// Use profiles to be able to start different instances of WireMock
// that behave slightly differently.
// Needs to be looked at in conjunction with PowerwallEndpoint inner classes.
public abstract class Profiles implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.ofEntries(Map.entry("quarkus.scheduler.enabled", "false"));
  }

  public static class Standard extends Profiles {

    @Override
    public List<TestResourceEntry> testResources() {
      return Collections.singletonList(new TestResourceEntry(PowerwallEndpoint.Standard.class));
    }
  }

  public static class NotFound extends Profiles {

    @Override
    public List<TestResourceEntry> testResources() {
      return Collections.singletonList(new TestResourceEntry(PowerwallEndpoint.NotFound.class));
    }
  }

  public static class Forbidden extends Profiles {

    @Override
    public List<TestResourceEntry> testResources() {
      return Collections.singletonList(new TestResourceEntry(PowerwallEndpoint.Forbidden.class));
    }
  }

  public static class NoToken extends Profiles {

    @Override
    public List<TestResourceEntry> testResources() {
      return Collections.singletonList(new TestResourceEntry(PowerwallEndpoint.NoToken.class));
    }
  }
}
