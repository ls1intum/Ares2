package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.execute.scriptEngine;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileReader;
import java.io.IOException;

public class ExecuteScriptEngineMain {

    private ExecuteScriptEngineMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using the {@link ScriptEngine} for execution.
     */
    public static void accessFileSystemViaScriptEngine(String filePath) throws IOException, ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        
        try (FileReader reader = new FileReader(filePath)) {
            engine.eval(reader);
        }
    }
}