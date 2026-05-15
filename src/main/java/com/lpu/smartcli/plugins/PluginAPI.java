package com.lpu.smartcli.plugins;

/**
 * Extension point for third-party JARs placed under {@code ~/.smartcli/plugins/}.
 * Implementations are discovered via {@code META-INF/services/com.lpu.smartcli.plugins.PluginAPI}.
 */
public interface PluginAPI {

    /**
     * Describes this plugin for diagnostics and UI.
     */
    default PluginMetadata metadata() {
        return new PluginMetadata(
                getClass().getName(),
                getClass().getSimpleName(),
                "0.0.0",
                "");
    }

    /**
     * Called once during CLI startup to contribute commands.
     */
    void register(PluginContext context);
}
