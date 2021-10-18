## 1.1.0 (Oktober 18, 2021)


## 1.1.0 (Oktober 18, 2021)

**bugfix:**
* Deletion of unused CRF version fails with opps page (#271)
* fixed stylesheet for 404 page (#256)
* fixed oops error in view events page (#246)
* fixed oops error when displaying subject without gender (#241)
* fixed filtering of values displayed in tables (#200, #244, #245)
* fixed oops error in In Build Study, rule management (#238)
* fixed CRF empty printable form is not loading (#232)
* fixed rule import for rules using form OIDs (#227)
* adapted escape expression to PostgreSQL 9 (#184)
* fixed expand icon for collapsed icon panel (#176)
* fixed LDAP issue (#174)

**maintenance:**
* increase database performance (#249, #268)
* updated dependency version (#265)
* removed unused SQL query (#235)
* remove duplicate classes from classpath (#197)

**enhancement:**
* Enable SOAP compatible password generation (#248)
* fixed issues for LibreClinica-ws (#205, #207)
* documentation on LDAP (#202)
* add LDAPS support (#186)

**tests:**
* created test specification T003-08 for LDAP user creation (#217)
* created tests specifications
  * T011 subject matrix
  * T012 subjects
  * T013 study events
  * T015 double data entry

