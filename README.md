# TODO Application

- [TODO Application](#todo-application)
  - [Development Mode](#development-mode)
  - [Build and Deploy with OpenShift](#build-and-deploy-with-openshift)
    - [Build and Deploy](#build-and-deploy)
      - [Dev Console](#dev-console)
    - [Kustomize](#kustomize)
  - [User Workload Monitor](#user-workload-monitor)
  - [GitOps with ArgoCD](#gitops-with-argocd)

## Development Mode
- Navigate to directory [todo](todo) or [todo-reactive](todo-reactive)
- Database container is configured automatically with Zero Config Setup (DevService). Check [application.properties](todo/src/main/resources/application.properties) that there is no database URL, user and password specified in default and ve profile.
  
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
- Open URL http://localhost:8080 with your browser
- Test RESTful API
  - Create new todo item
    
    ```bash
     curl -H "Content-type: application/json" \
     --data "@sample.json" -v \
      http://localhost:8080/api
    ```
  
  - List all todo items

    ```bash
     curl -v localhost:8080/api
    ```

  - Delete todo number 1

    ```bash
    curl -v -X DELETE http://localhost:8080/api/1
    ```

<!-- - Build container image
  - JVM fast-jar container with [build_jvm_container.sh](todo/build_jvm_container.sh)
  - Native container with [build_native_container.sh](todo/build_native_container.sh) -->
  
<!-- ## Build & Deploy on OpenShift

- Deploy todo application
  
 
    - Todo App

      
      - Select label and add lable app=todo -->
  <!-- - CLI with YAML files
    - [Build](todo/etc/build/todo-build.yaml)
      
      ```bash
      oc apply -f etc/build/todo-build.yaml
      oc apply -f etc/deploy/todo.yaml
      ``` -->
## Build and Deploy with OpenShift
### Build and Deploy
#### Dev Console
- Deploy PostgreSQL Database. Add->Database, Select PostgreSQL
    
        | Parameter                      | Value        | 
        |--------------------------------|--------------|
        | Database Service Name          | todo-db      |      
        | PostgreSQL Connection Username | todo         | 
        | PostgreSQL Connection Password | todoPassword | 
        | Database Name                  | todo         |  

- Build and deploy todo app from Git
  - Add->From Git
  - Git Repository: https://github.com/voraviz/quarkus-todo-app and select *Show Advanced Git Option*       
 
        
        | Parameter                      | Value        | 
        |--------------------------------|--------------|
        | Context dir                    | todo         | 
        | Application Name               | todo-app     | 
        | Name                           | todo         | 
      
      - Select Route option    
      - Select Build Configuration to add environment variables
 
        | Environment variables (build and runtime)  | Value        | 
        |--------------------------------|--------------|
        | QUARKUS_PACKAGE_TYPE         | uber-jar     | 
      
      - Select Deployment to add environment variables
 
        | Environment variables (build and runtime)  | Value        | 
        |--------------------------------|--------------|
        |quarkus.hibernate-orm.database.generation|create|

<!-- #### CLI

- PostgreSQL Database
  
  ```bash
  cd todo
  oc new-project todo
  oc create -f kustomize/base/todo-db.yaml
  watch oc get po
  oc get pvc
  ```

- Todo App
  
  ```bash
  oc new-app --name=todo \
     --context-dir=todo   --labels=app=todo \
     --allow-missing-images \
     https://github.com/voraviz/quarkus-todo-app  
  oc logs -f buildconfig/todo
  oc expose svc todo
  ```

  or build by YAML

  ```bash
  oc create -f etc/todo-build.yaml
  oc logs -f bc/todo
  oc new-app --name=todo --image-stream=todo:latest --labels=app=todo
  oc expose svc/todo
  ``` -->

### Kustomize

- Deploy with Kustomize
      
  ```bash
  oc create -k kustomize/overlays/dev
  watch oc get pods
  ```
  
  ![](images/app-topology.png)

## User Workload Monitor

- Enable User Workload Monitor
- Create service monitor to monitor todo app
  - Create [Service Monitor](todo/kustomize/base/service-monitor.yaml) 
    
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



## GitOps with ArgoCD
  WIP

