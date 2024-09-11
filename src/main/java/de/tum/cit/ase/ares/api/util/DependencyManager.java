package de.tum.cit.ase.ares.api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Manages dependencies and plugins in build configuration files such as pom.xml and build.gradle.
 * Provides methods to add and remove dependencies and plugins from these files.
 */
public class DependencyManager {

    /** Logger for the DependencyManager class. */
    private static final Logger log = LoggerFactory.getLogger(DependencyManager.class);

    /** The path to the build configuration file. */
    private final String filePath;

    /**
     * Constructs a DependencyManager for the specified file.
     *
     * @param filePath the path to the file (pom.xml or build.gradle)
     * @throws IllegalArgumentException if the filePath is null or empty
     */
    public DependencyManager(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new SecurityException("File path cannot be null or empty.");
        }
        this.filePath = filePath;
    }

    /**
     * Adds a dependency to the build file (pom.xml or build.gradle).
     *
     * @param groupId    the group ID of the dependency
     * @param artifactId the artifact ID of the dependency
     * @param version    the version of the dependency
     * @throws IOException                   if an I/O error occurs while accessing the file
     * @throws ParserConfigurationException  if a document builder cannot be created
     * @throws TransformerException          if an error occurs during the transformation of the XML document
     * @throws SAXException                  if an error occurs while parsing the XML file
     */
    public void addDependency(String groupId, String artifactId, String version) throws IOException, ParserConfigurationException, TransformerException, SAXException {
        if (filePath.trim().endsWith("pom.xml")) {
            addDependencyToPom(groupId, artifactId, version);
        } else if (filePath.trim().endsWith("build.gradle")) {
            addDependencyToGradle(groupId, artifactId, version);
        }
    }

    /**
     * Removes a dependency from the build file (pom.xml or build.gradle).
     *
     * @param groupId    the group ID of the dependency to remove
     * @param artifactId the artifact ID of the dependency to remove
     * @throws Exception if an error occurs while processing the file
     */
    public void removeDependency(String groupId, String artifactId) throws Exception {
        if (filePath.endsWith("pom.xml")) {
            removeDependencyFromPom(groupId, artifactId);
        } else if (filePath.endsWith("build.gradle")) {
            removeDependencyFromGradle(groupId, artifactId);
        }
    }

    /**
     * Adds a dependency to the pom.xml file.
     *
     * @param groupId    the group ID of the dependency
     * @param artifactId the artifact ID of the dependency
     * @param version    the version of the dependency
     * @throws ParserConfigurationException if a document builder cannot be created
     * @throws IOException                  if an I/O error occurs while accessing the file
     * @throws SAXException                 if an error occurs while parsing the XML file
     * @throws TransformerException         if an error occurs during the transformation of the XML document
     */
    private void addDependencyToPom(String groupId, String artifactId, String version) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        File pomFile = new File(filePath);
        var docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(pomFile);

        Node dependenciesNode = doc.getElementsByTagName("dependencies").item(0);

        if (dependenciesNode == null) {
            throw new IllegalStateException("No dependencies tag found in pom.xml");
        }

        // Check if the dependency already exists
        NodeList dependencies = dependenciesNode.getChildNodes();
        for (int i = 0; i < dependencies.getLength(); i++) {
            Node node = dependencies.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element dependency = (Element) node;
                String existingGroupId = dependency.getElementsByTagName("groupId").item(0).getTextContent();
                String existingArtifactId = dependency.getElementsByTagName("artifactId").item(0).getTextContent();

                if (existingGroupId.equals(groupId) && existingArtifactId.equals(artifactId)) {
                    DependencyManager.log.info("Dependency {}:{} already exists in pom.xml", groupId, artifactId);
                    return;  // Exit without adding duplicate
                }
            }
        }

        // Add the new dependency if not present
        Element dependency = doc.createElement("dependency");

        Element groupIdElement = doc.createElement("groupId");
        groupIdElement.appendChild(doc.createTextNode(groupId));
        dependency.appendChild(groupIdElement);

        Element artifactIdElement = doc.createElement("artifactId");
        artifactIdElement.appendChild(doc.createTextNode(artifactId));
        dependency.appendChild(artifactIdElement);

        Element versionElement = doc.createElement("version");
        versionElement.appendChild(doc.createTextNode(version));
        dependency.appendChild(versionElement);

        dependenciesNode.appendChild(dependency);

        saveXmlChanges(doc, pomFile);
    }

    /**
     * Removes a dependency from the pom.xml file.
     *
     * @param groupId    the group ID of the dependency to remove
     * @param artifactId the artifact ID of the dependency to remove
     * @throws Exception if an error occurs while processing the XML file
     */
    private void removeDependencyFromPom(String groupId, String artifactId) throws Exception {
        var pomFile = new File(filePath);
        var docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(pomFile);

        NodeList dependencies = doc.getElementsByTagName("dependency");

        for (int i = 0; i < dependencies.getLength(); i++) {
            Node node = dependencies.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element dependency = (Element) node;
                String depGroupId = dependency.getElementsByTagName("groupId").item(0).getTextContent();
                String depArtifactId = dependency.getElementsByTagName("artifactId").item(0).getTextContent();

                if (groupId.equals(depGroupId) && artifactId.equals(depArtifactId)) {
                    dependency.getParentNode().removeChild(dependency);
                }
            }
        }

        saveXmlChanges(doc, pomFile);
    }

    /**
     * Adds a dependency to the build.gradle file.
     *
     * @param groupId    the group ID of the dependency
     * @param artifactId the artifact ID of the dependency
     * @param version    the version of the dependency
     * @throws IOException if an I/O error occurs while reading or writing the file
     */
    private void addDependencyToGradle(String groupId, String artifactId, String version) throws IOException {
        String dependencyLine = "    implementation '" + groupId + ":" + artifactId + ":" + version + "'";
        Path path = Paths.get(filePath);
        List<String> lines = Files.readAllLines(path);

        // Check if the dependency already exists
        for (String line : lines) {
            if (line.trim().contains("implementation '" + groupId + ":" + artifactId)) {
                log.info("Dependency {}:{} already exists in build.gradle", groupId, artifactId);
                return;  // Exit without adding duplicate
            }
        }

        // Find the dependencies block and add the new dependency
        int dependenciesIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().equalsIgnoreCase("dependencies {")) {
                dependenciesIndex = i;
                break;
            }
        }

        if (dependenciesIndex != -1) {
            lines.add(dependenciesIndex + 1, dependencyLine);
            Files.write(path, lines);
        } else {
            throw new IOException("No dependencies block found in build.gradle");
        }
    }

    /**
     * Removes a dependency from the build.gradle file.
     *
     * @param groupId    the group ID of the dependency to remove
     * @param artifactId the artifact ID of the dependency to remove
     * @throws IOException if an I/O error occurs while reading or writing the file
     */
    private void removeDependencyFromGradle(String groupId, String artifactId) throws IOException {
        String dependencyPrefix = "implementation '" + groupId + ":" + artifactId + ":";
        Path path = Paths.get(filePath);
        List<String> lines = Files.readAllLines(path);
        lines.removeIf(line -> line.trim().startsWith(dependencyPrefix));
        Files.write(path, lines);
    }

    /**
     * Adds a plugin to the pom.xml file.
     *
     * @param groupId        the group ID of the plugin
     * @param artifactId     the artifact ID of the plugin
     * @param version        the version of the plugin
     * @param configurations additional configurations for the plugin
     * @param executions     optional executions to configure the plugin
     * @throws Exception if an error occurs while processing the XML file
     */
    public void addPlugin(String groupId, String artifactId, String version, List<Element> configurations, List<Element> executions) throws Exception {
        if (filePath.trim().endsWith("pom.xml")) {
            addPluginToPom(groupId, artifactId, version, configurations, executions);
        } else {
            throw new UnsupportedOperationException("Plugin management is only supported for pom.xml files.");
        }
    }

    /**
     * Adds a plugin to the pom.xml file, if not already present
     */
    private void addPluginToPom(String groupId, String artifactId, String version, List<Element> configurations, List<Element> executions) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        File pomFile = new File(filePath);
        var docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(pomFile);

        Node pluginsNode = getOrCreatePluginsNode(doc);

        if (!isPluginPresent(doc, groupId, artifactId)) {
            // Create plugin element
            Element plugin = doc.createElement("plugin");

            // Add groupId, artifactId, version
            Element groupIdElement = doc.createElement("groupId");
            groupIdElement.appendChild(doc.createTextNode(groupId));
            plugin.appendChild(groupIdElement);

            Element artifactIdElement = doc.createElement("artifactId");
            artifactIdElement.appendChild(doc.createTextNode(artifactId));
            plugin.appendChild(artifactIdElement);

            Element versionElement = doc.createElement("version");
            versionElement.appendChild(doc.createTextNode(version));
            plugin.appendChild(versionElement);

            // Add configurations (if any)
            if (configurations != null && !configurations.isEmpty()) {
                Element configurationElement = doc.createElement("configuration");
                for (Element config : configurations) {
                    configurationElement.appendChild(doc.importNode(config, true));
                }
                plugin.appendChild(configurationElement);
            }

            // Add executions (if any)
            if (executions != null && !executions.isEmpty()) {
                Element executionsElement = doc.createElement("executions");
                for (Element execution : executions) {
                    executionsElement.appendChild(doc.importNode(execution, true));
                }
                plugin.appendChild(executionsElement);
            }

            // Add the plugin to the <plugins> section
            pluginsNode.appendChild(plugin);

            // Save the updated pom.xml file
            saveXmlChanges(doc, pomFile);
        } else {
            log.info("Plugin {}:{} already exists.", groupId, artifactId);
        }
    }

    /**
     * Checks if a plugin is already present in the pom.xml file
     */
    private boolean isPluginPresent(Document doc, String groupId, String artifactId) {
        NodeList plugins = doc.getElementsByTagName("plugin");
        for (int i = 0; i < plugins.getLength(); i++) {
            Node node = plugins.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element plugin = (Element) node;
                String pluginGroupId = plugin.getElementsByTagName("groupId").item(0).getTextContent();
                String pluginArtifactId = plugin.getElementsByTagName("artifactId").item(0).getTextContent();

                if (groupId.equals(pluginGroupId) && artifactId.equals(pluginArtifactId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get or create the <plugins> node in the pom.xml file
     */
    private Node getOrCreatePluginsNode(Document doc) {
        NodeList pluginsList = doc.getElementsByTagName("plugins");
        Node pluginsNode;

        if (pluginsList.getLength() == 0) {
            // Create a new <plugins> section if it doesn't exist
            pluginsNode = doc.createElement("plugins");
            doc.getDocumentElement().appendChild(pluginsNode);
        } else {
            pluginsNode = pluginsList.item(0);
        }

        return pluginsNode;
    }

    /**
     * Saves the changes made to the XML document back to the file
     */
    private void saveXmlChanges(Document doc, File file) throws TransformerException {
        var transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

    /**
     * Example usage!!!
     */
    public static void main(String[] args) {
        addDependenciesAndPluginsForMaven("path/to/pom.xml");
        try {
            DependencyManager manager = new DependencyManager("/path/to/pom.xml");
            manager.addDependency("org.example", "example-artifact", "1.0.0");
            manager.removeDependency("org.example", "example-artifact");
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public static void addDependenciesAndPluginsForMaven(String pomPath) {
        try {
            DependencyManager manager = new DependencyManager(pomPath);

            // Create document to build XML elements
            var docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            // 1. Create the dependencies section
            Element dependencies = doc.createElement("dependencies");
            Element dependency = doc.createElement("dependency");

            Element groupIdDep = doc.createElement("groupId");
            groupIdDep.appendChild(doc.createTextNode("org.aspectj"));
            dependency.appendChild(groupIdDep);

            Element artifactIdDep = doc.createElement("artifactId");
            artifactIdDep.appendChild(doc.createTextNode("aspectjtools"));
            dependency.appendChild(artifactIdDep);

            Element versionDep = doc.createElement("version");
            versionDep.appendChild(doc.createTextNode("${aspectj.version}"));
            dependency.appendChild(versionDep);

            dependencies.appendChild(dependency);

            // 2. Create the configuration section
            Element complianceLevel = doc.createElement("complianceLevel");
            complianceLevel.appendChild(doc.createTextNode("21"));
            Element aspectDirectory = doc.createElement("aspectDirectory");
            aspectDirectory.appendChild(doc.createTextNode("src/test/java"));

            // First execution
            Element execution1 = doc.createElement("execution");
            Element goals1 = doc.createElement("goals");
            Element goalCompile = doc.createElement("goal");
            goalCompile.appendChild(doc.createTextNode("compile"));
            Element goalTestCompile = doc.createElement("goal");
            goalTestCompile.appendChild(doc.createTextNode("test-compile"));
            goals1.appendChild(goalCompile);
            goals1.appendChild(goalTestCompile);
            execution1.appendChild(goals1);

            // Second execution
            Element execution2 = doc.createElement("execution");
            Element executionId = doc.createElement("id");
            executionId.appendChild(doc.createTextNode("process-integration-test-classes"));
            Element phase = doc.createElement("phase");
            phase.appendChild(doc.createTextNode("process-test-classes"));
            Element goals2 = doc.createElement("goals");
            Element goalCompileAgain = doc.createElement("goal");
            goalCompileAgain.appendChild(doc.createTextNode("compile"));
            goals2.appendChild(goalCompileAgain);
            execution2.appendChild(executionId);
            execution2.appendChild(phase);
            execution2.appendChild(goals2);

            // Add the plugin with dependencies, configuration, and executions
            manager.addPlugin("dev.aspectj", "aspectj-maven-plugin", "1.14", List.of(complianceLevel, aspectDirectory), List.of(execution1, execution2));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}


