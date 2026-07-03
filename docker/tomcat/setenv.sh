#!/usr/bin/env bash

sed -i \
	-e "s|\${MARIADB_HOST}|$MARIADB_HOST|g" \
	-e "s|\${MARIADB_DATABASE}|$MARIADB_DATABASE|g" \
	-e "s|\${MARIADB_USER}|$MARIADB_USER|g" \
	-e "s|\${MARIADB_PASSWORD}|$MARIADB_PASSWORD|g" \
	"$CATALINA_HOME/conf/context.xml"
