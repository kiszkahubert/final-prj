variable "resource_group_name" {
  type        = string
  description = "Resource group name"
  default     = "k8s-cluster"
}

variable "location_name" {
  type        = string
  description = "Azure resource location"
  default     = "West Europe"
}

variable "virtual_network_name" {
  type        = string
  description = "VNet name"
  default     = "k8s-cluster-vnet"
}

variable "admin_username" {
  description = "Admin user for VMs"
  type        = string
  default     = "hubertkiszka"
}

variable "control_plane_vm_size" {
  description = "VM size for control plane, default 2 CPU/2GB RAM"
  type        = string
  default     = "Standard_DS2_v2"
}

variable "worker_node_count" {
  description = "Defines how many worker nodes (needed resources) should be created"
  type        = number
  default     = 2
}

variable "worker_node_vm_size" {
  description = "VM size for worker node, default 2 CPU/2GB RAM"
  type        = string
  default     = "Standard_DS2_v2"
}