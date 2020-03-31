#!/bin/bash

#Set environment variables
export PAT=<your PAT>
export ORG=<your ORG> #example: kylercai
export AGENT_POOL_NAME=<your agent pool name> #example: mypool

#export API_GET_POOL=https://agent:$PAT@dev.azure.com/$ORG/_apis/distributedtask/pools?poolName=$AGENT_POOL_NAME&api-version=5.1
export API_GET_POOL=https://agent:$PAT@dev.azure.com/$ORG/_apis/distributedtask/pools?poolName=$AGENT_POOL_NAME
export API_ADD_POOL=https://agent:$PAT@dev.azure.com/$ORG/_apis/distributedtask/pools?api-version=5.1
export CONTENT_TYPE="Content-Type:application/json"

#Get the Agent pool ID
export AGENT_POOL_ID=$(curl -s -H $CONTENT_TYPE $API_GET_POOL | jq '.value[0] | .id') 
export NULL="null"

if [ $AGENT_POOL_ID == $NULL ]; then 
   echo "Create new agent pool '$AGENT_POOL_NAME'"
   AGENT_POOL_ID=$(curl -s -H $CONTENT_TYPE -X POST --data '{"name": "'$AGENT_POOL_NAME'"}' $API_ADD_POOL | jq '.id')
fi

#Register the agent to the pool ID
echo "Register agents into agent pool '$AGENT_POOL_NAME'"
export API_REG_AGENT=https://agent:$PAT@dev.azure.com/$ORG/_apis/distributedtask/pools/$AGENT_POOL_ID/agents?api-version=5.1
for i in {1..2}; do 
   AGENT_NAME=$(curl -s -H $CONTENT_TYPE -X POST --data '{"name": "agent'$i'", "version": "2.165.2", "enabled": true, "status": "Offline"}' $API_REG_AGENT | jq '.name'); 
   echo "Registered '$AGENT_NAME'";
done
#for i in {1..2}; do echo curl -H $CONTENT_TYPE -X POST --data '{"name": "agent'$i'", "version": "2.165.2", "enabled": true, "status": "offline"}' $API_REG_AGENT; done
