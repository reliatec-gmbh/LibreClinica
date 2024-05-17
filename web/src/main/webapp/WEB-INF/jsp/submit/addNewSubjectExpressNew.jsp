<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<jsp:useBean scope="request" id="label" class="java.lang.String"/>

<jsp:useBean scope="session" id="study" class="org.akaza.openclinica.bean.managestudy.StudyBean" />
<jsp:useBean scope="request" id="pageMessages" class="java.util.ArrayList" />
<jsp:useBean scope="request" id="presetValues" class="java.util.HashMap" />

<jsp:useBean scope="request" id="groups" class="java.util.ArrayList" />

<c:set var="uniqueIdentifier" value="" />
<c:set var="chosenGender" value="" />
<c:set var="label" value="" />
<c:set var="secondaryLabel" value="" />
<c:set var="enrollmentDate" value="" />
<c:set var="startDate" value=""/>
<c:set var="dob" value="" />
<c:set var="yob" value="" />
<c:set var="groupId" value="${0}" />
<c:set var="studyEventDefinition" value=""/>
<c:set var="studyEventDefinitionID" value=""/>
<c:set var="location" value=""/>

<c:forEach var="presetValue" items="${presetValues}">
	<c:if test='${presetValue.key == "uniqueIdentifier"}'>
		<c:set var="uniqueIdentifier

" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "gender"}'>
		<c:set var="chosenGender" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "label"}'>
		<c:set var="label" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "secondaryLabel"}'>
		<c:set var="secondaryLabel" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "enrollmentDate"}'>
		<c:set var="enrollmentDate" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "startDate"}'>
		<c:set var="startDate" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "dob"}'>
		<c:set var="dob" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "yob"}'>
		<c:set var="yob" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "group"}'>
		<c:set var="groupId" value="${presetValue.value}" />
	</c:if>
	
	<c:if test='${presetValue.key == "studyEventDefinitionID"}'>
		<c:set var="studyEventDefinitionID" value="${presetValue.value}" />
	</c:if>
    <c:if test='${presetValue.key == "studyEventDefinitionID"}'>
        <c:set var="studyEventDefinitionID" value="${presetValue.value}" />
    </c:if>
	<c:if test='${presetValue.key == "location"}'>
		<c:set var="location" value="${presetValue.value}" />
	</c:if>
</c:forEach>

<!-- start of submit/addNewSubjectExpressNew.jsp -->
<form name="subjectForm" action="AddNewSubject" method="post">
<input type="hidden" name="subjectOverlay" value="true">

<div class="add_new_subject_div">
<table>
    <tr>
    	<td class="formlabel"></td>
        <td><h3><fmt:message key="add_new_subject" bundle="${resword}"/></h3></td>
        <td></td>
    </tr>
    <tr>
        <td class="formlabel">
            <jsp:include page="../include/showSubmitted.jsp" />
            <input type="hidden" name="addWithEvent" value="1"/>
            <fmt:message key="study_subject_ID" bundle="${resword}"/>:</td>
        <td>
            <table>
                <tr>
                    <td><div class="formfieldXL_BG">
                    <c:choose>
                     <c:when test="${study.studyParameterConfig.subjectIdGeneration =='auto non-editable'}">
                      <input onfocus="this.select()" type="text" value="" id="studySubjectIdText" class="formfield" disabled>
                      <input type="hidden" name="label" value="" id="studySubjectIdHidden">
                     </c:when>
                     <c:otherwise>
                       <input onfocus="this.select()" onchange="validationStudySubjectId()" onkeyup="validationStudySubjectId()"
                              id="studySubjectIdText" type="text" name="label" value="<c:out value="${label}"/>" class="formfieldXL">
                     </c:otherwise>
                    </c:choose>
                    </div></td>
                    <td>*</td>
                </tr>
                <tr><td colspan="2">
                    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="label"/></jsp:include>
                    <div id="spanAlert-Validation" class="alert" style="display: none;">Please correct the Study Subject Id</div>
                </td></tr>
            </table>
        </td>
    </tr>
    
