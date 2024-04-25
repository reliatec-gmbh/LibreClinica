## CI/CD demo
(1) simple change is manually made to some Java source code file in LibreClinica - Add an extra print statement in the LibreClinica landing page (page that opens after login) as -- "hello world”;
(2) Manually check in  to Github with a Github Commit comment - “Added hello world on landing page”;
(3) Once step 2 is complete then CI/CD pipeline is started manually;
(4) LibreClinica is rebuilt automatically with the change from step #1 & 2 and resulting executables go into Docker container;
(5) A PDF release note is automatically (not manually) created which has the Github commit comment from step #2 and this PDF release note is automatically placed in Confluence;
(6) Azure DevOp Pipeline then automatically runs a single automated test (may be a very, very simple automated test) on the new LibreClinica executable in Docker container from step #4;
(7) The result of the test from step #6 is automatically (not manually) placed on Confluence as text, html, markup, PDF or something else that people going to Confluence can read;
(8) Login to the newly built LibreClinica running on Azure in a Docker container (from step #4) using the LibreClinica root login;
(9) Some simple action can be performed in LibreClinica manually to show it is properly running (for example, manually entering in the general info for a new drug trial/study into LibreClinica and saving it). I also want to see the change was made in step 1, and that this step 9 shows the system is still working and whatever change was made (extra print statement, for example) shows up when the system is run. 
(10) Manually open the confluence page and see/download the PDF release note (step #5) and the test results (step #7).

## Steps to reproduce
Open the project in IntelliJ IDEA, as mentioned in the development documentation
Build the project and start the local web server
Check the hello world message in the login page
Open the autodeployed site, and check that the hello world message is identical to those in the local webs server login page
Check the hello world message in the web/src/main/resources/org/akaza/openclinica/i18n/debug.properties file in the line starting with 'test.message='
Commit the changes to the pipeline git repository, to the master branch
Check that the pipeline is triggered
While the pipeline runs, rebuild the project in IntelliJ IDEA and restart the local web server
Check that the hello world message is updated in the local web server login page
Check that the pipeline is finished successfully
Check that the hello world message is updated in the autodeployed site
Check that 2 new files are added to the Confluence page, one for the release note and one for the test results