package de.tum.cit.ase.ares.api.architecture.java.wala;

import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BytecodeConverter {

    public static void main(String[] args) throws IOException {
        // Example method signature list
        List<String> methodSignatures = Files.readAllLines(Path.of("C:\\Users\\sarps\\IdeaProjects\\Ares2\\src\\main\\resources\\de\\tum\\cit\\ase\\ares\\api\\templates\\architecture.java\\wala\\methods\\walamethods.txt"));

        // Process each method signature
        for (String signature : methodSignatures) {
            System.out.println(parseMethodSignature(signature));
        }
    }

    // Parse method signature and generate MethodReference
    public static String parseMethodSignature(String signature) {
        // Regular expression to match method signatures
        String regex = "(.*)\\.(.*)\\((.*)\\)\\s+\\((.*)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(signature);

        if (matcher.matches()) {
            String className = matcher.group(1);  // Convert '.' to '/'
            String methodName = matcher.group(2);
            String paramTypes = matcher.group(3);  // Extract parameters
            String packageOfClass = matcher.group(4);  // Extract return type

            // Build the method signature format for WALA
            String methodSignature = buildMethodSignature(paramTypes);

            return packageOfClass + "." + className + "." + methodName + "(" + methodSignature + ")";
        } else {
            System.err.println("Could not parse signature: " + signature);
            return null;
        }
    }

    // Build the method signature for WALA (bytecode type format)
    public static String buildMethodSignature(String paramTypes) {
        StringBuilder signature = new StringBuilder();

        // Process parameter types (e.g., "Ljava/io/File;")
        if (paramTypes != null && !paramTypes.isEmpty()) {
            for (String param : paramTypes.split(",")) {
                signature.append(toBytecodeType(param.trim()));
            }
        }

        return signature.toString();
    }

    // Convert Java type to bytecode type descriptor
    public static String toBytecodeType(String javaType) {
        // Handle common Java types and return the corresponding bytecode descriptor
        switch (javaType) {
            case "String": return "Ljava/lang/String;";
            case "File": return "Ljava/io/File;";
            case "Path": return "Ljava/nio/file/Path;";
            case "URI": return "Ljava/net/URI;";
            case "OpenOption": return "Ljava/nio/file/OpenOption;";
            case "ByteBuffer": return "Ljava/nio/ByteBuffer;";
            case "Redirect": return "Ljava/lang/ProcessBuilder$Redirect;";
            case "int": return "I";
            case "boolean": return "Z";
            case "long":
                return "J";
            case "float":
                return "F";
            case "double":
                return "D";
            case "byte":
                return "B";
            case "char":
                return "C";
            case "short":
                return "S";
            case "void":
                return "V";
            case "ProtectionDomain": return "Ljava/security/ProtectionDomain;";
            case "DataFlavor": return "Ljava/awt/datatransfer/DataFlavor;";
            case "Transferable": return "Ljava/awt/datatransfer/Transferable;";
            case "List": return "Ljava/util/List;";
            case "Flags": return "Lsun/nio/fs/Flags;";
            case "Charset": return "Ljava/nio/charset/Charset;";
            case "DocFlavor": return "Ljavax/print/DocFlavor;";
            case "List<?>": return "Ljava/util/List;";
            default:
                if (javaType.startsWith("Set")) return "Ljava/util/Set;";
                if (javaType.startsWith("Map")) return "Ljava/util/Map;";
                return "L" + javaType.replace('.', '/') + ";";  // Default case for other objects
        }
    }
}
