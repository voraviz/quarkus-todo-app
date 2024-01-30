# TODO Application

- [TODO Application](#todo-application)
  - [Local Deployment](#local-deployment)
    - [OTEL and Jaeger](#otel-and-jaeger)
    - [To-Do App](#to-do-app)
    - [Test](#test)
  - [OpenShift - OpenTelementry with Jaeger](#openshift---opentelementry-with-jaeger)
    - [Install Operators](#install-operators)
    - [Deploy to-do app](#deploy-to-do-app)
    - [Test](#test-1)
  - [OpenShift - OpenTelemetry with Tempo](#openshift---opentelemetry-with-tempo)
  - [OpenShift - Service Mesh with OpenTelemetry](#openshift---service-mesh-with-opentelemetry)
    - [Install Operators](#install-operators-1)
    - [Configure Service Mesh](#configure-service-mesh)

## Local Deployment

### OTEL and Jaeger
- Start OpenTelemetry and All-in-One Jaeger with Docker Compose
  
  ```bash
  cd etc/compose
  docker-compose up -d
  ```

### To-Do App

- Start Todo App in Dev Mode
  
  ```bash
  cd todo
  mvn quarkus:dev
  ```

### Test
- Access [todo app](http://localhost:8080/) then add and delete tasks
  
  - Delete and add tasks

    ![](images/todo-app-add-task.png) 


- Access [Jaeger Console](http://localhost:16686) 
  - Select operation e.g. *UPDATE*
  
    ![](images/jaeger-console-select-operation.png)

  - Overall trace detail
  
    ![](images/jaeger-console-trace.png)

  - View span detail. Notice *SQL statement* and *duration*
  
    ![](images/jaeger-console-select-statement.png)
  
## OpenShift - OpenTelementry with Jaeger

### Install Operators

- Install following Operators

  - Red Hat OpenShift distributed tracing platform (Jaeger)

    ```bash
    oc create -f etc/openshift/jaeger-sub.yaml
    ```

  - Red Hat OpenShift distributed tracing data collection (OTEL)

    ```bash
    oc create -f etc/openshift/otel-sub.yaml
    ```

- Verify operators are installed successfully

   ```bash
   oc get csv
   ```

   Result

   ```bash
    NAME                               DISPLAY                                          VERSION    REPLACES                           PHASE
    jaeger-operator.v1.51.0-1          Red Hat OpenShift distributed tracing platform   1.51.0-1   jaeger-operator.v1.47.1-5          Succeeded
    opentelemetry-operator.v0.89.0-3   Red Hat build of OpenTelemetry                   0.89.0-3   opentelemetry-operator.v0.81.1-5   Succeeded
   ```

   OpenShift Console

   ![](images/oprators.png)

- Create namespace for todo application

  ```bash
  oc new-project todo
  ```

- Create [Jaeger instance](etc/openshift/jaeger.yaml)
  
  ```bash
  oc create -f etc/openshift/jaeger.yaml -n todo 
  ```

  Check

  ```bash
  watch oc get po -l app.kubernetes.io/name=jaeger -n todo
  ```

  Result 

  ```bash
  NAME                      READY   STATUS    RESTARTS   AGE
  jaeger-867dcf97bd-xpjwq   2/2     Running   0          15s
  ```

- Create [OTEL instance](etc/openshift/otel-collector.yaml)
  
  Snippet from CRD
  
  ```yaml
  mode: deployment
  config: |
    receivers:
      otlp:
        protocols:
          grpc:
          http:

    exporters:
      jaeger:
        endpoint: jaeger-collector-headless.PROJECT.svc:14250
        tls:
          ca_file: "/var/run/secrets/kubernetes.io/serviceaccount/service-ca.crt"
          insecure: true
  ```
  
  ```bash
  cat etc/openshift/otel-collector.yaml | sed 's/PROJECT/'$(oc project -q)'/' | oc apply -n todo -f -
  ```

  Check 

  ```bash
  watch oc get po -l app.kubernetes.io/component=opentelemetry-collector -n todo
  ```

  Result

  ```bash
  NAME                              READY   STATUS    RESTARTS   AGE
  otel-collector-657b9d9c6f-ncm5q   1/1     Running   0          108s
  ```

  Check on Developer Console

  ![](images/dev-console-operators.png)



### Deploy to-do app
  
- Snippet from deployment with env for OTEL

  ```yaml
      spec:
      containers:
      - name: todo
        env:
        - name: quarkus.otel.exporter.otlp.traces.endpoint
          value: http://otel-collector:4317
  ```
  
- Deploy with kustomize

  ```bash
  oc apply -k kustomize/overlays/otel -n todo
  ```

- Check 

  ```bash
  oc get po -l app=todo -n todo
  oc get po -l app=todo-db -n todo
  ```

  View Developer Console

  ![](images/todo-with-operators.png)

### Test

- Add task to to-do app
  
  
- Login to jaeger
  
  ![](images/jaeger-todo-trace-overall.png)

- View trace
  
  ![](images/jaeger-todo-trace-sql-statement.png)

## OpenShift - OpenTelemetry with Tempo
## OpenShift - Service Mesh with OpenTelemetry

### Install Operators

- Install OpenShift Service Mesh and Kiali Operator

  ```bash
  oc create -f etc/openshift/service-mesh-sub.yaml
  oc create -f etc/openshift/kiali-sub.yaml
  ```

  Result

  ```bash
  NAME                               DISPLAY                                                 VERSION    REPLACES                           PHASE
  jaeger-operator.v1.39.0-3          Red Hat OpenShift distributed tracing platform          1.39.0-3   jaeger-operator.v1.34.1-5          Succeeded
  kiali-operator.v1.57.5             Kiali Operator                                          1.57.5     kiali-operator.v1.57.3             Succeeded
  opentelemetry-operator.v0.63.1-4   Red Hat OpenShift distributed tracing data collection   0.63.1-4   opentelemetry-operator.v0.60.0-2   Succeeded
  servicemeshoperator.v2.3.1         Red Hat OpenShift Service Mesh                          2.3.1-0    servicemeshoperator.v2.3.0         Succeeded
  ```

### Configure Service Mesh
- Create Namespace for control plane
  
  ```bash
  oc new-project todo-istio-system
  ```
- Create control plane
  
  ```bash
  oc create -f etc/openshift/smcp.yaml -n todo-istio-system
  watch oc get smcp/basic -n todo-istio-system
  ```
  
  Result

  ```bash
  NAME    READY   STATUS            PROFILES      VERSION   AGE
  basic   9/9     ComponentsReady   ["default"]   2.3.1     65s
  ```

- Join namspace todo to control plane
  
  ```bash
  cat etc/openshift/smmr.yaml | \
  sed 's/PROJECT/todo/' | \
  oc create -n todo-istio-system -f -
  oc get smmr -n todo-istio-system 
  ```

  Result

  ```bash
  servicemeshmemberroll.maistra.io/default created
  NAME      READY   STATUS       AGE
  default   1/1     Configured   1s
  ```

- Add sidecar and rewrite Liveness and Readiness probe to todo
  
  ```yaml
    template:
      metadata:
        annotations:
          sidecar.istio.io/inject: "true"
          sidecar.istio.io/rewriteAppHTTPProbers: "true"
  ```

  Update deployment with Kustomize

  ```bash
  oc apply -k kustomize/overlays/istio -n todo
  watch oc get po -l app=todo -n todo
  ```

- Create DestinationRule, Gateway and VirtualService

  ```bash
  cat etc/openshift/todo-istio.yaml|sed 's/DOMAIN/'$(oc whoami --show-console|awk -F'apps.' '{print $2}')/|oc apply -n todo -f -
  ```

- Get Istio ingress gateway route

  ```bash
  oc get route  -n todo-istio-system|grep 'todo.apps'|awk '{print $2}'
  ```

- Open todo app with URL from previous step and check Kiali Graph
  
  ![](images/todo-kiali.png)

- Configure OTEL to send trace to Service Mesh's Jaeger
  - OpenShift Admin Console, select project todo then select Installed Operators
  - Select Red Hat OpenShift distributed tracing data collection and select OpenTelemetry Collector
  - Select otel
  - Change endpoint to jaeger-collector-headless.todo-istio-system.svc:14250

    ![](images/config-otel-jager-collector.png)

- Check Service Mesh's Jaeger that OpenTracing is sent to Service Mesh's Jaeger

  ![](images/service-mesh-jaeger.png)

- Check todo pod's log for Trace ID
  
  ```bash
   09:32:44 [io.qu.ht.access-log] (executor-thread-0) =127.0.0.6 - - 06/Mar/2023:09:32:44 +0000 "DELETE /api/ HTTP/1.1" 204 - "http://todo.apps.cluster-srlk7.srlk7.sandbox565.opentlc.com/todo.html" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36" traceId=f433a3102857a47bcb6323ba65ae893a spanId=503008fb528089d7
  ```
  
  You can search Trace ID from Jaeger Console

  ![](images/jaeger-search-by-trace-id.png)