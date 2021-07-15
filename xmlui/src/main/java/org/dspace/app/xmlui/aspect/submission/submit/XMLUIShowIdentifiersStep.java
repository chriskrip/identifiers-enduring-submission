/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.submission.submit;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.aspect.submission.AbstractSubmissionStep;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.identifier.DOI;
import org.dspace.identifier.Handle;
import org.dspace.identifier.factory.IdentifierServiceFactory;
import org.dspace.identifier.service.IdentifierService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.xml.sax.SAXException;


import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * This step shows previously minted persistent identifiers enduring the
 * submission of a new item. The identifiers are displayed in an own step.
 *
 * @see org.dspace.submit.step.ShowIdentifiersStep
 * @see org.dspace.submit.step.MintIdentifiersStep
 *
 * @author Christian Krippes (christian dot krippes at bibsys dot uni dash giessen dot de)
 *
 * This code is an adaptation of the "identifiers enduring submission" feature for the xmlui.
 * The feature was written by Pascal-Nicolas Becker (pascal at the dash library dash code dot de)
 */

public class ShowIdentifiersStep extends AbstractSubmissionStep {

    private static final Logger log = Logger.getLogger(ShowIdentifiersStep.class);

    protected static final Message T_head =
            message("xmlui.Submission.submit.ShowIdentifiersStep.head");

    protected static final Message T_doi_label =
            message("xmlui.Submission.submit.ShowIdentifiersStep.doiLabel");

    protected static final Message T_handle_label =
            message("xmlui.Submission.submit.ShowIdentifiersStep.handleLabel");

    protected static final Message T_info_message =
            message("xmlui.Submission.submit.ShowIdentifiersStep.infoMessage");


    /**
     * Establish our required parameters.  AbstractStep will enforce these.
     */
    public ShowIdentifiersStep()
    {
        this.requireSubmission = true;
        this.requireStep = true;
    }

    @Override
    public void addBody(Body body) throws SAXException, WingException,
            SQLException, IOException, AuthorizeException
    {
        // Obtain the inputs (i.e. metadata fields we are going to display)
        Item item = submission.getItem();
        Collection collection = submission.getCollection();
        String actionURL = contextPath + "/handle/" + collection.getHandle() + "/submit/" + knot.getId() + ".continue";

        if (item == null) {
            // this should never happen. Catch and log it to prevent possible NullPointerException.
            //log.warn("ShowIdentifierStep called, but no item supplied.");
            throw new IllegalStateException("ShowIdentifiersStep called, but no item supplied.");
        }

        //Create a new div section for this step and set the heading
        Division identifierDivision = body.addInteractiveDivision("submit-identifiers", actionURL, Division.METHOD_POST, "primary submission");
        identifierDivision.setHead(T_head);
        addSubmissionProgressList(identifierDivision);
        List identifiersList = identifierDivision.addList("identifiers-list",  List.TYPE_FORM);
        identifiersList.addItem(T_info_message);

        HashMap<String, Boolean> showIdentifiersConfig = getShowIdentifiersConfig();
        HashMap<String, String> identifiers = getIdentifiers(showIdentifiersConfig, item);

        //Create new divisions for two kind of identifiers. Each division gets a list for results
        if (showIdentifiersConfig.get("showDOIs")) {
            identifiersList.addLabel(T_doi_label);
            identifiersList.addItemXref(identifiers.get("doi"),identifiers.get("doi"));
        }
        if (showIdentifiersConfig.get("showHandles")) {
            identifiersList.addLabel(T_handle_label);
            identifiersList.addItemXref(identifiers.get("handle"),identifiers.get("handle"));
        }
        //Add navigation controls
        List controls = identifierDivision.addList("submit-review", List.TYPE_FORM);
        addControlButtons(controls);
    }

