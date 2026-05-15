package com.lpu.smartcli.plugins;

import java.util.Objects;

/**
 * Basic descriptor for a SmartCLI plugin bundle.
 */
public final class PluginMetadata {
    private final String id;
    private final String displayName;
    private final String version;
    private final String author;

    public PluginMetadata(String id, String displayName, String version, String author) {
        this.id = Objects.requireNonNullElse(id, "unknown");
        this.displayName = Objects.requireNonNullElse(displayName, this.id);
        this.version = Objects.requireNonNullElse(version, "0.0.0");
        this.author = author == null ? "" : author;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        return displayName + " " + version + " (" + id + ")";
    }
}
