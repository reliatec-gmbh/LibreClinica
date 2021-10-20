# installation instructions for LibreClinica-1.0 - 1.1 on Debian 10 (Buster)

**prerequisite**  
It is expected that you have experience on how to install software and how to edit text files on Debian. 
Furthermore it is expected that you have a running Debian 10 (buster) installation with shell access and 
that the user you use for the installation is a member of the sudo group. For running LibreClinica you need a working 
mail transfer agent (MTA) to allow LibreClinica to send emails. How to install, configure and run a MTA is not explained
in this instruction.  
**This installation instruction is not a complete guide to run a productive LibreClinica instance since it is missing steps
to secure your server like configuring the firewall or enabling https. But it is sufficient for your system administrator
to get you a working copy of LibreClinica.**

_This installation instruction is a blue print for a successful LibreClinica installation. If you follow these steps
literally you end up with a working LibreClinica instance. Experienced users can modify the instructions to their needs
when necessary but should be aware that there are dependencies between the steps and that changes to one step might require
changes to another step._

1. **install required software components:**
    1. update package list: `sudo apt update`
    1. install software: `sudo apt install tomcat9 postgresql-11`  
        _openjdk-11 is installed as a dependency of tomcat9_
1. **setup directories:**  
    _The folder in this example is named libreclinica since this is the default path used when the
    *.war archive copied is named libreclinica.war. If you like to name the folder differently then name your
    war archive differently or configure the datainfo.properties (step 5) to use a different folder._
    1. configuration directory: `sudo mkdir -p /usr/share/tomcat9/libreclinica/config`
    1. log directory: `sudo mkdir /usr/share/tomcat9/libreclinica/logs`
    1. data directory: `sudo mkdir /usr/share/tomcat9/libreclinica/data`
    1. change owner: `sudo chown -R tomcat:tomcat /usr/share/tomcat9/libreclinica`
    1. create softlink: `sudo ln -s /usr/share/tomcat9/libreclinica/config/ /usr/share/tomcat9/libreclinica.config`
1. **setup database**
    1. create role: `sudo -u postgres createuser -e -I -D -R -S -P clinica`  
        *update datainfo.properties with the password you entered for the new postgres user (step 5)*
    1. create database: `sudo -u postgres createdb -e -O clinica -E UTF8 libreclinica`
1. **copy the \*.war archive** to the webapps folder  
    `cp LibreClinica-web-<version>.war /var/lib/tomcat9/webapps/<context name>.war`  
    `chown tomcat:tomcat /var/lib/tomcat9/webapps/<context name>.war`  
    *context name is the name that comes usually after the slash  
    e.g. for https://libreclinica.org/libreclinica and LibreClinica 1.1 the above copy command would read  
    cp LibreClinica-web-1.1.0.war /var/lib/tomcat9/webapps/libreclinica.war*
1. **create datainfo.properties**  
   You can create your own version of datainfo.properties or copy a template from LibreClinica by executing the command  
   ```
   sudo -u tomcat cp \
      /var/lib/tomcat9/webapps/libreclinica/WEB-INF/classes/datainfo.properties \
      /usr/share/tomcat9/libreclinica.config/
   ```
