/*
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: hazelcast-cluster-role
rules:
  - apiGroups:
      - ""
      # Access to apps API is only required to support automatic cluster state management
      # when persistence (hot-restart) is enabled.
      - apps
    resources:
      - endpoints
      - pods
      - nodes
      - services
      # Access to statefulsets resource is only required to support automatic cluster state management
      # when persistence (hot-restart) is enabled.
      - statefulsets
    verbs:
      - get
      - list
      # Watching resources is only required to support automatic cluster state management
      # when persistence (hot-restart) is enabled.
      - watch
  - apiGroups:
      - "discovery.k8s.io"
    resources:
      - endpointslices
    verbs:
      - get
      - list

---

apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: hazelcast-cluster-role-binding
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: hazelcast-cluster-role
subjects:
  - kind: ServiceAccount
    name: default
    namespace: default
 */

resource "kubernetes_service_account_v1" "hazelcast" {
  metadata {
    name = "hazelcast"
    namespace = "dev"
  }
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
}
