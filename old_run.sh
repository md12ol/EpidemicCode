#!/usr/bin/env bash

numPS=2;
PSnum=2;
endPS=2;
numProf=9;
numCores=2;

#Run line
#nohup ./runOne.sh &

#The lines below run the simulation with the correct profile, output and parameters
: ' Parameter Settings from params.txt
$1 - Toggle Density
$2 - Hop Density
$3 - Add Density
$4 - Delete Density
$5 - Swap Density
$6 - Local Toggle Density
$7 - Local Add Density
$8 - Local Delete Density
$9 - Null Density
'

: ' Run line params
$1 - Parameter Setting Number
$2-$10 - Densities
'

# Compiles the java files
javac -d out src/*.java;

echo "compiled";

# Makes the folders
java -cp out DirTools ${numPS} ${numProf};

echo "folders made";

#Removes the old outputs if they exist then creates all the directories for a new run
> nohup.out #Clears the nohup.out file

echo "nohup clear";

#While there are more Parameter Settings to process
declare -a pids;
cores=0;
profNum=1;

declare -A params;
out_idx=0;

while read -r line
do
    in_idx=0;
    IFS=' ' read -a ps <<< ${line};
    for val in ${ps[@]} ; do
        params[${out_idx}, ${in_idx}]=${val};
        in_idx=$(( in_idx + 1 ))
    done
    out_idx=$(( out_idx + 1 ))
done < "Input/params.dat"

echo "params loaded";

while [[ ( ${PSnum} < ${endPS} || ${PSnum} == ${endPS} ) ]]
do
    if [[ ( ${profNum} > ${numProf} ) ]]
    then
        profNum=1;
        PSnum=$(( PSnum + 1 ));
        readNow=true;
    fi

    if [[ ( ${cores} < ${numCores} ) ]]
    then
#        echo "starting " ${PSnum} ${profNum};
        java -Xms64M -Xmx64M -Xss32M -XXaggressive:memory -cp out RunOne \
        ${PSnum} ${profNum} ${ps[0]} ${ps[1]} \
        ${ps[2]} ${ps[3]} ${ps[4]} ${ps[5]} ${ps[6]} ${ps[7]} ${ps[8]} &
        pids[${cores}]=$!;
        cores=$(( cores + 1 ));
        echo "PIDs running " ${pids[@]};
        profNum=$(( profNum + 1 ));
    fi

    # NO MORE CORES D:
    while [[ ( ${cores} > ${numCores} || ${cores} == ${numCores} ) ]]
    do
#        echo "all running";
        for pid in ${pids[*]}
        do
            if [[ ! $( ps -p ${pid} | grep java ) ]]
            then
                echo "ending " ${pid};
                wait ${pid};
                cores=$(( cores - 1 ));
            fi
        done
        sleep 5s;
    done
    sleep 5s;
done

wait;