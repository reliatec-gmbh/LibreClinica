/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.web.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Created by krikorkrumlian on 8/7/15.
 */
public class ApiSecurityFilter extends OncePerRequestFilter {

    private String realm = "Protected";

    @Autowired
    private DataSource dataSource;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            StringTokenizer st = new StringTokenizer(authHeader);
            if (st.hasMoreTokens()) {
                String basic = st.nextToken();

                if (basic.equalsIgnoreCase("Basic")) {
                    try {
                        String credentials = new String(Base64.decodeBase64(st.nextToken().getBytes()), "UTF-8");
                        int p = credentials.indexOf(":");
                        if (p != -1) {
                            String _username = credentials.substring(0, p).trim();

                            UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
                            UserAccountBean ub = userAccountDAO.findByApiKey(_username);
                            if (!_username.equals("") && ub.getId() != 0) {
                                request.getSession().setAttribute("userBean", ub);
                            }else{
                                unauthorized(response, "Bad credentials");
                                return;
                            }
                        } else {
                            unauthorized(response, "Invalid authentication token");
                            return;
                        }
                    } catch (UnsupportedEncodingException e) {
                        throw new Error("Couldn't retrieve authentication", e);
                    }
                }
            }
        } else {
            unauthorized(response);
        }

        filterChain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
        response.sendError(401, message);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        unauthorized(response, "Unauthorized");
    }
}
