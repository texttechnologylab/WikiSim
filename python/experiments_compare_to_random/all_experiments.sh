#!/usr/bin/env bash

nohup python3 -u ../experiments/compare_to_random_ER.py ../../graphs/ ../../outputComplete/random/ > IN.out &
sleep 1
nohup python3 -u ../experiments/compare_to_random_ER.py ../../graphs/oecd/ ../../outputComplete/oecd/random/ > oecd.out &
sleep 1
nohup python3 -u ../experiments/compare_to_random_ER.py ../../graphs/oecdTopics/ ../../outputComplete/oecdTopics/random/ > oecdTopics.out &
sleep 1