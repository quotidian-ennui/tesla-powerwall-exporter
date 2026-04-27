plugins {
  java
  id("io.quarkus")
  jacoco
  id("io.freefair.lombok") version "9.4.0"
  id("com.github.jmongard.git-semver-plugin") version "0.19.0"
  id("org.barfuin.gradle.jacocolog") version "4.0.2"
  id("com.diffplug.spotless") version "8.4.0"
}

val disableSpotlessJava: String by extra {
  (findProperty("disableSpotlessJava") ?: "true").toString()
}

val disableSpotlessGradle: String by extra {
  (findProperty("disableSpotlessGradle") ?: "true").toString()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

repositories {
  mavenCentral()
  mavenLocal()
}

semver {
  // see https://github.com/jmongard/Git.SemVersioning.Gradle
  patchPattern = "\\A(chore|fix|refactor|deps)(?:\\([^()]+\\))?:"
  groupVersionIncrements = true
  noDirtyCheck = true
  gitDirectory = project.projectDir
  createReleaseCommit = false
  createReleaseTag = false
}

dependencies {
  implementation(enforcedPlatform("$quarkusPlatformGroupId:$quarkusPlatformArtifactId:$quarkusPlatformVersion"))
  implementation("io.quarkus:quarkus-scheduler")
  implementation("io.quarkus:quarkus-micrometer-registry-prometheus")
  implementation("io.quarkus:quarkus-arc")
  implementation("io.quarkus:quarkus-jackson")
  implementation("io.quarkus:quarkus-rest-client-jackson")
  implementation("io.quarkus:quarkus-smallrye-stork")
  implementation("io.smallrye.stork:stork-service-discovery-static-list")
  implementation("io.smallrye.stork:stork-load-balancer-sticky")
  implementation("org.apache.commons:commons-text")
  implementation("org.apache.commons:commons-lang3")
  testImplementation("io.quarkus:quarkus-junit")
  testImplementation("org.wiremock:wiremock:3.13.2")
  testImplementation("io.quarkus:quarkus-jacoco")
}

group = "io.github.quotidianennui"
version = semver.version

lombok {
  disableConfig = true
}

java {
  sourceCompatibility = org.gradle.api.JavaVersion.VERSION_21
  targetCompatibility = org.gradle.api.JavaVersion.VERSION_21
}

tasks.test {
  systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
  // quarkus.jacoco.report=false in app.properties
  // since we want jacocolog to emit its data
  finalizedBy(tasks.jacocoTestReport)
  extensions.configure<org.gradle.testing.jacoco.plugins.JacocoTaskExtension> {
    // Force it into the same location as quarkus.
    destinationFile =
      layout.buildDirectory
        .file("jacoco-quarkus.exec")
        .get()
        .asFile
    excludeClassLoaders = listOf("*QuarkusClassLoader")
  }
}

tasks.jacocoTestReport {
  reports {
    xml.required.set(true)
    html.required.set(true)
  }
}

tasks.compileJava {
  options.encoding = "UTF-8"
  options.compilerArgs.add("-parameters")
}

tasks.compileTestJava {
  options.encoding = "UTF-8"
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
  if (disableSpotlessGradle != "true") {
    kotlinGradle {
      ratchetFrom("origin/main")
      target("*.gradle.kts", "gradle/**/*.gradle.kts")
      ktlint().editorConfigOverride(
        mapOf(
          "indent_size" to 2,
          // intellij_idea is the default style we preset in Spotless, you can override it referring to https://pinterest.github.io/ktlint/latest/rules/code-styles.
          "ktlint_code_style" to "intellij_idea",
        ),
      )
    }
  }
  if (disableSpotlessJava != "true") {
    java {
      // Only spotlessCheck files that have changed in this branch, based on a diff
      // from origin/main...
      // Eventually becomes consistent.
      ratchetFrom("origin/main")
      // Allow // spotless:off // spotless:on
      toggleOffOn()
      // this conflicts with the standard behaviour of vscode-plugin + intellij
      // so use gjf in default mode only which is a pure alphabetical import order.
      // googleJavaFormat().reorderImports(false)
      googleJavaFormat()
      removeUnusedImports()
      // Only useful if you turn off reorderImports() above.
      // importOrder("\\#", "java|javax|jakarta", "", "lombok")

      // fix formatting of type annotations
      // formatAnnotations()
    }
  }
}
