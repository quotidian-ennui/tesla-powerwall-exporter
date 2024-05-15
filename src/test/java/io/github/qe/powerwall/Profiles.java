package io.github.qe.powerwall;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

  // @see PowerwallEndpoint.SemiBroken
  public static class NotFound extends Profiles  {

    @Override
    public List<TestResourceEntry> testResources() {
      return Collections.singletonList(new TestResourceEntry(PowerwallEndpoint.NotFound.class));
    }
  }

  public static class Forbidden extends Profiles  {
    @Override
    public List<TestResourceEntry> testResources() {
      return Collections.singletonList(new TestResourceEntry(PowerwallEndpoint.Forbidden.class));
    }
  }

}
