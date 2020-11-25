# migrating your OpenClinica 3.x installation to LibreClinica

**introduction**  
This manual is written for those who have a running OpenClinica installation, OC, and want to migrate that to LibreClinica, LC.  
For these instructions we assume that your OC version is 3.14 or higher and that it has been running on tomcat 7 and postgresql 9.   
In these instructions we assume that your OC was installed in /usr/local/tomcat7.  

An easy way to migrate from OC to LC is to first run a normal LibreClinica-installation, as described [here](install_lc-1.0_official.md "installation instruction").  
After we've done that successfully we first of all we must stop both OC and LC, so tomcat7 and tomcat9. To do this, issue:  
`sudo systemctl stop tomcat7` or `sudo /etc/init.d/tomcat7 stop`  
and  
`sudo systemctl stop tomcat9`  

Next step is to make a database-backup of the OC-database and restore that as the LC-database. We create the database-backup in a migration-folder:
```
cd $HOME  
mkdir oc_lc_migration  
cd oc_lc_migration  
sudo -u postgres pgdump openclinica pgd_openclinica_20200309
```  
In installing LibreClinica you have created a database libreclinica and we will use that to restore the openclinica-database to. In order to do that we must drop and recreate it. And in order to drop it, we must first end all active sessions:
```sql
sudo -u postgres psql
select pg_terminate_backend(pg_stat_activity.pid) from pg_stat_activity
  where pg_stat_activity.datname = 'libreclinica' and pid <> pg_backend_pid();

drop database libreclinica;
create database libreclinica with encoding='utf8' owner=clinica;
\q
sudo -u postgres psql libreclinica < pgd_openclinica_20200309
```

We now have the database ready for use, but there's one thing we must check and that is if there are any attached files. We do this by issueing  
```sql 
sudo -u postgres psql openclinica
select count(*) from item_data where [value] like '%openclinica.data%';
```
If the outcome is other than 0, we must change the path of the attached files. If for example in OC the path is **/usr/local/tomcat7/openclinica.data** and in LC the data folder is **/usr/share/tomcat9/libreclinica/data** then we should issue in psql:

```sql 
update item_data  
set value=replace(value, '/usr/local/tomcat7/openclinica.data', '/usr/share/tomcat9/libreclinica/data')  
where value like '/usr/local/tomcat7/%';
```

The next thing to do is copying the data-folder from OC to LC and change the ownership:  
```
sudo cp -rf /usr/local/tomcat7/openclinica.data/* /usr/share/tomcat9/libreclinica/data/
sudo chown -R tomcat:tomcat /usr/share/tomcat9/libreclinica
```

We can now start tomcat9 and your LibreClinica instance will be ready to use:  
`sudo systemctl start tomcat9`
