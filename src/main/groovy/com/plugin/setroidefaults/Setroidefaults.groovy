package com.plugin.setroidefaults;

import com.dtolabs.rundeck.core.plugins.Plugin
import com.dtolabs.rundeck.plugins.step.StepPlugin
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException
import com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants
import com.dtolabs.rundeck.plugins.ServiceNameConstants
import com.dtolabs.rundeck.plugins.step.PluginStepContext
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty
import com.dtolabs.rundeck.plugins.descriptions.RenderingOption
import com.dtolabs.rundeck.plugins.descriptions.RenderingOptions
import com.dtolabs.rundeck.core.execution.ExecutionListener
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.rundeck.storage.api.StorageException
import static com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants.GROUPING
import static com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants.GROUP_NAME

@Plugin(name = PLUGIN_NAME, service = ServiceNameConstants.WorkflowStep)
@PluginDescription(title = PLUGIN_TITLE, description = PLUGIN_DESCRIPTION)
class Setroidefaults implements StepPlugin {
    public static final String PLUGIN_NAME = "set-roi-defaults"
    public static final String PLUGIN_TITLE = "Set ROI Defaults"
    public static final String PLUGIN_DESCRIPTION = "Automatically sets default ROI metrics for Rundeck jobs by adding hours saved fields to existing jobs."

    Map<String, String> meta = Collections.singletonMap("content-data-type", "application/json")
    RoiManager roiManager

    @PluginProperty(
        title = "Rundeck URL",
        description = "Base URL of your Rundeck instance (e.g., 'https://rundeck.company.com'). If left blank, will use the current Rundeck instance.",
        defaultValue = "",
        required = false
    )
    @RenderingOptions([
        @RenderingOption(key = GROUP_NAME, value = "Rundeck Configuration")
    ])
    String rundeckUrl

    @PluginProperty(
        title = "API Version",
        description = "Rundeck API version to use. If left blank, will use version 46.",
        defaultValue = "46",
        required = false
    )
    @RenderingOption(key = GROUP_NAME, value = "Rundeck Configuration")
    String apiVersion

    @PluginProperty(
        title = "API Token Path",
        description = "REQUIRED: The path to the Key Storage entry for your Rundeck API Token.",
        required = true
    )
    @RenderingOptions([
        @RenderingOption(
            key = StringRenderingConstants.SELECTION_ACCESSOR_KEY,
            value = "STORAGE_PATH"
        ),
        @RenderingOption(
            key = StringRenderingConstants.STORAGE_PATH_ROOT_KEY,
            value = "keys"
        ),
        @RenderingOption(
            key = StringRenderingConstants.STORAGE_FILE_META_FILTER_KEY,
            value = "Rundeck-data-type=password"
        ),
        @RenderingOption(
            key = StringRenderingConstants.GROUP_NAME,
            value = "Rundeck Configuration"
        )
    ])
    String apiTokenPath

    @PluginProperty(
        title = "Default Hours Saved",
        description = "Default value for hours saved field (in decimal hours). Default is 0.1667 (10 minutes).",
        defaultValue = "0.1667",
        required = false
    )
    @RenderingOption(key = GROUP_NAME, value = "ROI Configuration")
    String defaultHoursSaved

    @PluginProperty(
        title = "Project Filter",
        description = "Process only this specific project. Leave blank to process current project only. Use 'all' to process all projects.",
        defaultValue = "",
        required = false
    )
    @RenderingOption(key = GROUP_NAME, value = "ROI Configuration")
    String projectFilter

    @PluginProperty(
        title = "Dry Run Mode",
        description = "If enabled, only shows what would be changed without making actual updates.",
        defaultValue = "true",
        required = false
    )
    @RenderingOptions([
        @RenderingOption(key = "selectionAccessor", value = "STATIC_VALUES"),
        @RenderingOption(key = "staticValues", value = "true,false"),
        @RenderingOption(key = GROUP_NAME, value = "Execution Options")
    ])
    String dryRunMode

