#!/bin/bash

./gradlew properties -q | grep "^version:" | awk '{print $2}' > .env