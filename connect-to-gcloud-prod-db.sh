#!/usr/bin/env bash

## When connecting to the db with postgres client you need to select the host as localhost !

### DUMP DATA FROM PROD
# ➜ /usr/local/Cellar/libpq/13.1/bin/pg_dump --dbname=makromapa-auth --file=/Users/mmalik/Documents/MakroMapa/auth/07_02_2021.sql --schema=public --username=makromapa-auth-user --host=127.0.0.1 --port=5432

###########

### RESTORE DATA FROM DATA DUMP FILE
# ➜ /usr/local/Cellar/libpq/13.1/bin/psql --file=/Users/mmalik/Documents/MakroMapa/auth/07_02_2021.sql --username=makromapa-auth-user --host=127.0.0.1 --port=5432 makromapa-auth
###########

./cloud_sql_proxy -instances="makromapa-305711:europe-west1:makromapa-auth=tcp:5432"

#c  For clean DB install UUID extension first
# CREATE EXTENSION IF NOT EXISTS "uuid-ossp";