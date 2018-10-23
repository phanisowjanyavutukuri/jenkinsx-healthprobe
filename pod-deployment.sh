
function application_deployment()
{
local IMAGE_NAME=${1:?Need the IMAGE_NAME}
local DEPLOYMENT_NAME=${2:?DEPLOYMENT_NAME}
local NAMESPACE=${3:?NAMESPACE}
local REPLICA_NUMBER=${4:?Need the REPLICA_NUMBER}
local TAG_NAME=${5:?Need the TAG_NAME}
local PROFILE=${6:?need the PROFILE}
local DATABASE_USER=${7:?DATABASE_USER}
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
      initContainers:
      - name: init-dependencyservice1
        image: fabric8/fabric8-dependency-wait-service:v6632df1
        command: ['sh', '-c', 'fabric8-dependency-wait-service-linux-amd64 postgres://$DATABASE_USER@35.225.91.64:5432']
        env:
        - name: 'DEPENDENCY_POLL_INTERVAL'
          value: '10'
        - name: 'DEPENDENCY_LOG_VERBOSE'
          value: 'true' 
      containers:
      - name: $DEPLOYMENT_NAME
        image: $IMAGE_NAME:$TAG_NAME
        args: ["--spring.profiles.active=$PROFILE"]
        ports:
        - containerPort: 8080
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 3
          periodSeconds: 3	
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 3
          periodSeconds: 3
      imagePullSecrets: 
      - name: gcr-json-key" | kubectl apply -f -
    }    