    private HashMap getIdentifiers(HashMap <String, Boolean> ShowIdentifiersConfig, Item SubmissionItem){
        //Create output object and set defaults
        HashMap Identifiers = new HashMap<String, String>();
        Identifiers.put("handle", "");
        Identifiers.put("doi", "");

        //Get identifiers
        IdentifierService identifierService = IdentifierServiceFactory.getInstance().getIdentifierService();
        if (identifierService != null){
            try {
                // Check config if this identifier should be displayed
                if (ShowIdentifiersConfig.get("showHandles")){
                    // get the identifier
                    String handle = identifierService.lookup(context, SubmissionItem, Handle.class);
                    //if its not empty, get the canonical form and save the result in the output object
                    if (!StringUtils.isEmpty(handle)) {
                        String canonicalHandle = HandleServiceFactory.getInstance().getHandleService().getCanonicalForm(handle);
                        Identifiers.put("handle", canonicalHandle);
                    }
                }
            }catch (Exception ex){
                // nothing to do here
            }
            try {
                // Check config if this identifier should be displayed
                if (ShowIdentifiersConfig.get("showDOIs")){
                    // get the identifier
                    String doi = identifierService.lookup(context, SubmissionItem, DOI.class);
                    if (!StringUtils.isEmpty(doi)) {
                        //if its not empty, get the external form and save the result in the output object
                        String doiUrl = IdentifierServiceFactory.getInstance().getDOIService().DOIToExternalForm(doi);
                        Identifiers.put("doi", doiUrl);
                    }
                }
            }catch (Exception ex){
                // nothing to do here
            }
        }
        return Identifiers;
    }

    private HashMap getShowIdentifiersConfig(){
        // Create output object
        HashMap IdentifiersConfig = new HashMap<String, Boolean>();
        // Read the configuration value
        String showIdentifiers = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("webui.submission.list-identifiers");
        // Set the default
        IdentifiersConfig.put("showDOIs", false);
        IdentifiersConfig.put("showHandles", false);

        // If the no identifier is defined we assume that all should be shown
        if (StringUtils.isEmpty(showIdentifiers)) {
            IdentifiersConfig.put("showDOIs", true);
            IdentifiersConfig.put("showHandles", true);
        } else {
            if (StringUtils.containsIgnoreCase(showIdentifiers, "doi")) {
                IdentifiersConfig.put("showDOIs", true);
            }
            if (StringUtils.containsIgnoreCase(showIdentifiers, "handle")) {
                IdentifiersConfig.put("showHandles", true);
            }
        }
        return IdentifiersConfig;
    }

    public void addPageMeta(PageMeta pageMeta) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException
    {
        super.addPageMeta(pageMeta);
    }

    @Override
    public List addReviewSection(List reviewList) throws WingException, IllegalStateException {
        //Create a new list section for this step (and set its heading)
        List identifiersList = reviewList.addList("submit-review-" + this.stepAndPage, List.TYPE_FORM);
        identifiersList.setHead(T_head);

        Item item = submissionInfo.getSubmissionItem().getItem();
        if (item == null) {
            // this should never happen. Catch and log it to prevent possible NullPointerException.
            //log.warn("ShowIdentifierStep called, but no item supplied.");
            throw new IllegalStateException("ShowIdentifiersStep called, but no item supplied.");
        }
        HashMap<String, Boolean> showIdentifiersConfig = getShowIdentifiersConfig();
        HashMap<String, String> identifiers = getIdentifiers(showIdentifiersConfig, item);

        //Create new divisions for three kinds of identifiers. Each division gets a list for results
        if (showIdentifiersConfig.get("showDOIs")) {
            identifiersList.addLabel(T_doi_label);
            identifiersList.addItemXref(identifiers.get("doi"),identifiers.get("doi"));
        }
        if (showIdentifiersConfig.get("showHandles")) {
            identifiersList.addLabel(T_handle_label);
            identifiersList.addItemXref(identifiers.get("handle"),identifiers.get("handle"));
        }
        return identifiersList;
    }
}
