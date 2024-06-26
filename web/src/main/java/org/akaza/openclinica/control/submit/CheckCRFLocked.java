/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * Created by IntelliJ IDEA.
 * User: A. Hamid
 * Date: Apr 12, 2010
 * Time: 3:32:44 PM
 */
public class CheckCRFLocked extends SecureController {
    /**
	 * 
	 */
	private static final long serialVersionUID = 4520097023965266054L;
	@Override
    protected void processRequest() throws Exception {
        int userId;
        String ecId = request.getParameter("ecId");
        if (ecId != null && !ecId.equals("")) {
            int crfId = Integer.parseInt(ecId);
            if (getCrfLocker().isLocked(crfId)) {
                userId = getCrfLocker().getLockOwner(crfId);
                UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
                UserAccountBean ubean = (UserAccountBean)udao.findByPK(userId);
                response.getWriter().print(resword.getString("CRF_unavailable") +
                        "\n"+ubean.getName() + " "+ resword.getString("Currently_entering_data")
                        + "\n"+resword.getString("Leave_the_CRF"));
            } else {
                response.getWriter().print("true");
            }
            return;
        }else if(request.getParameter("userId")!=null) {
            getCrfLocker().unlockAllForUser(Integer.parseInt(request.getParameter("userId")));
            if(request.getParameter("exitTo")!=null){
                response.sendRedirect(request.getParameter("exitTo"));
            }else{
                response.sendRedirect("ListStudySubjects");
            }

        }
    }
    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        return;
    }
}
