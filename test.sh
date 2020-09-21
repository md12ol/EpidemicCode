#!/usr/bin/env bash

while read line
do
    IFS=' ' read -a ps <<< ${line};
    params[idx]=ps;
    idx=$(( idx+1 ))
done < "Input/params.dat"

echo ${params}[0];
echo "success";