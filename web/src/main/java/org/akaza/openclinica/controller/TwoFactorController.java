package org.akaza.openclinica.controller;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.ResponseEntity.ok;

import org.akaza.openclinica.service.otp.TowFactorBean;
import org.akaza.openclinica.service.otp.TwoFactorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Simple Spring Controller used as bridge between client side and service stack
 * providing access to the 2-FA related use cases of generating .
 * 
 * @author thillger
 */
@Controller
@RequestMapping(value = "/factor")
@ResponseStatus(value = INTERNAL_SERVER_ERROR)
public class TwoFactorController {
    @Autowired
    private TwoFactorService factorService;

    /**
     * Returns a {@link TowFactorBean} with secret and QR information. Access is
     * via /pages/factor or /pages/factor?secret={someSecret}. When secret is
     * empty a new secret will get created.
     * 
     * @param secret Optional: Secret string.
     * @return {@link TowFactorBean} as JSON.
     */
    @GetMapping
    public ResponseEntity<TowFactorBean> generate(@RequestParam(name = "secret", required = false) String secret) throws Exception {
        if (isBlank(secret)) {
            TowFactorBean bean = factorService.generate();
            return ok(bean);
        }

        TowFactorBean bean = factorService.generate(secret);
        return ok(bean);
    }
}
