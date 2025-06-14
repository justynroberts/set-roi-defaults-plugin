package com.plugin.setroidefaults;

import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * If other functions are required for purposes of modularity or clarity, they should either be added to a Util Class
 * (if generic enough), or a PluginHelper Class that is accessible to the Plugin Class.
 */
class ExampleApis {
    String userRundeckBaseApiUrl
    String userRundeckApiVersion
    Headers headers
    OkHttpClient client

    ExampleApis(String userBaseApiUrl, String userApiVersion, String userAuthToken) {
        this.client = new OkHttpClient()
        this.headers = new Headers.Builder()
            .add('Accept', 'application/json')
            .add('Content-Type', 'application/json')
            .add('X-Rundeck-Auth-Token', userAuthToken)
            .build()

        if (!userBaseApiUrl) {
            userRundeckBaseApiUrl = Constants.BASE_API_URL
        } else {
            userRundeckBaseApiUrl = userBaseApiUrl
        }

        if (!userApiVersion) {
            userRundeckApiVersion = Constants.API_VERSION
        } else {
            userRundeckApiVersion = userApiVersion
        }
    }

    /**
     * Requests info on a single node by name, from a given Rundeck project by name.
     * https://docs.rundeck.com/docs/api/rundeck-api.html#getting-resource-info
     */
    String getResourceInfoByName(
        String projectName,
        String nodeName
    ) throws IOException {
        String resourceUrl = "/project/" + projectName + "/resource/" + nodeName
        String fullUrl = createFullUrl(userRundeckBaseApiUrl, userRundeckApiVersion, resourceUrl)

        Request request = new Request.Builder()
            .url(fullUrl)
            .headers(headers)
            .build()

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    /**
     * Requests info on a Rundeck project by name.
     * https://docs.rundeck.com/docs/api/rundeck-api.html#getting-project-info
     */
    String getProjectInfoByName(
        String projectName
    ) throws IOException {
        String resourceUrl = "/project/" + projectName
        String fullUrl = createFullUrl(userRundeckBaseApiUrl, userRundeckApiVersion, resourceUrl)

        Request request = new Request.Builder()
            .url(fullUrl)
            .headers(headers)
            .build()

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    private static String createFullUrl(String baseApiUrl, String apiVersion, String apiPath) {

        // Handle for user trailing forward slash
        if(baseApiUrl.endsWith("/")) {
            baseApiUrl = baseApiUrl.substring(0, baseApiUrl.length() - 1)
        }
        return baseApiUrl + "/" + apiVersion + "/" + apiPath
    }
}