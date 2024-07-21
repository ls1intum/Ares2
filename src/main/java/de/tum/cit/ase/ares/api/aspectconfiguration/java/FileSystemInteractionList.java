package de.tum.cit.ase.ares.api.aspectconfiguration.java;

import de.tum.cit.ase.ares.api.policy.FileSystemInteraction;

import java.util.Collections;
import java.util.List;

public class FileSystemInteractionList {

    private static List<FileSystemInteraction> allowedFileSystemInteractions = Collections.emptyList();

    public static List<FileSystemInteraction> getAllowedFileSystemInteractions() {
        return allowedFileSystemInteractions;
    }

    public static void setAllowedFileSystemInteractions(List<FileSystemInteraction> allowedFileSystemInteractions) {
        FileSystemInteractionList.allowedFileSystemInteractions = allowedFileSystemInteractions;
    }
}