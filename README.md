# Tesla Powerwall Exporter

Prometheus exporter for Tesla Powerwall and Solar.

The original fork branch is preserved as 'npm'; this now replicates the same functionality using Java + Quarkus.

There's no reason for it; things are pretty much the same but I was interested in finding out if how many lines of code it would take to do roughly the same thing in Java. As it turns out, not too many more, bearing in mind the verbosity of Java. I've held closely to the way it was in the original in terms of logic; stylistically it is different.

## Key differences

> Note that due to the nature of the environment I have intermittent WiFi dropouts; this has the tendency to cause the NodeJS app to crash and get restarted by K8S. This isn't a problem in and of itself but the scheduler extension to Quarkus should mean the app doesn't need restarting.

- Java 21+
- Using the bundled Dockerfiles from Quarkus (there is a Dockerfile.cgr for running using the chainguard images)
- Can compile down to native image

## Installation and Usage

Since it's quarkus, a lot of heavy lifting has been done already, but you're probably going to have to read the Quarkus docs. It's very vanilla in that respect.

```bash
# casey/just will save you pain here.
./gradlew -Dorg.gradle.console=plain -Dquarkus.package.jar.enabled=true -Dquarkus.package.jar.type=uber-jar build
```

Once you have installed the required dependencies, export the following environment variables (same as the original)

- `TESLA_ADDR` is the IP address of the Tesla Powerwall
- `TESLA_BACKUP_ADDR` is the backup IP Address of the Tesla Powerwall
  - Since you can bind the tesla to both WiFi and ethernet; this makes sense right?
  - If you haven't then just set this to be the same as `TESLA_ADDR` or remove it from `application.properties`
- `TESLA_EMAIL` and `TESLA_PASSWORD` are used for authenticating

```bash
$ export TESLA_ADDR="192.168.0.3"
$ export TESLA_EMAIL="myemail@myhost.com"
$ export TESLA_PASSWORD="MySecretPassword"
$ export TESLA_BACKUP_ADDR="$TESLA_ADDR"
# Java of course, has a lot of logging. In production mode, DEBUG only impacts "io.github.qe.*" classes.
$ export LOG_LEVEL=DEBUG
$ ./gradlew -Dorg.gradle.console=plain -Dquarkus.package.type=uber-jar build
bsh ❯ java -jar build/tesla-powerwall-exporter-3.0.0-SNAPSHOT-runner.jar
Picked up JAVA_TOOL_OPTIONS: -Dpolyglot.js.nashorn-compat=true -Dpolyglot.engine.WarnInterpreterOnly=false
__  ____  __  _____   ___  __ ____  ______
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/
2024-06-07 19:09:31,130 INFO  [io.quarkus] (main) tesla-powerwall-exporter 3.0.0-SNAPSHOT on JVM (powered by Quarkus 3.11.0) started in 0.451s. Listening on: http://0.0.0.0:9961
2024-06-07 19:09:31,131 INFO  [io.quarkus] (main) Profile prod activated.
2024-06-07 19:09:31,131 INFO  [io.quarkus] (main) Installed features: [cdi, micrometer, rest-client, rest-client-jackson, scheduler, smallrye-context-propagation, vertx]
2024-06-07 19:09:33,112 DEBUG [io.git.qe.pow.StatsCollector] (executor-thread-1) Powerwall stats: {tesla_load_instant_apparent_power=642.1633845837055, tesla_battery_instant_apparent_power=410.12193308819764, ...}
```

## Quarkus Native

 I've begun building it as a native binary with no special parameters (check the Justfile for how I'm building it); running in my homelab k8s environment it takes (after ~12hrs) about 13Mb of memory. CPU usage isn't significantly different. The native docker image is marked as `-native` in packages.

```bash
bsh ❯ build/tesla-powerwall-exporter-3.0.0-SNAPSHOT-runner
__  ____  __  _____   ___  __ ____  ______
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/
2024-06-07 19:12:10,468 INFO  [io.quarkus] (main) tesla-powerwall-exporter 3.0.0-SNAPSHOT native (powered by Quarkus 3.11.0) started in 0.012s. Listening on: http://0.0.0.0:9961
2024-06-07 19:12:10,469 INFO  [io.quarkus] (main) Profile prod activated.
2024-06-07 19:12:10,469 INFO  [io.quarkus] (main) Installed features: [cdi, micrometer, rest-client, rest-client-jackson, scheduler, smallrye-context-propagation, vertx]
2024-06-07 19:12:11,833 DEBUG [io.git.qe.pow.StatsCollector] (executor-thread-1) Powerwall stats: {tesla_load_instant_apparent_power=642.1633845837055, tesla_battery_instant_apparent_power=410.12193308819764, ...}
```
