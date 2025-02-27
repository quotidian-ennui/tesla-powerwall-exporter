locals {

  powerwall_version      = "1.4.0"
  powerwall_app_name     = "powerwall-exporter"
  powerwall_app_group    = "household-metrics"
  powerwall_image_name   = "ghcr.io/quotidian-ennui/tesla-powerwall-exporter:${local.powerwall_version}"
  powerwall_export_port  = 9961
  powerwall_port_name    = "metrics"
  powerwall_metrics_path = "/metrics"
  powerwall_app_labels = {
    "app"                        = local.powerwall_app_name
    "app.kubernetes.io/instance" = local.powerwall_app_name
    "app.kubernetes.io/name"     = local.powerwall_app_name
    "app.kubernetes.io/version"  = local.powerwall_version
    "app.kubernetes.io/part-of"  = local.powerwall_app_group
  }
  powerwall_selector = {
    "app.kubernetes.io/name"     = local.powerwall_app_name
    "app.kubernetes.io/instance" = local.powerwall_app_name
  }
  powerwall_secrets = {
    name     = "powerwall-export-secret"
    user     = "MyUserName" 
    password = "MyPassword" # gitleaks:allow
    ip_addr  = "10.0.0.1"
  }
  powerwall_env_vars = {
    # Because we know we're doing self-signed certs
    "NODE_NO_WARNINGS" = "1"
    "SCRAPE_INTERVAL"  = "47"
    "PORT"             = local.powerwall_export_port
  }
}
