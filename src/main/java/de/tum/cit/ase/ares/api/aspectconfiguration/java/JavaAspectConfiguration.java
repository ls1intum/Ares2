package de.tum.cit.ase.ares.api.aspectconfiguration.java;

import de.tum.cit.ase.ares.api.aspectconfiguration.AspectConfiguration;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;

/**
 * Aspect configuration for the Java programming language and concrete product of the abstract factory design pattern.
 *
 * @author Markus Paulsen
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @since 2.0.0
 */
public class JavaAspectConfiguration implements AspectConfiguration {

    private JavaSupportedAspectConfiguration javaSupportedAspectConfiguration;
    private SecurityPolicy securityPolicy;

    /**
     * Constructor for JavaAspectConfiguration.
     *
     * @param javaSupportedAspectConfiguration Selects the supported aspect configuration in the Java programming language
     * @param securityPolicy                   Security policy for the aspect configuration
     */
    public JavaAspectConfiguration(JavaSupportedAspectConfiguration javaSupportedAspectConfiguration, SecurityPolicy securityPolicy) {
        this.javaSupportedAspectConfiguration = javaSupportedAspectConfiguration;
        this.securityPolicy = securityPolicy;
    }

    /**
     * Creates the content of the aspect configuration file in the Java programming language.
     *
     * @return Content of the aspect configuration file
     */
    @Override
    public String createAspectConfigurationFileContent() {
        StringBuilder content = new StringBuilder();
        switch (javaSupportedAspectConfiguration) {
            case FILESYSTEMINTERACTION -> {
                content.append("package de.tum.cit.ase.ares.api.aspectconfiguration.java;\n\n") // TODO: Do we need packages in the files?
                        .append("public aspect PointcutDefinitions {\n\n")
                        .append("    pointcut filesWriteMethod() : call(* java.nio.file.Files.write(..));\n")
                        .append("    pointcut filesReadMethod() : call(* java.nio.file.Files.readAllBytes(..)) || call(* java.nio.file.Files.readAllLines(..));\n")
                        .append("    pointcut filesDeleteMethod() : call(* java.nio.file.Files.delete(..));\n")
                        .append("}\n\n")
                        .append("package de.tum.cit.ase.ares.api.aspectconfiguration.java;\n\n") // TODO: Do we need packages in the files?
                        .append("import java.nio.file.Path;\n") // TODO: Do we need imports in the files?
                        .append("import java.util.Arrays;\n")
                        .append("import java.util.List;\n")
                        .append("import java.util.stream.Collectors;\n\n")
                        .append("public aspect aspectJava {\n\n");

                content.append("    private static final List<FileSystemInteraction> allowedFileSystemInteractions = List.of(\n");
                content.append(securityPolicy.iAllowTheFollowingFileSystemInteractionsForTheStudents().stream()
                        .map(securityContent -> String.format("        new FileSystemInteraction(Path.of(\"%s\"), %s, %s, %s)",
                                securityContent.onThisPathAndAllPathsBelow().toString(),
                                securityContent.studentsAreAllowedToOverwriteAllFiles(),
                                securityContent.studentsAreAllowedToReadAllFiles(),
                                securityContent.studentsAreAllowedToDeleteAllFiles()))
                        .collect(Collectors.joining(",\n")));
                content.append("\n    );\n\n");
                content.append("    Object around() : PointcutDefinitions.filesWriteMethod() {\n")
                        .append("        Object[] args = thisJoinPoint.getArgs();\n")
                        .append("        Path path = (Path) args[0];\n")
                        .append("        String fileName = thisJoinPoint.getSourceLocation().getFileName();\n")
                        .append("        boolean isAllowed = allowedFileSystemInteractions.stream()\n")
                        .append("                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)\n")
                        .append("                        && interaction.studentsAreAllowedToOverwriteAllFiles());\n")
                        .append("        if (!isAllowed) {\n")
                        .append("            System.out.println(\"Files.write called with path: \" + path + \" in \" + thisJoinPoint.getSourceLocation() + \" - Access Denied\");\n")
                        .append("            throw new SecurityException(\"Write operation blocked by AspectJ for path: \" + path);\n")
                        .append("        } else {\n")
                        .append("            System.out.println(\"Files.write called with path: \" + path + \" in \" + thisJoinPoint.getSourceLocation() + \" - Access Granted\");\n")
                        .append("        }\n")
                        .append("        if (args.length > 1 && args[1] instanceof List) {\n")
                        .append("            List<String> lines = (List<String>) args[1];\n")
                        .append("            System.out.println(\"Files.write called with path: \" + path + \" and lines: \" + lines);\n")
                        .append("        } else if (args.length > 1 && args[1] instanceof byte[]) {\n")
                        .append("            byte[] bytes = (byte[]) args[1];\n")
                        .append("            System.out.println(\"Files.write called with path: \" + path + \" and bytes: \" + Arrays.toString(bytes));\n")
                        .append("        } else {\n")
                        .append("            System.out.println(\"Files.write called with path: \" + path + \" and other arguments: \" + Arrays.toString(args));\n")
                        .append("        }\n")
                        .append("        return proceed();\n")
                        .append("    }\n\n")
                        .append("    Object around() : PointcutDefinitions.filesReadMethod() {\n")
                        .append("        Object[] args = thisJoinPoint.getArgs();\n")
                        .append("        Path path = (Path) args[0];\n")
                        .append("        String fileName = thisJoinPoint.getSourceLocation().getFileName();\n")
                        .append("        boolean isAllowed = allowedFileSystemInteractions.stream()\n")
                        .append("                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)\n")
                        .append("                        && interaction.studentsAreAllowedToReadAllFiles());\n")
                        .append("        if (!isAllowed) {\n")
                        .append("            System.out.println(\"Files.read called with path: \" + path + \" in \" + thisJoinPoint.getSourceLocation() + \" - Access Denied\");\n")
                        .append("            throw new SecurityException(\"Read operation blocked by AspectJ for path: \" + path);\n")
                        .append("        } else {\n")
                        .append("            System.out.println(\"Files.read called with path: \" + path + \" in \" + thisJoinPoint.getSourceLocation() + \" - Access Granted\");\n")
                        .append("        }\n")
                        .append("        return proceed();\n")
                        .append("    }\n\n")
                        .append("    Object around() : PointcutDefinitions.filesDeleteMethod() {\n")
                        .append("        Object[] args = thisJoinPoint.getArgs();\n")
                        .append("        Path path = (Path) args[0];\n")
                        .append("        String fileName = thisJoinPoint.getSourceLocation().getFileName();\n")
                        .append("        boolean isAllowed = allowedFileSystemInteractions.stream()\n")
                        .append("                .anyMatch(interaction -> interaction.onThisPathAndAllPathsBelow().getFileName().toString().equals(fileName)\n")
                        .append("                        && interaction.studentsAreAllowedToDeleteAllFiles());\n")
                        .append("        if (!isAllowed) {\n")
                        .append("            System.out.println(\"Files.delete called with path: \" + path + \" in \" + thisJoinPoint.getSourceLocation() + \" - Access Denied\");\n")
                        .append("            throw new SecurityException(\"Delete operation blocked by AspectJ for path: \" + path);\n")
                        .append("        } else {\n")
                        .append("            System.out.println(\"Files.delete called with path: \" + path + \" in \" + thisJoinPoint.getSourceLocation() + \" - Access Granted\");\n")
                        .append("        }\n")
                        .append("        return proceed();\n")
                        .append("    }\n\n")
                        .append("}\n");
            }
            case NETWORKCONNECTION -> {
                // Placeholder
                content.append("// Network Connection Aspect Configuration\n");
            }
            case COMMAND_EXECUTION -> {
                // Placeholder
                content.append("// Command Execution Aspect Configuration\n");
            }
            case THREAD_CREATION -> {
                // Placeholder
                content.append("// Thread Creation Aspect Configuration\n");
            }
            case PACKAGE_IMPORT -> {
                // Placeholder
                content.append("// Package Import Aspect Configuration\n");
            }
        }
        return content.toString();
    }

