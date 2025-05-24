#!/bin/bash

if [ -f ./env/.env.prod ]; then
  set -o allexport
  source ./env/.env.prod
  set +o allexport
else
  echo ".env.prod file not found"
  exit 1
fi

set -u # or set -o nounset
: "$BASE_URL"
: "$API_BASE_URL"
: "$KEYCLOAK_BASE_URL"

kubectl delete configmap base-url-config
kubectl create configmap base-url-config --from-literal=BASE_URL=$BASE_URL --from-literal=API_BASE_URL=$API_BASE_URL --from-literal=KEYCLOAK_BASE_URL=$KEYCLOAK_BASE_URL

kubectl apply -f 'https://strimzi.io/install/latest?namespace=default'

# Create a namespace for ingress resources
kubectl create namespace public-ingress

# Add the Helm repository
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update

# Use Helm to deploy an NGINX ingress controller
helm upgrade --install ingress-nginx ingress-nginx \
  --repo https://kubernetes.github.io/ingress-nginx \
  --namespace public-ingress \
  --set controller.config.http2=true \
  --set controller.config.http2-push="on" \
  --set controller.config.http2-push-preload="on" \
  --set controller.ingressClassByName=true \
  --set controller.ingressClassResource.controllerValue=k8s.io/ingress-nginx \
  --set controller.ingressClassResource.enabled=true \
  --set controller.ingressClassResource.name=public \
  --set controller.service.externalTrafficPolicy=Local \
  --set controller.setAsDefaultIngress=true

# Label the cert-manager namespace to disable resource validation
kubectl label namespace public-ingress cert-manager.io/disable-validation=true
# Add the Jetstack Helm repository
helm repo add jetstack https://charts.jetstack.io
# Update your local Helm chart repository cache
helm repo update
# Install CRDs with kubectl
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.11.0/cert-manager.crds.yaml
# Install the cert-manager Helm chart
helm install cert-manager jetstack/cert-manager \
  --namespace public-ingress \
  --version v1.11.0

# Make sure you set your own email, before applying this manifest:
kubectl apply -f './k8s/cert/cluster-issuer.yml' --namespace public-ingress

# Temporary directory for the processed manifests
GENERATED_DIR=./k8s/generated
rm -rf $GENERATED_DIR
mkdir $GENERATED_DIR

# Process each manifest
for file in ./k8s/* ./k8s/prod/*; do
  if [ -d "$file" ]; then
    continue
  fi
  envsubst < "$file" > "$GENERATED_DIR/$(basename "$file")"
done

envsubst < "./skaffold-template.yaml" > "./skaffold.yaml"