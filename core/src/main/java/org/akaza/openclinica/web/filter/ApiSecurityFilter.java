/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2026 LibreClinica
 */
package org.akaza.openclinica.web.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;
import java.util.Base64;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Created by krikorkrumlian on 8/7/15.
 */
public class ApiSecurityFilter extends OncePerRequestFilter {

    @Autowired
    private DataSource dataSource;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Authorization header has to be present
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            unauthorized(response);
            return;
        }

        // Only basic is allowed with exact size of 2 (Basic and Base64 encoded input)
        StringTokenizer st = new StringTokenizer(authHeader);
        if (st.countTokens() != 2 || !st.nextToken().equalsIgnoreCase("Basic")) {
            unauthorized(response);
            return;
        }

        // Explicit decode input that rejects malformed Base64
        final byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(st.nextToken());
        } catch (IllegalArgumentException e) {
            unauthorized(response);
            return;
        }

        // Verify that decoded string contains separator delimited credentials
        String credentials = new String(decoded, StandardCharsets.UTF_8);
        int separator = credentials.indexOf(":");
        if (separator == -1) {
            unauthorized(response, "Invalid authentication token");
            return;
        }

        // Treat the username as apiKey while password can be whatever and ignore whitespaces
        String apiKey = credentials.substring(0, separator).trim();

        // User with matching apiKey has to be active, non-locked and apiKey activated
        UserAccountBean ub = new UserAccountDAO(dataSource).findByApiKey(apiKey);
        if (apiKey.isEmpty() ||
            ub.getId() == 0 ||
            !Boolean.TRUE.equals(ub.getEnabled()) ||
            !Boolean.TRUE.equals(ub.getAccountNonLocked()) ||
            !ub.isEnableApiKey()) {
            unauthorized(response, "Bad credentials");
            return;
        }

        // Assign userBean to the session for downstream controller processing
        request.getSession().setAttribute("userBean", ub);

        // Continue to controller execution
        filterChain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        String realm = "Protected";
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
        response.sendError(401, message);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        unauthorized(response, "Unauthorized");
    }

}
