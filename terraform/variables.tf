variable "resource_group_name" {
  type = string
  description = "Resource group name"
  default = "k8s-cluster"
}

variable "location_name" {
  type = string
  description = "Azure resource location"
  default = "West Europe"
}

variable "virtual_network_name" {
  type = string
  description = "VNet name"
  default = "k8s-cluster-vnet"
}