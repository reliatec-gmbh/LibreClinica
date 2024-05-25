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

These versions of required software packages are currently available in Debian 10 (Buster) Linux.

| Application Server | Java       | Database      | 
|--------------------|------------|---------------|
| Tomcat 9           | OpenJDK 11 | PostgreSQL 11 |

> **_NOTE:_** LibreClinica SOAP web API is legacy, not tested and not actively developed.

### Development

[LibreClinica Aims](https://libreclinica.org/goals.html) define main activities where community members can actively participate in the development process. In case of interest in contributing into the codebase, check out [LibreClinica Developer Documentation](https://libreclinica-docs.readthedocs.io) to learn how to [set up the development workstation](https://libreclinica-docs.readthedocs.io/en/latest/dev/dev-machine.html) and understand the [branching strategy](https://libreclinica-docs.readthedocs.io/en/latest/dev/developer.html) that is used to control the contribution workflows.

### Build and Deployment

1. **Prerequisites:**
    - Docker installed on your machine.
    - Git installed on your machine.
    - Access to the project repositories on GitHub.


2. **Clone the Repositories:**
    - Open a terminal and navigate to the directory where you want to clone the repository.
    - Run the following command to clone the LibreClinica repository:
      ```bash
      git clone https://github.com/Alagant/CDC_CTMS_LibreClinica.git
      ```
    - Run the following command to clone the Drug Management Module repository:
      ```bash
      git clone https://github.com/Alagant/libreclinica_drug_trial_module.git
      ```
    - Run the following command to clone the 0Auth Module repository:
      ```bash
      git clone https://github.com/Alagant/cdc_oauth_portal_mockup.git
      ```
    - Navigate into the cloned repository:
      ```bash
      cd CDC_CTMS_LibreClinica
      ```

3. **Setup Environment Variables:**
    - Before building the Docker image, you need to set up the environment variables. These variables are defined in the `.env` and `.env_emails` files. You can create a copy of these files and update the variables as per your requirements.
    - The `.env_emails` file should contain the following variables:
        - ADMINEMAIL: The email of the admin.
        - MAILUSERNAME: The username for the mail server.
        - DEVELOPEREMAILS: The emails of the developers.


4. **Build the Docker Image:**
    - Run the following command to build the Docker image:
      ```bash
      docker compose build --build-arg ENVIRONMENT=development --build-arg ADMIN_EMAIL=<your-email> --build-arg MAIL_USERNAME=<your-email-username> --build-arg MAIL_PASSWORD=<your-email-password> --build-arg MAIL_ERROR_MSG=<developer-emails>
      ```
    - Replace `<your-email>`, `<your-email-username>`, `<your-email-password>`, and `<developer-emails>` with your actual values.


5. **Run the Maven Install:**
    - Run the following command to execute the Maven install:
      ```bash
      docker compose run libreclinica mvn -B clean install -DskipTests
      ```

6. **Deploy the Application:**
    - Copy the built WAR file to the Docker volume for deployment:
      ```bash
      sudo cp /docker/volumes/cdc_ctms_libreclinica_libreclinica_src/_data/web/target/LibreClinica-web-1.3.1.war /docker/volumes/cdc_ctms_libreclinica_libreclinica_webapps/_data/LibreClinica.war
      ```
    - Start the Docker containers:
      ```bash
      docker compose up -d
      ```

7. **Access the Application:**
    - Once the Docker containers are up and running, you can access the application by opening a web browser and navigating to `http://localhost:8080/LibreClinica`.

### Contributions

LibreClinica is open source project that values input from contributors:

* Julia Bley, University Hospital RWTH Aachen
* Thomas Hillger, University Hospital RWTH Aachen
* Gerben Rienk Visser, Trial Data Solutions
* Tomas Skripcak, DKFZ Partner Site Dresden - member of the German Cancer Consortium (DKTK)
* Ralph Heerlein, ReliaTec GmbH
* Christian Hänsel, ReliaTec GmbH
* Otmar Bayer, ReliaTec GmbH 
