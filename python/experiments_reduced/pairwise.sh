#!/usr/bin/env bash

code=$1
echo computing $code similarities

original_data_root=../../graphsReduced
random_data_root=../../randomReduced
random_data_output=../../randomOutput
repetitions=100

# has to be run only once.
#python3 -u ../experiments/random_ER_pairwise_generation.py ${original_data_root} ${random_data_root} ${repetitions}

ub=`echo ${repetitions} - 1 | bc`
for i in `seq 0 $ub`; do
    infolder=${random_data_root}/gml/${i}/
    outfolder=${random_data_output}/${i}/
    python3 -u ../experiments/compute_${code}.py ${infolder} ${outfolder} none > logs/${i}_compute_${code}.out
done
