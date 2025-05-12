package de.tum.cit.ase.ares.api.aop.java.javaAOPModeData;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;

import java.io.IOException;
import java.util.List;

public interface JavaFileLoader {
    List<List<String>> loadCopyData(JavaAOPMode mode) throws IOException;
    List<List<String>> loadEditData(JavaAOPMode mode) throws IOException;
    List<List<String>> loadCopyData() throws IOException;
    List<List<String>> loadEditData() throws IOException;
}
