resource "kubernetes_service_account_v1" "hazelcast" {
  metadata {
    name = "hazelcast"
    namespace = "dev"
  }
  depends_on = [kubernetes_namespace_v1.this]
}

resource "kubernetes_cluster_role_v1" "hazelcast_cluster_role" {
  metadata {
    name = "hazelcast-cluster-role"
  }
  rule {
    api_groups = [""]
    resources = ["endpoints", "pods", "nodes", "services", "statefulsets"]
    verbs = ["get", "list", "watch"]
  }
  rule {
    api_groups = ["discovery.k8s.io"]
    resources = ["endpointslices"]
    verbs = ["get", "list"]
  }
  depends_on = [kubernetes_namespace_v1.this]
}

resource "kubernetes_cluster_role_binding_v1" "hazelcast_cluster_role_binding" {
  metadata {
    name = "hazelcast-cluster-role-binding"
  }
  role_ref {
    api_group = "rbac.authorization.k8s.io"
    kind = "ClusterRole"
    name = kubernetes_cluster_role_v1.hazelcast_cluster_role.metadata.0.name
  }
  subject {
    kind = "ServiceAccount"
    name = kubernetes_service_account_v1.hazelcast.metadata.0.name
    namespace = "dev"
  }
  depends_on = [kubernetes_namespace_v1.this]
}
