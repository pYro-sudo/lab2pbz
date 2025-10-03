project = "quarkus-app-1.0"

app "quarkus-app" {
  labels = {
    "service" = "quarkus-app",
    "env"     = "dev"
  }

  build {
    hook {
      when    = "before"
      command = ["sh", "-c", <<EOT
        mvn clean package -DskipTests
      EOT
      ]
    }

    use "docker" {
      dockerfile = "/Dockerfile"
    }

    registry {
      use "docker" {
        image = "pyrodocker1/quarkus-app"
        tag   = "latest"
      }
    }

    hook {
      when    = "after"
      command = ["sh", "-c", <<EOT
        docker tag waypoint.local/quarkus-app:latest pyrodocker1/quarkus-app:latest
        docker pull postgres:14-alpine
        docker pull redis:7.2-alpine
        docker pull liquibase/liquibase:4.18

        if ! kind get clusters | grep -q desktop; then
          kind create cluster --name desktop
        fi

        kind load docker-image postgres:14-alpine --name desktop
        kind load docker-image redis:7.2-alpine --name desktop
        kind load docker-image liquibase/liquibase:4.18 --name desktop
        kind load docker-image pyrodocker1/quarkus-app:latest --name desktop
      EOT
      ]
    }
  }

  deploy {
    use "exec" {
      command = ["sh", "-c", <<EOT
        echo "Deploying all Kubernetes resources..."

        if ! kubectl get namespace ingress-nginx > /dev/null 2>&1; then
          kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml
          kubectl wait --namespace ingress-nginx \
            --for=condition=ready pod \
            --selector=app.kubernetes.io/component=controller \
            --timeout=90s
        fi

        kubectl apply -f ./src/main/deployment/kubernetes/service-account.yaml
        kubectl apply -f ./src/main/deployment/kubernetes/postgres-deployment.yaml
        kubectl apply -f ./src/main/deployment/kubernetes/redis-deployment.yaml
        
        echo "Waiting for postgres and redis to be ready..."
        kubectl wait --for=condition=ready pod -l app=postgres --timeout=120s
        kubectl wait --for=condition=ready pod -l app=redis --timeout=120s

        kubectl apply -f ./src/main/deployment/kubernetes/liquibase-job.yaml

        echo "Waiting for database migrations to complete..."
        kubectl wait --for=condition=complete job/liquibase-migration --timeout=180s
        
        kubectl apply -f ./src/main/deployment/kubernetes/quarkus-service.yaml
        kubectl apply -f ./src/main/deployment/kubernetes/quarkus-deployment.yaml
        kubectl apply -f ./src/main/deployment/kubernetes/quarkus-hpa.yaml
        kubectl apply -f ./src/main/deployment/kubernetes/quarkus-pdb.yaml
        kubectl apply -f ./src/main/deployment/kubernetes/network-policy-np.yaml
        kubectl apply -f ./src/main/deployment/kubernetes/ingress.yaml
        
        echo "All resources deployed successfully!"
        echo "Application will be available at: http://quarkus-app.example.com"
        
        kubectl get pods,svc,deployments
      EOT
      ]
    }
  }

  release {
    use "exec" {
      command = ["sh", "-c", "echo Release completed - using custom Kubernetes manifests"]
    }
  }
}
