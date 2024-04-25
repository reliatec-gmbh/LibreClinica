Cypress.on('uncaught:exception', (err, runnable) => {
    console.log(err);
    return false;
});
describe('Enrollment form', () => {
    it('Login via OAuth',  () => {
        cy.visit('http://localhost:8080/LibreClinica/pages/login/login')
        cy.url().should('include', 'http://localhost:8080/LibreClinica/pages/login/login')
        cy.get('a:contains("OAuth")').should('exist').click()
        cy.origin('cdcoauth.alagant.com', () => {
            cy.get('[id="username"]').should('exist').type('test')
            cy.get('[id="password"]').should('exist').type('test')
            cy.get('button').should('exist').click()
        });

        /*
        cy.url().as('urlOriginal');

        let subjectId="";
         */
        /*
        cy.contains("Drug Management").click()
        cy.origin("https://dmm-qa.alagant.com", () => {
            console.clear()
            cy.get('[href="/subjects"]').should("exist").click()
            cy.get("div.main-content table tbody tr").should("exist")
                .first().find('td').first().should('exist')
                .then(($el) => $el.innerText)
                .as("subjectId")

                cy.get("@subjectId")
        })
        cy.wait(500)
        cy.visit("http://localhost:8080/LibreClinica/MainMenu")
        cy.origin('/LibreClinica/MainMenu', () => {
            console.log(cy.get('@subjectId'))
        })
        */


        /*

        const args = {"subjectId": subjectId };
        */




        cy.contains("Tasks").trigger('mouseover')
        cy.contains("Add Subject").trigger('mouseover').click()
        cy.get('form[action="AddNewSubject"]').should('exist')
        cy.get('form[action="AddNewSubject"] input[type="text"]').eq(0).should('exist')
            .type('38-01-00002')
        cy.get('form[action="AddNewSubject"] input[type="text"]').eq(1).should('exist')
            .type('38-01-00002')
        cy.get('form[action="AddNewSubject"] input[type="text"]').eq(2).should('exist')
            .type('156')
        cy.get('form[action="AddNewSubject"] input[type="text"]').eq(3).should('exist')
            .type(  '01-JAN-2024')
        cy.get('form[action="AddNewSubject"] select').eq(0).should('exist')
            .select( 'm')
        cy.get('form[action="AddNewSubject"] input[type="text"]').eq(4).should('exist')
            .type(  '01-JAN-2000')
        cy.contains("Save and Assign Study Event").click()

        cy.contains("Drug Management").click()
        cy.origin("https://dmm-qa.alagant.com", () => {
            console.clear()
            cy.get('[href="/randomize"]').should("exist").click()
            cy.get('button[type="submit"]').should("exist").click()
            /*
            cy.get("div.main-content table tbody tr").should("exist")
                .first().find('td').first().should('exist')
                .then(($el) => $el.innerText)
                .as("subjectId")

            cy.get("@subjectId")*/
        })
    })
});

