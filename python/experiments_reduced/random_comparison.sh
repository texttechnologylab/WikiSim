#!/usr/bin/env bash

nohup python3 -u ../experiments/compare_to_random_ER.py ../../graphsReduced/ ../../outputComplete/graphsReduced/random/ > reduced_random.out &
sleep 1
