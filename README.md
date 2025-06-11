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
  - This is optional, and will default to TESLA_ADDR if not set
- `TESLA_EMAIL` and `TESLA_PASSWORD` are used for authenticating

```console
$ export TESLA_ADDR="192.168.0.3"
$ export TESLA_EMAIL="myemail@myhost.com"
$ export TESLA_PASSWORD="MySecretPassword"
# Java of course, has a lot of logging. In production mode, DEBUG only impacts "io.github.qe.*" classes.
$ export LOG_LEVEL=DEBUG
$ ./gradlew -Dorg.gradle.console=plain -Dquarkus.package.type=uber-jar build
bsh ❯ java -jar build/tesla-powerwall-exporter-3.9.0-SNAPSHOT-runner.jar
    ____                                         ____   ______                      __
   / __ \____ _      _____  ______      ______ _/ / /  / ____/  ______  ____  _____/ /_
  / /_/ / __ \ | /| / / _ \/ ___/ | /| / / __ `/ / /  / __/ | |/_/ __ \/ __ \/ ___/ __/
 / ____/ /_/ / |/ |/ /  __/ /   | |/ |/ / /_/ / / /  / /____>  </ /_/ / /_/ / /  / /_
/_/    \____/|__/|__/\___/_/    |__/|__/\__,_/_/_/  /_____/_/|_/ .___/\____/_/   \__/
                                                              /_/

                                                              Powered by Quarkus 3.23.0
2025-06-11 17:48:39,221 INFO  [io.quarkus] (main) tesla-powerwall-exporter 3.9.0-SNAPSHOT on JVM (powered by Quarkus 3.23.0) started in 0.516s. Listening on: . Management interface listening on http://0.0.0.0:9961.
2025-06-11 17:48:39,222 INFO  [io.quarkus] (main) Profile prod activated.
2025-06-11 17:48:39,222 INFO  [io.quarkus] (main) Installed features: [cdi, micrometer, rest-client, rest-client-jackson, scheduler, smallrye-context-propagation, vertx]
2025-06-11 17:48:41,166 INFO  [io.git.qe.pow.PowerwallService] (executor-thread-1) Successfully scraped stats
2025-06-11 17:48:41,167 INFO  [io.git.qe.pow.PowerwallService] (executor-thread-1) Powerwall stats: {tesla_load_instant_apparent_power=1347.9227769052648,...
```

## Quarkus Native

 I've begun building it as a native binary with no special parameters (check the Justfile for how I'm building it); running in my homelab k8s environment it takes (after ~12hrs) about 13Mb of memory. CPU usage isn't significantly different. The native docker image is marked as `-native` in packages.

```console
bsh ❯ just build native
bsh ❯ build/tesla-powerwall-exporter-3.9.0-SNAPSHOT-runner
    ____                                         ____   ______                      __
   / __ \____ _      _____  ______      ______ _/ / /  / ____/  ______  ____  _____/ /_
  / /_/ / __ \ | /| / / _ \/ ___/ | /| / / __ `/ / /  / __/ | |/_/ __ \/ __ \/ ___/ __/
 / ____/ /_/ / |/ |/ /  __/ /   | |/ |/ / /_/ / / /  / /____>  </ /_/ / /_/ / /  / /_
/_/    \____/|__/|__/\___/_/    |__/|__/\__,_/_/_/  /_____/_/|_/ .___/\____/_/   \__/
                                                              /_/

                                                              Powered by Quarkus 3.23.0
2025-06-11 17:52:14,809 INFO  [io.quarkus] (main) tesla-powerwall-exporter 3.9.0-SNAPSHOT native (powered by Quarkus 3.23.0) started in 0.013s. Listening on: . Management interface listening on http://0.0.0.0:9961.
2025-06-11 17:52:14,809 INFO  [io.quarkus] (main) Profile prod activated.
2025-06-11 17:52:14,809 INFO  [io.quarkus] (main) Installed features: [cdi, micrometer, rest-client, rest-client-jackson, scheduler, smallrye-context-propagation, vertx]
2025-06-11 17:52:15,820 INFO  [io.git.qe.pow.PowerwallService] (executor-thread-1) Successfully scraped stats
2025-06-11 17:52:15,820 INFO  [io.git.qe.pow.PowerwallService] (executor-thread-1) Powerwall stats: {tesla_load_instant_apparent_power=1335.787455024189, ...}
```
