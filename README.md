[![DSpace Logo](https://the-library-code.de/dspace_logo.png)](http://www.dspace.org)

[![The Library Code GmbH](https://the-library-code.de/the_library_code_gmbh.png)](https://www.the-library-code.de)

# Persistent identifiers enduring Submission

This is an addition to [DSpace](http://www.dspace.org) (on github [@DSpace/DSpace](https://github.com/DSpace/DSpace)) to show persistent identifiers enduring the submission of any new item. It currently supports DSpace's JSPUI in version 5.8 only, but it should be simple to be ported to XMLUI or DSpace's version 6.x. If you need help porting it, please don't hesitate to contact [The Library Code](https://www.the-library-code.de). If you port it yourself, we are grateful if you create a pull request.

## Contributors and License

DSpace source code is freely available under a standard [BSD 3-Clause license](https://opensource.org/licenses/BSD-3-Clause), so is the source code of this add-on. The full license is available at http://www.dspace.org/license/.

This addion was developped by [The Library Code](https://www.the-library-code.de) with the support of [Technische Universität Hamburg](https://www.tuhh.de).

## Branches

We will add one branch per supported version. Currently DSpace 5.8 is the only supported version yet: [dspace-5.8-addition](https://github.com/the-library-code/identifiers-enduring-submission/tree/dspace-5.8-addition).

## Installation

To install this add-on, you will need to know how to compile and update DSpace. There are two ways to install this add-on, both are documented below. After installing the add-on please change your configuration as described in the section "Configuration" below. The Library Code also offers support to install this and other add-ons.

### Install the add-on by changing two poms and the message catalog

To install the update using maven's capability to automatically download and include all necessary files, you need change two pom files and the message catalog.

In the file `[dspace-source]/dspace/modules/additions/pom.xml` you will find a section `<dependencies>...</dependencies>`. At the end of the section, right before the closing tag, you add the following lines:

<pre>
  &lt;dependency&gt;
    &lt;groupId&gt;de.the-library-code.dspace&lt;/groupId&gt;
    &lt;artifactId&gt;addon-identifiers-enduring-submission-api&lt;/artifactId&gt;
    &lt;version&gt;5.8.0&lt;/version&gt;
    &lt;type&gt;jar&lt;/type&gt;
  &lt;/dependency&gt;
</pre>

In the file `[dspace-source]/dspace/modules/jspui/pom.xml` you will find a section `<dependencies>...</dependencies>`. At the end of the section, right before the closing tag, you add the following lines:

<pre>
  &lt;dependency&gt;
      &lt;groupId&gt;de.the-library-code.dspace&lt;/groupId&gt;
      &lt;artifactId&gt;addon-identifiers-enduring-submission-api&lt;/artifactId&gt;
      &lt;version&gt;5.8.0&lt;/version&gt;
      &lt;type&gt;jar&lt;/type&gt;
  &lt;/dependency&gt;
  &lt;dependency&gt;
      &lt;groupId&gt;de.the-library-code.dspace&lt;/groupId&gt;
      &lt;artifactId&gt;addon-identifiers-enduring-submission-jspui&lt;/artifactId&gt;
      &lt;version&gt;5.8.0&lt;/version&gt;
      &lt;type&gt;war&lt;/type&gt;
  &lt;/dependency&gt;
</pre>

Add the content of the file [additional-Messages.properties](https://github.com/the-library-code/identifiers-enduring-submission/blob/dspace-5.8-addition/additional-Messages.properties) to your message catalog. Your message catalog is located either under `[dspace-src]/dspace/modules/jspui/src/main/resources/Messages.properties` or `[dspace-src]/dspace-api/src/main/resources/Messages.properties`.

Then you change your configuration as described below, re-compile and update DSpace, and restart Tomcat to finish the installtion.

### Installing the add-on by copying its code into your overlays

Copy the files within the [jspui/src](https://github.com/the-library-code/identifiers-enduring-submission/tree/dspace-5.8-addition/jspui/src)- and [api/src](https://github.com/the-library-code/identifiers-enduring-submission/tree/dspace-5.8-addition/api/src)-directory into your overlays (`[dspace-src]/dspace/modules/additions/src/...` and `[dspace-src]/dspace/modules/jspui/src/...`). Please pay attention to not overwrite locally changed files. Add the content of the file [additional-Messages.properties](https://github.com/the-library-code/identifiers-enduring-submission/blob/dspace-5.8-addition/additional-Messages.properties) to your message catalog. Your message catalog is located either under `[dspace-src]/dspace/modules/jspui/src/main/resources/Messages.properties` or `[dspace-src]/dspace-api/src/main/resources/Messages.properties`. Change your configuration as described in the following section of this readme, recompile and update DSpace, and restart Tomcat to finish the installation.

This add-on added the following files:

 * api/src/main/java/org/dspace/submit/step/MintIdentifiersStep.java
 * api/src/main/java/org/dspace/submit/step/ShowIdentifiersStep.java
 * jspui/src/main/java/org/dspace/app/webui/submit/step/JSPShowIdentifiersStep.java 
 * jspui/src/main/webapp/submit/list-identifiers.jsp
 * jspui/src/main/webapp/submit/review-identifiers.jsp

## Configuration

To use this add-on you will have to change the configuration of DSpace's submission process. Read this [page in DSpace's documentation](https://wiki.duraspace.org/display/DSDOC5x/Submission+User+Interface). You will have to add two steps at the point in the submission process where the identifiers should be shown. We recommend to do this after the items are described and before any upload step. To do so, you need to change the file `item-submission.xml` in your configuration. The lines to add are listed below this paragraph. We also added an example to [config/item-submission.xml](https://github.com/the-library-code/identifiers-enduring-submission/blob/dspace-5.8-addition/config/item-submission.xml#L210-L226).

<pre>
 &lt;step&gt;
	&lt;processing-class&gt;org.dspace.submit.step.MintIdentifiersStep&lt;/processing-class&gt;
	&lt;workflow-editable&gt;false&lt;/workflow-editable&gt;
  &lt;/step&gt;
  &lt;step&gt;
	&lt;heading&gt;submit.progressbar.showidentifiers&lt;/heading&gt;
	&lt;processing-class&gt;org.dspace.submit.step.ShowIdentifiersStep&lt;/processing-class&gt;
	&lt;jspui-binding&gt;org.dspace.app.webui.submit.step.JSPShowIdentifiersStep&lt;/jspui-binding&gt;
	&lt;workflow-editable&gt;true&lt;/workflow-editable&gt;
  &lt;/step&gt; 
</pre>

Per default all identifiers will be listed. You can change this and configure which identifiers are shown by adding a configuration property with the key `webui.submission.list-identifiers` to your dspace.cfg. Currently a space-separated list containing any of the following value is supported:

 * all
 * handle
 * doi
 * other

## In case of questions

If you have further questions, please don't hesitate to contact [The Library Code GmbH](https://www.the-library-code.de).
