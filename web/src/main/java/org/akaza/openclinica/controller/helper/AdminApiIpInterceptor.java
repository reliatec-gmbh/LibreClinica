/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2026 LibreClinica
 */

package org.akaza.openclinica.controller.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.akaza.openclinica.dao.core.CoreResources;
import org.springframework.web.servlet.HandlerInterceptor;

public class AdminApiIpInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminApiIpInterceptor.class);

    private List<String> allowedIPs = Collections.emptyList();

    public void init() {
        String configuredIPs = CoreResources.getField("adminApi.ipList");
        List<String> ips = new ArrayList<>();

        if (configuredIPs != null && !configuredIPs.trim().isEmpty()) {
            for (String ip : configuredIPs.split(",")) {

                if (!ip.trim().isEmpty()) {
                    ips.add(ip.trim());
                }
            }
        }

        allowedIPs = Collections.unmodifiableList(ips);

        LOGGER.info("Admin API IP allowlist initialized with {} entries", allowedIPs.size());
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();

        // Allow login without IP restriction for third parties that are using this endpoint
        if (uri.endsWith("/accounts/login")) {
            return true;
        }

        String clientAddress = request.getRemoteAddr();

        // Only allowed IP addresses will have access to the admin services
        for (String allowedAddress : allowedIPs) {
            if (isAllowed(clientAddress, allowedAddress)) {
                return true;
            }
        }

        LOGGER.info("Admin API access denied: clientIp={}, path={}", clientAddress, request.getRequestURI());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write(
        "{\"error\":\"NoAccessRight\"," +
            "\"message\":\"Your IP address is not allowed to access this API.\"}"
        );

        return false;
    }

    private boolean isAllowed(String clientAddress, String allowedAddress) {

        // When localhost is specified, check it against the standard IPv4 and IPv6 strings
        if ("localhost".equalsIgnoreCase(allowedAddress)) {

            return "127.0.0.1".equals(clientAddress) ||
                    "::1".equals(clientAddress) ||
                    "0:0:0:0:0:0:0:1".equalsIgnoreCase(clientAddress);
        }

        // Otherwise look for exact IP match
        return allowedAddress.equals(clientAddress);
    }
}
