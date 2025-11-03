terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "4.0.0"
    }
    tls = {
      source  = "hashicorp/tls"
      version = "4.1.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "3.7.2"
    }
  }
}

provider "azurerm" {
  features {}
  subscription_id = "0c8c8195-48bf-4a00-a8e3-8dde3f74cfdb"
}

provider "tls" {}

data "azurerm_client_config" "current" {}

resource "tls_private_key" "ssh_key" {
  algorithm = "RSA"
  rsa_bits  = 4096
}

resource "random_string" "suffix" {
  length  = 8
  special = false
  upper   = false
}

resource "azurerm_resource_group" "k8s-cluster" {
  name     = var.resource_group_name
  location = var.location_name
}

resource "azurerm_virtual_network" "k8s-cluster-vnet" {
  name                = var.virtual_network_name
  address_space       = ["10.0.0.0/16"]
  location            = azurerm_resource_group.k8s-cluster.location
  resource_group_name = azurerm_resource_group.k8s-cluster.name
}

resource "azurerm_subnet" "k8s-cluster-control-plane-subnet" {
  name                 = "k8s-cluster-control-plane-subnet"
  resource_group_name  = azurerm_resource_group.k8s-cluster.name
  virtual_network_name = azurerm_virtual_network.k8s-cluster-vnet.name
  address_prefixes     = ["10.0.1.0/24"]
}

resource "azurerm_subnet" "k8s-cluster-worker-node-subnet" {
  name                 = "k8s-cluster-worker-node-subnet"
  resource_group_name  = azurerm_resource_group.k8s-cluster.name
  virtual_network_name = azurerm_virtual_network.k8s-cluster-vnet.name
  address_prefixes     = ["10.0.2.0/24"]
}

resource "azurerm_public_ip" "control_plane_ip" {
  name                = "k8s-control-plane-ip"
  location            = azurerm_resource_group.k8s-cluster.location
  resource_group_name = azurerm_resource_group.k8s-cluster.name
  allocation_method   = "Static"
  sku                 = "Standard"
}

resource "azurerm_public_ip" "worker_node_ip" {
  count               = var.worker_node_count
  name                = "k8s-worker-node-${count.index}-ip"
  location            = azurerm_resource_group.k8s-cluster.location
  resource_group_name = azurerm_resource_group.k8s-cluster.name
  allocation_method   = "Static"
  sku                 = "Standard"
}

resource "azurerm_network_interface" "control_plane_nic" {
  name                = "k8s-control-plane-nic"
  location            = azurerm_resource_group.k8s-cluster.location
  resource_group_name = azurerm_resource_group.k8s-cluster.name

  ip_configuration {
    name                          = "internal"
    subnet_id                     = azurerm_subnet.k8s-cluster-control-plane-subnet.id
    private_ip_address_allocation = "Dynamic"
    public_ip_address_id          = azurerm_public_ip.control_plane_ip.id
  }
}

resource "azurerm_linux_virtual_machine" "control_plane" {
  name                            = "k8s-control-plane"
  location                        = azurerm_resource_group.k8s-cluster.location
  resource_group_name             = azurerm_resource_group.k8s-cluster.name
  admin_username                  = var.admin_username
  size                            = var.control_plane_vm_size
  disable_password_authentication = true
  network_interface_ids           = [azurerm_network_interface.control_plane_nic.id]

  admin_ssh_key {
    public_key = tls_private_key.ssh_key.public_key_openssh
    username   = var.admin_username
  }

  os_disk {
    caching              = "ReadWrite"
    storage_account_type = "Standard_LRS"
  }

  source_image_reference {
    offer     = "0001-com-ubuntu-server-jammy"
    publisher = "Canonical"
    sku       = "22_04-lts"
    version   = "latest"
  }

  identity {
    type = "SystemAssigned"
  }

  custom_data = base64encode(templatefile("${path.module}/cloud-init/control-plane.yml.tftpl", {
    key_vault_name = azurerm_key_vault.k8s_vault.name
    git_repo_url   = var.git_repo_url
    git_branch     = var.git_branch
    git_path       = var.git_path
  }))
}

resource "azurerm_network_interface" "worker_node_nic" {
  count               = var.worker_node_count
  name                = "k8s-worker-node-${count.index}-nic"
  location            = azurerm_resource_group.k8s-cluster.location
  resource_group_name = azurerm_resource_group.k8s-cluster.name

  ip_configuration {
    name                          = "internal"
    subnet_id                     = azurerm_subnet.k8s-cluster-worker-node-subnet.id
    private_ip_address_allocation = "Dynamic"
    public_ip_address_id          = azurerm_public_ip.worker_node_ip[count.index].id
  }
}

