create the folder /home/azureuser/dbbackup
Set up a cron job to run the following command each 12 hours:
/usr/bin/docker compose run postgres pg_dump -d libreclinica -U clinica -h postgres>/home/azureuser/dbbackup/libreclinica_$(date -Iseconds).sql
This can be done by adding the following line to the crontab:
45 10,22 * * * cd /home/azureuser/Alagant_projects/LibreClinica/ && /usr/bin/docker compose run postgres pg_dump -d libreclinica -U clinica -h postgres>/home/azureuser/dbbackup/libreclinica_$(date -Iseconds).sql
this command will run at 10:45 and 22:45 each day, at the timezone of the server.