<c:choose>
	<c:when test="${study.studyParameterConfig.subjectPersonIdRequired =='required'}">
		<tr>
			<td class="formlabel">
				<fmt:message key="person_ID" bundle="${resword}"/>:
			</td>
			<td>
				<div class="formfieldXL_BG">
				<input onfocus="this.select()" type="text" name="uniqueIdentifier" value="<c:out value="${uniqueIdentifier}"/>" class="formfieldL">&nbsp;
				<span class="required">*</span>
				<br />
				<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="uniqueIdentifier"/></jsp:include>
				</div>
			</td>
	    </tr>
    </c:when>
    <c:when test="${study.studyParameterConfig.subjectPersonIdRequired =='optional'}">
		<tr>
			<td class="formlabel">
				<fmt:message key="person_ID" bundle="${resword}"/>:
	        </td>
			<td>
				<div class="formfieldXL_BG">
				<input onfocus="this.select()" type="text" name="uniqueIdentifier" value="<c:out value="${uniqueIdentifier}"/>" class="formfieldL">&nbsp;
				<br />
				<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="uniqueIdentifier"/></jsp:include>
				</div>
			</td>
	    </tr>
    </c:when>
    <c:otherwise>
		<input type="hidden" name="uniqueIdentifier" value="<c:out value="${uniqueIdentifier}"/>">
    </c:otherwise>
</c:choose>

    <tr>
        <td class="formlabel"><fmt:message key="enrollment_type" bundle="${resword}"/>:</td>
        <td>
            <table>
                <tr><td>
                    <div class="formfieldM_BG">
                        <select name="studyEventDefinition" class="formfieldM" id="enrollmentTypeSelection" onchange="handleSelection()">
                            <option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
                            <option value="EN"><fmt:message key="enrollment_type_enrollment" bundle="${resword}"/></option>
                            <option value="NE"><fmt:message key="enrollment_type_non_enrollment" bundle="${resword}"/></option>
                        </select>
                    </div>
                </td>
                    <td><span class="formlabel">*</span></td>
                </tr>
            </table>
        </td>
    </tr>

    <tr>
        <td class="formlabel">
            <fmt:message key="enrollment_date" bundle="${resword}"/>:
        </td>
        <td>
			<div class="formfieldM_BG">
	            <input onfocus="this.select()" type="text" name="enrollmentDate" value="<c:out value="${enrollmentDate}" />" class="formfieldM" id="enrollmentDateField" />&nbsp;		
				<a href="#">
					<img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="enrollmentDateTrigger" />
					<script type="text/javascript">Calendar.setup({inputField  : "enrollmentDateField", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "enrollmentDateTrigger", customPX: 300, customPY: 10 }); </script>
				</a>&nbsp;
				<span class="required">*</span>
				<br />
				<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="enrollmentDate"/></jsp:include>
			</div>
        </td>
    </tr>


        <c:if test="${study.studyParameterConfig.genderRequired !='not used'}">
	        <tr>
	        <td class="formlabel"><fmt:message key="gender" bundle="${resword}"/>:</td>
	        <td>
				<table>
	                <tr>
	                    <td>
	                    	<div class="formfieldS_BG">
	                        <select name="gender" class="formfieldS">
	                            <option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
	                            <c:choose>
	                                <c:when test="${!empty chosenGender}">
	                                    <c:choose>
	                                        <c:when test='${chosenGender == "m"}'>
	                                            <option value="m" selected><fmt:message key="male" bundle="${resword}"/></option>
	                                            <option value="f"><fmt:message key="female" bundle="${resword}"/></option>
	                                        </c:when>
	                                        <c:otherwise>
	                                            <option value="m"><fmt:message key="male" bundle="${resword}"/></option>
	                                            <option value="f" selected><fmt:message key="female" bundle="${resword}"/></option>
	                                        </c:otherwise>
	                                    </c:choose>
	                                </c:when>
	                                <c:otherwise>
	                                    <option value="m"><fmt:message key="male" bundle="${resword}"/></option>
	                                    <option value="f"><fmt:message key="female" bundle="${resword}"/></option>
	                                </c:otherwise>
	                            </c:choose>
	                        </select>
	                		</div>
	                	</td>
					    <td>
					        <c:choose>
					        	<c:when test="${study.studyParameterConfig.genderRequired !='false'}">
					           		<span class="formlabel">*</span>
					        	</c:when>
					        </c:choose>
					    </td>
					</tr>
					<tr><td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="gender"/></jsp:include></td></tr>
				</table>
	        </td>
	    </tr>
	</c:if>

