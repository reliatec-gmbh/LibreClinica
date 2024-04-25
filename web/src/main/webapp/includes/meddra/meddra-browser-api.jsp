<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.meddra" var="meddra"/>

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
    <title><fmt:message key='meddra_header_title' bundle='${meddra}'/></title>
    <link rel="shortcut icon" type="image/x-icon" href="${pageContext.request.contextPath}/images/favicon.ico">
    <link rel="stylesheet" href="estilo.css" type="text/css"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container ">
    <div class="row">
        <div class="col-md-12" style="text-align: center;">
            <h2><fmt:message key='form_title' bundle='${meddra}'/></h2>
        </div>
    </div>
    <div class="row">
        <div class="col-md-4">
            <label for="language" class="form-label"><fmt:message key='form_label_language' bundle='${meddra}'/></label>
            <select class="form-select" id="language" name="language">
            </select>
        </div>
        <div class="col-md-4">
            <label for="version" class="form-label"><fmt:message key='form_label_version' bundle='${meddra}'/></label>
            <select class="form-select" id="version" name="version">
            </select>
        </div>
        <div class="col-md-4">
            <label for="bview" class="form-label"><fmt:message key='form_label_bview' bundle='${meddra}'/></label>
            <select class="form-select" id="bview" name="bview">
                <option value="SOC">SOC</option>
                <option value="SMQ">SMQ</option>
            </select>
        </div>
    </div>
    <div class="row">
        <div class="col-md-8">
            <label for="searchterm" class="form-label"><fmt:message key='form_label_search_term' bundle='${meddra}'/></label>
            <input type="text" class="form-control" name="searchterm" id="searchterm">
        </div>
        <div class="col-md-2">
            <label  class="form-label">&nbsp;</label>
            <button onclick="getData()" class="btn btn-primary form-control"><fmt:message key='form_label_button' bundle='${meddra}'/></button>
        </div>
    </div>
    <div id="apiResult"></div>
</div>

<div class="container mt-3">
    <h3><fmt:message key='table_result_title' bundle='${meddra}'/></h3>
    <table id="tableResult">
        <thead>
        <tr>
            <th scope="col"><fmt:message key='table_result_title_pcode' bundle='${meddra}'/></th>
            <th scope="col"><fmt:message key='table_result_title_category' bundle='${meddra}'/></th>
            <th scope="col"><fmt:message key='table_result_title_code' bundle='${meddra}'/></th>
            <th scope="col"><fmt:message key='table_result_title_name' bundle='${meddra}'/></th>
            <th scope="col"><fmt:message key='table_result_title_abbrev' bundle='${meddra}'/></th>
            <th scope="col"><fmt:message key='table_result_title_level' bundle='${meddra}'/></th>
        </tr>
        </thead>
        <tbody id="tableBody"></tbody>
    </table>
</div>
<script>
    // Asignar el valor del mensaje a una variable global
    const token_url = "<fmt:message key='token_url' bundle='${meddra}'/>";
    const token_grant_type = "<fmt:message key='token_grant_type' bundle='${meddra}'/>";
    const token_username = "<fmt:message key='token_username' bundle='${meddra}'/>";
    const token_password = "<fmt:message key='token_password' bundle='${meddra}'/>";
    const token_scope = "<fmt:message key='token_scope' bundle='${meddra}'/>";
    const token_client_id = "<fmt:message key='token_client_id' bundle='${meddra}'/>";

    const language_url = "<fmt:message key='language_url' bundle='${meddra}'/>";
    const release_url = "<fmt:message key='release_url' bundle='${meddra}'/>";
    const search_url = "<fmt:message key='search_url' bundle='${meddra}'/>";
</script>
  <script type="text/JavaScript" src="api.js"></script>
</body>
</html>
