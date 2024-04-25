Cypress.on('uncaught:exception', (err, runnable) => {
    return false;
});

//Funcion la cual permite obtener la fecha actual en formato DD-MM-YYYY donde MM es el mes y se describe con palabras

function getFormattedDate (date) {
    var month = date.getMonth() + 1;
    var day = date.getDate();
    var year = date.getFullYear();
    var monthName = "";
    switch (month) {
        case 1:
            monthName = "Jan";
            break;
        case 2:
            monthName = "Feb";
            break;
        case 3:
            monthName = "Mar";
            break;
        case 4:
            monthName = "Apr";
            break;
        case 5:
            monthName = "May";
            break;
        case 6:
            monthName = "Jun";
            break;
        case 7:
            monthName = "Jul";
            break;
        case 8:
            monthName = "Aug";
            break;
        case 9:
            monthName = "Sep";
            break;
        case 10:
            monthName = "Oct";
            break;
        case 11:
            monthName = "Nov";
            break;
        case 12:
            monthName = "Dec";
            break;
    }
    return day + "-" + monthName + "-" + year;
}
function getNowDate() {
    return getFormattedDate(new Date());
}

//Funcion la cual me permite generar una fecha aleatoria con un rengo de 24 años hacia atras en formato DD-MM-YYYY donde MM es el mes y se describe con palabras
function randomDate() {
    var startDate = new Date(1986, 0, 1);
    var endDate = new Date(2005, 0, 1);
    var spaces = (endDate.getTime() - startDate.getTime());
    var timestamp = Math.round(Math.random() * spaces);
    timestamp += startDate.getTime();
    return getFormattedDate(new Date(timestamp));
}

describe('Load page', () => {
    it('Visits the LibreClinica', () => {
        cy.visit('http://localhost:8080/LibreClinica/pages/login/login')
        cy.contains('Login')
    })
    it('Login and navegate for main pages', () => {
        cy.visit('http://localhost:8080/LibreClinica/pages/login/login')
        cy.url().should('include', 'http://localhost:8080/LibreClinica/pages/login/login')
        cy.get('[id="username"]').should('exist').type('root')
        cy.get('[id="j_password"]').should('exist').type('kevin12345')
        cy.get('[name="submit"]').should('exist').click()
        cy.url().should('include', 'http://localhost:8080/LibreClinica/MainMenu')
        cy.contains('Welcome to')
        cy.contains('Subject Matrix').click()
        cy.contains('Subject Matrix')
        cy.contains('Add New Subject').click()
        cy.get('[id="enrollmentDateField"]').should('exist').type(getNowDate())
        cy.get('[name="gender"]').should('exist').select(1);
        cy.get('[id="dobField"]').should('exist').type(randomDate())
        cy.get('[name="studyEventDefinition"]').should('exist').select(1);
        cy.get('[name="addSubject"]').should('exist').click()
        cy.get('a[href="#"][onclick*="checkCRFLockedInitial"][onmousedown*="setImage"][onmouseup*="setImage"]').should('exist').parent().first().click()
        cy.get('[id="input637"]').should('exist').type('30')
        cy.get('[id="input638"]').should('exist').type(getNowDate())
        cy.get('[id="input639"]').should('exist').type(randomDate())
        cy.get('[id="input641"]').should('exist').type(getNowDate())
        cy.get('[id="input642"]').should('exist').type(getNowDate())
        cy.get('[id="input644"]').should('exist').type('170')
        cy.get('[value="cm"]').should('exist').check()
        cy.get('[id="input646"]').should('exist').type('75')
        cy.get('[value="kg"]').should('exist').check()
        cy.get('[id="input648"]').should('exist').type(getNowDate())
        cy.get('[id="input649"]').should('exist').type(getNowDate())
        cy.get('[value="N"]').should('exist').check()
        cy.get('[id="input651"]').should('exist').type(getNowDate())
        cy.get('[id="input653"]').should('exist').type('Kevin')
        cy.get('[id="input654"]').should('exist').type(getNowDate())
        cy.get('[id="srl"]').should('exist').click()

        cy.get('.table_header_column_top')
            .contains('Study Subject ID')
            .next()
            .invoke('text')
            .then((subjectId) => {
                cy.log('El "Subject ID" deseado es: ' + subjectId);
                cy.contains('Subject Matrix').click()
                function navigateToNextPage(text) {
                    cy.get('body').then(($body) => {
                        if ($body.text().includes(text)) {
                            cy.log('Study Subject ID: ' + text + ' found on the current page.');
                            return;
                        }
                        cy.get('a > img[src="/LibreClinica/images/table/nextPage.gif"], a > img[src="/LibreClinica/images/table/nextPageDisabled.gif"]', { timeout: 10000 }).then($nextPage => {
                            if ($nextPage.length > 0 && $nextPage.attr('src') === "/LibreClinica/images/table/nextPage.gif") {
                                cy.wrap($nextPage).click();
                                navigateToNextPage(text);
                            }
                        });
                    });
                }
                navigateToNextPage(subjectId);
            });
    })
});