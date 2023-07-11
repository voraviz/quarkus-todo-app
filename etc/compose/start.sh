#!/bin/bash
podman run  \
-p 16686:16686 -p 14268:14268 -p 14250:14250 \
-e TZ=Asia/Bangkok \
-d jaegertracing/all-in-one:latest
podman run  \
-v  ./otel-collector-config.yaml:/etc/otelcol/config.yaml \
-p 13133:13133 -p 4317:4317  -e TZ=Asia/Bangkok \
-d otel/opentelemetry-collector:latest \
"--config=/etc/otelcol/config.yaml"

# --name jaeger-all-in-one
# --name otel-collector