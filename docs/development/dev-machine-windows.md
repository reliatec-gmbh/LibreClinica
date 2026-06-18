# Setting Up a LibreClinica Development Environment on Windows

> **Note:** This guide documents the setup process as tested on Windows 11
> with IntelliJ IDEA 2026.1 (March 2026). While the build process and core
> data entry functionality have been verified, some LibreClinica features
> involving file path handling may behave differently on Windows compared to
> Linux. Known edge cases should be reported as issues. Contributions to
> expand or correct this guide are welcome.

This guide documents the full process for setting up a LibreClinica development
environment on Windows. It complements the existing Linux-focused documentation
and covers several Windows-specific issues not currently addressed in the official
developer documentation.

Tested on: Windows 10/11, IntelliJ IDEA 2026.1, OpenJDK 8 + 11, Maven 3.9.x,
PostgreSQL 14, Tomcat 9.

---

## Prerequisites

### Required tools

| Tool | Version | Download |
|------|---------|----------|
| Git | Latest | https://git-scm.com/downloads |
| OpenJDK 8 | Temurin 8 LTS | https://adoptium.net/temurin/releases/?version=8 |
| OpenJDK 11 | Temurin 11 LTS | https://adoptium.net/temurin/releases/?version=11 |
| Apache Maven | 3.9.x | https://maven.apache.org/download.cgi |
| Apache Tomcat | 9.x | https://tomcat.apache.org/download-90.cgi |
| PostgreSQL | 13 or 14 | https://www.postgresql.org/download/windows/ |
| IntelliJ IDEA | Community or Ultimate | https://www.jetbrains.com/idea/download/ |

### Recommended folder structure

Keep all Java tooling in a dedicated `C:\tools\` directory, completely separate
from any existing web development stacks (e.g. WAMP, XAMPP):

```
C:\
+-- tools\
|   +-- jdk8\          (OpenJDK 8 - required for the ODM module build)
|   +-- jdk11\         (OpenJDK 11 - default JDK for the project)
|   +-- maven\         (Apache Maven - must contain bin\mvn.cmd directly)
|   +-- tomcat9\
|       +-- apache-tomcat-9.x.x\
|           +-- libreclinica.config\   (create this folder manually)
+-- dev\
    +-- LibreClinica\  (your cloned repository)
```

> **Important:** Extract Maven so that `C:\tools\maven\bin\mvn.cmd` exists
> directly. Do not nest it inside a version-named subfolder or Maven will not
> be found on the PATH.

---

## Step 1 - Configure environment variables

After installing the tools, configure your system PATH. Open
**Edit the system environment variables** from the Windows search bar, click
**Environment Variables**, then edit the **System** Path variable.

Add the following entries:

```
C:\tools\jdk11\bin
C:\tools\maven\bin
```

Also create a new System variable:

```
Variable name:  JAVA_HOME
Variable value: C:\tools\jdk11
```

> **Important:** After saving, close and reopen any terminal windows. Existing
> sessions will not pick up the new PATH.

Verify in a new PowerShell window:

```powershell
java -version    # should show openjdk 11.x.x
javac -version   # should show javac 11.x.x
mvn -version     # should show Apache Maven 3.x.x
```

---

## Step 2 - Fork and clone the repository

### Fork on GitHub

Go to `https://github.com/reliatec-gmbh/LibreClinica` and click **Fork** to
create your personal copy.

### Clone your fork

```powershell
cd C:\dev
git clone https://github.com/YOUR-USERNAME/LibreClinica.git
cd LibreClinica
```

### Add the upstream remote

```powershell
git remote add upstream https://github.com/reliatec-gmbh/LibreClinica.git
git remote -v
```

You should see both `origin` (your fork) and `upstream` (the official repo).

### Switch to the development branch

All contributions target `lc-develop`, not `master`:

```powershell
git checkout lc-develop
git pull upstream lc-develop
```

### Create a feature branch for your work

```powershell
git checkout -b fix/your-issue-description-123
```

---

## Step 3 - Configure Maven for Windows

### Known issue: jitpack.io dependency corruption

On Windows, Maven may download corrupted (empty) JAR files from `jitpack.io`
for certain dependencies. This manifests as:

```
error reading ...xml-apis-ext-1.3.04.jar; zip file is empty
```

**Fix:** Create a `settings.xml` file in your Maven user directory to redirect
jitpack requests to Maven Central. Use PowerShell to write the file cleanly
without BOM encoding issues:

