resource "kubernetes_service_v1" "powerwall" {
  depends_on = [kubernetes_namespace_v1.tooling]
  metadata {
    namespace = kubernetes_namespace_v1.tooling.metadata[0].name
    name      = local.powerwall_app_name
    labels    = local.powerwall_app_labels
  }
  spec {
    selector = local.powerwall_selector
    port {
      name        = local.powerwall_port_name
      port        = local.powerwall_export_port
      target_port = local.powerwall_port_name
      protocol    = "TCP"
    }
    type = "ClusterIP"
  }
}

resource "kubernetes_secret_v1" "powerwall_export_secret" {

  metadata {
    namespace = kubernetes_namespace_v1.tooling.metadata[0].name
    name      = local.powerwall_secrets.name
  }

  data = {
    TESLA_ADDR     = local.powerwall_secrets.ip_addr
    TESLA_EMAIL    = local.powerwall_secrets.user
    TESLA_PASSWORD = local.powerwall_secrets.password
  }
}


resource "kubernetes_deployment_v1" "powerwall" {
  depends_on       = [kubernetes_namespace_v1.tooling]
  wait_for_rollout = local.wait_for_rollout
  metadata {
    namespace = kubernetes_namespace_v1.tooling.metadata[0].name
    name      = local.powerwall_app_name
    labels    = local.powerwall_app_labels
  }

  spec {
    replicas               = 1
    revision_history_limit = 5
    selector {
      match_labels = local.powerwall_selector
    }
    template {
      metadata {
        labels = local.powerwall_app_labels
      }
      spec {
        container {
          image             = local.powerwall_image_name
          name              = local.powerwall_app_name
          image_pull_policy = local.default_image_pull_policy
          env {
            name = "TESLA_ADDR"
            value_from {
              secret_key_ref {
                name     = kubernetes_secret_v1.powerwall_export_secret.metadata[0].name
                key      = "TESLA_ADDR"
                optional = false
              }
            }
          }
          env {
            name = "TESLA_EMAIL"
            value_from {
              secret_key_ref {
                name     = kubernetes_secret_v1.powerwall_export_secret.metadata[0].name
                key      = "TESLA_EMAIL"
                optional = false
              }
            }
          }
          env {
            name = "TESLA_PASSWORD"
            value_from {
              secret_key_ref {
                name     = kubernetes_secret_v1.powerwall_export_secret.metadata[0].name
                key      = "TESLA_PASSWORD"
                optional = false
              }
            }
          }
          dynamic "env" {
            for_each = local.powerwall_env_vars
            content {
              name  = env.key
              value = env.value
            }
          }
          resources {
            limits = {
              cpu    = "100m"
              memory = "96Mi"
            }
            requests = {
              cpu    = "100m"
              memory = "64Mi"
            }
          }
          port {
            container_port = local.powerwall_export_port
            host_port      = local.powerwall_export_port
            name           = local.powerwall_port_name
            protocol       = "TCP"
          }
          liveness_probe {
            http_get {
              path = "/"
              port = local.powerwall_export_port
            }
          }
          readiness_probe {
            http_get {
              path = "/"
              port = local.powerwall_export_port
            }
          }
          startup_probe {
            tcp_socket {
              port = local.powerwall_export_port
            }
          }
        }
      }
    }

  }
}

resource "kubernetes_manifest" "powerwall_service_monitor" {
  manifest = {
    apiVersion = "monitoring.coreos.com/v1"
    kind       = "ServiceMonitor"
    metadata = {
      namespace = kubernetes_namespace_v1.tooling.metadata[0].name
      name      = kubernetes_deployment_v1.powerwall.metadata[0].name
      labels    = local.powerwall_app_labels
    }
    spec = {
      selector = {
        matchLabels = local.powerwall_selector
      }
      endpoints = [{
        port          = local.powerwall_port_name
        interval      = "30s"
        scrapeTimeout = "10s"
        path          = local.powerwall_metrics_path
      }]
    }
  }
}
