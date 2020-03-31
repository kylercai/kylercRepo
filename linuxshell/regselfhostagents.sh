#!/bin/bash

#first use below API to retrieve the desired agent pool ID
#https://dev.azure.com/<your ORG>/_apis/distributedtask/pools?api-version=5.1
#

#Set environment variables
export PAT=<your PAT>
export AGENT_POOL_ID=7
export ORG=<your ORG>
export ORG_URL=https://agent:$PAT@dev.azure.com/$ORG/_apis/distributedtask/pools/$AGENT_POOL_ID/agents?api-version=5.1

#try below echo statement before triggering a real operation
#for i in {1..2}; do echo curl -H "Content-Type:application/json" -X POST --data '{"name": "agent'$i'", "version": "2.165.2", "enabled": true, "status": "offline"}' $ORG_URL; done

for i in {1..10}; do curl -H "Content-Type:application/json" -X POST --data '{"name": "agent'$i'", "version": "2.165.2", "enabled": true, "status": "offline"}' $ORG_URL; done
