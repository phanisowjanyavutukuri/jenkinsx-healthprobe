
function application_deployment()
{
local IMAGE_NAME=${1:?Need the IMAGE_NAME}
local DEPLOYMENT_NAME=${2:?DEPLOYMENT_NAME}
local NAMESPACE=${3:?NAMESPACE}
local REPLICA_NUMBER=${4:?Need the REPLICA_NUMBER}
local TAG_NAME=${5:?Need the TAG_NAME}
local PROFILE=${6:?need the PROFILE}
echo "
apiVersion: apps/v1
kind: Deployment
metadata:
  name: $DEPLOYMENT_NAME
  namespace: $NAMESPACE
  labels:
    app: $DEPLOYMENT_NAME
spec:
  replicas: $REPLICA_NUMBER
  selector:
    matchLabels:
      app: $DEPLOYMENT_NAME
  template:
    metadata:
      labels:
        app: $DEPLOYMENT_NAME 
    spec:
      containers:
      - name: $DEPLOYMENT_NAME
        image: $IMAGE_NAME:$TAG_NAME
        args: ["--spring.profiles.active=$PROFILE"]
        ports:
        - containerPort: 8080 
      imagePullSecrets: 
      - name: gcr-json-key" | kubectl apply -f -
    }    