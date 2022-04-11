#!/bin/bash

#Set environment variables
export RESGRP="apimtnrg"
export SUBSCRIPTION_ID="your subscription id"

az account set -s $SUBSCRIPTION_ID
echo Create resource group $RESGRP ...
az group create -l chinaeast2 -g $RESGRP > ./$RESGRP.log

#try echo below statement before triggering a real operation
for i in {11..28}; do
    echo create $RESGRP$i in background ...
    sleep 3
    nohup az apim create -n $RESGRP$i -g $RESGRP -l chinaeast2 --sku-name Premium --publisher-email azureop01@gmail.com --publisher-name Azure >> ./$RESGRP.log 2>&1 &
done
