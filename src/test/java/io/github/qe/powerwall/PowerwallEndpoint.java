package io.github.qe.powerwall;

import static com.github.tomakehurst.wiremock.client.WireMock.forbidden;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;

public abstract class PowerwallEndpoint implements QuarkusTestResourceLifecycleManager {
  public static final String TOKEN = """
    { "token" : "123456" }
    """;

  // https://github.com/vloschiavo/powerwall2/blob/master/samples/21.44-foogod/running/system_status.soe.json
  public static final String SYSTEM_SOE_JSON = """
    { "percentage": 42.415190953701725 }
    """;

  // https://github.com/vloschiavo/powerwall2/blob/master/samples/21.44-foogod/running/meters.aggregates.json
  // but delete the unsued fields.
  public static final String METERS_JSON = """
    {
      "site": {
        "instant_power": 72,
        "instant_reactive_power": -329,
        "instant_apparent_power": 336.7862823809782,
        "frequency": 0,
        "energy_exported": 348463.5610710742,
        "energy_imported": 752045.9551097859,
        "instant_average_voltage": 213.65712736765886,
        "instant_average_current": 0,
        "instant_total_current": 0
      },
      "battery": {
        "instant_power": -60,
        "instant_reactive_power": 30,
        "instant_apparent_power": 67.08203932499369,
        "frequency": 60.007000000000005,
        "energy_exported": 567740,
        "energy_imported": 642500,
        "instant_average_voltage": 246.8,
        "instant_average_current": 0,
        "instant_total_current": 0
      },
      "load": {
        "instant_power": 1154,
        "instant_reactive_power": -318.75,
        "instant_apparent_power": 1197.2124132751046,
        "frequency": 0,
        "energy_exported": 0,
        "energy_imported": 1595342.4463185878,
        "instant_average_voltage": 213.65712736765886,
        "instant_average_current": 5.401177176805384,
        "instant_total_current": 5.401177176805384
      },
      "solar": {
        "instant_power": 1144,
        "instant_reactive_power": -14,
        "instant_apparent_power": 1144.0856611285712,
        "frequency": 0,
        "energy_exported": 1269175.54256601,
        "energy_imported": 2655.4902861339488,
        "instant_average_voltage": 212.17622392718746,
        "instant_average_current": 0,
        "instant_total_current": 0
      }
    }
    """;

  // cut down from
  // https://github.com/vloschiavo/powerwall2/blob/master/samples/21.44-foogod/running/system_status.json
  public static final String SYSTEM_JSON = """
    {
      "command_source": "Configuration",
      "nominal_full_pack_energy": 14061,
      "nominal_energy_remaining": 5964,
      "inverter_nominal_usable_power": 5800,
      "expected_energy_remaining": 0
    }
    """;

  protected WireMockServer wiremockServer;

  @Override
  public void stop() {
    if (wiremockServer != null) {
      wiremockServer.stop();
    }
  }

  @Override
  public Map<String, String> start() {
    WireMockConfiguration cfg = WireMockConfiguration.wireMockConfig().templatingEnabled(true)
        .dynamicHttpsPort().dynamicPort();
    wiremockServer = new WireMockServer(cfg);
    wiremockServer.start();
    configureWiremock();
    return Map.ofEntries(Map.entry("powerwall.gateway.server", wiremockServer.baseUrl()),
        Map.entry("powerwall.gateway.login", "example@example.com"),
        Map.entry("powerwall.gateway.pw", "password"),
        Map.entry("quarkus.scheduler.enabled", "false")
    );
  }

  protected abstract void configureWiremock();

  public static class Standard extends PowerwallEndpoint {
    @Override
    protected void configureWiremock() {
      wiremockServer.givenThat(post(urlEqualTo("/api/login/Basic"))
          .willReturn(okJson(TOKEN)));
      wiremockServer.givenThat(get(urlEqualTo("/api/meters/aggregates")).willReturn(
          okJson(METERS_JSON)));
      wiremockServer.givenThat(get(urlEqualTo("/api/system_status")).willReturn(
          okJson(SYSTEM_JSON)));
      wiremockServer.givenThat(get(urlEqualTo("/api/system_status/soe")).willReturn(
          okJson(SYSTEM_SOE_JSON)));
    }
  }

  // Mostly work but will return 404 when we get the state of energy.
  public static class NotFound extends PowerwallEndpoint {
    @Override
    protected void configureWiremock() {
      wiremockServer.givenThat(post(urlEqualTo("/api/login/Basic"))
          .willReturn(okJson(TOKEN)));
      wiremockServer.givenThat(get(urlEqualTo("/api/meters/aggregates")).willReturn(
          okJson(METERS_JSON)));
      wiremockServer.givenThat(get(urlEqualTo("/api/system_status")).willReturn(
          okJson(SYSTEM_JSON)));
      wiremockServer.givenThat(get(urlEqualTo("/api/system_status/soe")).willReturn(
          notFound()));
    }
  }

  // Nothing works because you cannot login.
  public static class Forbidden extends PowerwallEndpoint {
    @Override
    protected void configureWiremock() {
      wiremockServer.givenThat(post(urlEqualTo("/api/login/Basic"))
          .willReturn(forbidden()));
    }
  }
}
