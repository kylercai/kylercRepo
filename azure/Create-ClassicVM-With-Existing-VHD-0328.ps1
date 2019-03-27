#Login to Azure

Add-AzureAccount -Environment azurechinacloud

#Required Variables
$SubID = '<your subscription ID>'
$StorageAccountName = '<your storage account name>'
$DiskName = '<the disk name to be created and attached to VM>'
$DiskLabel = '<the disk label>'
$VHDfile = '<the VHD file stored in the storage account blob storage, to be used to create VM disk>'
$Size = 'Standard_DS1'   # Standard_DS1_v2
$VmName = '<the VM name>' # juhaokanvm002
$Subnet = 'default' # default
$CloudService = '<the cloud service name>' # juhaokansv
$Location = 'China North' # China North 2
$VnetName = '<the vnet name>' # juhaokan

#Optional Variables
#$AVsetName = 'Availbility Set Name'
#$AffinityGroup = 'Affininity Group Name'

#Login to Azure

# Select Subscription and set current storage account
Select-AzureSubscription -SubscriptionId $SubID

# Set current storage account name where your vhd backup stored.
Set-AzureSubscription -SubscriptionId $SubID -CurrentStorageAccountName $StorageAccountName

# Create a OS disk based on the backup vhd.
Add-AzureDisk -DiskName $DiskName -MediaLocation $VHDfile -Label $DiskLabel -OS "Linux"

#Create VM config for existing VHD
$vm = New-AzureVMConfig -DiskName $DiskName -InstanceSize $Size -Name $VmName 

#Setup networking VNet and static IP, if applicable
$vm | Set-AzureSubnet -SubnetNames $Subnet
#Test-AzureStaticVNetIP -VNetName $VnetName -IPAddress 10.0.0.5
#$vm | Set-AzureStaticVNetIP -IPAddress "<The static IP>"

#Create endpoint for VM
#$vm | Add-AzureEndpoint -Name "PowerShell" -Protocol TCP -LocalPort 5986 -PublicPort 5986
$vm | Add-AzureEndpoint -Name "SSH" -Protocol TCP -LocalPort 22 -PublicPort 2222

#Create VM
New-AzureVM -ServiceName $CloudService -VMs $vm -Location $Location -VNetName $VnetName  #-AffinityGroup $AffinityGroup