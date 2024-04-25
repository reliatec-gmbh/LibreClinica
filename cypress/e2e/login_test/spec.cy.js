Cypress.on('uncaught:exception', (err, runnable) => {
  return false;
});

describe('Load page', () => {
  it('Visits the LibreClinica', () => {
    cy.visit('http://localhost:8080/LibreClinica/pages/login/login')
    cy.contains('Login')
  })
  it('Login with normal creds', () => {
    cy.visit('http://localhost:8080/LibreClinica/pages/login/login')
    cy.url().should('include', 'http://localhost:8080/LibreClinica/pages/login/login')
    cy.get('[id="username"]').should('exist').type('root')
    cy.get('[id="j_password"]').should('exist').type('kevin12345')
    cy.get('[name="submit"]').should('exist').click()
    cy.url().should('include', 'http://localhost:8080/LibreClinica/MainMenu')
    cy.contains('Welcome to')
    cy.contains('Tasks').trigger('mouseover')
    cy.get('[href="/LibreClinica/ListUserAccounts"]').should('exist').click()
    cy.get('[href="CreateUserAccount"]').first().click()
    cy.contains('Create a User Account')
    cy.get('[id="userName"]').should('exist').type('test'+Math.floor(Math.random() * 100000))
    cy.get('[id="firstName"]').should('exist').type('test'+Math.floor(Math.random() * 100000))
    cy.get('[id="lastName"]').should('exist').type('test'+Math.floor(Math.random() * 100000))
    cy.get('[id="email"]').should('exist').type(Math.floor(Math.random() * 100000)+'krystof.olik@alagant.com')
    cy.get('[id="institutionalAffiliation"]').should('exist').type('test')
    cy.get('[id="activeStudy"]').select('1')
    cy.wait(3000)
    cy.url().should('include', 'http://localhost:8080/LibreClinica/CreateUserAccount')
    cy.get('[id="role"]').select('3')
    cy.get('[id="type"]').select('2')
    cy.get('[name="Submit"]').should('exist').click({multiple: true})
  })
})