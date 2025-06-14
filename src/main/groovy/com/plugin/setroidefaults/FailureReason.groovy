package com.plugin.setroidefaults;

import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason

/**
 * This enum lists the known reasons this plugin might fail.
 *
 * There should be a FailureReason enum that implements the FailureReason interface.
 * There should regularly be failure reasons for Authentication errors, Key Storage errors, etc.
 * Use these to represent reasons your plugin may fail to execute.
 */
enum PluginFailureReason implements FailureReason {
    KeyStorageError,
    ResourceInfoError
}