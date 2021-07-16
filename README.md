# TODO Application

- [TODO Application](#todo-application)
  - [Deploy on OpenShift](#deploy-on-openshift)
<!-- ![GitHub Workflow Status](https://img.shields.io/github/workflow/status/cescoffier/quarkus-todo-app/Build) -->

<!-- ## Database -->

<!-- Run:

```bash
docker run --ulimit memlock=-1:-1 -it --rm=true --memory-swappiness=0 \
    --name postgres-quarkus-rest-http-crud -e POSTGRES_USER=restcrud \
    -e POSTGRES_PASSWORD=restcrud -e POSTGRES_DB=rest-crud \
    -p 5432:5432 postgres:13.1
``` -->
## Deploy on OpenShift
- Create PostgreSQL
  - Command line
    
    ```bash
    oc apply -f etc/todo-db.yaml
    ```

  - Developer Console. Select Add->YAML by drag & drop YAML
    - Create PVC: [todo-db-pvc.yaml](etc/todo-db-pvc.yaml)
    - Create Secret: [todo-db-secret.yaml](etc/todo-db-secret.yaml)
    - Create PostgreSQL: [todo-db-deploymentconfig.yaml](etc/todo-db-deploymentconfig.yaml)
    - Create Service: [todo-db-service.yaml](etc/todo-db-service.yaml)
- Deploy todo application with Developer Console
  - Add->From Git
  - Git Repository: https://github.com/voraviz/quarkus-todo-app
  - Add label app=todo
    
    ![](images/app-topology.png)

- Create Service Monitor
  - Command line
    
    ```bash
    oc apply -f etc/service-monitor.yaml
    ```

  - Developer Console. Select Add->YAML by drag & drop [service-monitor.yaml](etc/service-monitor.yaml)
  - Scale todo to 2 pods
  - Run following command to gererate workload to getAll method
    
    ```bash
    siege -c 4 -t 5m -d 1 http://$(oc get route/todo -o jsonpath='{.spec.host}')
    ```

  - Developer Console, Monitoring->Metrics->Custom Query and select checkbox Stacked
    
    ```bash
    rate(application_io_quarkus_sample_TodoResource_countGetAll_total[1m])
    ```

    ![](images/app-monitor.png)
