package de.tum.cit.ase.ares.api.aspectconfiguration.java;

public aspect PointcutDefinitions {

        // Pointcut for ColorEditor methods
        pointcut colorEditorMethods() :
                (execution(com.sun.beans.editors.ColorEditor.new(..)) ||
                        execution(* com.sun.beans.editors.ColorEditor.action(..)) ||
                        execution(* com.sun.beans.editors.ColorEditor.keyUp(..)) ||
                        execution(* com.sun.beans.editors.ColorEditor.setAsText(..)) ||
                        execution(* com.sun.beans.editors.ColorEditor.setValue(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

        // Pointcut for FontEditor methods
        pointcut fontEditorMethods() :
                (execution(com.sun.beans.editors.FontEditor.new(..)) ||
                        execution(* com.sun.beans.editors.FontEditor.action(..)) ||
                        execution(* com.sun.beans.editors.FontEditor.setAsText(..)) ||
                        execution(* com.sun.beans.editors.FontEditor.setValue(..))) &&
                        !within(de.tum.cit.ase.ares.api..*);

}