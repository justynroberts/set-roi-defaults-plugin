package com.plugin.setroidefaults

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import okhttp3.*
import java.util.concurrent.TimeUnit

class RoiManager {
    private String baseUrl
    private String apiToken
    private String apiVersion
    private String roiPluginName
    private OkHttpClient client
    private Headers headers
    private JsonSlurper jsonSlurper = new JsonSlurper()

    RoiManager(String rundeckUrl, String apiToken, String apiVersion = "46") {
        this.baseUrl = rundeckUrl?.endsWith('/') ? rundeckUrl[0..-2] : rundeckUrl
        this.apiToken = apiToken
        this.apiVersion = apiVersion
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        this.headers = new Headers.Builder()
            .add('X-Rundeck-Auth-Token', apiToken)
            .add('Content-Type', 'application/json')
            .add('Accept', 'application/json')
            .build()
    }

    List<Map> getProjects() {
        String url = "${baseUrl}/api/${apiVersion}/projects"
        try {
            Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .build()
            
            Response response = client.newCall(request).execute()
            if (response.successful) {
                return jsonSlurper.parseText(response.body().string()) as List<Map>
            }
            return []
        } catch (Exception e) {
            throw new RuntimeException("Error fetching projects: ${e.message}", e)
        }
    }

    List<Map> getProjectJobs(String projectName) {
        String url = "${baseUrl}/api/${apiVersion}/project/${projectName}/jobs"
        try {
            Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .build()
            
            Response response = client.newCall(request).execute()
            if (response.successful) {
                return jsonSlurper.parseText(response.body().string()) as List<Map>
            }
            return []
        } catch (Exception e) {
            throw new RuntimeException("Error fetching jobs for project ${projectName}: ${e.message}", e)
        }
    }

    Map getJobDefinition(String jobId) {
        String url = "${baseUrl}/api/${apiVersion}/job/${jobId}"
        try {
            Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .build()
            
            Response response = client.newCall(request).execute()
            if (response.successful) {
                def jobDef = jsonSlurper.parseText(response.body().string())
                return (jobDef instanceof List && jobDef.size() > 0) ? jobDef[0] as Map : jobDef as Map
            }
            return null
        } catch (Exception e) {
            throw new RuntimeException("Error fetching job definition for ${jobId}: ${e.message}", e)
        }
    }

    String detectRoiPluginName(Map jobDef) {
        if (!jobDef || !jobDef.plugins) return null
        
        Map plugins = jobDef.plugins as Map
        Map executionPlugins = plugins.ExecutionLifecycle as Map
        
        if (!executionPlugins) return null
        
        for (String pluginName : executionPlugins.keySet()) {
            if (pluginName.toLowerCase().contains('roi') && pluginName.toLowerCase().contains('metric')) {
                return pluginName
            }
        }
        return null
    }

    boolean ensureRoiPluginName() {
        if (roiPluginName) return true
        
        List<Map> projects = getProjects()
        if (!projects) return false
        
        for (Map project : projects.take(3)) {
            String projectName = project.name
            if (!projectName) continue
            
            List<Map> jobs = getProjectJobs(projectName)
            if (!jobs) continue
            
            for (Map job : jobs.take(5)) {
                String jobId = job.id
                if (!jobId) continue
                
                Map jobDef = getJobDefinition(jobId)
                if (jobDef) {
                    String detected = detectRoiPluginName(jobDef)
                    if (detected) {
                        roiPluginName = detected
                        return true
                    }
                }
            }
        }
        return false
    }

