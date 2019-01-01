#!/bin/sh

get_count=$1

#array_speed=(0 0 0 0 0 0 0 0 0 0)
#array_speed=(0 0 0 0 0 0 0 0 0 0)
#length=${#array_speed[*]}
pids=(0 0 0 0 0 0 0 0 0 0)
outputfile=""

rm -f 1G.*
rm -f tmp*

for i in $(seq 1 $get_count)
do
    #outputfile="down"$i
    #echo $outputfile
    #wget  http://123.129.247.153:80/1G.img --output-file $outputfile 2>&1 | grep % | awk '{$array_speed[$i]=substr($8,1,index($8,"M")-1);}' &
	wget  http://123.129.247.153:80/1G.img 2>&1 | grep M | awk '{$speed=substr($8,1,index($8,"M")-1);print $speed > "'tmp$i'"}' & 
    pid=$!
    echo $pid
    pids[$i]=$pid
    #echo $pids[$i]
    #tempspeed=eval $(cat outputfile | grep % | awk '{$speed=substr($8,1,index($8,"M")-1); print $speed}')
done

sleep 1
total_speed=0
sum=0
pocess_alive=1
while [ $process_alive -eq 1 ] 
do
	pocess_alive=0
	for i in $(seq 1 $get_count)
		pid=`ps -ef  | grep -v grep | grep $pids[$i] | awk '{print $2}'`
		if [ -z $pid ]; then
			break
		else
			process_alive=1
			do
				sleep 0.02
		#echo $[$i+1] ${array_speed[$i]}
#        echo ${array_speed[@]}
	#    echo $tempspeed
		#total_speed=$[$sum+${array_speed[$i]}];
		#sum=$total_speed
			  
				#eval $(tail -10 tmp$i | awk '{$sum=$1+$2+$3+$4+$5+$6+$7+$8+$9+$10;print $sum}')
				#tail -10 tmp$i | awk '{$sum=2;print $sum}'
				tail -10 tmp$i | awk '{sum+=$1} END {print "speed=", sum/NR}' 
		#		echo  "average=" $speed
		#sleep 0.01
		#cat $outputfile | grep % | eval $(awk '{print("var_speed=%s", substr($8,1,index($8,"M")-1);}')
		#total_speed=$[$sum+$(tail -1 tmp$i)];
		#sum=$total_speed
			done
		fi
    #echo $total_speed
done