```powershell
[xml]$xml = [xml]'<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
  http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <mirrors>
    <mirror>
      <id>block-jitpack</id>
      <name>Redirect jitpack to Central</name>
      <url>https://repo1.maven.org/maven2</url>
      <mirrorOf>jitpack.io</mirrorOf>
    </mirror>
  </mirrors>
</settings>'
$xml.Save("C:\Users\$env:USERNAME\.m2\settings.xml")
```

> **Note:** Do not create this file using Windows Notepad. Notepad adds a BOM
> character that makes the XML unparseable by Maven. Always use PowerShell or
> a code editor such as VS Code.

### Known issue: IntelliJ Maven sync warning

When opening the project in IntelliJ, the Maven sync panel may show:

```
Cannot resolve plugin org.eclipse.m2e:lifecycle-mapping:1.0.0
```

This is an Eclipse-specific plugin referenced in the `pom.xml` for Eclipse IDE
users. It has no effect on IntelliJ builds and can be safely ignored. To
suppress the warning in IntelliJ go to **Settings -> Build Tools -> Maven ->
Ignored Files** and add `org.eclipse.m2e:lifecycle-mapping:1.0.0` to the
path patterns field.

---

## Step 4 - Java version requirements (critical for Windows)

LibreClinica requires **two JDK versions** to build successfully on Windows:

| Module | Required JDK | Reason |
|--------|-------------|--------|
| ODM | **Java 8** | `jaxb2-maven-plugin:2.3` uses `com.sun.codemodel` classes removed in Java 9+ |
| Core, Web, WS | Java 11 | Standard project requirement |

### Building with Java 8 for the ODM module

The `jaxb2-maven-plugin:2.3` used by the ODM module depends on internal Sun
classes that were removed from Java 9 onwards. Building with Java 11 as
`JAVA_HOME` causes:

```
A required class was missing: com/sun/codemodel/CodeWriter
```

**Fix:** Temporarily override `JAVA_HOME` to Java 8 for the build session:

```powershell
$env:JAVA_HOME = "C:\tools\jdk8"
$env:PATH = "C:\tools\jdk8\bin;" + $env:PATH
java -version    # confirm: openjdk version "1.8.x"
mvn clean install -DskipTests
```

This overrides Java only for the current PowerShell session. Your system
default remains Java 11.

---

## Step 5 - Set up the PostgreSQL database

Open pgAdmin or psql and run:

```sql
CREATE USER clinica WITH PASSWORD 'clinica';
CREATE DATABASE libreclinica OWNER clinica ENCODING 'UTF8';
```

Verify the connection:

```powershell
psql -U clinica -d libreclinica -h localhost
```

---

## Step 6 - Open the project in IntelliJ IDEA

1. Open IntelliJ -> **File -> Open** -> select `C:\dev\LibreClinica\pom.xml` ->
   **Open as Project**
2. When prompted, set the Project SDK to your **OpenJDK 11** installation:
   **File -> Project Structure -> Project -> SDK -> Add JDK -> C:\tools\jdk11**
3. Open the Maven tool window: **View -> Tool Windows -> Maven**
4. Click **Reload All Maven Projects** (circular arrows icon)
5. In the Maven panel, enable **Skip Tests** mode (lightning bolt icon) --
   the existing tests require a running database and will fail without one

---

## Step 7 - Configure Tomcat in IntelliJ

1. Go to **Run -> Edit Configurations -> + -> Tomcat Server -> Local**
2. Set the application server path to your Tomcat installation:
   `C:\tools\tomcat9\apache-tomcat-9.x.x`
3. Under the **Deployment** tab, add the `LibreClinica-web` artifact

---

## Step 8 - Configure datainfo.properties

Create the config directory inside your Tomcat installation:

```powershell
New-Item -ItemType Directory -Path "C:\tools\tomcat9\apache-tomcat-9.x.x\libreclinica.config"
```

After the first build, copy the template config file:

```powershell
Copy-Item "C:\dev\LibreClinica\web\src\main\webapp\WEB-INF\classes\datainfo.properties" "C:\tools\tomcat9\apache-tomcat-9.x.x\libreclinica.config\datainfo.properties"
```

Edit `datainfo.properties` with your local database settings:

```properties
dbType=postgres
dbUser=clinica
dbPass=clinica
db=libreclinica
dbPort=5432
dbHost=localhost
```

> **Important:** Keep this file outside the repository. It contains local
> credentials and is excluded from version control by design.

---

## Step 9 - Run the application

In IntelliJ, select your Tomcat configuration and click **Run -> Debug**.

Maven will compile the project, package it into a `.war` archive, and deploy it
to Tomcat. On first start, Liquibase will automatically initialise the database
schema.

Access the running application at:

```
http://localhost:8080/LibreClinica
```

Default credentials:

```
Username: root
Password: 12345678
```
