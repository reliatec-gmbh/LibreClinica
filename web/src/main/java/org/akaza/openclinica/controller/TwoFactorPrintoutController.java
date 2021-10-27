package org.akaza.openclinica.controller;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import javax.servlet.http.HttpServletResponse;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.service.otp.CertificateBean;
import org.akaza.openclinica.service.otp.TwoFactorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Simple Spring Controller used as bridge between client side and service stack
 * providing access to the 2-FA related certificate printout use cases.
 * 
 * @author thillger
 */
@Controller
@RequestMapping(value = "/printout")
@ResponseStatus(value = INTERNAL_SERVER_ERROR)
public class TwoFactorPrintoutController {
    @Autowired
    private TwoFactorService factorService;
	@Autowired
    private UserAccountDAO dao;

	/**
	 * 
	 * @param userId
	 * @param response
	 * @throws Exception In cases of errors
	 */
	@GetMapping(produces = MediaType.APPLICATION_PDF_VALUE) // @formatter:off // ResponseEntity<TowFactorBean>
	public void printout(@RequestParam(name = "userId", required = true) int userId, HttpServletResponse response) throws Exception {
		response.setHeader("Content-Disposition", "attachment; filename=\"certificate.pdf\"");
		response.addHeader("Content-Type", "application/pdf");
		response.setContentType("application/octet-stream");
		
	    UserAccountBean userAccount = dao.findByPK(userId);
	
	    // Secret in blocks of four! 
		CertificateBean certificateBean = new CertificateBean();
		certificateBean.setUsername(userAccount.getLastName() + ", " + userAccount.getFirstName());
		certificateBean.setSecret(userAccount.getAuthsecret());
		certificateBean.setEmail(userAccount.getEmail());
		certificateBean.setLogin(userAccount.getName());
		
		factorService.printoutCertificate(certificateBean, response.getOutputStream());
    }
}
