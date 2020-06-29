---
name: Bug report
about: Report a LibreClinica bug
title: "[BUG] "
labels: bug
assignees: ''

---

**Description:**
A clear description of the bug.

**Requirements:**
List the requirements that need to be fulfilled to follow the steps to reproduce the issue.

e.g.  
_A study X with some basic configuration to enter data and at least one CRF that contains a regular expression like this for the input field validation.
> regexp:/[1-9][0-9]|[1-9][0-9]\\.[0-9]/_

**Steps to follow:**
1. login
1. open the CRF in DataEntry mode
1. enter the value `79.5` for the input field I1
1. click save

**Expected result:**
Describe the result that is expected after the steps have been followed.

e.g.  
_The input field with the above mentioned regular expression will accept the value `79.5` and the form can be saved successfully._

**Actual result:**
Describe the result that is observerd after the steps have been followed and the bug is present.

_The input field with the above mentioned regular expression will not accept the value `79.5` but the value `79\.5` and saving the form with the value `79.5` set will result in an error message._

**Related (optional):**
List related issues.

**Screenshots (optional):**
If applicable, add screenshots to help explain your problem.

**Server Setup (optional):**
 - OS [e.g. Debian 10]
 - Application Server [e.g. Tomcat-9]
 - Java [e.g. openjdk-14-jdk]
 - Postgresql [e.g. 12]

**Additional context (optional):**
Add any other context about the problem here.
