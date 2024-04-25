// rt_functions.js
// is part of a
// library of JavaScript functions to be used in CRF-Definitions.xls
// in OpenClinica or LibreClinica
// (c) 2018, 2024 ReliaTec GmbH, Garching, www.reliatec.de
// This is proprietary code, use or distribution without licensing or
// written permission by ReliaTec is prohibited.

function divsByName(name) {
	var divs = jQuery("div[name^='" + name + "']");
	return divs;
}

function enableUndoForRadioButtons() {
	var undoImgs = divsByName('undoradio');
	undoImgs.each(
		function(index, elem){
			var img = jQuery('<img src="images/bt_Restore.gif">');
			jQuery(elem).replaceWith(img);
			jQuery(img).click(function(){
				resetRadioButtons(jQuery(this));
			});
		});
};

function resetRadioButtons(element) {
	// identify the group radio buttons
	var radioGroup = jQuery(element).parent().parent().find("input");
	for (i = 0; i < radioGroup.length; i++) { 
		radioGroup[i].checked = false;  
	}
	radioGroup.change();
};

function updateInputStyles() {
	var divName = 'inputWidth';
	var updateInputStyleDivs = divsByName(divName);
	updateInputStyleDivs.each(
		function(index, elem){
			elem = jQuery(elem);
			var width = elem.attr('name').replace(divName,'');
			var parentTagName = elem.parent().prop('tagName');
			if(parentTagName == 'TH') {
				updateGroupInputStyles(index, elem, width);
			} else {
				updateNonGroupInputStyles(index, elem, width);
			}
		});
}

function nonGroupFormElement(referenceElem) {
	elem = jQuery(referenceElem);
	var formElem = elem.parent().parent().find("input");
	if(!formElem || formElem.length < 1) {
		formElem = elem.parent().parent().find("select");
	}
	if(!formElem || formElem.length < 1) {
		formElem = elem.parent().parent().find("textarea");
	}
	return formElem;
}

function updateNonGroupInputStyles(index, elem, width) {
	elem = jQuery(elem);
	// get form field
	var formElem = nonGroupFormElement(elem);
	// set width if the input field is found
	if(formElem) {
		formElem.width(width);
	}
}

function updateGroupInputStyles(index, elem, width){
	elem = jQuery(elem);
	var elemHeader = elem.parent();
	// set width of table header
	elemHeader.width(width);
	// get column index
	var colNr = elemHeader.parent().children().index(elemHeader) + 1;
	// get form field
	var elemInput = elemHeader.parent().parent().parent().children('tbody').children('tr').children('td:nth-child(' + colNr + ')').children('input');
	if(elemInput.length < 1) {
		elemInput = elemHeader.parent().parent().parent().children('tbody').children('tr').children('td:nth-child(' + colNr + ')').children('select');
	}
	// set width if the input field is found
	if(elemInput) {
		elemInput.css({'width':'' + width + 'px'});
	}
}

function configureToolTips() {
	var toolTipDivs = jQuery("div[id^='tooltip']");
	toolTipDivs.each(
		function(index, elem){
			// move the element in a surounding container div
			elem = jQuery(elem);
			var elemId = elem.attr('id');
			elem.removeAttr('id');

			var newElem = jQuery('<div style="display:none;"></div>');
			newElem.attr('id',elemId);

			elem.replaceWith(newElem);
			elem.appendTo(newElem);
			elem.show();

			// connect the tooltip with the trigger element
			var elemTrigger = jQuery("span[name^='" + elemId + "']");
			if(elemTrigger) {
				var elemTriggerHtml = elemTrigger.html();
				var newElemTrigger = jQuery('<a href="javascript:void(0)" onmouseover=\'TagToTip("' + elemId + '")\' onmouseout="UnTip()">(?)</a>');
				if(!(typeof elemTriggerHtml === 'undefined')) {
					newElemTrigger.html(elemTriggerHtml);
				}
				elemTrigger.replaceWith(newElemTrigger);
			}
		}
	);
}

function copyValue(divName, value){
	var divs = divsByName(divName);
	divs.each(
		function(index, referenceElem){
			referenceElem = jQuery(referenceElem);
			var elem = nonGroupFormElement(referenceElem)
			var currentValue = currentElementValue(elem);

			if(currentValue != value){
				setElementValue(elem, value);
			}
		});
}

function setElementValue(targetElem, val) {
    if(targetElem) {
	    if(targetElem.is(':radio')) {
	      if(val != null) {
		targetElem.val([val]);
	      } else {
		targetElem.prop('checked', false);
	      }
	      targetElem.change();
	    } else if(targetElem.is(':checkbox')) {
	      var inputId = targetElem.attr('id');
	      if(val != null && !Array.isArray(val)) {
		      val = [val];
	      }
			var checkboxes = jQuery('[id=' + inputId + ']');
			if(val != null) {
				checkboxes.filter(function(){
					return !val.includes(jQuery(this).val());
				}).prop('checked', false);
				checkboxes.filter(function(){
					return val.includes(jQuery(this).val());
				}).prop('checked', true);
			} else {
				checkboxes.prop('checked', false);
			}
			checkboxes.change();
	    } else {
	      targetElem.val(val);
	      targetElem.change();
	    }
    }
}