resource "azurerm_linux_virtual_machine" "worker_node" {
  count                           = var.worker_node_count
  name                            = "k8s-worker-node-${count.index}"
  location                        = azurerm_resource_group.k8s-cluster.location
  resource_group_name             = azurerm_resource_group.k8s-cluster.name
  admin_username                  = var.admin_username
  size                            = var.worker_node_vm_size
  disable_password_authentication = true
  network_interface_ids           = [azurerm_network_interface.worker_node_nic[count.index].id]

  admin_ssh_key {
    public_key = tls_private_key.ssh_key.public_key_openssh
    username   = var.admin_username
  }

  os_disk {
    caching              = "ReadWrite"
    storage_account_type = "Standard_LRS"
  }

  source_image_reference {
    offer     = "0001-com-ubuntu-server-jammy"
    publisher = "Canonical"
    sku       = "22_04-lts"
    version   = "latest"
  }

  identity {
    type = "SystemAssigned"
  }

  depends_on = [azurerm_linux_virtual_machine.control_plane]

  custom_data = base64encode(templatefile("${path.module}/cloud-init/worker-node.yml.tftpl", {
    key_vault_name = azurerm_key_vault.k8s_vault.name
  }))
}

resource "azurerm_key_vault" "k8s_vault" {
  name                            = "k8s-vault-${random_string.suffix.result}"
  location                        = azurerm_resource_group.k8s-cluster.location
  resource_group_name             = azurerm_resource_group.k8s-cluster.name
  tenant_id                       = data.azurerm_client_config.current.tenant_id
  sku_name                        = "standard"
  enabled_for_template_deployment = true
}

resource "azurerm_key_vault_access_policy" "control_plane_policy" {
  key_vault_id       = azurerm_key_vault.k8s_vault.id
  tenant_id          = data.azurerm_client_config.current.tenant_id
  object_id          = azurerm_linux_virtual_machine.control_plane.identity[0].principal_id
  secret_permissions = ["Set", "Delete"]
}

resource "azurerm_key_vault_access_policy" "worker_node_policy" {
  count              = var.worker_node_count
  key_vault_id       = azurerm_key_vault.k8s_vault.id
  tenant_id          = data.azurerm_client_config.current.tenant_id
  object_id          = azurerm_linux_virtual_machine.worker_node[count.index].identity[0].principal_id
  secret_permissions = ["Get"]
}

resource "azurerm_network_security_group" "control_plane_nsg" {
  name                = "k8s-control-plane-nsg"
  location            = azurerm_resource_group.k8s-cluster.location
  resource_group_name = azurerm_resource_group.k8s-cluster.name

  security_rule {
    name                       = "AllowSSH"
    priority                   = 100
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_range     = "22"
    source_address_prefix      = var.admin_ip
    destination_address_prefix = "*"
  }
}

resource "azurerm_network_interface_security_group_association" "control_plane_nsg_assoc" {
  network_interface_id      = azurerm_network_interface.control_plane_nic.id
  network_security_group_id = azurerm_network_security_group.control_plane_nsg.id
}

resource "azurerm_network_security_group" "worker_node_nsg" {
  count               = var.worker_node_count
  name                = "k8s-worker-node-${count.index}-nsg"
  location            = azurerm_resource_group.k8s-cluster.location
  resource_group_name = azurerm_resource_group.k8s-cluster.name

  security_rule {
    name                       = "AllowSSH"
    priority                   = 100
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_range     = "22"
    source_address_prefix      = var.admin_ip
    destination_address_prefix = "*"
  }

  security_rule {
    name                       = "AllowNodePort"
    priority                   = 110
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_range     = "30000-32767"
    source_address_prefix      = "*"
    destination_address_prefix = "*"
  }

  security_rule {
    name                       = "AllowHTTPS"
    priority                   = 120
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_range     = "443"
    source_address_prefix      = "*"
    destination_address_prefix = "*"
  }
}

resource "azurerm_network_interface_security_group_association" "worker_node_nsg_assoc" {
  count                     = var.worker_node_count
  network_interface_id      = azurerm_network_interface.worker_node_nic[count.index].id
  network_security_group_id = azurerm_network_security_group.worker_node_nsg[count.index].id
}