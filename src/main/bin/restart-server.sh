#! /bin/sh

ps -ef | grep  'mysql-replication' | grep -v 'grep' | awk '{print $2}' | xargs kill -9

./start-server.sh