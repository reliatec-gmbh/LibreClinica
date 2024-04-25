<%--
  Created by IntelliJ IDEA.
  User: jarias
  Date: 09/01/2024
  Time: 3:59 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.whodrug" var="whodrug"/>

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
    <title><fmt:message key='whodrug_header_title' bundle='${whodrug}'/></title>
    <link rel="shortcut icon" type="image/x-icon" href="${pageContext.request.contextPath}/images/favicon.ico">
    <link href="resources/bootstrap.min.css" rel="stylesheet">
    <style>
        .scroll-container {
            width: 100%; 
            max-width: 1300px; 
            overflow-x: auto;
            height: 530px;
        }
        .table-content {
          min-width: 100%; 
          white-space: nowrap; 
        }
      </style>
</head>

<body>
    <div class="container">
        <h1><fmt:message key='form_title' bundle='${whodrug}'/></h1>
        <div class="row">
            <div class="col-4">
                <label for="lsearchTerm" class="form-label"><fmt:message key='form_label_search' bundle='${whodrug}'/></label>
                <input type="text" class="form-control" name="searchterm" id="searchterm">
            </div>
            <div class="col-2">
                <label for="btn" class="form-label">&nbsp;</label>
                <button onclick="getData();" class="btn btn-primary form-control"><fmt:message key='form_label_button' bundle='${whodrug}'/></button>
            </div>
        </div>
        <div id="textResult"></div>
        <br>
        <br>
        <div class="row">
            <div class="col-12">
                <div class="scroll-container">
                    <table id="result" class="table table-striped table-content">
                        <thead id="headTable">
                        </thead>
                        <tbody id="bodyTable">
            
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
    <script src="resources/jquery-3.6.0.min.js"></script>
    <script src="resources/string-similarity.min.js"></script>
    <script src="api.js"></script>
    <script>
        function process(term) {
            let results = search(term);
            if (results.length === 0) {
                let similarly = searchTermsSimilar(term);
                if (similarly !="-1") {
                    results = search(similarly); 
                }
            }
            return results;
        }
        function getData() {
            var searchTerm = document.getElementById('searchterm').value;
            if (searchTerm != "") {
               let result = process(searchTerm);
               if (result.length == 0) {
                document.getElementById('textResult').innerHTML = "No results found";
                document.getElementById('headTable').innerHTML = "";
                document.getElementById('bodyTable').innerHTML = "";
                console.log(result);
                return;
            } else {
                document.getElementById('textResult').innerHTML = "";
                document.getElementById('headTable').innerHTML = "";
                document.getElementById('bodyTable').innerHTML = "";
                showTable(result);
            }
            }
        }
    </script>
</body>

</html>
