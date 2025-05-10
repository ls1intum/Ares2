package de.tum.cit.ase.ares.api.aop.java.javaAOPModeData;

import de.tum.cit.ase.ares.api.util.FileTools;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class JavaCFGFileLoader extends AbstractJavaCFGFileLoader {

    @Override
    protected List<List<String>> loadCopyData() throws IOException {
        return FileTools.readCFGFile(getCopyPaths());
    }

    private Path getCopyPaths() {
        return FileTools.resolveOnPackage("configuration/BasePhobos.cfg");
    }

    @Override
    protected List<List<String>> loadEditData() throws IOException {
        return List.of();
    }

}