function getSubjectID(){
	var subjectID = jQuery("#centralContainer").find("table:first").find("tbody:first").children("tr:nth-child(1)").children("td:nth-child(2)").children("h1").text();
	subjectID = jQuery.trim(subjectID);
	return subjectID;
}

function copySubjectID(){
	var divName = 'copySubjectID';
	var subjectID = getSubjectID();
	copyValue(divName, subjectID);
}

function setReadOnly() {
	var divName = 'readOnly';
	var divs = divsByName(divName);
	divs.each(
		function(index, div){
			elem = nonGroupFormElement(div);
			disableElement(elem);
		});
}

function getEvent() {
	const event = jQuery.trim(
		jQuery(".tablebox_center")
		.find("tbody:first")
		.children("tr:nth-child(1)")
		.children("td:nth-child(2)")
		.text()
	);

	// name and date are separated by a non-breaking space
	const match = event.match(/^(?<name>.+)\xa0\((?<date>\d{2}-[A-Za-z]{3}-\d{4})\)$/);
	if (match === null)	return null;

	return {
		name: match.groups.name,
		date: match.groups.date,
	};
}

function getEventName() {
	return getEvent()?.name ?? null;
}

function copyEventName() {
	copyValue(
		"copyEventName",
		getEventName(),
	);
}

function getEventDate() {
	return getEvent()?.date ?? null;
}

function copyEventDate() {
	copyValue(
		"copyEventDate",
		getEventDate(),
	);
}

function getBirth(){
	var birth = jQuery(".tablebox_center").find("tbody:first").children("tr:nth-child(3)").children("td:nth-child(4)").text();
	birth = jQuery.trim(birth);
	return birth;
}

function copyBirth(){
	var divName = 'copyBirth';
	var birth = getBirth();
	copyValue(divName, birth);
}

function getGender() {
	var gender = jQuery(".tablebox_center").find("tbody:first").children("tr:nth-child(1)").children("td:nth-child(4)").text();
	gender = jQuery.trim(gender);
	return gender
}

function copyGender() {
	var divName = 'copyGender';
	var gender = getGender();
	copyValue(divName, gender);
}

function disable(targetId) {
  var targetElem = nonGroupFormElement(jQuery(targetId));
  disableElement(targetElem);
}

function disableElement(targetElem) {
  targetElem.attr('readonly',true);
  targetElem.css("background-color", "#E6E6E6");
  targetElem.click(function(event){event.preventDefault();});

  var inputID = targetElem.attr('id');
  if(inputID) {
	  var anchorID = inputID.replace('input','anchor');
	  jQuery("#" + anchorID).each(function(index, elem){elem.remove();});
  }
}

function enableUpdateTargetOnChange(triggerId, targetId, calculation) {
  var triggerElems = nonGroupFormElement(jQuery(triggerId));
  var targetElem = nonGroupFormElement(jQuery(targetId));
  if(triggerElems != null && !Array.isArray(triggerElems)) {
  	triggerElems = [triggerElems];
  }

  triggerElems.each(
	  function(triggerElem, index){
		  triggerElem = jQuery(triggerElem);
		  triggerElem.change(function() {
			  var result = calculation(currentElementValue(triggerElem));
			  setElementValue(targetElem, result);
		  });
		  var triggerElemNative = document.getElementById(triggerElem.attr('id'));
		  if(triggerElemNative != null && typeof triggerElemNative.onchange == 'function') {
			  var f = triggerElemNative.onchange;
			  triggerElemNative.onchange = function(event) {
				  if(typeof event == 'undefined') {
					  jQuery(this).change();
				  } else {
					  f();
				  }
			  }
		  }
  });
}

function requestValue(studySubjectOID, studyOID, eventOID, itemOID, calculationFunction) {
  var url = "rest/clinicaldata/xml/view/" + studyOID + "/" + studySubjectOID + "/" + eventOID + "/*";
  var successFunction = function(xml) {
    console.log('SUCCESS FUNCTION');

    
    var studySubjectOID = this['studySubjectOID'];
    var subjectID = this['subjectID'];
    var eventOID = this['eventOID'];
    var itemOID = this['itemOID'];
    var calculationFunction = this['calculation'];

    var subjectCriteria = null;
    if(studySubjectOID == '*') {
      subjectCriteria = 'OpenClinica:StudySubjectID="' + subjectID + '"';
    } else {
      subjectCriteria = 'SubjectKey="' + studySubjectOID + '"';
    }

    odm = jQuery(xml);
    jQuery(xml).find('ODM').find('SubjectData[' + subjectCriteria + ']').find('StudyEventData[StudyEventOID="' + eventOID + '"]').find('ItemData[ItemOID="' + itemOID + '"]').each(function(){
      var value = jQuery(this).attr('Value');
      calculationFunction(value);
    });
  };

  var subjectID = null;
  if(studySubjectOID == '*') {
    subjectID = getSubjectID();
    url = 'rest/clinicaldata/xml/view/' + studyOID + '/*/*/*';
  }

  jQuery.ajax({
		type: "GET",
		url: url,
		dataType: "xml",
		success: successFunction,
    studySubjectOID: studySubjectOID,
    subjectID: subjectID,
    studyOID: studyOID,
    eventOID: eventOID,
    itemOID: itemOID,
    calculation: calculationFunction
  });
}

