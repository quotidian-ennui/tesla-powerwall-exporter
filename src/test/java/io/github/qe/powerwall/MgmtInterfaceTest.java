package io.github.qe.powerwall;

import static org.hamcrest.MatcherAssert.assertThat;

import io.github.qe.powerwall.Profiles.Standard;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

@QuarkusTest
@TestProfile(Standard.class)
@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
class MgmtInterfaceTest {
  @TestHTTPResource(value = "/management", management = true)
  URL mgmtUrl;

  @Inject PowerwallService service;
  @Inject ManagementInfo info;

  @Test
  void testMgmtLoginInfo() throws Exception {
    String expected =
        String.format(
            "%s://%s:%d/info/loginStatus",
            mgmtUrl.getProtocol(), mgmtUrl.getHost(), mgmtUrl.getPort());
    service.login();
    URL targetURL = URI.create(expected).toURL();
    try (InputStream in = targetURL.openStream()) {
      String contents = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      assertThat(contents, Matchers.containsString("loggedin"));
    }
  }

  @Test
  void testMgmtNetworks() throws Exception {
    String expected =
        String.format(
            "%s://%s:%d/info/networks",
            mgmtUrl.getProtocol(), mgmtUrl.getHost(), mgmtUrl.getPort());
    service.login();
    URL targetURL = URI.create(expected).toURL();
    try (InputStream in = targetURL.openStream()) {
      String contents = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      assertThat(contents, Matchers.containsString("ethernet_tesla_internal_default"));
    }
  }
}
