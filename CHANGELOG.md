## 1.4.0 (June 13, 2025)

**bugfix:**

* fixed textareas dropping entries > 2000 characters (#391)
* AuditLogEvent value length harmonisation (#431)
* Username DB column length hramonisation (#429)
* fixed data extraction SQL failing on PostgreSQL >= 15
* make SOAP study listAllRequest showing also child studies/sites for monitor users (#406)
* allow discrepancy notes to be added to empty fields (#404)
* fixed inconsistent preselection of discrepancy note type in repeating groups (#343) by setting it to Annotation for data entry (#414)
* make flags for discrepancy notes change to correct color (#417)
* not offer datamanagers with disabled accounts for configuration of new studies (#422)

**maintenance:**

* link for unsupported rule designer removed (#128)
* removed unused logos (#403, #410)

**enhancement:**

* ListUserAccountsView: provide information on parent study (#419)

**tests:**
