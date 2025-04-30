#
# Creates a resource group for Event Booking in your Azure account.
#
resource "azurerm_resource_group" "moviebets" {
  name     = var.app_name
  location = var.location
}
