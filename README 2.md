# 🎬 Movie Bets

<ul style="list-style-type:disc">
  <ul>
    <li>✅ <b>Cloud-Native Spring Boot Development Environment and Startup Template</b>
    <li>✅ <b>Concurrency and Resiliency Patterns in Saga Transactions for Spring Boot Microservices</b>
    <li>✅ <b>Part 5: Reliable Long-Running Saga Business Processes</b>
    <li>✅ <b>Kafka Dynamic Transaction Processing Queues for Long-Running Saga Business Processes</b>
  </ul>
</ul>

---

## ✅ Highlights

- **Cloud-Native Spring Boot Microservices Template**
- **Concurrency and Resiliency Patterns for Saga Transactions**
- **Kafka Dynamic Processing Queues**
- **Reliable Long-Running Business Processes**

---

## 📘 Concurrency & Resiliency Patterns

This project demonstrates a robust and scalable architecture built around **Spring Boot microservices**, **Kafka**, and **JPA**, with a focus on asynchronous Saga transactions and cloud-native best practices.

### Key Features

- ✅ **Cloud-Native Development & Production Setup**  
  Spring Boot, Kubernetes, Skaffold, and Terraform

- ✅ **Asynchronous Saga Transactions**  
  Event-Driven Kafka processing with Outbox Pattern

- ✅ **Idempotency & At-Least-Once Delivery**  
  Safe retries and message deduplication

- ✅ **Event-Driven Choreography Saga**  
  Kafka listeners with JPA & Kafka transaction managers

- ✅ **Concurrency with Kafka Consumer Groups & Partitions**  
  Guarantee ordered processing for specific message keys

- ✅ **Error Handling & Resiliency**  
  Retryable exceptions and dead-letter queues

- ✅ **E2E Testing Framework**  
  Completable Futures with Spring Cloud OpenFeign

---

## 🚀 Cloud-Native Startup Template

A full-stack cloud-native environment ready for local and production deployments.

### Technologies

- **Frontend:** Next.js (React) with Keycloak OAuth2
- **Backend:** Spring Boot Microservices with Kafka, PostgreSQL, JPA
- **DevOps:** Skaffold, Kubernetes, Helm, Terraform, GitHub Actions
- **Security:** Keycloak, Spring Cloud Gateway
- **Testing:** Spring Cloud OpenFeign, E2E Test Services
- **Monitoring:** Kafka UI, Swagger UI

### Stack Overview

```
✅ React & Next.js UI  
✅ Spring Boot Microservices with Kafka  
✅ PostgreSQL & Outbox Pattern with JPA  
✅ Keycloak Authorization Server  
✅ Spring Cloud Gateway  
✅ Skaffold for Local & Prod K8s Dev  
✅ GitHub Actions CI/CD with GitOps  
✅ Azure AKS via Terraform  
✅ Kafka UI & Swagger UI  
```

---

## 🔗 Useful Resources

- [Saga Patterns for Spring Boot Microservices (Parts 1–4)](https://www.linkedin.com/posts/michaelsklyar_concurrency-and-resiliency-patterns-in-activity-7168742915765059586-irvJ)
- [Apache Kafka for Spring Boot Microservices (Udemy)](https://www.udemy.com/course/apache-kafka-for-spring-boot-microservices)
- [Saga Pattern + Debezium + Kafka Example](https://github.com/uuhnaut69/saga-pattern-microservices)
- [Stock Tracking Platform (Kafka + Debezium)](https://github.com/skyglass/stock-tracking-03)
- [Video Platform (Kafka + Minio + FFmpeg)](https://github.com/greeta-video-01/video-api)
- [E2E Testing Pipeline with OpenFeign](https://www.linkedin.com/pulse/e2e-testing-pipeline-spring-boot-microservices-using-openfeign/)
- [Microservices with Node JS and React (Udemy)](https://www.udemy.com/course/microservices-with-node-js-and-react)

---

## 📚 Step-by-Step Setup Guide

### 1️⃣ Create Your GitHub Repository

Clone this repo and push it to your own GitHub repository.

---

### 2️⃣ Prepare Azure Account

Ensure your Azure account has sufficient permissions. Sign up for a [Free Trial](https://azure.microsoft.com/en-us/free) if needed.

---

### 3️⃣ Prepare GitHub Account

Make sure you have a GitHub account with access to create secrets and configure workflows.

---

### 4️⃣ Customize Source Code & GitHub Workflows

- Update `master` to your main branch in `.github/workflows/deploy-*.yaml`
- Change the domain in `k8s/prod/ingress-srv.yaml` (e.g., replace `skycomposer.net`)

---

### 5️⃣ Register Your Domain

Required for TLS configuration. Set up DNS and follow:  
[TLS on AKS with Let’s Encrypt (Medium)](https://medium.com/@jainchirag8001/tls-on-aks-ingress-with-letsencrypt-f42d65725a3)

---

### 6️⃣ Complete Udemy Course

Highly recommended:  
[Apache Kafka for Event-Driven Spring Boot Microservices](https://www.udemy.com/course/apache-kafka-for-spring-boot-microservices)

---

## ⚙️ Local Kubernetes Environment (with Skaffold)

1. Enable Kubernetes in Docker Desktop (Settings → Kubernetes → Enable)
2. Create a local Kubernetes cluster
3. Install nginx ingress controller:

```bash
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.4.0/deploy/static/provider/cloud/deploy.yaml
```

4. Create `.env.local` inside `env/`:

```env
CONTAINER_REGISTRY="moviebets.azurecr.io"
DOCKER_FILE_NAME="Dockerfile"
DOCKER_PUSH="false"
VERSION="latest"
BASE_URL="http://ingress-nginx-controller.ingress-nginx.svc.cluster.local"
API_BASE_URL="http://ingress-nginx-controller.ingress-nginx.svc.cluster.local/api"
KEYCLOAK_BASE_URL="http://localhost/keycloak"
```

5. Build and run:

```bash
mvn clean install -DskipTests
sh skaffold-local.sh
```

6. Open `http://localhost` and test the app.

---

## ☁️ Azure Production Environment (with Terraform & Skaffold)

1. Create `terraform.auto.tfvars` in `infra/`:

```hcl
kubernetes_version = "1.32.2"
app_name = "your-globally-unique-name"
location = "westeurope" # or other Azure region
```

2. Provision cloud resources:

```bash
az login
cd infra
terraform init
terraform apply --auto-approve
```

3. Set up Kubernetes context:

```bash
az aks get-credentials --resource-group your_app_name --name your_app_name
```

4. Create `.env.prod` inside `env/`:

```env
CONTAINER_REGISTRY="yourregistry.azurecr.io"
DOCKER_FILE_NAME="Dockerfile"
DOCKER_PUSH="true"
VERSION="latest"
BASE_URL="https://yourdomain.com"
API_BASE_URL="https://yourdomain.com/api"
KEYCLOAK_BASE_URL="https://yourdomain.com/keycloak"
```

5. Deploy to Azure:

```bash
sh skaffold-prod.sh
```

6. Open your domain in browser and test the production app.

---

## 🎉 Congratulations!

You’ve successfully deployed and tested **Movie Bets App** with a robust cloud-native setup, resilient event-driven architecture, and production-ready Kubernetes configuration.
