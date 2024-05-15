package io.github.qe.powerwall;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;

public class MockPowerwallEndpoint implements QuarkusTestResourceLifecycleManager {

  private WireMockServer wiremockServer;
  private static final String TOKEN = """
    { "token" : "123456" }
    """;

  // https://github.com/vloschiavo/powerwall2/blob/master/samples/21.44-foogod/running/system_status.soe.json
  private static final String SYSTEM_SOE_JSON = """
    { "percentage": 42.415190953701725 }
    """;

  // https://github.com/vloschiavo/powerwall2/blob/master/samples/21.44-foogod/running/meters.aggregates.json
  // but delete the unsued fields.
  private static final String METERS_JSON = """
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
  private static final String SYSTEM_JSON = """
    {
      "command_source": "Configuration",
      "nominal_full_pack_energy": 14061,
      "nominal_energy_remaining": 5964,
      "inverter_nominal_usable_power": 5800,
      "expected_energy_remaining": 0
    }
    """;

  @Override
  public Map<String, String> start() {
    wiremockServer = new WireMockServer();
    wiremockServer.start();
    wiremockServer.stubFor(post(urlEqualTo("/api/login/Basic")).willReturn(
      aResponse().withHeader("Content-Type", "application/json").withBody(TOKEN)));
    wiremockServer.stubFor(get(urlEqualTo("/api/meters/aggregates")).willReturn(
      aResponse().withHeader("Content-Type", "application/json").withBody(METERS_JSON)));
    wiremockServer.stubFor(get(urlEqualTo("/api/system_status")).willReturn(
      aResponse().withHeader("Content-Type", "application/json").withBody(SYSTEM_JSON)));
    wiremockServer.stubFor(get(urlEqualTo("/api/system_status/soe")).willReturn(
      aResponse().withHeader("Content-Type", "application/json").withBody(SYSTEM_SOE_JSON)));

    return Map.ofEntries(Map.entry("powerwall.gateway.server", wiremockServer.baseUrl()),
      Map.entry("powerwall.gateway.login", "example@example.com"),
      Map.entry("powerwall.gateway.pw", "password"));
  }

  @Override
  public void stop() {
    if (wiremockServer != null) {
      wiremockServer.stop();
    }
  }
}
