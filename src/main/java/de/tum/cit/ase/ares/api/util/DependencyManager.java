package de.tum.cit.ase.ares.api.util;

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

public class DependencyManager {

    private final String filePath;

    public DependencyManager(String filePath) {
        this.filePath = filePath;
    }

    public void addDependency(String groupId, String artifactId, String version) throws Exception {
        if (filePath.endsWith("pom.xml")) {
            addDependencyToPom(groupId, artifactId, version);
        } else if (filePath.endsWith("build.gradle")) {
            addDependencyToGradle(groupId, artifactId, version);
        }
    }

    public void removeDependency(String groupId, String artifactId) throws Exception {
        if (filePath.endsWith("pom.xml")) {
            removeDependencyFromPom(groupId, artifactId);
        } else if (filePath.endsWith("build.gradle")) {
            removeDependencyFromGradle(groupId, artifactId);
        }
    }

    private void addDependencyToPom(String groupId, String artifactId, String version) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        File pomFile = new File(filePath);
        var docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(pomFile);

        Node dependenciesNode = doc.getElementsByTagName("dependencies").item(0);

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

    private void addDependencyToGradle(String groupId, String artifactId, String version) throws IOException {
        String dependencyLine = "implementation '" + groupId + ":" + artifactId + ":" + version + "'";
        Path path = Paths.get(filePath);
        List<String> lines = Files.readAllLines(path);
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

    private void removeDependencyFromGradle(String groupId, String artifactId) throws IOException {
        String dependencyPrefix = "implementation '" + groupId + ":" + artifactId + ":";
        Path path = Paths.get(filePath);
        List<String> lines = Files.readAllLines(path);
        lines.removeIf(line -> line.trim().startsWith(dependencyPrefix));
        Files.write(path, lines);
    }

    private void saveXmlChanges(Document doc, File file) throws TransformerException {
        var transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

    public static void main(String[] args) {
        try {
            DependencyManager manager = new DependencyManager("/home/sarps/IdeaProjects/Ares2/src/test/java/de/tum/cit/ase/ares/integration/testuser/subject/example/build/tools/pom.xml");
//            manager.addDependency("org.example", "example-artifact", "1.0.0");
            manager.removeDependency("org.example", "example-artifact");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


