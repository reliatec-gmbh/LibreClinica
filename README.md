LibreClinica
============

# Docker System Requirements


| Application Server | Java       | Database      | 
|--------------------|------------|---------------|
| ```tomcat:7-jdk8-slim```           | ```maven:3.5.0-jdk-8``` | ```postgres:11``` |



_the real open source electronic data capture (EDC) for clinical studies._

[LibreClinica](https://libreclinica.org) is the community driven successor of OpenClinica. It is an open source clinical trial software for Electronic Data Capture (EDC) and Clinical Data Management (CDM). 

### Getting Started

Check out [LibreClinica Documentation](https://libreclinica.org/documentation) to learn how to [install](https://libreclinica.org/documentation/install.html), [validate](https://libreclinica.org/documentation#Tests) and [use](https://libreclinica.org/documentation/manuals.html) the software.

#### System Requirements

Recommended system software packages considering Debian Linux based environment for running particular LibreClinica release including overview of database schema changes.

| LibreClinica | Application Server  | Java       | Database                     | Schema Changeset | 
|--------------|---------------------|------------|------------------------------|------------------|
| v1.2.1       | Tomcat 9            | OpenJDK 11 | PostgreSQL 12, PostgreSQL 13 | --               |
| v1.2.0       | Tomcat 9            | OpenJDK 11 | PostgreSQL 12, PostgreSQL 13 | lc-1.2.0         |
| v1.1.0       | Tomcat 9            | OpenJDK 11 | PostgreSQL 11                | --               |
| v1.0.0       | Tomcat 9            | OpenJDK 11 | PostgreSQL 11                | lc-1.0.0         |

> **_NOTE:_** LibreClinica SOAP web API is legacy, not tested and not actively developed.

### Development

[LibreClinica Aims](https://libreclinica.org/goals.html) define main activities where community members can actively participate in the development process. In case of interest in contributing into the codebase, check out [LibreClinica Developer Documentation](https://libreclinica.org/documentation/development.html) to learn how to [set up the development workstation](https://libreclinica.org/documentation/development/docsify.html#/development/dev-machine) and understand the [branching strategy](https://libreclinica.org/documentation/development/docsify.html#/development/developer) that is used to control the contribution workflows.

### Installation
To set up a permanent server, you can copy the file install/Libreclinica_docker.service to the folder /etc/systemd/system and run `systemctl enable Libreclinica_docker.service` to enable the service. Then you can start the service with `systemctl start Libreclinica_docker.service`. The service will automatically start on boot.
### Database backup, migration and querying
If you want to back up your LibreClinica database, you can use the following command:
`docker compose run postgres pg_dump -d libreclinica -U clinica -h postgres>libreclinica_$(date -Iseconds).sql`
Note that Windows does not support file names with colons, so you might want to replace the colons with underscores.
To restore a database, you can use the following command:
`docker compose run postgres psql -d libreclinica -U clinica -h postgres -f libreclinica<backup time>.sql`,
for example:
`docker compose run postgres psql -d libreclinica -U clinica -h postgres -f libreclinica_2023-12-24T01_20_23-06_00.sql`,
If you want to query the database, you can use the following command:
`docker compose run postgres psql -d libreclinica -U clinica -h postgres -c "SELECT * from public.user_account;"`
replacing the SELECT query with your own query.
To delete the subjects from the database, but retaining CRFs, users and study data, use the following command:
```docker compose run postgres psql -d libreclinica -U clinica -h postgres -c "TRUNCATE table subject, dn_subject_map, dn_study_subject_map, study_subject, event_crf, study_event, subject_group_map, dn_event_crf_map, item_data, dn_study_event_map, dn_item_data_map;"```
In the corresponding DMM instance, you should execute: `flask clear_subjects_and_drug_quantities`
To delete the event definitions from the database, use the following command:
```docker compose run postgres psql -d libreclinica -U clinica -h postgres -c "TRUNCATE table study_event_definition,event_definition_crf,study_event,dataset_crf_version_map,dn_study_event_map,event_crf,dn_event_crf_map,item_data,dn_item_data_map;"```

### Contributions
                          
LibreClinica is open source project that values input from contributors:

* Julia Bley, University Hospital RWTH Aachen
* Thomas Hillger, University Hospital RWTH Aachen
* Gerben Rienk Visser, Trial Data Solutions
* Tomas Skripcak, DKFZ Partner Site Dresden - member of the German Cancer Consortium (DKTK)
* Ralph Heerlein, ReliaTec GmbH
* Christian Hänsel, ReliaTec GmbH
* Otmar Bayer, ReliaTec GmbH 
