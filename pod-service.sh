
function application_service()
{

local SERVICE_NAME=${1:?Provide the SERVICE_NAME}
local NAMESPACE=${2:?Provide the NAMESPACE}
echo "
apiVersion: v1
kind: Service
metadata:
  name: $SERVICE_NAME
  namespace: $NAMESPACE
  labels:
    run: $SERVICE_NAME
spec:
  type: NodePort
  ports:
  - port: 8080
    targetPort: 8080
    protocol: TCP
  selector:
    app: $SERVICE_NAME " | kubectl apply -f -
    }    