function enableRadioMessage(containerId, value) {
	var msgContainer = jQuery(containerId);
	var radiosToCheck = msgContainer.parent().parent().find("input");
	radiosToCheck.change(function(){setMessage(radiosToCheck, msgContainer, value);});
	setMessage(radiosToCheck, msgContainer, value);
}

function setMessage(radiosToCheck, msgContainer, value) {
	for (var i = 0; i < radiosToCheck.length; i++) {
		if (radiosToCheck[i].checked) {
			var valueToCheck = radiosToCheck[i].value;
			break;
		}
	}
	if (valueToCheck && (valueToCheck == value)){
		msgContainer.show();
	}
	else {
		msgContainer.hide();
	}
}

function gotoUrl(url) {
	location.assign(url);
}

function currentElementValue(targetElem) {
	if(targetElem == null) {
		return null;
	}
	if(targetElem.is(':radio')) {
		var checkedRadio = jQuery(targetElem).filter(':checked');
		if(checkedRadio) {
			return checkedRadio.val();
		}
	} else if(targetElem.is(':checkbox')) {
		var checkedCheckboxes = targetElem.filter(':checked');
		if(checkedCheckboxes) {
			if(checkedCheckboxes.length == 1) {
				return checkedCheckboxes.val();
			} else if(checkedCheckboxes.length > 1) {
				return checkedCheckboxes.map(function(){return jQuery(this).val()}).toArray();
			}
		}
	} else {
		return targetElem.val();
	}
	return null;
}

function getRadioButtonValue(selector) {
	return (
		document.querySelector(selector)
		.parentNode
		.parentNode
		.querySelector('input[type="radio"]:checked')
		.value
	);
}

/*
 * fetch item value
 */

async function fetchItemValue(studySubjectOID, studyOID, eventOID, itemOID) {
	const url = `rest/clinicaldata/xml/view/${studyOID}/${studySubjectOID}/${eventOID}/*`;
	const subjectCriteria = `SubjectKey="${studySubjectOID}"`;

	const res = await fetch(url);
	const xml = await res.text();

	return (
		jQuery(jQuery.parseXML(xml))
		.find("ODM")
		.find(`SubjectData[${subjectCriteria}]`)
		.find(`StudyEventData[StudyEventOID="${eventOID}"]`)
		.find(`ItemData[ItemOID="${itemOID}"]`)
		.attr("Value")
	);
}

/*
 * validation functions
 */

function validatedSubmit(formSelector, validators, skipValidation = null) {
	document.querySelector(formSelector).addEventListener("submit", async (event) => {
		event.preventDefault();

		const form = event.target;

		// submit if skipValidation returns true
		if (skipValidation !== null && skipValidation(event)) {
			const submitter = event.submitter;

			// store submitter as hidden input
			if (submitter !== null) {
				var input = document.createElement("input");
				input.setAttribute("type", "hidden");
				input.setAttribute("name", submitter.name);
				input.setAttribute("value", submitter.value);

				// append hidden input to form
				form.appendChild(input);
			}

			// submit form
			form.submit();
			return;
		}

		const valids = await Promise.all(validators.map(it => it()));

		// submit if all validations succeeded
		if (valids.every(it => it)) {
			form.submit();
		}
	});
}

function validateOnSave(formSelector, validators) {
	// remove "form.submit()" call from "Save" submit buttons
	for (const submit of document.querySelectorAll('input[type="submit"][name="submittedResume"]')) {
		submit.removeAttribute("onclick");
		submit.addEventListener("click", (event) => {
			elem = event.target;
			form = elem.closest('form');

			// disable all submit buttons
			for (const submit of form.querySelectorAll('input[type="submit"]')) {
				submit.setAttribute("disabled", "");
			}

			// submit form
			form.requestSubmit(elem);

			// re-enable submit buttons after 3 seconds
			setTimeout(
				() => {
					for (const submit of form.querySelectorAll('input[type="submit"]')) {
						submit.removeAttribute("disabled");
					}
				},
				3000
			);
		});
	}

	return validatedSubmit(
		formSelector,
		validators,
		(event) => event.submitter !== null && event.submitter.name === "submittedExit"
	);
}

jQuery(document).ready(enableUndoForRadioButtons);
jQuery(document).ready(updateInputStyles);
jQuery(document).ready(configureToolTips);
jQuery(document).ready(copySubjectID);
jQuery(document).ready(copyEventName);
jQuery(document).ready(copyEventDate);
jQuery(document).ready(copyBirth);
jQuery(document).ready(copyGender);
jQuery(document).ready(setReadOnly);
jQuery(document).ready();
jQuery.noConflict();
