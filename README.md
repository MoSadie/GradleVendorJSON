# Gradle Vendor JSON
## What is this?
This project is a gradle plugin designed to help generate and keep up to date [vendor JSON files](https://docs.wpilib.org/en/stable/docs/software/vscode-overview/3rd-party-libraries.html#how-does-it-work-java-c).

It'll read the existing json file, then update the file with any values set in the vendorJSON block in the build.gradle file.

## How do I use this?
First, you need to apply the plugin by adding this to the beginning of your build.gradle file:
```groovy
plugins {
  // other plugins here.
  id "io.github.mosadie.vendorJSON" version "1.1"
}
```

Then you configure the `vendorJSON` task in your build.gradle file.
You only have to set the values you want to change, with the exception of the file name.
Example for a complete vendorJSON block that updates the version number and file name:
```groovy
vendorJSON {
    fileName = "robotLib.json"
    version = LibraryVersion
    addJavaArtifact("com.github.ORF-4450", archivesBaseName, LibraryVersion)
}
```

Here is another example with all possible values:
```groovy
vendorJSON {
    name = "Project Name"
    version = "Version"
    uuid = "uuid"
    addMavenUrl("http://example.com/maven/")
    jsonUrl = "http://example.com/exampleJson.json"
    frcYear = 2024
    fileName = "FileName.json"
    addJavaArtifact("the.group.id", "artifactId", "version")
    // need more Java Artifacts? Call addJavaArtifact again!
    addJniArtifact("the.group.id", "artifactId", "version", false, stringArray("examplePlatform"), true)
    // need more JNI Artifacts? Call addJniArtifact again!
    addCppArtifact("the.group.id", "artifactId", "version", "libName", "configuration", "headerClassifier", "sourcesClassifier", stringArray("examplePlatform"), true, false)
    // need more C++ Artifacts? Call addCppArtifact again!
}
```

Finally, just run the `vendorJSON` task from the command line like this: `./gradlew.bat vendorJSON` (Windows) or `gradlew vendorJSON` (Other platforms)
