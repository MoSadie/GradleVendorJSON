package io.github.mosadie.gradlevendorjson;

import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import com.google.gson.Gson;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

class VendorJSONTask extends DefaultTask {
    String name;
    String version;
    String uuid;
    ArrayList<String> mavenUrls;
    String jsonUrl;
    String fileName;
    ArrayList<GVJSONJavaArtifact> javaDependencies;
    ArrayList<GVJSONJNIArtifact> jniDependencies;
    ArrayList<GVJSONCppArtifact> cppDependencies;
    
    Gson gson;
    
    public VendorJSONTask() {
        setDefaults();
        createGson();
    }

    public void addMavenUrl(String url) {
        mavenUrls.add(url);
    }
    
    public void addJavaArtifact(String groupId, String artifactId, String version) {
        GVJSONJavaArtifact javaArt = new GVJSONJavaArtifact();
        javaArt.groupId = groupId;
        javaArt.artifactId = artifactId;
        javaArt.version = version;
        javaDependencies.add(javaArt);
    }
    
    public void addJniArtifact(String groupId, String artifactId, String version, boolean isJar, String[] validPlatforms, boolean skipInvalidPlatforms) {
        GVJSONJNIArtifact jniArt = new GVJSONJNIArtifact();
        jniArt.groupId = groupId;
        jniArt.artifactId = artifactId;
        jniArt.version = version;
        jniArt.isJar = isJar;
        jniArt.validPlatforms = validPlatforms;
        jniArt.skipInvalidPlatforms = skipInvalidPlatforms;
        jniDependencies.add(jniArt);
    }
    
    public void addCppArtifact(String groupId, String artifactId, String version, String libName, String configuration, String headerClassifier, String sourcesClassifier, String[] binaryPlatforms, boolean skipInvalidPlatforms, boolean sharedLibrary) {
        GVJSONCppArtifact cppArt = new GVJSONCppArtifact();
        
        cppArt.groupId = groupId;
        cppArt.artifactId = artifactId;
        cppArt.version = version;
        cppArt.libName = libName;
        cppArt.configuration = configuration;
        
        cppArt.headerClassifier = headerClassifier;
        cppArt.sourcesClassifier = sourcesClassifier;
        cppArt.binaryPlatforms = binaryPlatforms;
        cppArt.skipInvalidPlatforms = skipInvalidPlatforms;
        
        cppArt.sharedLibrary = sharedLibrary;

        cppDependencies.add(cppArt);
    }
    
    public void setDefaults() {
        name = "";
        version = "";
        uuid = "";
        mavenUrls = new ArrayList<String>();
        jsonUrl = "";
        fileName = getProject().getName() + ".json";
        javaDependencies = new ArrayList<GVJSONJavaArtifact>();
        jniDependencies = new ArrayList<GVJSONJNIArtifact>();
        cppDependencies = new ArrayList<GVJSONCppArtifact>();
        
    }
    
