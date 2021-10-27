<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri = "http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<%@ page import="org.akaza.openclinica.i18n.util.ResourceBundleProvider" %>
<c:import url="../include/home-header.jsp">
    <c:param name="profilePage" value="yes"/>
</c:import>

<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
		<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>
		<b><fmt:message key="instructions" bundle="${resword}"/></b>
		<div class="sidebar_tab_content">
		</div>
		</td>
	</tr>
	<tr id="sidebar_Instructions_closed" style="display: all">
		<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>
		<b><fmt:message key="instructions" bundle="${resword}"/></b>
		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope="request" id="studies" class="java.util.ArrayList"/>
<jsp:useBean scope="request" id="mustChangePass" class="java.lang.String"/>
<jsp:useBean scope="session" id="study" class="org.akaza.openclinica.bean.managestudy.StudyBean"/>
<jsp:useBean scope="session" id="userBean1" class="org.akaza.openclinica.bean.login.UserAccountBean"/>

<h1><span class="title_manage"><fmt:message key="change_user_profile" bundle="${resword}"/></span></h1>
<strong>
<fmt:message key="browser_locale" bundle="${resword}"/>
 <c:set var="language"  value="<%=ResourceBundleProvider.getLocale().getDisplayLanguage()%>"></c:set>
<c:set var="country"  value="<%=ResourceBundleProvider.getLocale().getDisplayCountry()%>"></c:set>
                        <c:out value="${language}"/>
						<c:if test="${country!=''}">
						/
						<c:out value="${country}"/>
						</c:if>
    &nbsp;<fmt:message key="language" bundle="${resword}"/></strong>
<br><br>

