#!/usr/bin/env bash

nohup python3 -u ../experiments/compare_to_random.py ../../graphs/ ../../outputComplete/random/ &
sleep 1
nohup python3 -u ../experiments/compare_to_random.py ../../graphs/oecd/ ../../outputComplete/oecd/random/ &
sleep 1
nohup python3 -u ../experiments/compare_to_random.py ../../graphs/oecdTopics/ ../../outputComplete/oecdTopics/random/ &
sleep 1