    public void createGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gson = gsonBuilder.create();
    }
    
    @TaskAction
    public void taskAction() {
        if (!getProject().file(fileName).exists()) {
            createDefaultJsonFile(getProject().file(fileName));
        }
        GVJSON gvjson = null;
        try {
            try(FileReader reader = new FileReader(getProject().file(fileName))) {
                gvjson = gson.fromJson(reader, GVJSON.class);
            }
        } catch (FileNotFoundException e) {
            //TODO Do something?
            return;
        } catch (IOException e) {
            //TODO Do something?
            return;
        }
        
        if (gvjson == null) {
            //TODO Do something?
            return;
        }
        
        if (name != "") {
            gvjson.name = name;
        }
        
        if (version != "") {
            gvjson.version = version;
        }
        
        if (uuid != "") {
            gvjson.uuid = uuid;
        }
        
        if (mavenUrls.size() != 0) {
            gvjson.mavenUrls = mavenUrls.toArray(gvjson.mavenUrls);
        }
        
        if (jsonUrl != "") {
            gvjson.jsonUrl = jsonUrl;
        }
        
        if (fileName != getProject().getName()+".json") {
            gvjson.fileName = fileName;
        }
        
        if (javaDependencies.size() != 0) {
            gvjson.javaDependencies = javaDependencies.toArray(gvjson.javaDependencies);
        }
        
        if (jniDependencies.size() != 0) {
            gvjson.jniDependencies = jniDependencies.toArray(gvjson.jniDependencies);
        }
        
        if (cppDependencies.size() != 0) {
            gvjson.cppDependencies = cppDependencies.toArray(gvjson.cppDependencies);
        }
        
        try{
            getProject().file(fileName).delete();
            getProject().file(fileName).createNewFile();
            try(FileWriter writer = new FileWriter(getProject().file(fileName))) {
                writer.write(gson.toJson(gvjson));
            }
        } catch (IOException e) {
            //TODO Do something?
        }
    }
    
    private void createDefaultJsonFile(File file) {
        GVJSON gvjson = new GVJSON();
        gvjson.name = getProject().getName();
        gvjson.version = "v0.0.0";
        gvjson.uuid = UUID.randomUUID().toString();
        gvjson.mavenUrls = new String[] { "http://example.com" };
        gvjson.jsonUrl = "http://example.com/" + file.getName();
        gvjson.fileName = file.getName();
        
        GVJSONJavaArtifact defaultJavaArt = new GVJSONJavaArtifact();
        defaultJavaArt.groupId = "com.example";
        defaultJavaArt.artifactId = "exampleJavaLibrary";
        defaultJavaArt.version = "v0.0.0";
        
        gvjson.javaDependencies = new GVJSONJavaArtifact[] { defaultJavaArt };
        
        GVJSONJNIArtifact defaultJniArt = new GVJSONJNIArtifact();
        
        defaultJniArt.groupId = "com.example";
        defaultJniArt.artifactId = "exampleJNILibrary";
        defaultJniArt.version = "v0.0.0";
        defaultJniArt.isJar = false;
        defaultJniArt.validPlatforms = new String[] { "examplePlatform" };
        defaultJniArt.skipInvalidPlatforms = true;
        
        gvjson.jniDependencies = new GVJSONJNIArtifact[] { defaultJniArt };
        
        GVJSONCppArtifact defaultCppArtifact = new GVJSONCppArtifact();
        
        defaultCppArtifact.groupId = "com.example";
        defaultCppArtifact.artifactId = "exampleCppLibrary";
        defaultCppArtifact.version = "v0.0.0";
        defaultCppArtifact.libName = "Example Cpp Library";
        defaultCppArtifact.configuration = "exampleConfig";
        defaultCppArtifact.headerClassifier = "headers";
        defaultCppArtifact.sourcesClassifier = "sources";
        defaultCppArtifact.skipInvalidPlatforms = true;
        defaultCppArtifact.binaryPlatforms = new String[] { "examplePlatform" };
        defaultCppArtifact.sharedLibrary = false;
        
        gvjson.cppDependencies = new GVJSONCppArtifact[] { defaultCppArtifact };
        
        try {
            if (file.exists()) {
                file.delete();
                file.createNewFile();
            }
            
            try(FileWriter writer = new FileWriter(file)) {
                writer.write(gson.toJson(gvjson));
            }
        } catch (IOException e) {
            //TODO Do something?
        }
    }

    public static String[] stringArray(String... a) {
        return a;
    }
}

class GVJSON {
    String name;
    String version;
    String uuid;
    String[] mavenUrls;
    String jsonUrl;
    String fileName;
    GVJSONJavaArtifact[] javaDependencies;
    GVJSONJNIArtifact[] jniDependencies;
    GVJSONCppArtifact[] cppDependencies;
}

class GVJSONJavaArtifact {
    String groupId;
    String artifactId;
    String version;
}

class GVJSONJNIArtifact {
    String groupId;
    String artifactId;
    String version;
    
    boolean isJar;
    
    String[] validPlatforms;
    boolean skipInvalidPlatforms;
}

class GVJSONCppArtifact {
    String groupId;
    String artifactId;
    String version;
    String libName;
    String configuration;
    
    String headerClassifier;
    String sourcesClassifier;
    String[] binaryPlatforms;
    boolean skipInvalidPlatforms;
    
    boolean sharedLibrary;
}