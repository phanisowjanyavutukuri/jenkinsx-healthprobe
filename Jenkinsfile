pipeline {
  options {
    disableConcurrentBuilds()
  }
  agent {
    kubernetes {
      label "module-1"
      yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    application: "module-1"
  ci: true
spec:
  containers:
  - name: docker-dind
    image: gcr.io/woven-sensor-214209/dind-with-git-gcloud
    command:  ["sh"]
    args: ["-c","dockerd --host=unix:///var/run/docker.sock --host=tcp://0.0.0.0:2375 -H unix:///var/run/docker.sock --insecure-registry=35.231.59.10:8083 "]
    tty: true 
    securityContext:
      privileged: true
    
  
  
  - name: kubectl
    image: gcr.io/woven-sensor-214209/gcloud-with-kubectl-git
    imagePullPolicy: Always
    tty: true
    securityContext:
      privileged: true
  imagePullSecrets: 
  - name: gcr-json-key
    
  
 


"""
    }
  }
  environment {
    DEPLOY_NAMESPACE = "production"
  }
  stages {
			stage('Build') {
				
				steps {
				  container('docker-dind') {
						  
					 sh '''
					  
					 docker login -u _json_key -p "$(cat /home/first.json)" https://gcr.io
                     docker build -t aclcarrier --file AclCarrier-Dockerfile .
					 
					 '''
				   
				   }
				   }
				   }
		   stage('Tag and Push') {
        
				steps {
				container('docker-dind') {
                
				 sh '''
				 
				 TAG_NAME=$(git rev-parse HEAD)
				 IMAGE_TAG=${TAG_NAME:0:7}
                 docker login -u _json_key -p "$(cat /home/first.json)" https://gcr.io
                 docker tag  aclcarrier  gcr.io/woven-sensor-214209/aclcarrier:$IMAGE_TAG
                 docker push gcr.io/woven-sensor-214209/aclcarrier:$IMAGE_TAG
               
                 



                              
				 '''
           }  
		   }
		 
			}
			stage('Cluster-Context') {
					
					steps {
					  container('kubectl') {
							  
						 sh '''
                   gcloud auth activate-service-account --key-file=/home/first.json
					gcloud container clusters get-credentials fms-kubernetes-cluster-1 --zone us-central1-a --project woven-sensor-214209
                    kubectl config set-cluster gke_woven-sensor-214209_us-central1-a_fms-kubernetes-cluster-1
					kubectl config set-context gke_woven-sensor-214209_us-central1-a_fms-kubernetes-cluster-1
				    		 	 
						 
						 '''
					   }
					   }
					   }
          stage('Stage-Deployment') {

                        steps {
                          container('kubectl') {

                             sh '''
							 TAG_NAME=$(git rev-parse HEAD)
				             IMAGE_TAG=${TAG_NAME:0:7}
                             source pod-deployment.sh; application_deployment gcr.io/woven-sensor-214209/aclcarrier aclcarrier stage 1 $IMAGE_TAG stag
							 source pod-service.sh; application_service  aclcarrier stage
                             '''
                           }
                           }
                           }
stage('Manual Approval: Prod Deploy'){
steps{
 timeout(time: 10, unit: 'MINUTES') {
    input "Deploy Carriers into prod?" 
}
}
}

stage('Production-Deployment') {

                        steps {
                          container('kubectl') {

                             sh '''
							 TAG_NAME=$(git rev-parse HEAD)
				             IMAGE_TAG=${TAG_NAME:0:7}
                             source pod-deployment.sh; application_deployment gcr.io/woven-sensor-214209/aclcarrier aclcarrier production 1 $IMAGE_TAG prod
							 source pod-service.sh; application_service  aclcarrier production
                             '''
                           }
                           }
                           }
   
  }
  
  }
