#!/usr/bin/env bash
###############################################################################################
## This script is responsible for running services needed by LOYALTY API  (Database, etc.)   ##
## Just run script, with no args: ./run-env.sh                                               ##
## All services will expose their ports on local machine                                     ##
## For example database will be exposed on localhost:5432                                    ##
###############################################################################################


docker-compose  -f builder/docker-compose.local.dependencies.yml up --build -V --force-recreate --detach
