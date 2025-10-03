# lab2pbzweb

Basically a CRUD app, that is for managing the company product workflow.

## What you need to deploy
- Kubernetes(KinD)
- Docker
- Hashicorp Waypoint
- Java 17+

## What was used
- Kubernetes(KinD)
- Docker
- Hashicorp Waypoint
- Helm
- nginx
- Java 17+
- Maven 3.9+
- Quarkus 3.24.5+ (but basically better check out pom.xml)
- Hibernate with Panache
- Redis
- PostgreSQL
- Liquibase
- Prometheus(but as a dependency for Quarkus, so it is basically built-in, no worries about it)
- Swagger(well, because i didn't want to do front)
- JUnit5 + Mockito

## How to launch
```shell
waypoint up
```
but remeber to write in ```hosts``` file the configuration of the app like ```<ip-of-the-ingress> quarkus-app.example.com```

## How to check out

well, visit ```http://quarkus-app.example.com:8080/swagger-ui```, _but check out the status of kubernetes resources by writing `kubectl get resources && kubectl get resources -n ingress-nginx`_
