package io.github.mosadie.gradlevendorjson;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * The Gradle plugin's main class.
 */
public class GradleVendorJSONPlugin implements Plugin<Project> {
    /**
     * Applies the plugin to the project, creating the task 'vendorJSON'.
     */
    public void apply(Project project) {
        project.getTasks().create("vendorJSON", VendorJSONTask.class);
    }
}