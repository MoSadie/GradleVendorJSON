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

/**
 * The task that generates/updates a vendor json file.
 */
public class VendorJSONTask extends DefaultTask {
    /** The name of the library. */
    String name;
    /** The version string of the library. */
    String version;
    /** The UUID of the library. */
    String uuid;
    /** The FRC year this library is for */
    String frcYear;
    /** An array of maven repository urls to add to the robot project. */
    ArrayList<String> mavenUrls;
    /** The URL to check for updated versions of this file at. */
    String jsonUrl;
    /** The name of this file. */
    String fileName;
    /** The Java dependencies. */
    ArrayList<GVJSONJavaArtifact> javaDependencies;
    /** The JNI dependencies. */
    ArrayList<GVJSONJNIArtifact> jniDependencies;
    /** The C++ dependencies. */
    ArrayList<GVJSONCppArtifact> cppDependencies;
    
    /** The Gson object to seralize and deseralize json. */
    private Gson gson;
    
    /** Set the default values for the fields and create the Gson object. */
    public VendorJSONTask() {
        setDefaults();
        createGson();
    }

    /** 
     * Add/updates a maven repository url in the vendor json file.
     * @param url The maven repository's url.
     */
    public void addMavenUrl(String url) {
        mavenUrls.add(url);
    }
    
    /**
     * Add/updates a Java artifact in the vendor json file.
     * @param groupId The groupId of the artifact. (Ex. com.github.ORF-4450)
     * @param artifactId The artifactId of the artifact. (Ex. RobotLib)
     * @param version The version string of the artifact. (Ex. 3.0)
     */
    public void addJavaArtifact(String groupId, String artifactId, String version) {
        GVJSONJavaArtifact javaArt = new GVJSONJavaArtifact();
        javaArt.groupId = groupId;
        javaArt.artifactId = artifactId;
        javaArt.version = version;
        javaDependencies.add(javaArt);
    }
    
    /**
     * Adds/updates a JNI (Java Native Interface) artifact in the vendor json file.
     * @param groupId The groupId of the artifact. (Ex. com.github.ORF-4450)
     * @param artifactId The artifactId of the artifact. (Ex. RobotLib-JNI)
     * @param version The version string of the artifact. (Ex. 3.0)
     * @param isJar Indicates if the file(s) to be downloaded is/are jar file(s).
     * @param validPlatforms Array of valid platforms for the JNI files.
     * @param skipInvalidPlatforms Indicates if including the JNI artfact should be skipped when buliding for an invalid platform.
     */
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
    
    //TODO: I'm not a C++ guy and have no clue what some of these things mean.

    /**
     * Adds/updates a C++ artifact in the vendor json file.
     * @param groupId The groupId of the artifact. (Ex. com.github.ORF-4450)
     * @param artifactId The artifactId of the artifact. (Ex. RobotLib-Cpp)
     * @param version The version string of the artifact. (Ex. 3.0)
     * @param libName The name of the library.
     * @param configuration Honestly, no idea. Can be safely deleted from the vendor json file after initial generation.
     * @param headerClassifier Points at a headers file? Not sure.
     * @param sourcesClassifier Points at a sources file? Not sure.
     * @param binaryPlatforms Array of valid platforms to include the C++ artifact for.
     * @param skipInvalidPlatforms Indicates if the C++ artifact should be skipped when attempting to build for an invalid platform.
     * @param sharedLibrary Indicates if the library is a shared library or not.
     */
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
    
    /** 
     * Set the default values for the fields.
    */
    void setDefaults() {
        name = "";
        version = "";
        uuid = "";
        frcYear = "";
        mavenUrls = new ArrayList<String>();
        jsonUrl = "";
        fileName = getProject().getName() + ".json";
        javaDependencies = new ArrayList<GVJSONJavaArtifact>();
        jniDependencies = new ArrayList<GVJSONJNIArtifact>();
        cppDependencies = new ArrayList<GVJSONCppArtifact>();
        
    }
    
    /**
     * Create the Gson object to handle parsing and creating json strings.
     */
    void createGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gson = gsonBuilder.create();
    }
    
    /**
     * Attampt to save all changes to the vendor json file to the actual file on the computer.
     */
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
            getProject().getLogger().error("An error occured while trying to access the file " + fileName + ". The file was not found. Aborting updating JSON file.");
            return;
        } catch (Exception e) {
            getProject().getLogger().error("An error occured while trying to access the file " + fileName + ". Error: "+ e.getMessage() +". Aborting updating JSON file.");
            return;
        }
        
        if (gvjson == null) {
            getProject().getLogger().error("An error occured while trying to create the data object. Aborting updating JSON file.");
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
            getProject().getLogger().error("Something went wrong creating the json file. Error: " + e.getMessage());
        }
    }
    
    /**
     * Create an example vendor json file.
     * @param file the File to create.
     */
    private void createDefaultJsonFile(File file) {
        GVJSON gvjson = new GVJSON();
        gvjson.name = getProject().getName();
        gvjson.version = "v0.0.0";
        gvjson.uuid = UUID.randomUUID().toString();
        gvjson.frcYear = 2024;
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
            getProject().getLogger().error("Something went wrong creating the example json file. Error: " + e.getMessage());
        }
    }

    /**
     * Takes a array of Strings and returns that array of Strings.
     * Used as a bandaid for making arrays of Strings in a build.gradle file.
     * @param a The array of Strings, just comma seperated, no [ or ].
     * @return The array of Strings, as an array of Strings.
     */
    public static String[] stringArray(String... a) {
        return a;
    }
}

/**
 * A template class for the entire vendor json file,
 * with every possible option.
 */
class GVJSON {
    String name;
    String version;
    String uuid;
    String frcYear;
    String[] mavenUrls;
    String jsonUrl;
    String fileName;
    GVJSONJavaArtifact[] javaDependencies;
    GVJSONJNIArtifact[] jniDependencies;
    GVJSONCppArtifact[] cppDependencies;
}

/**
 * The Java artifact part of the vendor json file,
 * with all possible options.
 */
class GVJSONJavaArtifact {
    String groupId;
    String artifactId;
    String version;
}

/**
 * The JNI artifact part of the vendor json file,
 * with all possible options.
 */
class GVJSONJNIArtifact {
    String groupId;
    String artifactId;
    String version;
    
    boolean isJar;
    
    String[] validPlatforms;
    boolean skipInvalidPlatforms;
}

/**
 * The C++ artifact part of the vendor json file,
 * with all possible options.
 */
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