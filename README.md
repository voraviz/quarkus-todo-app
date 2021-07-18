# TODO Application

- [TODO Application](#todo-application)
  - [Development Mode](#development-mode)
  - [Deploy on OpenShift](#deploy-on-openshift)
  - [User Workload Monitor](#user-workload-monitor)
  - [Test REST API](#test-rest-api)
  - [Kustomize](#kustomize)

## Development Mode
- Database container is configured automatically with Zero Config Setup (DevService). Check [application.properties](src/main/resources/application.properties) that there is no database URL, user and password specified in default profile.
  
  ```bash
  mvn quarkus:dev
  ```

  Output

  ```bash
  ...
  11:42:17 INFO  [or.te.DockerClientFactory] (build-37) Ryuk started - will monitor and terminate Testcontainers containers on JVM exit
  11:42:17 INFO  [or.te.DockerClientFactory] (build-37) Checking the system...
  11:42:17 INFO  [or.te.DockerClientFactory] (build-37) ✔︎ Docker server version should be at least 1.6.0
  11:42:18 INFO  [or.te.DockerClientFactory] (build-37) ✔︎ Docker environment should have more than 2GB free disk space
  11:42:18 INFO  [🐳.2]] (build-37) Creating container for image: postgres:13.2
  11:42:18 INFO  [🐳.2]] (build-37) Starting container with ID: 0040a40f1bbe0f583455d047ba3abf6e0cd7d9718fec342f9bb0a3fdf46bc315
  11:42:19 INFO  [🐳.2]] (build-37) Container postgres:13.2 is starting: 0040a40f1bbe0f583455d047ba3abf6e0cd7d9718fec342f9bb0a3fdf46bc315
  11:42:22 INFO  [🐳.2]] (build-37) Container postgres:13.2 started in PT4.512469S
  ...
  ```
  
- Build container image
  - JVM fast-jar container with [build_jvm_container.sh](build_jvm_container.sh)
  - Native container with [build_native_container.sh](build_native_container.sh)
  
## Deploy on OpenShift

- Deploy todo application
  
  - Developer Console
    - PostgreSQL
      - Add->Database, Select PostgreSQL
    
        | Parameter                      | Value        | 
        |--------------------------------|--------------|
        | Database Service Name          | todo-db      | 
        | PostgreSQL Connection Username | todo         | 
        | PostgreSQL Connection Password | todoPassw0rd |  
        | Volume Capacity | 1Gi |  

    - Todo App
      - Add->From Git
      - Git Repository: https://github.com/voraviz/quarkus-todo-app
      - Select *Route* and add label *app=todo*

  - CLI with YAML files
    - [Build](etc/build/todo-build.yaml)
      
      ```bash
      oc apply -f etc/build/todo-build.yaml
      oc apply -f etc/deploy/todo.yaml
      ```
    - Deploy [PostgreSQL](etc/deploy/todo-db.yaml) and [Todo App](etc/deploy/todo.yaml)
      
      ```bash
      oc apply -f etc/deploy/todo-db.yaml
      oc apply -f etc/deploy/todo.yaml
      ```
    
    ![](images/app-topology.png)

## User Workload Monitor
- Monitor application's metrics with service monitor
  - Create [Service Monitor](etc/deploy/service-monitor.yaml) 
    
    ```bash
    oc apply -f etc/deploy/service-monitor.yaml
    ```

  - Scale todo to 2 pods
    
    ```bash
    oc scale deployment/todo --replicas=2
    ```

  - Run following command to gererate workload to getAll method
    
    ```bash
    siege -c 5 -t 5m -d 1 http://$(oc get route/todo -o jsonpath='{.spec.host}')/api
    ```

  - Developer Console, Monitoring->Metrics->Custom Query and select checkbox Stacked
    
    ```bash
    rate(application_io_quarkus_sample_TodoResource_countGetAll_total[1m])
    ```

    ![](images/app-monitor.png)

## Test REST API
- Test API
  - Get all todo 
  
    ```bash
    curl -v  http://$(oc get route/todo -o jsonpath='{.spec.host}')/api
    ```
  
  - Create todo
    
    ```bash
     curl -H "Content-type: application/json" \
     --data "@sample.json" -v \
     http://$(oc get route/todo -o jsonpath='{.spec.host}')/api
    ```
 
  - Delete todo number 1
   
    ```bash
    curl -v -X DELETE http://$(oc get route/todo -o jsonpath='{.spec.host}')/api/1
    ```
## Kustomize
- Sample Kustomize
  
  ```bash
  oc create -k kustomize/overlays/dev
  ```