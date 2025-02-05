package io.github.qe.powerwall;

import static org.hamcrest.MatcherAssert.assertThat;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@QuarkusTest
class MgmtInterfaceTest {
  @TestHTTPResource(value = "/management", management = true)
  URL mgmtUrl;

  @Inject StatsCollector service;

  @Test
  void testMgmtLoginInfo() throws Exception {
    String expected =
        String.format(
            "%s://%s:%d/info/loginStatus",
            mgmtUrl.getProtocol(), mgmtUrl.getHost(), mgmtUrl.getPort());
    service.login();
    System.err.println(mgmtUrl);
    System.err.println(expected);
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
    System.err.println(mgmtUrl);
    System.err.println(expected);
    URL targetURL = URI.create(expected).toURL();
    try (InputStream in = targetURL.openStream()) {
      String contents = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      assertThat(contents, Matchers.containsString("ethernet_tesla_internal_default"));
    }
  }
}