<c:choose>
	<c:when test="${study.studyParameterConfig.collectDob == '1'}">
    <tr>
        <td class="formlabel">
        	<fmt:message key="date_of_birth" bundle="${resword}"/>:
        </td>
        <td>
        	<div class="formfieldM_BG">
            	<input onfocus="this.select()" type="text" name="dob" value="<c:out value="${dob}" />" class="formfieldM" id="dobField" />
				<a href="#">
                    <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="dobTrigger" />
					<script type="text/javascript">
                        Calendar.setup({inputField  : "dobField", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "dobTrigger", customPX: 300, customPY: 10 });
					</script>
				</a>&nbsp;
				<span class="required">* </span>
			</div>
			<br />
			<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="dob"/></jsp:include>
		</td>
	</tr>
	</c:when>
    <c:when test="${study.studyParameterConfig.collectDob == '2'}">
		<tr>
        	<td class="formlabel">
        		<fmt:message key="year_of_birth" bundle="${resword}"/>:
	        </td>
	        <td>
	        	<div class="formfieldM_BG">
		        	<input onfocus="this.select()" type="text" name="yob" value="<c:out value="${yob}" />" class="formfieldM" />&nbsp;
					(<fmt:message key="date_format_year" bundle="${resformat}"/>)&nbsp; 
					<span class="required">*</span>
					<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="yob"/></jsp:include>
				</div>
			</td>
		</tr>
	</c:when>
	<c:otherwise>
    	<input type="hidden" name="dob" value="" />
	</c:otherwise>
</c:choose>

<c:if test="${(!empty studyGroupClasses)}">
	<tr>
		<td class="formlabel">
			<fmt:message key="subject_group_class" bundle="${resword}"/>:
		</td>
      	<td>
      		<div class="formfieldM_BG">
      			<table>
				<c:set var="count" value="0"/>
				<c:forEach var="group" items="${studyGroupClasses}">
					<tr>
						<td style="border: none;">
							<span class="groupclass_name"><c:out value="${group.name}"/></span>&nbsp;
						</td>
						<td style="border: none;">
		             		<select name="studyGroupId<c:out value="${count}"/>" class="formfieldM">
								<option value=""><c:out value="${group.name}"/>:</option>
									<c:forEach var="studyGroup" items="${group.studyGroups}">
										<option value="<c:out value="${studyGroup.id}"/>"><c:out value="${studyGroup.name}"/></option>
									</c:forEach>
							</select>&nbsp;
							<c:if test="${group.subjectAssignment=='Required'}">
								<span class="required">*</span>
							</c:if>
							<c:import url="../showMessage.jsp"><c:param name="key" value="studyGroupId${count}" /></c:import>
						</td>
             		<c:set var="count" value="${count+1}"/>
             		</tr>
				</c:forEach>
				</table>
			</div>
		</td>
    </tr>
</c:if>

    <tr>
        <td class="formlabel"><fmt:message key="SED_2" bundle="${resword}"/>:</td>
        <td>
            <table>
                <tr><td>
                    <div class="formfieldM_BG">
                        <select name="studyEventDefinitionID" class="formfieldM">
                            <option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
                            <c:forEach var="event" items="${allDefsArray}">
                                <option <c:if test="${studyEventDefinitionID == event.id}">SELECTED</c:if> value="<c:out value="${event.id}"/>"><c:out value="${event.name}" />
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    </td>
                    <td><span class="formlabel">*</span></td>
                </tr>
                <tr>
                    <td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="studyEventDefinitionID"/></jsp:include></td>
                </tr>

            </table>
        </td>
    </tr>

	<tr>
		<td class="formlabel">
            <fmt:message key="start_date" bundle="${resword}"/>:
        </td>
        <td>
        	<div class="formfieldM_BG">
				<input type="text" name="startDate" value="<c:out value="${startDate}" />" class="formfieldM" id="enrollmentDateField2" />
				<a href="#" >
                	<img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="enrollmentDateTrigger2"/>
                </a>
                <script type="text/javascript">
                	Calendar.setup({inputField  : "enrollmentDateField2", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "enrollmentDateTrigger2" ,customPX: 300, customPY: 10 });
				</script>
                &nbsp;
				<span class="required">*</span>
				<br />
				<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="startDate"/></jsp:include>
			</div>
		</td>
	</tr>

