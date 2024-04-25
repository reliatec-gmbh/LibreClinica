<%--
  Created by IntelliJ IDEA.
  User: jarias
  Date: 01/04/2023
  Time: 3:59 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.labels_n" var="labels_n"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='study' class='org.akaza.openclinica.bean.managestudy.StudyBean' />
<jsp:useBean scope='session' id='userRole' class='org.akaza.openclinica.bean.login.StudyUserRoleBean' />
<jsp:useBean scope='request' id='isAdminServlet' class='java.lang.String' />
<!DOCTYPE html>
<html lang="en">

<head>
    <c:set var="contextPath" value="${fn:replace(pageContext.request.requestURL, fn:substringAfter(pageContext.request.requestURL, pageContext.request.contextPath), '')}" />
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=8" />
    <title><fmt:message key='labels_n_header_title' bundle='${labels_n}'/></title>
    <link rel="shortcut icon" type="image/x-icon" href="${pageContext.request.contextPath}/images/favicon.ico">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container">
        <div class="row">
            <h3><p id="titlesite"></p></h3>
        </div>
        <div class="row">
            <h3><fmt:message key='form_title' bundle='${labels_n}'/></h3>
        </div>
        <div class="row justify-content-md-center">
            <div class="col col-sm-2">
                <select name="tbtclabels" id="tbtclabels" class="form-select">
                    <option value="0">(Select Number)</option>
                </select>
            </div>
            <div class="col col-lg-2"></div>
        </div>
    </div>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="api.js"></script>
    <script>
        $(document).ready(function () {
            const currentUrl = window.location.href;
            console.log(currentUrl);
            const label=  getParameters(currentUrl);
            $('#tbtclabels').on('change', function() {
                const selectedOption = $(this).val();
                const parameters = {
                    label: label,
                    selected: selectedOption
                };
                console.log(parameters);
                window.opener.postMessage(parameters, '*');
            });
        });
    </script>
</body>
</html>
