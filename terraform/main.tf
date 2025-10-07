terraform {
  required_providers {
    azurerm = {
      source = "hashicorp/azurerm"
      version = "=3.0.0"
    }
  }
}

provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "k8s-cluster" {
  name = var.resource_group_name
  location = var.location_name
}

resource "azurerm_virtual_network" "k8s-cluster-vnet" {
  name = var.virtual_network_name
  address_space = ["10.0.0.0/16"]
  location = azurerm_resource_group.k8s-cluster.location
  resource_group_name = azurerm_resource_group.k8s-cluster.name
}

resource "azurerm_subnet" "k8s-cluster-control-plane-subnet" {
  name = "k8s-cluster-control-plane-subnet"
  resource_group_name = azurerm_resource_group.k8s-cluster.name
  virtual_network_name = azurerm_virtual_network.k8s-cluster-vnet.name
  address_prefixes = ["10.0.1.0/24"]
}

resource "azurerm_subnet" "k8s-cluster-worker-node-subnet" {
  name = "k8s-cluster-worker-node-subnet"
  resource_group_name = azurerm_resource_group.k8s-cluster.name
  virtual_network_name = azurerm_virtual_network.k8s-cluster-vnet.name
  address_prefixes = ["10.0.2.0/24"]
}

resource "azurerm_subnet" "k8s-cluster-pods-subnet" {
  name = "k8s-cluster-pods-subnet"
  resource_group_name = azurerm_resource_group.k8s-cluster.name
  virtual_network_name = azurerm_virtual_network.k8s-cluster-vnet.name
  address_prefixes = ["10.0.16.0/20"]
}

resource "azurerm_subnet" "k8s-services-subnet" {
  name = "k8s-services-subnet"
  resource_group_name = azurerm_resource_group.k8s-cluster.name
  virtual_network_name = azurerm_virtual_network.k8s-cluster-vnet.name
  address_prefixes = ["10.0.3.0/24"]
}

# resource "azurerm_network_security_group" "k8s-cluster-nsg" {
#   name =
# }