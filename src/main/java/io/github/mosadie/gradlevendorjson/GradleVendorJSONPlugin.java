package io.github.mosadie.gradlevendorjson;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GradleVendorJSONPlugin implements Plugin<Project> {
    public void apply(Project project) {
        project.getTasks().create("vendorJSON", VendorJSONTask.class);
    }
}