microservices=("adoption" "animal" "api-gateway" "authentication" "cloud-config" "images" "notification" "transaction")

storage=("db" "minio")

for storage in "${storage[@]}"; do
  helm template -f "${storage}"-values.yaml storage | kubectl --kubeconfig ~/.kube/pavel -n dev apply -f -
done

for microservice in "${microservices[@]}"; do
  helm template -f "${microservice}"-values.yaml microservice | kubectl --kubeconfig ~/.kube/pavel -n dev apply -f -
done

