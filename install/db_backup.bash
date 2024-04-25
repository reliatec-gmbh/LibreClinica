#!/usr/bin/env bash
# Script to backup LibreClinica database
dir="$(dirname "$0")"
backup_dir=$HOME/dbbackup
if [ $1 ];
then
  backupfilename=$1
else
  backupfilename=libreclinica_$(date -Iseconds).sql
fi

echo Backing up the database to /home/azureuser/dbbackup/$backupfilename
cd ${dir}/.. && /usr/bin/docker compose run postgres pg_dump -d libreclinica -U clinica -h postgres>${backup_dir}/$backupfilename
