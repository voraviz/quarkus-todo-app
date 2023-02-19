# TODO Application

- [TODO Application](#todo-application)
  - [Local Deployment](#local-deployment)
    - [OTEL and Jaeger](#otel-and-jaeger)
    - [To-Do App](#to-do-app)
    - [Test](#test)
  - [OpenShift - OpenTelementry](#openshift---opentelementry)
    - [Install Operators](#install-operators)
    - [Deploy to-do app](#deploy-to-do-app)
    - [Test](#test-1)
  - [OpenShift - Service Mesh with OpenTelemetry](#openshift---service-mesh-with-opentelemetry)
    - [Install Operators](#install-operators-1)
    - [Configure Service Mesh](#configure-service-mesh)

## Local Deployment

### OTEL and Jaeger
- Start OpenTelemetry and All-in-One Jaeger with Docker Compose
  
  ```bash
  cd todo/etc/compose
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
  
## OpenShift - OpenTelementry

### Install Operators

- Install following Operators

  - Red Hat OpenShift distributed tracing platform (Jaeger)

    ```bash
    oc create -f etc/jaeger-sub.yaml
    ```

  - Red Hat OpenShift distributed tracing data collection (OTEL)

    ```bash
    cd todo
    oc create -f etc/otel-sub.yaml
    ```

- Verify operators are installed successfully

   ```bash
   oc get csv
   ```

   Result

   ```bash
   NAME                               DISPLAY                                                 VERSION    REPLACES                                       PHASE
   devworkspace-operator.v0.17.0      DevWorkspace Operator                                   0.17.0     devworkspace-operator.v0.16.0-0.1666668361.p   Succeeded
   jaeger-operator.v1.39.0-3          Red Hat OpenShift distributed tracing platform          1.39.0-3   jaeger-operator.v1.34.1-5                      Succeeded
   opentelemetry-operator.v0.63.1-4   Red Hat OpenShift distributed tracing data collection   0.63.1-4   opentelemetry-operator.v0.60.0-2               Succeeded
   ```

   OpenShift Console

   ![](images/oprators.png)

- Create namespace for todo application

  ```bash
  oc new-project todo
  ```

- Create [Jaeger instance](todo/etc/openshift/jaeger.yaml)
  
  ```bash
  oc create -f todo/etc/openshift/jaeger.yaml -n todo 
  ```

  Check

  ```bash
  oc get po -l app.kubernetes.io/name=jaeger -n todo
  ```

  Result 

  ```bash
  NAME                     READY   STATUS    RESTARTS   AGE
  jaeger-5649d6997-48c4l   2/2     Running   0          3m4s
  ```

- Create [OTEL instance](todo/etc/openshift/otel-collector.yaml)
  
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
  cat todo/etc/openshift/otel-collector.yaml | sed 's/PROJECT/'$(oc project -q)'/' | oc create -n todo -f -
  ```

  Check 

  ```bash
  oc get po -l app.kubernetes.io/component=opentelemetry-collector -n todo
  ```

  Result

  ```bash
  NAME                              READY   STATUS    RESTARTS   AGE
  otel-collector-657b9d9c6f-ncm5q   1/1     Running   0          108s
  ```

  Check on Developer Console

  ![](images/dev-console-operators.png)



### Deploy to-do app

- Deploy with Kustomize
  
  ```bash
  oc apply -k todo/kustomize/overlays/dev -n todo
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

## OpenShift - Service Mesh with OpenTelemetry

### Install Operators

- Install OpenShift Service Mesh and Kiali Operator

  ```bash
  oc create -f todo/etc/openshift/service-mesh-sub.yaml
  oc create -f todo/etc/openshift/kiali-sub.yaml
  ```

### Configure Service Mesh
- Create Namespace for control plane
  
  ```bash
  oc new-project todo-istio-system
  ```
- Create control plane
  
  ```bash
  oc create -f todo/etc/openshift/smcp.yaml -n todo-istio-system
  oc apply -f todo/etc/openshift/smcp-ha.yaml -n todo-istio-system
  watch oc get smcp/basic -n todo-istio-system
  ```
  
  Result

  ```bash
  NAME    READY   STATUS            PROFILES      VERSION   AGE
  basic   9/9     ComponentsReady   ["default"]   2.3.1     21m
  ```

- Join namspace todo to control plane
  
  ```bash
  cat todo/etc/openshift/smmr.yaml | \
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

- Add sidecar to todo

  ```bash
  oc patch deployment/todo -p '{"spec":{"template":{"metadata":{"annotations":{"sidecar.istio.io/inject":"true"}}}}}' -n todo
  watch oc get po -l app=todo -n todo
  ```

  Result

  ```bash

  ```

