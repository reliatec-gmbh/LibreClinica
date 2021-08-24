<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
                      "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
<style>
table {
	border-collapse: collapse;
	font-family: Tahoma;
}
td {
	padding-right: 2px;
	padding-left: 2px;
}
</style>
</head>
<font face="Tahoma" size="2">
Dear ${(person.firstName)!"n/a"} ${(person.surName)!"n/a"},<br/><br/>

you are getting this e-mail due to person relavant changes regarding login or application permissions. The Study Management Tool 
provides you the following roles with according privileges.<br/><br/>

Your login is: <b>${person.login}</b><br>
Access Level: <b>${person.access_level.name}</b><br>
Status:<b>${person.status}</b><br><br>



For technical support please file an <a href="mailto:ctca@mi.rwth-aachen.de?subject=Support Request">issue</a>.<br/><br/>

Your CTC-A web team
</font>
</html>