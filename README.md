# demo-crac

Spring Boot application with CRaC (Coordinated Restore at Checkpoint) support.

## Quick Start with Docker

1.  Run the `Dockerfile-crac-build` file

    ```shell
    docker build -f Dockerfile-crac-build -t my_app_on_crac .
    ```
1. Inject secrets, e.g.
    ```shell
    export ENV_SUPER_SECRET="flappy-bird"
    ```
1. Set up the JAR for snapshotting inside the crac-runner container, pulling in `ENV_SUPER_SECRET`
    ```shell
    rm -rf crac-files/ && mkdir -p crac-files && docker run -d -e ENV_SUPER_SECRET=$ENV_SUPER_SECRET --cap-add=CHECKPOINT_RESTORE --cap-add=SYS_PTRACE --rm --name crac-runner -p 8080:8080 -v $PWD/crac-files:/opt/crac-files my_app_on_crac java -XX:CRaCCheckpointTo=/opt/crac-files -XX:+CRaCImageCompression -jar /opt/app/app.jar
    ```
1. While this Docker container is running, grab the PID of the running JAR file
    ```shell
    docker exec crac-runner jcmd $(docker exec crac-runner jcmd | grep app.jar | cut -d' ' -f1) JDK.checkpoint
    ```
1. Now that we have a checkoint created, build the "runner" Docker image
    ```shell
    docker build -f Dockerfile-crac-run -t my_app_crac_restore .
    ```
1. Then, go ahead and run this in a named Docker container
    ```shell
    docker run -d --rm --name crac-restore -p 8080:8080 my_app_crac_restore
    ```
1. Finally, let's test
    ```shell
    curl --insecure localhost:8080/hello
    ```

    You should see something like this:

    ```shell
    {"message":"Hello, World! The secret word is: <whatever you injected>","timestamp":1743638777557}
    ```

    Additionally, the `Actuator` endpoint works as-expected:

    ```shell
    curl --insecure localhost:8080/actuator/health
    {"status":"UP","groups":["liveness","readiness"]}
    ```

## Benchmarks

Running from the fat-jar via Docker named container `crac-runner`

```shell
2025-04-02 20:33:19 
2025-04-02 20:33:19   .   ____          _            __ _ _
2025-04-02 20:33:19  /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
2025-04-02 20:33:19 ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
2025-04-02 20:33:19  \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
2025-04-02 20:33:19   '  |____| .__|_| |_|_| |_\__, | / / / /
2025-04-02 20:33:19  =========|_|==============|___/=/_/_/_/
2025-04-02 20:33:19 
2025-04-02 20:33:19  :: Spring Boot ::               (v3.3.10)
2025-04-02 20:33:19 
2025-04-02 20:33:19 2025-04-03T00:33:19.770Z  INFO 129 --- [crac] [           main] c.b.crac.CracApplicationKt               : Starting CracApplicationKt v0.0.1-SNAPSHOT using Java 21.0.6 with PID 129 (/opt/app/app.jar started by root in /)
2025-04-02 20:33:19 2025-04-03T00:33:19.771Z DEBUG 129 --- [crac] [           main] c.b.crac.CracApplicationKt               : Running with Spring Boot v3.3.10, Spring v6.1.18
2025-04-02 20:33:19 2025-04-03T00:33:19.771Z  INFO 129 --- [crac] [           main] c.b.crac.CracApplicationKt               : No active profile set, falling back to 1 default profile: "default"
2025-04-02 20:33:20 2025-04-03T00:33:20.685Z  INFO 129 --- [crac] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2025-04-02 20:33:20 2025-04-03T00:33:20.968Z  INFO 129 --- [crac] [           main] o.s.b.web.embedded.netty.NettyWebServer  : Netty started on port 8080 (http)
2025-04-02 20:33:20 2025-04-03T00:33:20.981Z  INFO 129 --- [crac] [           main] c.b.crac.CracApplicationKt               : Started CracApplicationKt in 1.464 seconds (process running for 1.745)
```

Running from named Docker container `crac-restore`

```shell
2025-04-02 20:34:49 Restoring from checkpoint...
2025-04-02 20:34:49 Restored from checkpoint
2025-04-02 20:34:49 2025-04-03T00:34:49.602Z  INFO 129 --- [crac] [Attach Listener] o.s.c.support.DefaultLifecycleProcessor  : Restarting Spring-managed lifecycle beans after JVM restore
2025-04-02 20:34:49 2025-04-03T00:34:49.616Z  INFO 129 --- [crac] [Attach Listener] o.s.b.web.embedded.netty.NettyWebServer  : Netty started on port 8080 (http)
2025-04-02 20:34:49 2025-04-03T00:34:49.618Z  INFO 129 --- [crac] [Attach Listener] o.s.c.support.DefaultLifecycleProcessor  : Spring-managed lifecycle restart completed (restored JVM running for 170 ms)
```

## Notes

- The checkpoint directory must exist and be writable
- Docker requires `--privileged` flag for CRaC capabilities (or other flags, as-specified above)
- Checkpoint files are stored in ./crac-files


