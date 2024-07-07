package de.tum.cit.ase.ares.api.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SecurityPolicyReader {

    private SecurityPolicyReader() {
        throw new UnsupportedOperationException("This class should not be instantiated");
    }

    public static SecurityPolicy readSecurityPolicy(Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        String content = Files.readString(path);
        return mapper.readValue(content, SecurityPolicy.class);
    }
}
