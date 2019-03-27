set myVnet=hisvnet
set mySubnet=hissubnet01
set myResourceGroup=hisrg
set myVnetResSecGroup=hisnsg
set myPublicIp=hispubip
set myVM=hisvm01
set myNic=hisnic01
set myVMSize=Standard_DS2_v2
set myLocation=ChinaNorth2

rem BJ VM  create

az group create --name %myResourceGroup% --location %myLocation%

az network vnet create --resource-group %myResourceGroup% --name %myVnet% --address-prefix 192.168.0.0/16 --subnet-name %mySubnet%  --subnet-prefix 192.168.1.0/24

az network nsg create --resource-group %myResourceGroup% --name %myVnetResSecGroup%

az network nsg rule create --resource-group %myResourceGroup% --nsg-name %myVnetResSecGroup% --name Allow-SSH-Internet  --access Allow --protocol Tcp  --direction Inbound  --priority 100 --source-address-prefix Internet   --source-port-range *  --destination-address-prefix *  --destination-port-range 22

az network public-ip create --name %myPublicIp% --resource-group %myResourceGroup%

az network nic create --resource-group %myResourceGroup% --name %myNic% --vnet-name %myVnet% --subnet %mySubnet% --accelerated-networking true --public-ip-address %myPublicIp% --network-security-group %myVnetResSecGroup%

az vm create --resource-group %myResourceGroup% --name %myVM% --image "OpenLogic:CentOS:7.1:7.1.20150731"  --size %myVMSize%   --authentication-type password --admin-username azureuser --admin-password "onBoard@0312"  --nics %myNic%
