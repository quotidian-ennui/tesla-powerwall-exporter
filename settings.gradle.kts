pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    mavenLocal()
  }
  val quarkusPluginId: String by settings
  val quarkusPluginVersion: String by settings
  plugins {
    id(quarkusPluginId) version quarkusPluginVersion
  }
}

rootProject.name = "tesla-powerwall-exporter"
