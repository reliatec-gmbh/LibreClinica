<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<div class="protocol_deviation_editor_popup form-standard" style="display: flex; flex-direction: column; padding: 10px; row-gap: 10px;">
    <style scoped>
        div.calendar {
            z-index: 2000;
        }
        #protocol-deviation-editor h1 {
            display: block;
            color: var(--lightblue-d20);
            font-family: Tahoma, Arial, Helvetica, sans-serif;
        }

        #protocol-deviation-editor .validation-error {
            font-weight: bold;
            display: block;
            color: var(--orange);
        }

        #protocol-deviation-editor h2 {
            display: block;
            color: white;
            font-family: Tahoma, Arial, Helvetica, sans-serif;
            font-size: 1rem;
            padding: 4px;
            background: var(--lightblue-d20);
        }
        #protocol-deviation-editor li {
            list-style-image: none;
            margin-bottom: 20px;
            min-height: 40px;
        }

        #protocol-deviation-editor li .question {
            font-weight: bold;
            margin-bottom: 10px;
        }
        #protocol-deviation-editor li .detail {
            font-weight: normal;
        }

        #protocol-deviation-editor li .detail label {
            font-weight: normal;
            margin-bottom: 8px;
        }

        #protocol-deviation-editor li .detail.vertical {
            font-weight: bold;
        }

        #protocol-deviation-editor li .detail textarea {
            display: block;
            height: 60px;
            width: 90%;
        }

        #protocol-deviation-editor li .detail.vertical > label {
            display: block;
        }

        #protocol-deviation-editor li:nth-child(even) {
            background: var(--gray-l40);
        }
    </style>
    <h1 id="title-editor">New Protocol Deviation</h1>
    <form method="post" onsubmit="return validateForm()"
          action="${pageContext.request.contextPath}/ProtocolDeviations">
        <input type="hidden" name="protocol_deviation_id"/>
        <c:import url="../submit/protocolDeviationEditorSectionA.jsp">
        </c:import>
        <c:import url="../submit/protocolDeviationEditorSectionB.jsp">
        </c:import>
        <c:import url="../submit/protocolDeviationEditorSectionC.jsp">
        </c:import>
        <c:import url="../submit/protocolDeviationEditorSectionD.jsp">
        </c:import>
        <c:import url="../submit/protocolDeviationEditorSectionE.jsp">
        </c:import>
        <c:import url="../submit/protocolDeviationEditorSectionF.jsp">
        </c:import>
        <c:import url="../submit/protocolDeviationEditorSectionG.jsp">
        </c:import>

    <h2>Participants</h2>
        <div style="display: flex;">
            <div class="formlabel" style="width: 120px;">
                Subject Id:
            </div>
            <div class="formlabel" style="">
                <select class="formfield" id="new-subject">
                    <option value="">(Select a participant)</option>
                    <c:forEach var="p" items="${subjects}">
                        <option value="${p.id}">
                                ${p.label}
                        </option>
                    </c:forEach>
                </select>
                <button type="button" class="button" id="add-subject">Add</button>
            </div>
        </div>
        <%--
        <div style="display: flex;">
            <div class="formlabel" style="width: 120px;">Text to search</div>
            <div><input type="text"></div>
        </div>
        --%>

        <div style="height: 240px;" id="subjects-added" class="protocol-deviation-subject-container">

        </div>
        <div class="buttons">
            <input type="hidden" name="action" value="saveProtocolDeviations"/>
            <
            <button class="button" type="submit">
                <fmt:message key="save" bundle="${resword}"/>
            </button>
            <button type="button" class="button" id="cancel">
                <fmt:message key="cancel" bundle="${resword}"/>
            </button>
        </div>
    </form>

</div>
<script>
    function validateForm() {
        const toValidateRadios= ['item_a_1', 'item_a_2', 'item_a_6', 'item_a_7', 'item_a_8',
            'item_b_1', 'item_b_2', 'item_b_3', 'item_b_4', 'item_b_5', 'item_b_6',
            'item_b_7', 'item_b_8', 'item_b_9', 'item_b_10', 'item_b_11', 'item_b_12',
            'item_b_13', 'item_b_14', 'item_b_15', 'item_b_16', 'item_b_17', 'item_b_18',
            'item_g_1', 'item_g_3', 'item_g_4', 'item_g_5',
        ];

        jQuery('#protocol-deviation-editor').find('.validation-error').remove();
        let retval = true;
        for(var v of toValidateRadios) {
            const selector='input[name="'+v+'"]';
            //console.log(selector, jQuery(selector));
            if(!jQuery(selector+':checked').val()) {
                let parent = jQuery(selector).first().closest('li');
                //console.log(parent);
                jQuery(parent).append('<div class="validation-error">This field cannot be empty</div>');
                retval = false;
            }
        }

        //retval = false;
        return retval;
    }
</script>
<DIV ID="testdiv1" STYLE="position:absolute;z-index:5;visibility:hidden;background-color:white;layer-background-color:white;"></DIV>
