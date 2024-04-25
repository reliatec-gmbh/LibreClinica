Set Up a Development Machine
============================

Description of steps necessary for setting up a development environment
for LibreClinica. These instructions are written for a **Linux** machine
but many if not all steps are same of similar for development on
Windows/OSX.

# Getting Source Code

The LibreClinica source code is hosted on GitHub, and it is necessary to
have a git client installed on the developer machine in order to
download a local copy of LibreClinica.

``` {.sourceCode .shell}
$ git clone https://github.com/reliatec-gmbh/LibreClinica.git
```

If you have a GitHub account, and you are considering to contribute to
LibreClinica project, it is also possible to first fork the project and
clone it from your forked copy. This will allow you to define origin
remote that will point to your fork on GitHub and upstream remote that
will point to LibreClinica main repository. This way the pull requests
can be easily submitted even if you do not have an official main
contributor status (which opens the possibility to push directly to main
LibreClinica repository).

# System Requirements

Check the currently valid LibreClinica [system
requirements](https://libreclinica.org/download.html).

## Java

Download the supported version of OpenJDK and in order to install it
unzip the archive to directory of your choice.

## Tomcat

Download the supported version of Tomcat and in order to install it
unzip the archive to directory of your choice.

> **note**
>
> Download the packed version of Tomcat distribution not the installer.

## Database

Download and install supported version of PostgreSQL using your machine
native tools for package management or installer in case of Windows.
Once database system is running, it is possible to use the `psql` shell
to create the empty database (using the *postgres* user).

``` {.sourceCode .sql}
CREATE ROLE clinica LOGIN ENCRYPTED PASSWORD 'clinica' SUPERUSER NOINHERIT NOCREATEDB NOCREATEROLE
```

``` {.sourceCode .sql}
CREATE DATABASE libreclinica WITH ENCODING='UTF8' OWNER=clinica
```

## Build Automation

Maven is used for LibreClinica dependency management and build
automation. IDE such IntelliJ IDEA can come with bundled Maven however
some may prefer to download and install (unzip) a specific version of
Maven to directory of your choice.

# Project in IDE

> **note**
>
> The steps bellow correspond to the usage of recent IntelliJ IDEA.

## Setup Project

Project can set up in IDE by using Open in pop up (Welcome) screen or by
clicking of `File > Open` in IDE main window and selecting the parent
LibreClinica folder created by git checkout process or the main pom.xml
file in this folder. This will trigger the load of LibreClinica with all
its modules (currently there are 4 modules: core, odm, web and ws).

## Target JDK

One need to configure the project JDK, which should correspond to the
supported version of JDK installed in specific folder in previous steps.

1.  `File > Project Structure`
2.  Under *Project Settings* click on *Project*
3.  Under *Project SDK* click on *Add SDK* and then click *JDK...*
4.  Browse to the path of your JDK and click *OK*
5.  Under *Project language level* click on *6 - @Override in
    interfaces*
6.  Click *OK* to close the *Project Structure* window

## Run/Debug Configuration

Use `Run > Edit Configurations` option in menu and click on *plus* icon
to set up new *Tomcat Server local* configuration. You can name it
according to your choice (e.g. local-lc). Continue with setup of
application server.

1.  In *Server* tab, click on *Configure* next to *Application server*
2.  Click on *plus* icon to add new Tomcat application server
3.  Browse to the path of your Tomcat
4.  Click on *OK*
5.  You can name your application server if necessary
6.  Click *OK* to close Application Servers dialog

Followed by configuration of JRE runtime.

1.  For *JRE* it should be possible to select runtime environment from
    previously selected project JDK

Before launch, you may remove the build option with *minus* icon and
instead define Maven goal that should be executed in order to create
project deployable artifact. This means that Maven will be responsible
for build and not the IDE itself.

1.  Click on *plus* icon
2.  Select *Run Maven Goal* item
3.  Specify executing of Maven `clean install`
4.  Confirm with *OK*

On a next *Deployment* tab.

1.  Use *plus* icon under *Deploy at the server startup*
2.  Select *Artifact...*
3.  Choose `LibreClinica-web:war`
4.  Confirm with *OK*
5.  Change application context to `/LibreClinica`
6.  You can remove the *build artifact* step from *Before launch*
    section (with *minus* icon) as this task will be executed as Maven
    goal

## Maven Configuration

Open the Maven tool window at `View > Tool Windows > Maven`. Maven
should download dependencies based on changes in module pom files. You
may want to click on *Reload All Maven Projects* button, but it should
not be necessary. What is necessary for *Maven install* goal to execute
properly is toggle `Skip Tests mode` (flash in circle button). There is
limited amount of unit tests in LibreClinica. Those that are there will
fail because of default database configuration and should not be
considered for now. Making LibreClinica unit testable is one of our
goals for future.

## Debug

After this you should be able to execute `Run > Debug`. Maven should
compile LibreClinica, package it into *war* archive and IDE will perform
deployment to configured Tomcat should take place. However, the first
start of application will fail with deployment error due to missing
config files.

You need to create `libreclinica.config` folder in the folder where you
installed the Tomcat (TOMCAT\_HOME). Locate the deployed LibreClinica in
the *webapps* folder of your Tomcat and copy the *datainfo.properties*
template file from there.

You need to edit the `datainfo.properties` files in the folders core/src/main/resources/org/akaza/openclinica
and (TOMCAT\_HOME)/libreclinica_config to point to your database on developer machine. The usual configuration follows:

``` {.sourceCode .bash}
dbType=postgres
dbUser=clinica
dbPass=clinica
db=libreclinica
dbPort=5432
dbHost=localhost
```

Restart the application in Debug mode (`Run > Debug`). If you provided
correct DB configuration and database server is running, the database
scheme will be initialised on deployment (executing all *liquibase*
changelogs).

# Running System

Running system is listening on below-mentioned URL, with one Default
Study where the root user is assigned to.

[<http://localhost:8080/LibreClinica>](http://localhost:8080/LibreClinica)

Default user credentials are: User Name: root; Password: 12345678
