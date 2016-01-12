# Introduction #
The goal of this tutorial is to allow you to install a writeback data source into your tomcat environment, and to use the sample client.


# Getting to run the Data Source #
There are several ways to obtain the data source. Probably the easiest way is to get the [WAR file](http://code.google.com/p/writeback/downloads/detail?name=writeback-datasource-1.6.5.war), but you can also get the source from our [SVN](https://writeback.googlecode.com/svn).

We will assume you are using the WAR file and you are using a [Tomcat](http://tomcat.apache.org/) server installed in your local machine in the port 8080.

You have to copy the WAR file into the webapps folder, and restart tomcat. Alternatively you can use the Tomcat manager to deploy the war file and to start the application.

As we are using the version 1.6.5 of the data source all the following links will contain that version, you might want to change this version number for the one you are using.

You can now test Mydas is running by visiting http://localhost:8080/writeback-datasource-1.6.5/das
The response document should be the active sources in the MyDas installation, which in this case should be only the writeback. It should look similar to:
```
<?xml version="1.0" standalone="no"?>
 <SOURCES>
  <SOURCE uri="writeback" doc_href="http://writeback.com" title="writeback" description="Writeback Data Source">
   <MAINTAINER email="writeback@ebi.ac.uk" />
   <VERSION uri="writeback" created="2010-08-19">
    <COORDINATES uri="http://www.ebi.ac.uk/das-srv/uniprot/das/aristotle" source="Protein Sequence" authority="UniProt" test_range="P00280">UniProt,Protein Sequence</COORDINATES>
    <CAPABILITY type="das1:writeback" query_uri="http://www.ebi.ac.uk/das-srv/uniprot/das/uniprot/writeback" />
   </VERSION>
  </SOURCE>
 </SOURCES>
```

# The writeback components #
Since version 1.6.5 the writeback data source includes a demo client to interact with the server. The only purpose of such a client is for documenting by example and it does not pretend to be a full interface for the system.

The next part of this tutorial is based in the mentioned client. The demo can be found in http://localhost:8080/writeback-datasource-1.6.5/. The client is implemented in javascript and uses Jquery to do the Ajax calls. As client and server are here running in the same machine it is possible to execute Ajax calls without any proxy.

The writeback some unusual the HTTP methods (PUT and DELETE), HTML does not support this methods, however javascript does, here we will show how to use this client and how it was implemented.

## Create a user ##
Users are part of the writeback, for identification purposes but it does not have any authorization consequences, at least not yet, at least not in the communitarian version of the writeback.
Because of that, the module for creation and authentication is as simple as possible. It just has two fields: `login` and `password`. It is in the plan to improve this, once a authentication/authorization is defined for DAS.

Now go to the sample application (http://localhost:8080/writeback-datasource-1.6.5/) and in the 'Adding a user' section, fill the form with 'tester' in both `login` and `password`, and then click to the button `CREATE-USER`. And if everything is working well a 'User created' message should appears under the button.

In its current state, a user can be created by executing the command `createuser` of the data source via GET method. It should contains the 2 parameters `user` and `password`. An empty response with HTTP code equal to 200 OK indicates that the user was created. Any other HTTP code should be interpreted as an error, that could it be caused by using an existing user name or for communication problems.

The HTML form to capture the user data is:
```
<section id="creating_user">
	<h2>Adding a user</h2>
	<p>Create a user:</p>
	<table>
		<tr><td>User:</td><td><input type="text" id="user" /></td></tr>
		<tr><td>Password:</td><td><input type="password" id="password" /></td></tr>
		<tr>
			<td colspan="2"><input type="button" onclick="createUser();" value="CREATE-USER"/></td>
		</tr>
		<tr>
			<td colspan="2"><div id="result_cu"></div></td>
		</tr>
	</table>
</section>
```

When the `CREATE-USER` is pressed the javascript method `createUser()` is triggered:
```
function createUser(){
	$('#result_cu').html("");
	var user = $("#user").val();
	var pass = $("#password").val();
	$.ajax({
		type: "GET",
		url: "das/writeback/createuser",
		data: "user="+user+"&password="+pass,
		success: function(msg){
			$('#result_cu').html("User created");
		},
		error: function(jqXHR, textStatus, errorThrown){
			$('#result_cu').html("ERROR: "+errorThrown);
		}
	});
}
```
This function captures the values of the form, and makes an ajax call. In case of success, the `<div>` under the button is filled with the "User created" message, and with an error message in any other case.

## Create a Feature ##
Now into the core of the writeback we can create a feature for an specific segment by sending via POST a DAS GFF document. So, in the section `Adding/Updating a Feature` of the sample application (http://localhost:8080/writeback-datasource-1.6.5/) you can input the next XML document in the text area:
```
<?xml version="1.0" standalone="no"?>
<DASGFF>
 <GFF>
  <SEGMENT id="P05067" start="1" stop="770" version="7dd43312cd29a262acdc0517230bc5ca" label="Amyloid beta A4 protein" >
   <FEATURE id="new" label="testAnnotation" >
    <TYPE id="SO:0001072" cvId="SO:0001072" >extramembrane_polypeptide_region</TYPE>
    <METHOD id="ECO:0000053" cvId="ECO:0000053" >computational combinatorial analysis</METHOD>
    <START>50</START>
    <END>100</END>
    <NOTE>Test annotation</NOTE>
    <NOTE>USER=tester</NOTE><NOTE>PASSWORD=tester</NOTE>
   </FEATURE>
  </SEGMENT>
 </GFF>
</DASGFF>
```
By clicking the `ADD-FEATURE` button the XML is submitted via POST and the writeback data source, is saving it as a new feature. The response of the writeback source is displayed under the button:
```
<?xml version="1.0" standalone="no"?>
<?xml-stylesheet href="xslt/features.xsl" type="text/xsl"?>
<DASGFF>
  <GFF href="http://localhost:8080/das/writeback">
    <SEGMENT id="P05067" start="1" stop="770" version="7dd43312cd29a262acdc0517230bc5ca" label="Amyloid beta A4 protein">
      <FEATURE id="http://writeback/0" label="testAnnotation">
        <TYPE id="SO:0001072" cvId="SO:0001072">extramembrane_polypeptide_region</TYPE>
        <METHOD id="ECO:0000053" cvId="ECO:0000053">computational combinatorial analysis</METHOD>
        <START>50</START>
        <END>100</END>
        <NOTE>Test annotation</NOTE>
        <NOTE>USER=tester</NOTE>
        <NOTE>DATE=2011-03-30 11:42:56.325</NOTE>
        <NOTE>VERSION=1</NOTE>
      </FEATURE>
    </SEGMENT>
  </GFF>
</DASGFF>
```
Note that now the id of the feature is `http://writeback/0` and there is a note showing the version of this annotation: `<NOTE>USER=tester</NOTE>`.

The HTML form to capture the XML is:
```
<section id="adding">
	<h2>Adding/Updating a Feature</h2>
	<p>Put your DASGFF XML here:</p>
	<textarea rows="10" cols="50" id="featureXMLadd"></textarea>
	<p>
		<input type="button" onclick="create();" value="ADD-FEATURE"/>
		<input type="button" onclick="update();" value="UPDATE-FEATURE"/>
	</p>
	<p><div id="result_au"></div></p>
</section>
```
A click in the `ADD-FEATURE` button triggers the function `create()`:
```
function create(){
	var content = encodeURI($("#featureXMLadd").val());
	$.ajax({
		type: "POST",
		url: "das/writeback",
		dataType: 'text',
		data: "_content="+content,
		success: function(msg){
			var xml = msg.replace(/</g,"&lt;");
			$('#result_au').html("<pre class='brush: xml'>"+xml+"</pre>");
		},
		error: function(jqXHR, textStatus, errorThrown){
			$('#result_au').html("ERROR: "+errorThrown);
		}
	});
}
```
This function executes an ajax call, but this time it uses POST, which indicates to the writeback server that this feature has to be created. The XML is been encoded for the communication with the javascript function `encodeURI()` and is sent as the value of a parameter called `_content`.
The success function just replaces all the `<` characters for its HTML code `&lt` for displaying options and then put that as the content of the `<div>` under the button.

## Edit a feature ##
If we use the same XML but click in the `UPDATE-FEATURE` the response of the server will be:
```
<?xml version="1.0" standalone="no"?>
<?xml-stylesheet href="xslt/features.xsl" type="text/xsl"?>
<DASGFF>
  <GFF href="http://localhost:8080/das/writeback">
    <SEGMENT id="P05067" start="1" stop="770" version="7dd43312cd29a262acdc0517230bc5ca" label="Amyloid beta A4 protein">
      <FEATURE id="http://writeback/0" label="testAnnotation">
        <TYPE id="SO:0001072" cvId="SO:0001072">extramembrane_polypeptide_region</TYPE>
        <METHOD id="ECO:0000053" cvId="ECO:0000053">computational combinatorial analysis</METHOD>
        <START>50</START>
        <END>100</END>
        <NOTE>Test annotation</NOTE>
        <NOTE>USER=tester</NOTE>
        <NOTE>DATE=2011-03-30 11:42:56.325</NOTE>
        <NOTE>VERSION=1</NOTE>
      </FEATURE>
      <FEATURE id="http://writeback/new" label="testAnnotation">
        <TYPE id="SO:0001072" cvId="SO:0001072">extramembrane_polypeptide_region</TYPE>
        <METHOD id="ECO:0000053" cvId="ECO:0000053">computational combinatorial analysis</METHOD>
        <START>50</START>
        <END>100</END>
        <NOTE>Test annotation</NOTE>
        <NOTE>USER=tester</NOTE>
        <NOTE>DATE=2011-03-30 12:17:17.344</NOTE>
        <NOTE>VERSION=1</NOTE>
      </FEATURE>
    </SEGMENT>
  </GFF>
</DASGFF>
```
First notice that writeback responses will include all the other annotations that the server has for this segment, and therefore in this segment we have 2 annotations, the one created and the one edited.
For the writeback server an update request means that the feature already exists, but this does not necessary means that have to exists in the writeback server; it could be in a different server. The id of he edited feature is `http://writeback/new` and its version is 1. We can edit this feature by using the id in the feature element of the XML. So use the next XML for a new update that will change the coordinates of the feature:
```
<?xml version="1.0" standalone="no"?>
<DASGFF>
 <GFF>
  <SEGMENT id="P05067" start="1" stop="770" version="7dd43312cd29a262acdc0517230bc5ca" label="Amyloid beta A4 protein" >
   <FEATURE id="http://writeback/new" label="testAnnotation" >
    <TYPE id="SO:0001072" cvId="SO:0001072" >extramembrane_polypeptide_region</TYPE>
    <METHOD id="ECO:0000053" cvId="ECO:0000053" >computational combinatorial analysis</METHOD>
    <START>150</START>
    <END>200</END>
    <NOTE>Test annotation 2</NOTE>
    <NOTE>USER=tester</NOTE><NOTE>PASSWORD=tester</NOTE>
   </FEATURE>
  </SEGMENT>
 </GFF>
</DASGFF>
```
The response still containing 2 features, because the last update created another version of a feature that is already in the server. You can notice that now the version is `<NOTE>VERSION=2</NOTE>` and the `START` and `END` elements are now 150 and 200.
```
<?xml version="1.0" standalone="no"?>
<?xml-stylesheet href="xslt/features.xsl" type="text/xsl"?>
<DASGFF>
  <GFF href="http://localhost:8080/das/writeback">
    <SEGMENT id="P05067" start="1" stop="770" version="7dd43312cd29a262acdc0517230bc5ca" label="Amyloid beta A4 protein">
      <FEATURE id="http://writeback/new" label="testAnnotation">
        <TYPE id="SO:0001072" cvId="SO:0001072">extramembrane_polypeptide_region</TYPE>
        <METHOD id="ECO:0000053" cvId="ECO:0000053">computational combinatorial analysis</METHOD>
        <START>150</START>
        <END>200</END>
        <NOTE>Test annotation 2</NOTE>
        <NOTE>USER=tester</NOTE>
        <NOTE>DATE=2011-03-30 14:17:36.53</NOTE>
        <NOTE>VERSION=2</NOTE>
      </FEATURE>
      <FEATURE id="http://writeback/0" label="testAnnotation">
        <TYPE id="SO:0001072" cvId="SO:0001072">extramembrane_polypeptide_region</TYPE>
        <METHOD id="ECO:0000053" cvId="ECO:0000053">computational combinatorial analysis</METHOD>
        <START>50</START>
        <END>100</END>
        <NOTE>Test annotation</NOTE>
        <NOTE>USER=tester</NOTE>
        <NOTE>DATE=2011-03-30 11:42:56.325</NOTE>
        <NOTE>VERSION=1</NOTE>
      </FEATURE>
    </SEGMENT>
  </GFF>
</DASGFF>
```

The HTML form for the editing is the same that for the creation, however the `UPDATE-FEATURE` button is now triggering the method `update()`:
```
function update(){
	var content = $("#featureXMLadd").val();
	$.ajax({
		type: "PUT",
		url: "das/writeback",
		dataType: 'text',
		data: content,
		contentType: "application/xml",
		success: function(msg){
			var xml = msg.replace(/</g,"&lt;");
			$('#result_au').html("<pre class='brush: xml'>"+xml+"</pre>");
		},
		error: function(jqXHR, textStatus, errorThrown){
			$('#result_au').html("ERROR: "+errorThrown);
		}
	});
}
```
Is important to highlight that this function is using the HTTP method PUT, and therefore the XML is the content of the request and is not wrapped in the traditional `parameter=value` way; that makes necessary to include the contentType of such content which here is `application/xml`. Besides that the method is similar to `create()`.

## Getting a segment ##
Here the writeback behaves as any other DAS source, the query methods are the same. So, for this example if we query by `P05067` the response will be the same than the previous XML, because the create, update and delete methods of the writeback will respond as the feature command after the changes have been executed.

You can try to request a segment that haven't been registered in the writeback, for example `not_in_wb` and the response is following the DAS spec by reporting an Unknown Segement:
```
<?xml version="1.0" standalone="no"?>
<?xml-stylesheet href="xslt/features.xsl" type="text/xsl"?>
<DASGFF>
  <GFF href="http://localhost:8080/das/writeback/features?segment=not_in_wb">
    <UNKNOWNSEGMENT id="not_in_wb" />
  </GFF>
</DASGFF>
```
The HTML form for this section is quite simple:
```
<section id="getting">
	<h2>Getting Features By Segment</h2>
	<p>Segment ID:</p>
	<input type="text" id="segment" />
	<p>
		<input type="button" onclick="displaySegment()" value="GET-SEGMENT" />
	</p>
	<p><div id="result_gs"></div></p>
</section>
```
And by clicking the `GET-SEGMENT` button the `displaySegment()` is invoked:
```
function displaySegment(){
	$('#result_gs').html("");
	var seg = $("#segment").val();
	$.ajax({
		type: "GET",
		url: "das/writeback/features",
		dataType: 'text',
		data: "segment="+seg,
		success: function(msg){
			var xml = msg.replace(/</g,"&lt;");
			$('#result_gs').html("<pre class='brush: xml'>"+xml+"</pre>");
		},
		error: function(jqXHR, textStatus, errorThrown){
			$('#result_gs').html("ERROR: "+errorThrown);
		}
	});
}
```
Nothing interesting in this method that we haven't mention in the previous ones.

## Deleting a Feature ##
Now in this section you will be able to tag a feature as `DELETED`, the writeback is not going to really delete a feature, non the writeback annotations and neither the other servers annotations. What the writeback does is to create a new version of the feature that is marked as deleted, so a client will know that a feature has been "deleted" and it can display it in an appropriate way.

In the demo application you can fill the form with the information of the feature that we have edited twice in this tutorial:
  * Feature id: http://writeback/new
  * Segment id: P05067
  * Original server URL: http://writeback
  * User: tester
  * Password: tester

And the response after pressing the `DELETE FEATURE` button should be:
```
<?xml version="1.0" standalone="no"?>
<?xml-stylesheet href="xslt/features.xsl" type="text/xsl"?>
<DASGFF>
  <GFF href="http://localhost:8080/das/writeback?segmentid=P05067&featureid=http://writeback/new&user=tester&password=tester&href=http://writeback">
    <SEGMENT id="P05067" start="1" stop="770" version="7dd43312cd29a262acdc0517230bc5ca" label="Amyloid beta A4 protein">
      <FEATURE id="http://writeback/0" label="testAnnotation">
        <TYPE id="SO:0001072" cvId="SO:0001072">extramembrane_polypeptide_region</TYPE>
        <METHOD id="ECO:0000053" cvId="ECO:0000053">computational combinatorial analysis</METHOD>
        <START>50</START>
        <END>100</END>
        <NOTE>Test annotation</NOTE>
        <NOTE>USER=tester</NOTE>
        <NOTE>DATE=2011-03-30 11:42:56.325</NOTE>
        <NOTE>VERSION=1</NOTE>
      </FEATURE>
      <FEATURE id="http://writeback/new" label="DELETED">
        <TYPE id="DELETED" />
        <METHOD id="DELETED" />
        <SCORE>0.0</SCORE>
        <NOTE>USER=tester</NOTE>
        <NOTE>DATE=2011-03-30 15:00:54.623</NOTE>
        <NOTE>VERSION=3</NOTE>
      </FEATURE>
    </SEGMENT>
  </GFF>
</DASGFF>
```

Note that now this feature is in its version number 3 and the value of the parameter `label` for the element `FEATURE` is `DELETED`. The id of the elements `TYPE` and `METHOD` is also `DELETED`.

The HTML form for this section is:

```
<section id="deleting">
	<h2>Deleting a Feature</h2>
	<table>
		<tr><td>Feature ID:</td><td><input type="text" id="feature_to_del" /></td></tr>
		<tr><td>Segment ID:</td><td><input type="text" id="segment_to_del" /></td></tr>
		<tr><td>Original server URL:</td><td><input type="text" id="href_to_del" /></td></tr>
		<tr><td>User:</td><td><input type="text" id="user_d" /></td></tr>
		<tr><td>Password:</td><td><input type="password" id="password_d" /></td></tr>
		<tr>
			<td colspan="2"><input type="button" onclick="deleteFeature()" value="DELETE FEATURE"/></td>
		</tr>
		<tr>
			<td colspan="2"><div id="result_df"></div></td>
		</tr>
	</table>
</section>
```
And the method executed is:
```
function deleteFeature(){
	$('#result_df').html("");
	var user = $("#user_d").val();
	var pass = $("#password_d").val();
	var feature = $("#feature_to_del").val();
	var segment = $("#segment_to_del").val();
	var href = $("#href_to_del").val();
	$.ajax({
		type: "DELETE",
		url: "das/writeback?segmentid="+segment+"&featureid="+feature+"&user="+user+"&password="+pass+"&href="+href,
		dataType: 'text',
		success: function(msg){
			var xml = msg.replace(/</g,"&lt;");
			$('#result_df').html("<pre class='brush: xml'>"+xml+"</pre>");
		},
		error: function(jqXHR, textStatus, errorThrown){
			$('#result_df').html("ERROR: "+errorThrown);
		}
	});
}
```
Here the HTTP method is `DELETE` and the parameters are passed in the url. As usual the response is displayed in the `<div>` under the button.

## Getting the history of a feature ##
The last command to discuss in this tutorial give us the chance to see all the versions that an specific feature have, which may be used for rolling back an annotation(by re-submitting).

Go to the 'Getting the history of a Feature' section of the demo app, and submit the value `http://writeback/new`. The response will show the 3 versions that we have created in this tutorial:
```
<?xml version="1.0" standalone="no"?>
<?xml-stylesheet href="xslt/features.xsl" type="text/xsl"?>
<DASGFF>
  <GFF href="http://localhost:8080/das/writeback/historical?feature=http://writeback/new">
    <SEGMENT id="P05067" start="1" stop="770" version="7dd43312cd29a262acdc0517230bc5ca" label="Amyloid beta A4 protein">
      <FEATURE id="http://writeback/new" label="DELETED">
        <TYPE id="DELETED" />
        <METHOD id="DELETED" />
        <SCORE>0.0</SCORE>
        <NOTE>USER=tester</NOTE>
        <NOTE>DATE=2011-03-30 15:00:54.623</NOTE>
        <NOTE>VERSION=3</NOTE>
      </FEATURE>
      <FEATURE id="http://writeback/new" label="testAnnotation">
        <TYPE id="SO:0001072" cvId="SO:0001072">extramembrane_polypeptide_region</TYPE>
        <METHOD id="ECO:0000053" cvId="ECO:0000053">computational combinatorial analysis</METHOD>
        <START>50</START>
        <END>100</END>
        <NOTE>Test annotation</NOTE>
        <NOTE>USER=tester</NOTE>
        <NOTE>DATE=2011-03-30 12:17:17.344</NOTE>
        <NOTE>VERSION=1</NOTE>
      </FEATURE>
      <FEATURE id="http://writeback/new" label="testAnnotation">
        <TYPE id="SO:0001072" cvId="SO:0001072">extramembrane_polypeptide_region</TYPE>
        <METHOD id="ECO:0000053" cvId="ECO:0000053">computational combinatorial analysis</METHOD>
        <START>150</START>
        <END>200</END>
        <NOTE>Test annotation 2</NOTE>
        <NOTE>USER=tester</NOTE>
        <NOTE>DATE=2011-03-30 14:17:36.53</NOTE>
        <NOTE>VERSION=2</NOTE>
      </FEATURE>
    </SEGMENT>
  </GFF>
</DASGFF>
```

The HTML form for this is quite similar to the one to show the features of a segment.
```
<section id="history">
	<h2>Getting the history of a Feature</h2>
	<p>Feature ID:</p>
	<input type="text" id="feature_h" />
	<p>
		<input type="button" onclick="displayHistory()" value="GET-HISTORY" />
	</p>
	<p><div id="result_hs"></div></p>
</section>
```
The javascript method executed for this capability is `displayHistory()` which is also similar to the one for displaying the features of a segment: `displaySegment()`, with the difference that now the url to query includes the command historical and the parameter to pass is `feature`:
```
function displayHistory(){
	$('#result_hs').html("");
	var feat = $("#feature_h").val();
	$.ajax({
		type: "GET",
		url: "das/writeback/historical",
		dataType: 'text',
		data: "feature="+feat,
		success: function(msg){
			var xml = msg.replace(/</g,"&lt;");
			$('#result_hs').html("<pre class='brush: xml'>"+xml+"</pre>");
		},
		error: function(jqXHR, textStatus, errorThrown){
			$('#result_hs').html("ERROR: "+errorThrown);
		}
	});
}
```

## Conclusions ##
If you have reach this point you have went through all the concepts of a Data Source that implements the writeback capability. This demo app is using Derby for persistent storage, but you can easily configure it to use PostgreSQL or MySQL because it uses Hibernate.

This implementation of the writeback has been though for open collaborative environments, for more specific uses of the writeback concept, you can get the source code from the SVN and adapt it to your requirements.