<form action="UpdateProfile" method="post">
<fmt:message key="field_required" bundle="${resword}"/><br>
<input type="hidden" name="action" value="confirm">
<!-- These DIVs define shaded box borders -->
<div style="width: 600px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0">
  <tr><td class="formlabel"><fmt:message key="first_name" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG"><input type="text" name="firstName" value="<c:out value="${userBean1.firstName}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="firstName"/></jsp:include></td><td class="formlabel">*</td></tr>
  <tr><td class="formlabel"><fmt:message key="last_name" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG"><input type="text" name="lastName" value="<c:out value="${userBean1.lastName}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="lastName"/></jsp:include></td><td class="formlabel">*</td></tr>
  <tr><td class="formlabel"><fmt:message key="email" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG"><input type="text" name="email" value="<c:out value="${userBean1.email}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="email"/></jsp:include></td><td class="formlabel">*</td></tr>
  <tr valign="bottom"><td class="formlabel"><fmt:message key="institutional_affiliation" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG"><input type="text" name="institutionalAffiliation" value="<c:out value="${userBean1.institutionalAffiliation}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="institutionalAffiliation"/></jsp:include></td><td class="formlabel">*</td></tr>
  <tr><td class="formlabel"><fmt:message key="default_active_study" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
   <c:set var="activeStudy1" value="${userBean1.activeStudyId}"/>
    <select name="activeStudyId" class="formfieldXL">
      <c:if test="${activeStudy1 ==0}"><option value="">-<fmt:message key="select" bundle="${resword}"/>-</option></c:if>
       <c:forEach var="study" items="${studies}">
        <c:choose>
         <c:when test="${activeStudy1 == study.id}">
          <option value="<c:out value="${study.id}"/>" selected><c:out value="${study.name}"/>
         </c:when>
         <c:otherwise>
          <option value="<c:out value="${study.id}"/>"><c:out value="${study.name}"/>
         </c:otherwise>
        </c:choose>
     </c:forEach>
    </select></div>
  </td></tr>
  <c:if test="${not userBean1.ldapUser}">
  <tr valign="bottom"><td class="formlabel"><fmt:message key="password_challenge_question" bundle="${resword}"/>:</td><td>
  <div class="formfieldXL_BG">
  <select name="passwdChallengeQuestion" class="formfieldXL">
  <c:set var="question1" value="Mother's Maiden Name"/>
  <c:choose>
       <c:when test="${userBean1.passwdChallengeQuestion == question1}">
            <option selected><fmt:message key="mother_maiden_name" bundle="${resword}"/></option>
            <option><fmt:message key="favourite_pet" bundle="${resword}"/></option>
            <option><fmt:message key="city_of_birth" bundle="${resword}"/></option>
            <option><fmt:message key="favorite_color" bundle="${resword}"/></option>
        </c:when>
        <c:when test="${userBean1.passwdChallengeQuestion == 'Favorite Animal'}">
            <option selected><fmt:message key="favourite_pet" bundle="${resword}"/></option>
            <option><fmt:message key="city_of_birth" bundle="${resword}"/></option>
            <option><fmt:message key="mother_maiden_name" bundle="${resword}"/></option>
            <option><fmt:message key="favorite_color" bundle="${resword}"/></option>
        </c:when>
        <c:when test="${userBean1.passwdChallengeQuestion == 'City of Birth'}">
               <option><fmt:message key="favourite_pet" bundle="${resword}"/></option>
               <option selected><fmt:message key="city_of_birth" bundle="${resword}"/></option>
               <option><fmt:message key="mother_maiden_name" bundle="${resword}"/></option>
               <option><fmt:message key="favorite_color" bundle="${resword}"/></option>
        </c:when>
        <c:otherwise>
               <option><fmt:message key="favourite_pet" bundle="${resword}"/></option>
               <option><fmt:message key="city_of_birth" bundle="${resword}"/></option>
               <option><fmt:message key="mother_maiden_name" bundle="${resword}"/></option>
               <option selected><fmt:message key="favorite_color" bundle="${resword}"/></option>
        </c:otherwise>
   </c:choose>
   </select>
   </div></td><td class="formlabel">*</td></tr>
  <tr><td class="formlabel"><fmt:message key="password_challenge_answer" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="passwdChallengeAnswer" value="<c:out value="${userBean1.passwdChallengeAnswer}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="passwdChallengeAnswer"/></jsp:include></td><td class="formlabel">*</td></tr>

  <tr><td class="formlabel"><fmt:message key="old_password" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG"><input type="password" name="oldPasswd" value="" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="oldPasswd"/></jsp:include></td><td class="formlabel">*</td></tr>
  <tr><td class="formlabel"><fmt:message key="new_password" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG"><input type="password" name="passwd" value="" class="formfieldXL"></div>
  <c:if test="${mustChangePass != 'yes'}"><fmt:message key="leave_in_blank" bundle="${resword}"/></c:if>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="passwd"/></jsp:include></td></tr>
  <tr><td class="formlabel"><fmt:message key="confirm_new_password" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG"><input type="password" name="passwd1" value="" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="passwd1"/></jsp:include></td></tr>
  </c:if>
  <tr>
  	<td class="formlabel"><fmt:message key="phone" bundle="${resword}"/>:</td>
  	<td>
  		<div class="formfieldXL_BG"><input type="text" name="phone" value="<c:out value="${userBean1.phone}"/>" class="formfieldXL"></div>
	  	<jsp:include page="../showMessage.jsp">
	  		<jsp:param name="key" value="phone"/>
	  	</jsp:include>
	</td>
	<td class="formlabel">*</td>
  </tr>
  <c:if test="${factorService.twoFactorActivatedLetter}">
	  <tr>
	  	<td class="formlabel"><fmt:message key="auth_type" bundle="${resword}" />:</td>
	  	<td>
			<c:set var="editable" scope="request" value= "" />
			<c:if test="${userBean1.twoFactorActivated}">
				<c:set var="editable" scope="request" value= "disabled" />
			</c:if>
			<input type="radio" id="authtypeStandard" name="authtype" style="display:inline-block;" value="STANDARD" disabled <c:if test="${userBean1.authtype eq 'STANDARD'}">checked</c:if>/> Standard Authentication<br/>
			<input type="radio" id="authtypeMarked" name="authtype" style="display:inline-block;" value="MARKED" disabled <c:if test="${userBean1.authtype eq 'MARKED'}">checked</c:if>/> Marked for 2-Factor Authentication<br/>
			<input type="radio" id="authtypeTwoFactor" name="authtype" style="display:inline-block;" value="TWO_FACTOR" ${userBean1.twoFactorMarked ? "": "disabled"} <c:if test="${userBean1.authtype eq 'TWO_FACTOR'}">checked</c:if>/> 2-Factor Authentication<br/>
			<!-- Need 'authsecret' and 'authtype' as hidden to avoid getting information lost. -->
			<input type="hidden" name="authtype" value="${userBean1.authtype}" />
			<input type="hidden" name="authsecret" value="${userBean1.authsecret}" />

			<jsp:include page="../showMessage.jsp">
				<jsp:param name="key" value="auth_type"/>
			</jsp:include>
		</td>
		<td class="formlabel">* </td>
	</tr>
  </c:if>
  <c:if test="${factorService.twoFactorActivatedApplication}">
	  <tr>
	  	<td class="formlabel"><fmt:message key="auth_type" bundle="${resword}" />:</td>
	  	<td>
  			<input type="radio" id="authtypeStandard" name="authtype" style="display:inline-block;" value="STANDARD" onchange="enableDisableQrCodeButton();" <c:if test="${userBean1.authtype eq 'STANDARD'}">checked</c:if> /> Standard Authentication
			<input type="radio" id="authtypeTwoFactor" name="authtype" style="display:inline-block;" value="TWO_FACTOR" onchange="enableDisableQrCodeButton();" <c:if test="${userBean1.authtype eq 'TWO_FACTOR'}">checked</c:if>/> 2-Factor Authentication

			<jsp:include page="../showMessage.jsp">
				<jsp:param name="key" value="auth_type"/>
			</jsp:include>
		</td>
		<td class="formlabel">* </td>
	</tr>
	<tr id="qrImageRow" style="visibility:hidden;">
	  	<td class="formlabel"><fmt:message key="scan_image" bundle="${resword}"/>:</td>
		<td colspan="2">
			<img id="qrImage" src="#" width="0" height="0" style="visibility:hidden;" />
			<input type="hidden" id="authsecret" name="authsecret" class="formfieldXL" value="${userBean1.authsecret}" readonly />
		</td>
    </tr>
	<c:if test="${factorService.twoFactorActivatedApplication}">
		<script src="https://unpkg.com/axios/dist/axios.min.js"></script>
		<script type="text/javascript">
			window.onload = function() {
				enableDisableQrCodeButton();
			};
			 
			function qrButtonClick() {
				var secret = document.getElementById("authsecret");
				var twoFactorRadio = document.getElementById("authtypeTwoFactor");
			
				var controllerUrl = 'pages/factor';
				controllerUrl = twoFactorRadio.checked && '' != secret.value ? (controllerUrl + '?secret=' + secret.value) : controllerUrl;
				var vis = twoFactorRadio.checked ? 'visible' : 'hidden';
				
				const retrieveQrCode = () => {
					axios.get(controllerUrl).then(response => {
						const res = response.data;
					  
						var image = document.getElementById("qrImage");
						image.style.visibility = 'visible';
						image.src = res.imageUrl;
						image.height = 100;
						image.width = 100;
						
						var secretField = document.getElementById("authsecret");
						secretField.value = res.authSecret;
						
						var qrRow = document.getElementById("qrImageRow");
						qrRow.style.visibility = vis;
					}).catch(error => console.error(error));
				};
				
				retrieveQrCode();
			}
			
			function enableDisableQrCodeButton() {
				var twoFactorRadio = document.getElementById("authtypeTwoFactor");
				var vis = twoFactorRadio.checked ? 'visible' : 'hidden';
				
				var secret = document.getElementById("authsecret");
				secret.value = twoFactorRadio.checked ? secret.value : '';
			
				var qrButton = document.getElementById("qrButton");
				qrButton.style.visibility = vis;
				
				var qrRow = document.getElementById("qrImageRow");
				qrRow.style.visibility = qrButton.style.visibility;
				
				var image = document.getElementById("qrImage");
				image.style.visibility = 'hidden';
				image.height = 0;
				image.width = 0;
				image.src = '#';
			}
		</script>
	 </c:if>
  </c:if>
</table>
</div>

</div></div></div></div></div></div></div></div>

</div>
	<input type="submit" name="Submit" value="<fmt:message key="confirm_profile_changes" bundle="${resword}"/>" class="button_long" />
	<input type="button" onclick="confirmCancel('MainMenu');" name="cancel" value="<fmt:message key="cancel" bundle="${resword}" />" class="button_medium" />
	<c:if test="${factorService.twoFactorActivatedApplication}">
		<input type="button" id="qrButton" value="QR-Code" onClick="qrButtonClick();" style="visibility:hidden;" class="button_medium" title="Click to request a QR-code which can be scanned with your Athenticator-App." />
	</c:if>
</form>
<jsp:include page="../include/footer.jsp" />
