package com.facebook.nailgun;

final class JavaVersionUtil {
    private JavaVersionUtil() {
    }

    static int featureVersion() {
        return parseFeatureVersion(System.getProperty("java.version"));
    }

    // Adapted from https://stackoverflow.com/a/49512420.
    static int parseFeatureVersion(String version) {
        if (version.startsWith("1.")) {
            version = version.substring(2);
        }
        // Allow these formats:
        // 1.8.0_72-ea
        // 9-ea
        // 9
        // 9.0.1
        final int dotPos = version.indexOf('.');
        final int dashPos = version.indexOf('-');
        return Integer.parseInt(version.substring(0, dotPos > -1 ? dotPos : dashPos > -1 ? dashPos : version.length()));
    }
}
