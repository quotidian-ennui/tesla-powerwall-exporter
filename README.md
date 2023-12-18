# Tesla Powerwall Exporter

Prometheus exporter for Tesla Powerwall and Solar.

The original fork branch is preserved as 'npm'; this now replicates the same functionality using Java + Quarkus.

There's no reason for it; things are pretty much the same but I was interested in finding out if how many lines of code it would take to do roughly the same thing in Java. As it turns out, not too many more, bearing in mind the verbosity of Java. I've held closely to the way it was in the original in terms of logic; stylistically it is different.

## Key differences

> Note that due to the nature of the environment I have intermittent WiFi dropouts; this has the tendency to cause the NodeJS app to crash and get restarted by K8S. This isn't a problem in and of itself but the scheduler extension to Quarkus should mean the app doesn't need restarting.

- Java 17 or 21
- Using the bundled Dockerfiles from Quarkus (there is a Dockerfile.cgr for running using the chainguard images)
- Can compile down to native image (testing in progress)

## Installation and Usage

Since it's quarkus, a lot of heavy lifting has been done already, but you're probably going to have to read the Quarkus docs. It's very vanilla in that respect.

```bash
$ ./gradlew -Dorg.gradle.console=plain build
```

Once you have installed the required dependencies, export the following three environment variables (same as the original)
- `TESLA_ADDR` is the IP address of the Tesla Powerwall
- `TESLA_EMAIL` and `TESLA_PASSWORD` are used for authenticating

```bash
$ export TESLA_ADDR="192.168.0.3"
$ export TESLA_EMAIL="myemail@myhost.com"
$ export TESLA_PASSWORD="MySecretPassword"
# Java of course, has a lot of logging. In production mode, DEBUG only impacts "io.github.qe.*" classes.
$ export LOG_LEVEL=DEBUG
# default is 9961, as per the original but NODE_PORT -> QUARKUS_HTTP_PORT
$ export QUARKUS_HTTP_PORT=9963
$ ./gradlew -Dorg.gradle.console=plain -Dquarkus.package.type=uber-jar build
$ java -jar build/tesla-powerwall-exporter-2.0.0-SNAPSHOT-runner.jar
__  ____  __  _____   ___  __ ____  ______
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/
2023-12-11 17:08:46,077 INFO  [io.quarkus] (main) tesla-powerwall-exporter 2.0.0-SNAPSHOT on JVM (powered by Quarkus 3.6.1) started in 0.397s. Listening on: http://0.0.0.0:9963
2023-12-11 17:08:46,078 INFO  [io.quarkus] (main) Profile prod activated.
2023-12-11 17:08:46,078 INFO  [io.quarkus] (main) Installed features: [cdi, config-yaml, micrometer, qute, scheduler, smallrye-context-propagation, vertx]
2023-12-11 17:08:47,041 DEBUG [io.git.qe.pow.RestClient] (executor-thread-1) Login to ...
2023-12-11 17:08:47,925 DEBUG [io.git.qe.pow.RestClient] (executor-thread-1) Scraping meters/aggregates
2023-12-11 17:08:48,010 DEBUG [io.git.qe.pow.RestClient] (executor-thread-1) Scraping system_status/soe
2023-12-11 17:08:48,026 DEBUG [io.git.qe.pow.RestClient] (executor-thread-1) Scraping system_status
```


