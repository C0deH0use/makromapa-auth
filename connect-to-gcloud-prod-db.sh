#!/usr/bin/env bash

#gcloud auth login
gcloud config set project makromapa-321521
./cloud_sql_proxy -instances="makromapa-321521:europe-west1:makromapa-db=tcp:5432"