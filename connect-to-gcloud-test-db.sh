#!/usr/bin/env bash

## When connecting to the db with postgres client you need to select the host as localhost !

### DUMP DATA FROM PROD
# ➜ /usr/local/bin/pg_dump --dbname=makromapa-db-prod --file=/Users/mmalik/Documents/WORK/MakroMapa/data_dump_2020_03.sql --schema=public --username=makromapa-db-user --host=127.0.0.1 --port=5432
###########

### RESTORE DATA FROM DATA DUMP FILE
# ➜ /usr/local/Cellar/postgresql/12.3_4/bin/psql --file=/Users/mmalik/Documents/WORK/MakroMapa/data_dump_2020_03.sql --username=makromapa-db-user --host=127.0.0.1 --port=5432 makromapa-db-prod
###########

./cloud_sql_proxy -instances="makromapa-dev-1:europe-west1:makromapa-auth=tcp:5432"
