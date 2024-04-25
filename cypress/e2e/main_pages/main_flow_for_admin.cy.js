/*Cypress.on('uncaught:exception', (err, runnable) => {
    return false;
});*/

const path = "http://localhost:8080";
describe('Load page', () => {
    it('Visits the LibreClinica', () => {
        cy.visit(path+'/LibreClinica/pages/login/login')
        cy.contains('Login')
    })
    it('Login and navegate for main pages', () => {
        cy.visit(path+'/LibreClinica/pages/login/login')
        cy.url().should('include', path+'/LibreClinica/pages/login/login')
        cy.get('[id="username"]').should('exist').type('root')
        cy.get('[id="j_password"]').should('exist').type('kevin12345')
        cy.get('[name="submit"]').should('exist').click()
        cy.url().should('include', path+'/LibreClinica/MainMenu')
        cy.contains('Welcome to')
        cy.contains('Change Study/Site')
        cy.contains('Log Out')
        cy.contains('Home').click()
        cy.contains('Subject Matrix').click()
        cy.contains('Notes & Discrepancies').click()
        cy.contains('Study Audit Log').click()

        cy.contains('Tasks').trigger('mouseover')
        cy.contains('Submit Data')
        cy.get('a:contains("Subject Matrix")').should('exist').last().click()
        cy.contains('Subject Matrix')


        cy.contains('Tasks').trigger('mouseover')
        cy.contains('Submit Data')
        cy.contains('Schedule Event').click()
        cy.contains('Schedule Study Event')

        cy.contains('Tasks').trigger('mouseover')
        cy.contains('Submit Data')
        cy.contains('Add Subject').click()
        cy.contains('Add Subject')

        cy.contains('Tasks').trigger('mouseover')
        cy.contains('Submit Data')
        cy.contains('View Events').click()
        cy.contains('View All Events in')

        cy.contains('Tasks').trigger('mouseover')
        cy.contains('Submit Data')
        cy.get('[href="/LibreClinica/ViewNotes?module=submit"]').last().click()
        cy.contains('Notes and Discrepancies')

        cy.contains('Tasks').trigger('mouseover')
        cy.contains('Submit Data')
        cy.contains('Import Data').click()
        cy.contains('Import CRF Data')

        cy.contains('Tasks').trigger('mouseover')
        cy.contains('Submit Data')
        cy.contains('Source Data Verification').click()
        cy.contains('Source Data Verification for')

        cy.contains('Tasks').trigger('mouseover')
        cy.contains('Submit Data')
        cy.get('[href="/LibreClinica/StudyAuditLog"]').last().click()
        cy.contains('View Study Log for')

        cy.contains('Tasks').trigger('mouseover')
        cy.contains('Submit Data')
        cy.contains('View Datasets').click()
        cy.contains('View Dataset')

        cy.contains('Tasks').trigger('mouseover')
        cy.contains('Submit Data')
        cy.contains('Create Dataset').click()
        cy.contains('Create Dataset')

        cy.contains('Tasks').trigger('mouseover')
        cy.contains('Submit Data')
        cy.contains('Users').click()
        cy.contains('Manage All Users In')

        cy.contains('Tasks').trigger('mouseover')
        cy.contains('Submit Data')
        cy.contains('View Study').click()
        cy.contains('Download the study metadata')

        cy.contains('Tasks').trigger('mouseover')
        cy.contains('Submit Data')
        cy.contains('Subjects').click()
        cy.contains('Administer Subjects')

        cy.contains('Tasks').trigger('mouseover')
        cy.contains('Submit Data')
        cy.contains('Jobs').click()
        cy.contains('Administer All Jobs')

        cy.contains('Tasks').trigger('mouseover')
        cy.contains('Submit Data')
        cy.contains('Studies').click()
        cy.contains('Administer Studies')

        cy.contains('Tasks').trigger('mouseover')
        cy.contains('Submit Data')
        cy.contains('Users').click()
        cy.contains('Manage All Users In')

        cy.contains('Tasks').trigger('mouseover')
        cy.contains('Submit Data')
        cy.contains('CRFs').click()
        cy.contains('Manage Case Report Forms (CRFs)')

        cy.contains('Tasks').trigger('mouseover')
        cy.contains('Submit Data')
        cy.contains('Update Profile').click()
        cy.contains('Change User Profile')

        cy.contains('Tasks').trigger('mouseover')
        cy.contains('Submit Data')
        cy.contains('Log Out').click()
    })
});