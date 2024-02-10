#!/bin/bash
SECRET=tempo-s3
SOURCE_NS=todo
DEST_NS=test
oc get secret $SECRET -n $SOURCE_NS -oyaml \
| grep -v '^\s*namespace:\s' \
| oc apply -n $DEST_NS -f -