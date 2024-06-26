/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.logic.expressionTree;

import org.akaza.openclinica.exception.OpenClinicaSystemException;

/**
 * @author Krikor Krumlian
 * 
 */
public class ConstantNode extends ExpressionNode {
    String number; // The number.

    ConstantNode(String val) {
        // Construct a ConstNode containing the specified number.
        number = val;
    }

    @Override
    String getNumber() {
        return number;
    }

    @Override
    String testCalculate() throws OpenClinicaSystemException {
        return (String) calculate();
    }

    @Override
    Object calculate() throws OpenClinicaSystemException {
        // The value of the node is the number that it contains.
        return number;
    }

    @Override
    void printStackCommands() {
        // On a stack machine, just push the number onto the stack.
        logger.info("  Push " + number);
    }
}