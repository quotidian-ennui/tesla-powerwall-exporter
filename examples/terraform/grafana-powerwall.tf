resource "kubernetes_config_map_v1" "powerwall_grafana" {
  metadata {
    namespace = kubernetes_namespace_v1.tooling.metadata[0].name
    name      = "powerwall"
    labels = {
      "grafana_dashboard"         = "1"
      "app.kubernetes.io/part-of" = "powerwall-metrics"
    }
  }

  data = {
    "powerwall.json" = <<EOF
{
  "annotations": {
    "list": [
      {
        "builtIn": 1,
        "datasource": {
          "type": "grafana",
          "uid": "-- Grafana --"
        },
        "enable": true,
        "hide": true,
        "iconColor": "rgba(0, 211, 255, 1)",
        "name": "Annotations & Alerts",
        "target": {
          "limit": 100,
          "matchAny": false,
          "tags": [],
          "type": "dashboard"
        },
        "type": "dashboard"
      }
    ]
  },
  "editable": true,
  "fiscalYearStartMonth": 0,
  "graphTooltip": 0,
  "links": [],
  "liveNow": false,
  "panels": [
    {
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "thresholds"
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "yellow",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          },
          "unit": "kwatth"
        },
        "overrides": [
          {
            "matcher": {
              "id": "byName",
              "options": "Value"
            },
            "properties": [
              {
                "id": "displayName",
                "value": "Solar"
              }
            ]
          }
        ]
      },
      "gridPos": {
        "h": 4,
        "w": 2,
        "x": 0,
        "y": 0
      },
      "id": 3,
      "options": {
        "colorMode": "value",
        "graphMode": "none",
        "justifyMode": "auto",
        "orientation": "auto",
        "reduceOptions": {
          "calcs": [],
          "fields": "",
          "values": false
        },
        "textMode": "auto"
      },
      "pluginVersion": "9.2.4",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "editorMode": "code",
          "exemplar": false,
          "expr": "round(tesla_solar_instant_power / 1000, 0.1) ",
          "format": "table",
          "instant": true,
          "legendFormat": "Solar",
          "range": false,
          "refId": "A"
        }
      ],
      "title": "Solar Generation",
      "transformations": [
        {
          "id": "merge",
          "options": {}
        }
      ],
      "type": "stat"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "thresholds"
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          },
          "unit": "kwatth"
        },
        "overrides": [
          {
            "matcher": {
              "id": "byName",
              "options": "Value"
            },
            "properties": [
              {
                "id": "displayName",
                "value": "Battery"
              }
            ]
          }
        ]
      },
      "gridPos": {
        "h": 4,
        "w": 2,
        "x": 2,
        "y": 0
      },
      "id": 10,
      "options": {
        "colorMode": "value",
        "graphMode": "none",
        "justifyMode": "auto",
        "orientation": "auto",
        "reduceOptions": {
          "calcs": [],
          "fields": "",
          "values": false
        },
        "textMode": "auto"
      },
      "pluginVersion": "9.2.4",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "editorMode": "code",
          "exemplar": false,
          "expr": "round(tesla_battery_instant_power / 1000, 0.1) ",
          "format": "table",
          "instant": true,
          "legendFormat": "Battery",
          "range": false,
          "refId": "A"
        }
      ],
      "title": "Battery Draw",
      "transformations": [
        {
          "id": "merge",
          "options": {}
        }
      ],
      "type": "stat"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "thresholds"
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "blue",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          },
          "unit": "kwatth"
        },
        "overrides": [
          {
            "matcher": {
              "id": "byName",
              "options": "Value"
            },
            "properties": [
              {
                "id": "displayName",
                "value": "Power"
              }
            ]
          }
        ]
      },
      "gridPos": {
        "h": 4,
        "w": 2,
        "x": 4,
        "y": 0
      },
      "id": 9,
      "options": {
        "colorMode": "value",
        "graphMode": "none",
        "justifyMode": "auto",
        "orientation": "auto",
        "reduceOptions": {
          "calcs": [],
          "fields": "",
          "values": false
        },
        "textMode": "auto"
      },
      "pluginVersion": "9.2.4",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "editorMode": "code",
          "exemplar": false,
          "expr": "round(tesla_load_instant_power / 1000, 0.1) ",
          "format": "table",
          "instant": true,
          "legendFormat": "Power",
          "range": false,
          "refId": "A"
        }
      ],
      "title": "Usage",
      "transformations": [
        {
          "id": "merge",
          "options": {}
        }
      ],
      "type": "stat"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "thresholds"
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "red",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          },
          "unit": "kwatth"
        },
        "overrides": [
          {
            "matcher": {
              "id": "byName",
              "options": "Value"
            },
            "properties": [
              {
                "id": "displayName",
                "value": "Grid"
              }
            ]
          }
        ]
      },
      "gridPos": {
        "h": 4,
        "w": 2,
        "x": 6,
        "y": 0
      },
      "id": 8,
      "options": {
        "colorMode": "value",
        "graphMode": "none",
        "justifyMode": "auto",
        "orientation": "auto",
        "reduceOptions": {
          "calcs": [],
          "fields": "",
          "values": false
        },
        "textMode": "auto"
      },
      "pluginVersion": "9.2.4",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "editorMode": "code",
          "exemplar": false,
          "expr": "round(tesla_site_instant_power / 1000, 0.1) ",
          "format": "table",
          "instant": true,
          "legendFormat": "Grid",
          "range": false,
          "refId": "A"
        }
      ],
      "title": "Grid",
      "transformations": [
        {
          "id": "merge",
          "options": {}
        }
      ],
      "type": "stat"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "description": "Battery has a hard floor of 5% charge",
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "thresholds"
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "purple",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          },
          "unit": "percent"
        },
        "overrides": [
          {
            "matcher": {
              "id": "byName",
              "options": "Value"
            },
            "properties": [
              {
                "id": "displayName",
                "value": "Battery %"
              }
            ]
          }
        ]
      },
      "gridPos": {
        "h": 4,
        "w": 2,
        "x": 8,
        "y": 0
      },
      "id": 5,
      "options": {
        "colorMode": "value",
        "graphMode": "none",
        "justifyMode": "auto",
        "orientation": "auto",
        "reduceOptions": {
          "calcs": [],
          "fields": "",
          "values": false
        },
        "textMode": "auto"
      },
      "pluginVersion": "9.2.4",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "editorMode": "code",
          "exemplar": false,
          "expr": "tesla_powerwall_state_of_charge_percentage",
          "format": "table",
          "instant": true,
          "legendFormat": "Charge",
          "range": false,
          "refId": "A"
        }
      ],
      "title": "Battery",
      "transformations": [],
      "type": "stat"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "description": "The battery has a hard floor of 5% so this value will always ~700",
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "thresholds"
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "purple",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          },
          "unit": "kwatth"
        },
        "overrides": [
          {
            "matcher": {
              "id": "byName",
              "options": "Value"
            },
            "properties": [
              {
                "id": "displayName",
                "value": "Charge"
              }
            ]
          }
        ]
      },
      "gridPos": {
        "h": 4,
        "w": 2,
        "x": 10,
        "y": 0
      },
      "id": 13,
      "options": {
        "colorMode": "value",
        "graphMode": "none",
        "justifyMode": "auto",
        "orientation": "auto",
        "reduceOptions": {
          "calcs": [],
          "fields": "",
          "values": false
        },
        "textMode": "auto"
      },
      "pluginVersion": "9.2.4",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "editorMode": "code",
          "exemplar": false,
          "expr": "tesla_powerwall_nominal_energy_remaining / 1000",
          "format": "table",
          "instant": true,
          "legendFormat": "Charge",
          "range": false,
          "refId": "A"
        }
      ],
      "title": "Stored",
      "transformations": [],
      "type": "stat"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "description": "Shows us the wear, max is ~14kWh",
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "thresholds"
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "purple",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          },
          "unit": "kwatth"
        },
        "overrides": [
          {
            "matcher": {
              "id": "byName",
              "options": "Value"
            },
            "properties": [
              {
                "id": "displayName",
                "value": "Charge"
              }
            ]
          }
        ]
      },
      "gridPos": {
        "h": 4,
        "w": 2,
        "x": 12,
        "y": 0
      },
      "id": 14,
      "options": {
        "colorMode": "value",
        "graphMode": "none",
        "justifyMode": "auto",
        "orientation": "auto",
        "reduceOptions": {
          "calcs": [],
          "fields": "",
          "values": false
        },
        "textMode": "auto"
      },
      "pluginVersion": "9.2.4",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "editorMode": "code",
          "exemplar": false,
          "expr": "tesla_powerwall_nominal_full_pack_energy / 1000",
          "format": "table",
          "instant": true,
          "legendFormat": "Charge",
          "range": false,
          "refId": "A"
        }
      ],
      "title": "Capacity",
      "transformations": [],
      "type": "stat"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "palette-classic"
          },
          "custom": {
            "axisCenteredZero": false,
            "axisColorMode": "text",
            "axisLabel": "",
            "axisPlacement": "auto",
            "barAlignment": 0,
            "drawStyle": "line",
            "fillOpacity": 0,
            "gradientMode": "none",
            "hideFrom": {
              "legend": false,
              "tooltip": false,
              "viz": false
            },
            "lineInterpolation": "linear",
            "lineWidth": 1,
            "pointSize": 5,
            "scaleDistribution": {
              "type": "linear"
            },
            "showPoints": "auto",
            "spanNulls": false,
            "stacking": {
              "group": "A",
              "mode": "none"
            },
            "thresholdsStyle": {
              "mode": "off"
            }
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          },
          "unit": "kwatth"
        },
        "overrides": [
          {
            "matcher": {
              "id": "byFrameRefID",
              "options": "Battery"
            },
            "properties": [
              {
                "id": "displayName",
                "value": "Battery"
              }
            ]
          },
          {
            "matcher": {
              "id": "byFrameRefID",
              "options": "Solar"
            },
            "properties": [
              {
                "id": "displayName",
                "value": "Solar"
              }
            ]
          },
          {
            "matcher": {
              "id": "byFrameRefID",
              "options": "Grid"
            },
            "properties": [
              {
                "id": "displayName",
                "value": "Grid"
              }
            ]
          },
          {
            "matcher": {
              "id": "byFrameRefID",
              "options": "Load"
            },
            "properties": [
              {
                "id": "displayName",
                "value": "House"
              }
            ]
          }
        ]
      },
      "gridPos": {
        "h": 8,
        "w": 24,
        "x": 0,
        "y": 4
      },
      "id": 12,
      "options": {
        "legend": {
          "calcs": [],
          "displayMode": "list",
          "placement": "bottom",
          "showLegend": true
        },
        "tooltip": {
          "mode": "single",
          "sort": "none"
        }
      },
      "pluginVersion": "9.2.4",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "editorMode": "code",
          "exemplar": true,
          "expr": "tesla_battery_instant_power / 1000",
          "format": "table",
          "legendFormat": "Battery",
          "range": true,
          "refId": "Battery"
        },
        {
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "editorMode": "code",
          "exemplar": true,
          "expr": "tesla_solar_instant_power / 1000",
          "format": "table",
          "hide": false,
          "legendFormat": "Solar",
          "range": true,
          "refId": "Solar"
        },
        {
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "editorMode": "code",
          "exemplar": true,
          "expr": "tesla_site_instant_power / 1000",
          "format": "table",
          "hide": false,
          "legendFormat": "Grid",
          "range": true,
          "refId": "Grid"
        },
        {
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "editorMode": "code",
          "exemplar": true,
          "expr": "tesla_load_instant_power / 1000",
          "format": "table",
          "hide": false,
          "legendFormat": "Load",
          "range": true,
          "refId": "Load"
        }
      ],
      "title": "Powerwall Overview",
      "type": "timeseries"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "prometheus"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "palette-classic"
          },
          "custom": {
            "axisCenteredZero": false,
            "axisColorMode": "text",
            "axisLabel": "",
            "axisPlacement": "auto",
            "barAlignment": 0,
            "drawStyle": "line",
            "fillOpacity": 31,
            "gradientMode": "opacity",
            "hideFrom": {
              "legend": false,
              "tooltip": false,
              "viz": false
            },
            "lineInterpolation": "smooth",
            "lineStyle": {
              "fill": "solid"
            },
            "lineWidth": 1,
            "pointSize": 5,
            "scaleDistribution": {
              "type": "linear"
            },
            "showPoints": "auto",
            "spanNulls": false,
            "stacking": {
              "group": "A",
              "mode": "none"
            },
            "thresholdsStyle": {
              "mode": "off"
            }
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          },
          "unit": "kwatt"
        },
        "overrides": [
          {
            "matcher": {
              "id": "byName",
              "options": "Value"
            },
            "properties": [
              {
                "id": "displayName",
                "value": "Solar"
              }
            ]
          }
        ]
      },
      "gridPos": {
        "h": 8,
        "w": 24,
        "x": 0,
        "y": 12
      },
      "id": 7,
      "options": {
        "legend": {
          "calcs": [],
          "displayMode": "list",
          "placement": "bottom",
          "showLegend": false
        },
        "tooltip": {
          "mode": "single",
          "sort": "none"
        }
      },
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "prometheus"
          },
          "editorMode": "code",
          "exemplar": true,
          "expr": "round(tesla_solar_instant_power / 1000, 0.1)",
          "format": "table",
          "legendFormat": "Solar",
          "range": true,
          "refId": "A"
        }
      ],
      "title": "Solar Generation",
      "type": "timeseries"
    }
  ],
  "schemaVersion": 37,
  "style": "dark",
  "tags": [
    "tesla"
  ],
  "templating": {
    "list": []
  },
  "time": {
    "from": "now-24h",
    "to": "now"
  },
  "timepicker": {},
  "timezone": "",
  "title": "Tesla Powerwall",
  "uid": "w5X00_O4z",
  "version": 1,
  "weekStart": ""
}
    EOF
  }

}
