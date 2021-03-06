package com.mbb.jira.postfunction.jira.workflow;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.impl.UserCFType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.UserField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import webwork.action.ActionContext;

import javax.inject.Inject;
import java.util.*;

/**
 * This is the factory class responsible for dealing with the UI for the post-function.
 * This is typically where you put default values into the velocity context and where you store user input.
 */
@Scanned
public class UserPickerPostFunctionFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory, UserField
{
    public static final String FIELD_MESSAGE = "messageField";

    @JiraImport
    private final ConstantsManager constantsManager;

    @JiraImport
    private final IssueTypeManager issueTypeManager;
    @JiraImport
    private final CustomFieldManager customFieldManager;
    //private WorkflowManager workflowManager;
    ArrayList<CustomField> userPickers;
    CustomField selectedCUPF, selectedCUPT;


    @Inject
    public UserPickerPostFunctionFactory(CustomFieldManager customFieldManager, IssueTypeManager issueTypeManager, ConstantsManager constantsManager) {


        this.issueTypeManager = issueTypeManager;
        this.customFieldManager = customFieldManager;
        this.constantsManager = constantsManager;

    }

    @Override
    protected void getVelocityParamsForInput(Map<String, Object> velocityParams) {
        Map<String, String[]> myParams = ActionContext.getParameters();
        userPickers = new ArrayList<CustomField>();
        //final JiraWorkflow jiraWorkflow = workflowManager.getWorkflow(myParams.get("workflowName")[0]);
        Collection<IssueType> issueTypes = issueTypeManager.getIssueTypes();
        Collection<CustomField> customFields = customFieldManager.getCustomFieldObjects();
        for(CustomField customField : customFields){

            if (customField.getCustomFieldType() instanceof UserCFType) {
                userPickers.add(customField);

            }
        }
        //the default message

        velocityParams.put("customFieldsFrom", Collections.unmodifiableCollection(userPickers));
        velocityParams.put("customFieldsTo", Collections.unmodifiableCollection(userPickers));
    }


    @Override
    protected void getVelocityParamsForEdit(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
        getVelocityParamsForInput(velocityParams);
        String selectedUPFrom = getSelectedCustomIds(descriptor).toArray()[0].toString();
        String selectedUPTo = getSelectedCustomIds(descriptor).toArray()[1].toString();
        selectedCUPF = customFieldManager.getCustomFieldObject(selectedUPFrom);
        selectedCUPT = customFieldManager.getCustomFieldObject(selectedUPTo);
        velocityParams.put("selectedCustomField1", selectedCUPF);
        velocityParams.put("selectedCustomField2", selectedCUPT);
    }


    @Override
    protected void getVelocityParamsForView(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {

        Collection selectedCustomFieldIds = getSelectedCustomIds(descriptor);
        List<CustomField> selectedCustomFields = new ArrayList<>();
        for (Object selectedCustomId : selectedCustomFieldIds) {
            String customFieldId = (String) selectedCustomId;
            CustomField customField = customFieldManager.getCustomFieldObject(customFieldId);
            if (customField != null) {
                selectedCustomFields.add(customField);
            }
        }


        velocityParams.put("customFields", Collections.unmodifiableCollection(selectedCustomFields));

    }


    public Map<String, Object> getDescriptorParams(Map<String, Object> formParams) {

        Collection customids = formParams.keySet();
        StringBuilder custIds = new StringBuilder();
        String[] upToId = (String[]) formParams.get("userPickerTo");
        String[] upFromId = (String[]) formParams.get("userPickerFrom");


        custIds.append(upFromId[0]).append(",").append(upToId[0]);


        return MapBuilder.build("customFields", custIds.substring(0, custIds.length()));
    }

    private Collection getSelectedCustomIds(AbstractDescriptor descriptor) {
        Collection<String> selectedCustomIds = new ArrayList<>();
        if (!(descriptor instanceof FunctionDescriptor)) {
            throw new IllegalArgumentException("Descriptor must be a ConditionDescriptor.");
        }

        FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;

        String customF = (String) functionDescriptor.getArgs().get("customFields");
        StringTokenizer st = new StringTokenizer(customF, ",");

        while (st.hasMoreTokens()) {
            selectedCustomIds.add(st.nextToken());
        }
        return selectedCustomIds;
    }

}