<c:choose>
	<c:when test="${study.studyParameterConfig.eventLocationRequired == 'required'}">
		<tr>
        	<td class="formlabel">
        		<fmt:message key="location" bundle="${resword}"/>:
        	</td>
			<td>
				<div class="formfieldXL_BG">
	                <input type="text" name="location" value="<c:out value="${location}"/>" class="formfieldXL">&nbsp;
					<span class="required">*</span>
					<br />
					<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="location"/></jsp:include>
				</div>
			</td>
		</tr>
    </c:when>
    <c:when test="${study.studyParameterConfig.eventLocationRequired == 'optional'}">
		<tr>
        	<td class="formlabel">
        		<fmt:message key="location" bundle="${resword}"/>:
        	</td>
			<td>
				<div class="formfieldXL_BG">
	                <input type="text" name="location" value="<c:out value="${location}"/>" class="formfieldXL">&nbsp;
					<br />
					<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="location"/></jsp:include>
				</div>
			</td>
		</tr>
	</c:when>
    <c:otherwise>
		<input type="hidden" name="location" value=""/>
    </c:otherwise>
    </c:choose>
    <tr>
        <td colspan="2" align="center">
        	<input type="submit" name="addSubject" id="add" value="<fmt:message key="add2" bundle="${resword}"/>" class="button" />
        	&nbsp;
        	<input type="button" id="cancel" name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>" class="button"/>
		</td>
	</tr>
</table>
</div>
</form>

<script>
    function handleSelection() {
        var selectedValue = document.getElementById("enrollmentTypeSelection").value;

        var nextCountOfStudySubjectsAtStudyOrSite = "<%=study.getNextCountOfStudySubjectsAtStudyOrSite()%>";
        var siteIdentifier = "<%=study.getSiteIdOfStudy()%>";

        nextCountOfStudySubjectsAtStudyOrSite = nextCountOfStudySubjectsAtStudyOrSite.padStart(5, "0");
        siteIdentifier = siteIdentifier.padStart(2, "0");

        var studySubjectIdEnrollment = siteIdentifier + "-P" + nextCountOfStudySubjectsAtStudyOrSite;
        var studySubjectIdNonEnrollment = "38-" + siteIdentifier + "-N" + nextCountOfStudySubjectsAtStudyOrSite;

        switch (selectedValue) {
            case "EN":
                document.getElementById("studySubjectIdText").value = studySubjectIdEnrollment;
                document.getElementById("studySubjectIdHidden").value = studySubjectIdEnrollment;
                break;
            case "NE":
                document.getElementById("studySubjectIdText").value = studySubjectIdNonEnrollment;
                document.getElementById("studySubjectIdHidden").value = studySubjectIdNonEnrollment;
                break;
            default:
                document.getElementById("studySubjectId").value = "";
                break;
        }
    }

    function validationStudySubjectId() {
        var selectedValue = document.getElementById("enrollmentTypeSelection").value;
        var studySubjectIdValue = document.getElementById("studySubjectIdText").value;
        var errorMessageValidationDiv = document.getElementById("spanAlert-Validation");
        var submitBottom = document.getElementById("add");

        var siteIdentifier = "<%=study.getSiteIdOfStudy()%>";
        siteIdentifier = siteIdentifier.padStart(2, "0");

        var regexStudySubjectIdEnrollment = new RegExp("^" + siteIdentifier + "-P\\d{5}$");
        var regexStudySubjectIdNonEnrollment = new RegExp("^38-" + siteIdentifier + "-N\\d{5}$");

        console.log(siteIdentifier)
        console.log(studySubjectIdValue)
        console.log(regexStudySubjectIdEnrollment)
        console.log(regexStudySubjectIdNonEnrollment)

        switch (selectedValue) {
            case "EN":
                if (regexStudySubjectIdEnrollment.test(studySubjectIdValue)) {
                    errorMessageValidationDiv.style.display = "none";
                    submitBottom.disabled = false;
                } else {
                    errorMessageValidationDiv.style.display = "block";
                    submitBottom.disabled = true;
                }
                break;
            case "NE":
                if (regexStudySubjectIdNonEnrollment.test(studySubjectIdValue)) {
                    errorMessageValidationDiv.style.display = "none";
                    submitBottom.disabled = false;
                } else {
                    errorMessageValidationDiv.style.display = "block";
                    submitBottom.disabled = true;
                }
                break;
            default:
                break;
        }
    }
</script>
<!-- start of submit/addNewSubjectExpressNew.jsp -->
