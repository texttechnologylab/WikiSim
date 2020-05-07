#!/usr/bin/env bash

ls logs | grep compute | sed 's/_compute_/-/' | cut -d'-' -f2 | sort | uniq -c
