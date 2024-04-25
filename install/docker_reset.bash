#!/usr/bin/env bash
docker compose down
docker container stop $(docker container ls -aq)||echo No Docker containers to stop
docker container prune -f
docker image prune -af
####docker volume prune -af
docker network prune -f
docker system prune -af