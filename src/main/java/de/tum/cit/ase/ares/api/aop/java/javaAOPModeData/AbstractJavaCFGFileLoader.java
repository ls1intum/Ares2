package de.tum.cit.ase.ares.api.aop.java.javaAOPModeData;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;

import java.io.IOException;
import java.util.List;

public abstract class AbstractJavaCFGFileLoader implements JavaFileLoader {


    protected abstract List<List<String>> loadCopyData()
            throws IOException;

    protected abstract List<List<String>> loadEditData() throws IOException;


    @Override
    public final List<List<String>> loadCopyData(JavaAOPMode mode)
            throws IOException
    {
        return loadCopyData();
    }

    @Override
    public final List<List<String>> loadEditData(JavaAOPMode mode)
            throws IOException
    {
        return loadEditData();
    }
}