    /**
     * Runs the aspect configuration in Java programming language.
     */
    @Override
    public void runAspectConfiguration() {
        String aspectFileName; // TODO: discuss on how the file is received or where it is stored
        switch (javaSupportedAspectConfiguration) {
            case FILESYSTEMINTERACTION -> {
                aspectFileName = "aspectJava.aj";
                try {
                    updateAspectFile(aspectFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            case NETWORKCONNECTION -> {
                // Placeholder
                aspectFileName = "aspectNetworkConnection.aj";
            }
            case COMMAND_EXECUTION -> {
                // Placeholder
                aspectFileName = "aspectCommandExecution.aj";
            }
            case THREAD_CREATION -> {
                // Placeholder
                aspectFileName = "aspectThreadCreation.aj";
            }
            case PACKAGE_IMPORT -> {
                // Placeholder
                aspectFileName = "aspectPackageImport.aj";
            }
        }
    }

    private void updateAspectFile(String aspectFileName) throws IOException {
        Path path = Path.of(aspectFileName);
        String content = Files.readString(path);

        // Find the list definition and replace it with the new list
        String newListContent = "    private static final List<FileSystemInteraction> allowedFileSystemInteractions = List.of(\n";
        newListContent += securityPolicy.iAllowTheFollowingFileSystemInteractionsForTheStudents().stream()
                .map(interaction -> String.format("        new FileSystemInteraction(Path.of(\"%s\"), %s, %s, %s)",
                        interaction.onThisPathAndAllPathsBelow().toString(),
                        interaction.studentsAreAllowedToOverwriteAllFiles(),
                        interaction.studentsAreAllowedToReadAllFiles(),
                        interaction.studentsAreAllowedToDeleteAllFiles()))
                .collect(Collectors.joining(",\n"));
        newListContent += "\n    );\n";

        content = content.replaceAll("private static final List<FileSystemInteraction> allowedFileSystemInteractions = List.of\\([\\s\\S]*?\\);", newListContent);

        // Write the updated content back to the file
        Files.writeString(path, content, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
