#!/usr/bin/env bash

## When connecting to the db with postgres client you need to select the host as localhost !

### DUMP DATA FROM PROD
# ➜ /usr/local/Cellar/libpq/13.1/bin/pg_dump --dbname=makromapa-db --file=/Users/mmalik/Documents/MakroMapa/04_04_2021_db.sql --schema=public --username=makromapa-db-user --host=127.0.0.1 --port=5432
############
# ➜ /usr/local/Cellar/libpq/13.1/bin/pg_dump --dbname=makromapa-auth --file=/Users/mmalik/Documents/MakroMapa/04_04_2021_auth.sql --schema=public --username=makromapa-auth-user --host=127.0.0.1 --port=5432
######################

### RESTORE DATA FROM DATA DUMP FILE
# CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
# ➜ /usr/local/Cellar/libpq/13.1/bin/psql --file=/Users/mmalik/Documents/MakroMapa/04_04_2021_auth.sql --username=makromapa-auth-user --host=127.0.0.1 --port=5432 makromapa-auth
#
# ➜ /usr/local/Cellar/libpq/13.1/bin/psql --file=/Users/mmalik/Documents/MakroMapa/04_04_2021_db.sql --username=makromapa-db-user --host=127.0.0.1 --port=5432 makromapa-db
###########

gcloud auth login
gcloud config set project makromapa-309419
./cloud_sql_proxy -instances="makromapa-309419:europe-west1:makromapa=tcp:5432"
