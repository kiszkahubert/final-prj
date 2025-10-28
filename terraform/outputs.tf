output "control_plane_public_ip" {
  description = "Public IP address of Control Plane VM"
  value       = azurerm_public_ip.control_plane_ip.ip_address
}

output "ssh_private_key" {
  description = "Private SSH key"
  value       = tls_private_key.ssh_key.private_key_pem
  sensitive   = true
}

output "ssh_command" {
  description = "Command to SSH into control plane"
  value       = "ssh -i id_rsa ${var.admin_username}@${azurerm_public_ip.control_plane_ip.ip_address}"
  sensitive   = true
}

output "worker_node_public_ips" {
  description = "Public IP addresses of worker nodes"
  value       = [for ip in azurerm_public_ip.worker_node_ip : ip.ip_address]
}