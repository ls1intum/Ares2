package de.tum.cit.ase.ares.api.aop.java.javaAOPModeData;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;

import java.util.List;

public interface JavaFileLoader {
    List<List<String>> loadCopyData(JavaAOPMode mode);
    List<List<String>> loadEditData(JavaAOPMode mode);
}
