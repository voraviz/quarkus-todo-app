#!/bin/bash
DEFAULT_STORAGE_CLASS=$(oc get sc -A -o jsonpath='{.items[?(@.metadata.annotations.storageclass\.kubernetes\.io/is-default-class=="true")].metadata.name}')
cat etc/openshift/cluster-monitoring-config.yaml | sed 's/storageClassName:.*/storageClassName: '$DEFAULT_STORAGE_CLASS'/' | oc apply -f  -
sleep 60
oc -n openshift-user-workload-monitoring wait --for condition=ready \
   --timeout=180s pod -l app.kubernetes.io/name=prometheus
oc -n openshift-user-workload-monitoring wait --for condition=ready \
   --timeout=180s pod -l app.kubernetes.io/name=thanos-ruler
oc get pvc -n openshift-monitoring
oc create -f etc/openshift/otel-sub.yaml
oc create -f etc/openshift/tempo-sub.yaml
sleep 60
oc wait --for condition=established --timeout=300s \
crd/opentelemetrycollectors.opentelemetry.io \
crd/tempostacks.tempo.grafana.com
oc get csv
oc get csv -n openshift-tempo-operator | grep -v NAME
oc create -f etc/openshift/tempo-odf-bucket.yaml
oc get objectbucketclaim.objectbucket.io/tempo -n openshift-storage
S3_BUCKET=$(oc get ObjectBucketClaim tempo -n openshift-storage -o jsonpath='{.spec.bucketName}')
REGION="''"
ACCESS_KEY_ID=$(oc get secret tempo -n openshift-storage -o jsonpath='{.data.AWS_ACCESS_KEY_ID}'|base64 -d)
SECRET_ACCESS_KEY=$(oc get secret tempo -n openshift-storage -o jsonpath='{.data.AWS_SECRET_ACCESS_KEY}'|base64 -d)
ENDPOINT="http://s3.openshift-storage.svc.cluster.local:80"
PROJECT=todo-tempo
oc new-project $PROJECT
oc create secret generic tempo-s3 \
    --from-literal=name=tempo \
    --from-literal=bucket=$S3_BUCKET  \
    --from-literal=endpoint=$ENDPOINT \
    --from-literal=access_key_id=$ACCESS_KEY_ID \
    --from-literal=access_key_secret=$SECRET_ACCESS_KEY \
    -n $PROJECT
cat etc/openshift/tempo-stack.yaml | sed 's/PROJECT/'$PROJECT'/' | oc apply -n $PROJECT -f -
oc wait --for condition=ready --timeout=180s pod -l app.kubernetes.io/managed-by=tempo-operator  -n $PROJECT 
cat etc/openshift/otel-collector-tempo.yaml | sed 's/PROJECT/'$PROJECT'/' | oc apply -n $PROJECT -f -
oc wait --for condition=ready --timeout=180s pod  -l  app.kubernetes.io/managed-by=opentelemetry-operator -n $PROJECT
TODO_URL=https://$(oc get route todo -n $PROJECT -o jsonpath='{.spec.host}')
JAEGER_URL=https://$(oc get route tempo-simplest-gateway -n $PROJECT -o jsonpath='{.spec.host}')/api/traces/v1/dev/search
echo "Todo: $TODO_URL"
echo "Jaeger: $JAEGER_URL"