    @Override
    void executeStep(final PluginStepContext context,
                                final Map<String, Object> configuration) {

        ExecutionListener logger = context.getExecutionContext().getExecutionListener()
        
        logger.log(2, "Starting ROI Defaults Plugin execution")
        
        // Get configuration values
        String apiToken
        try {
            apiToken = Util.getPasswordFromKeyStorage(apiTokenPath, context)
        } catch (StorageException e) {
            throw new StepException(
                "Error accessing API token at ${apiTokenPath}: ${e.getMessage()}",
                PluginFailureReason.KeyStorageError
            )
        }

        String effectiveRundeckUrl = rundeckUrl ?: determineCurrentRundeckUrl(context)
        String effectiveApiVersion = apiVersion ?: "46"
        String effectiveDefaultHours = defaultHoursSaved ?: "0.1667"
        boolean isDryRun = dryRunMode?.toLowerCase() == "true"
        String currentProject = context.getFrameworkProject()
        String effectiveProjectFilter = projectFilter ?: currentProject

        logger.log(2, "Configuration:")
        logger.log(2, "  Rundeck URL: ${effectiveRundeckUrl}")
        logger.log(2, "  API Version: ${effectiveApiVersion}")
        logger.log(2, "  Default Hours: ${effectiveDefaultHours}")
        logger.log(2, "  Dry Run: ${isDryRun}")
        logger.log(2, "  Project Filter: ${effectiveProjectFilter}")
        logger.log(2, "  Current Project: ${currentProject}")

        try {
            if (!roiManager) {
                roiManager = new RoiManager(effectiveRundeckUrl, apiToken, effectiveApiVersion)
            }

            // Auto-detect ROI plugin name
            if (!roiManager.ensureRoiPluginName()) {
                logger.log(1, "WARNING: Could not detect ROI plugin name. Will use default 'roi-metrics-data'")
            }

            // Get projects to process
            List<Map> projects = roiManager.getProjects()
            if (!projects) {
                throw new StepException("No projects found or error occurred", PluginFailureReason.ResourceInfoError)
            }

            logger.log(2, "Found ${projects.size()} projects")

            int totalJobsProcessed = 0
            int totalJobsUpdated = 0

            for (Map project : projects) {
                String projectName = project.name
                
                // Apply project filter - support both exact match and current project context
                if (effectiveProjectFilter != "all" && 
                    projectName != effectiveProjectFilter && 
                    !projectName.contains(effectiveProjectFilter)) {
                    logger.log(3, "Skipping project: ${projectName} (doesn't match filter: ${effectiveProjectFilter})")
                    continue
                }

                logger.log(2, "Processing project: ${projectName}")

                List<Map> jobs = roiManager.getProjectJobs(projectName)
                if (!jobs) {
                    logger.log(2, "[${projectName}] No jobs found")
                    continue
                }

                for (Map job : jobs) {
                    totalJobsProcessed++
                    String jobId = job.id
                    String jobName = job.name ?: 'Unknown'
                    String jobContext = "[${projectName}] ${jobName}"

                    try {
                        Map jobDef = roiManager.getJobDefinition(jobId)
                        if (!jobDef) {
                            logger.log(1, "❌ ${jobContext} - Failed to retrieve definition")
                            continue
                        }

                        Map hoursFieldInfo = roiManager.hasHoursField(jobDef)
                        if (hoursFieldInfo.hasField) {
                            String currentValue = hoursFieldInfo.value ?: "unknown"
                            logger.log(2, "✅ ${jobContext} - Already has hours (${currentValue})")
                            continue
                        }

                        if (isDryRun) {
                            logger.log(2, "🆕 ${jobContext} - WOULD ADD hours [${effectiveDefaultHours}]")
                            totalJobsUpdated++
                        } else {
                            if (processJobRoiMetrics(jobDef, projectName, jobId, jobContext, effectiveDefaultHours, logger)) {
                                logger.log(2, "🆕 ${jobContext} - Added hours field")
                                totalJobsUpdated++
                            } else {
                                logger.log(1, "❌ ${jobContext} - Failed to add hours field")
                            }
                        }
                    } catch (Exception e) {
                        logger.log(0, "❌ ${jobContext} - Error: ${e.message}")
                    }
                }
            }

            // Output summary
            logger.log(2, "")
            logger.log(2, "Summary:")
            logger.log(2, "  Projects: ${projects.size()}")
            logger.log(2, "  Jobs Processed: ${totalJobsProcessed}")
            logger.log(2, "  Jobs Updated: ${totalJobsUpdated}")
            
            if (isDryRun) {
                logger.log(1, "⚠️  DRY RUN MODE: No actual changes were made!")
                logger.log(1, "⚠️  To apply changes: Set 'Dry Run Mode' to false and run again")
            } else {
                logger.log(2, "✅ Execution complete - changes have been applied to jobs")
            }

            // Output context for downstream steps
            context.getExecutionContext().getOutputContext().addOutput("summary", "projectsCount", projects.size().toString())
            context.getExecutionContext().getOutputContext().addOutput("summary", "jobsProcessed", totalJobsProcessed.toString())
            context.getExecutionContext().getOutputContext().addOutput("summary", "jobsUpdated", totalJobsUpdated.toString())
            context.getExecutionContext().getOutputContext().addOutput("summary", "dryRun", isDryRun.toString())

        } catch (Exception e) {
            logger.log(0, "Error during ROI processing: ${e.message}")
            throw new StepException("ROI processing failed: ${e.message}", PluginFailureReason.ResourceInfoError)
        }
    }

    private boolean processJobRoiMetrics(Map jobDef, String projectName, String jobId, String jobContext, String defaultHours, ExecutionListener logger) {
        try {
            logger.log(3, "Processing job ${jobContext} - checking for existing ROI plugin")
            
            // Try to update existing ROI plugin first
            boolean updatedExisting = roiManager.updateExistingRoiPlugin(jobDef, defaultHours, jobContext)
            if (!updatedExisting) {
                logger.log(3, "No existing ROI plugin found, adding new one")
                // If no existing plugin, add new one
                if (!roiManager.addRoiMetricsPlugin(jobDef, defaultHours, jobContext)) {
                    logger.log(0, "Failed to add ROI metrics plugin to ${jobContext}")
                    return false
                }
                logger.log(3, "Added new ROI plugin to ${jobContext}")
            } else {
                logger.log(3, "Updated existing ROI plugin for ${jobContext}")
            }

            // Update the job
            logger.log(3, "Updating job definition for ${jobContext}")
            boolean success = roiManager.updateJob(projectName, jobId, jobDef)
            if (success) {
                logger.log(3, "Successfully updated job ${jobContext}")
            } else {
                logger.log(0, "Failed to update job ${jobContext}")
            }
            return success
        } catch (Exception e) {
            logger.log(0, "Error processing job ${jobContext}: ${e.message}")
            logger.log(3, "Stack trace: ${e.toString()}")
            return false
        }
    }

    private String determineCurrentRundeckUrl(PluginStepContext context) {
        // Try to determine current Rundeck URL from context
        // This is a fallback - in practice, users should provide the URL
        return "http://localhost:4440"
    }
}