    Map<String, Object> hasHoursField(Map jobDef) {
        try {
            if (!jobDef?.plugins?.ExecutionLifecycle) {
                return [hasField: false, value: null]
            }
            
            Map executionPlugins = jobDef.plugins.ExecutionLifecycle as Map
            
            for (entry in executionPlugins) {
                String pluginName = entry.key
                Map pluginConfig = entry.value as Map
                
                if (!pluginConfig) continue
                
                if ((roiPluginName && pluginName == roiPluginName) || 
                    (pluginName.toLowerCase().contains('roi') && pluginName.toLowerCase().contains('metric'))) {
                    
                    String userRoiData = pluginConfig.userRoiData
                    if (userRoiData) {
                        try {
                            List fields = jsonSlurper.parseText(userRoiData) as List
                            for (field in fields) {
                                if (field instanceof Map) {
                                    String fieldKey = field.key?.toString()?.toLowerCase()
                                    if (fieldKey?.contains('hours')) {
                                        return [hasField: true, value: field.value ?: 'unknown']
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // Continue checking other plugins
                        }
                    }
                }
            }
            return [hasField: false, value: null]
        } catch (Exception e) {
            return [hasField: false, value: null]
        }
    }

    boolean addRoiMetricsPlugin(Map jobDef, String defaultHours, String jobContext = "") {
        try {
            if (!jobDef.plugins) {
                jobDef.plugins = [:]
            }
            if (!jobDef.plugins.ExecutionLifecycle) {
                jobDef.plugins.ExecutionLifecycle = [:]
            }
            
            String pluginName = roiPluginName ?: "roi-metrics-data"
            
            List roiData = [
                [
                    key: 'hours',
                    label: 'Hours Saved By automation',
                    desc: 'Number of hours saved by this automation',
                    value: defaultHours
                ]
            ]
            
            jobDef.plugins.ExecutionLifecycle[pluginName] = [
                userRoiData: JsonOutput.toJson(roiData)
            ]
            
            return true
        } catch (Exception e) {
            throw new RuntimeException("Error adding ROI metrics plugin${jobContext ? ' to ' + jobContext : ''}: ${e.message}", e)
        }
    }

    boolean updateExistingRoiPlugin(Map jobDef, String defaultHours, String jobContext = "") {
        try {
            Map executionPlugins = jobDef?.plugins?.ExecutionLifecycle as Map
            if (!executionPlugins) return false
            
            for (entry in executionPlugins) {
                String pluginName = entry.key
                Map pluginConfig = entry.value as Map
                
                if (!pluginConfig) continue
                
                if ((roiPluginName && pluginName == roiPluginName) || 
                    (pluginName.toLowerCase().contains('roi') && pluginName.toLowerCase().contains('metric'))) {
                    
                    String userRoiData = pluginConfig.userRoiData ?: '[]'
                    List roiFields
                    try {
                        roiFields = jsonSlurper.parseText(userRoiData) as List
                    } catch (Exception e) {
                        roiFields = []
                    }
                    
                    // Check if hours field already exists
                    for (field in roiFields) {
                        if (field instanceof Map) {
                            String fieldKey = field.key?.toString()?.toLowerCase()
                            if (fieldKey?.contains('hours')) {
                                return false // Field already exists
                            }
                        }
                    }
                    
                    // Add hours field
                    roiFields.add([
                        key: 'hours',
                        label: 'Hours Saved By automation',
                        desc: 'Number of hours saved by this automation',
                        value: defaultHours
                    ])
                    
                    pluginConfig.userRoiData = JsonOutput.toJson(roiFields)
                    return true
                }
            }
            return false
        } catch (Exception e) {
            throw new RuntimeException("Error updating existing ROI plugin${jobContext ? ' for ' + jobContext : ''}: ${e.message}", e)
        }
    }

    boolean updateJob(String projectName, String jobId, Map jobData) {
        try {
            // Clean up job data for update
            Map updateData = new HashMap(jobData)
            ['id', 'href', 'permalink', 'averageDuration', 'project'].each { field ->
                updateData.remove(field)
            }
            
            // Validate required fields
            if (!updateData.name) {
                throw new RuntimeException("Job data missing required 'name' field")
            }
            
            return updateJobViaImport(projectName, jobId, updateData)
        } catch (Exception e) {
            throw new RuntimeException("Error updating job ${jobId}: ${e.message}", e)
        }
    }

    private boolean updateJobViaImport(String projectName, String jobId, Map jobData) {
        String url = "${baseUrl}/api/${apiVersion}/project/${projectName}/jobs/import?dupeOption=update&format=json"
        
        try {
            String jsonPayload = JsonOutput.toJson([jobData])
            
            RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), 
                jsonPayload
            )
            
            Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .post(body)
                .build()
            
            Response response = client.newCall(request).execute()
            String responseBody = response.body()?.string()
            
            if (response.code() in [200, 201, 204]) {
                return true
            } else {
                throw new RuntimeException("HTTP ${response.code()}: ${responseBody}")
            }
        } catch (Exception e) {
            throw new RuntimeException("Error importing job ${jobId}: ${e.message}", e)
        }
    }
}