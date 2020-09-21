#!/usr/bin/env bash

numPS=8;
numProf=9;
PS=6;

#Run line
#nohup ./runMany.sh &

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
$1 - Vaccine Strategy
$2 - Vaccine Delay
$3 - Fitness Function
'

# Compiles the java files
javac -d out ./src/*.java;

# Makes the folders
java -cp out DirTools ${numPS} ${numProf};

#Removes the old outputs if they exist then creates all the directories for a new run
> nohup.out; #Clears the nohup.out file

java -Xms16M -Xmn640K -Xmx128M -Xss512K -cp out RunOne ${PS} 0.492493	0.0061366	0.0119631	0.0516803	0.00828679	0.00524339	0.284642	0.012744	0.126811 &