1. **configure datainfo.properties**  
    Edit /usr/share/tomcat9/libreclinica.config/datainfo.properties to match your requirements.  
    _Detailed instructions on how to configure it properly can be found in the different 
    sections of the datainfo.properties._  
    Make sure, that tomcat can read the file by issuing  
    `chmod o+r /usr/share/tomcat9/libreclinica.config/datainfo.properties`
    
    **relevant keys for your datainfo.properties**  
    For a common installation it should be sufficient to change the following keys:
    1. **database**
        * dbType=postgres
        * dbUser=clinica
        * dbPass=SecretPassword
        * db=libreclinica
        * dbPort=5432
        * dbHost=localhost
    1. **email server**
        * mailHost=smtp.example.com
        * mailPort=25|465|custom port
        * mailProtocol=smtp|smtps
        * mailUsername=SecretUser
        * mailPassword=SecretPassword
        * mailSmtpAuth=true|false
        * mailSmtpStarttls.enable=true|false
        * mailSmtpsAuth=true|false
        * mailSmtpsStarttls.enable=true|false
        * mailSmtpConnectionTimeout=5000
    1. **misc**
        * mailErrorMsg=support@example.com
        * adminEmail=admin@example.com
        * sysURL=https://example.com/libreclinica/MainMenu
             
    For an enterprise installation you may want to additionally enable user authentication against an LDAP/Active Directory server:
    
    <details>
    <summary>Click to expand!</summary>
   
    1. **LDAP/Active Directory server**
    
        * ldap.enabled=true
        
        LDAP/ActiveDirectory server host can be configured with standard (usually port 389) or encrypted communication (usually port 636):
        
        * ldap.host=ldap://dc.example.com:389|ldaps://dc.example.com:636
        
        Distinguished name of LDAP/ActiveDirectory service user account and its password. The actual components of distinguished name depends on object organisation hierarchy that your LDAP/ActiveDirectory server uses: 
        
        * ldap.userDn=cn=SecretLdapServiceUser,ou=service,ou=department,dc=example,dc=com                        
        * ldap.password=SecretLdapServiceUserPassword
                	
        Query that returns LDAP/ActiveDirectory account based on user name entered on login screen:
        
        * ldap.loginQuery=(sAMAccountName={0})
   
        Base DN where discoverable LDAP/ActiveDirectory user accounts needs to belong to:       
        
        * ldap.userSearch.baseDn=ou=department,dc=example,dc=com
       
        Query that returns LDAP/ActiveDirectory user account based on user name entered on add LDAP user screen    
       
        * ldap.userSearch.query=(&(objectClass=person)(sAMAccountName=\*{0}*)) 
        
        Properties that define parameters mapping between LDAP/ActiveDirectory user account and associated LibreClinica user account:
        
        * ldap.userData.distinguishedName=distinguishedName         
        * ldap.userData.username=sAMAccountName         
        * ldap.userData.firstName=givenName        
        * ldap.userData.lastName=sn         
        * ldap.userData.email=mail     
        * ldap.userData.organization=company      
    
    If you choose to use encrypted LDAPS communication you may need to perform additional steps depending on certificate that your LDAP/Active Directory server uses. If it is globally trusted then it should work directly. However more common is situation in which LDAP/Active Directory server provides self singed certificate and Java application such as LibreClinica will not consider this as trusted which results to SSL handshake breakage. If this is the case, it is necessary to obtain the certificate of LDAP server and import it into java cacerts default keystore (e.g. using the keytool):
    
    `JAVA_HOME/bin/keytool -importcert -file serverca.cer -keystore JAVA_HOME/jre/lib/security/cacerts -alias "serveraliasca"`
    
    One can check if the certificate was installed (e.g. using keytool):
    
    `JAVA_HOME/bin/keytool -list -keystore JAVA_HOME/jre/lib/security/cacerts`
    </details>
    
1. **setup ReadWritePaths**  
    edit /etc/systemd/system/multi-user.target.wants/tomcat9.service and  
    add `ReadWritePaths=/usr/share/tomcat9/libreclinica`  
    and reload the unit files with `systemctl daemon-reload`
1. **restart tomcat** `systemctl restart tomcat9`

You now should be able to access your LibreClinica installation port 8080. e.g.  
http://\<ip of your machine\>:8080/libreclinica with the default credentials (user: root, password: 12345678).

In a productive environment your system administrator should configure a web server 
like nginx or apache to act as a reverse proxy for your LibreClinica installation so that
you can access your installation from the URL configured for key _sysURL_.

# Troubleshooting
* **Problem:** On some systems an error 500 (Message "Servlet.init() for servlet [pages] threw exception") appears when requesting the login-screen for the very first time
* **Cause:** 2 versions of the castor library in the WAR
* **Fix:** delete castor-1.2.jar from the WEB-INF/lib by issuing ``rm delete castor-1.2.jar``. If you followed the instructions above, the extracted WAR containing WEB-INF/lib should be found under /var/lib/tomcat9/webapps/.
