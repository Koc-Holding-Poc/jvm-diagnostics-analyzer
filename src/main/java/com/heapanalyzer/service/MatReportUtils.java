package com.heapanalyzer.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Shared utilities for reading Eclipse MAT report ZIPs and converting their
 * HTML content to structured plain text suitable for LLM consumption.
 *
 * <p>Previously duplicated verbatim between {@link MatAnalysisService} and
 * {@link MatQueryService}; centralised here to guarantee consistent behaviour
 * and simplify future maintenance.</p>
 */
final class MatReportUtils {

    private MatReportUtils() {}

    // Pre-compiled HTML stripping patterns.
    // Using negative-lookahead (`(?!...)`) instead of `(?s).*?` prevents
    // catastrophic backtracking (ReDoS) on adversarial HTML content.
    private static final Pattern SCRIPT_PATTERN =
            Pattern.compile("<script[^>]*>(?:(?!</script>)[\\s\\S])*</script>", Pattern.CASE_INSENSITIVE);
    private static final Pattern STYLE_PATTERN =
            Pattern.compile("<style[^>]*>(?:(?!</style>)[\\s\\S])*</style>", Pattern.CASE_INSENSITIVE);
    private static final Pattern COMMENT_PATTERN =
            Pattern.compile("<!--(?:(?!-->)[\\s\\S])*-->");

    // ========================== ZIP Utilities ==========================

    /**
     * Returns the first ZIP file in {@code directory} whose name matches the
     * given glob pattern, or {@code null} if none is found.
     */
    static Path findZip(Path directory, String glob) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, glob)) {
            for (Path p : stream) return p;
        }
        return null;
    }

    /**
     * Returns the ZIP entry with the given {@code name}, also checking for the
     * entry nested one level deep (e.g. {@code pages/index.html}).
     */
    static ZipEntry findEntry(ZipFile zip, String name) {
        ZipEntry entry = zip.getEntry(name);
        if (entry != null) return entry;
        return zip.stream()
                .filter(e -> e.getName().endsWith("/" + name) && !e.isDirectory())
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the first HTML entry whose name contains {@code keyword}
     * (case-insensitive), or {@code null} if none is found.
     */
    static ZipEntry findEntryContaining(ZipFile zip, String keyword) {
        return zip.stream()
                .filter(e -> !e.isDirectory() && e.getName().endsWith(".html"))
                .filter(e -> e.getName().toLowerCase().contains(keyword.toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    /** Reads a ZIP entry and returns its content as a UTF-8 string. */
    static String readEntry(ZipFile zip, ZipEntry entry) throws IOException {
        try (var is = zip.getInputStream(entry)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    // ========================== Text Processing ==========================

    /**
     * Converts MAT HTML report content to clean plain text, preserving table
     * structure and removing noise (scripts, styles, HTML comments).
     */
    static String htmlToText(String html) {
        return SCRIPT_PATTERN.matcher(html).replaceAll("")
                .transform(s -> STYLE_PATTERN.matcher(s).replaceAll(""))
                .transform(s -> COMMENT_PATTERN.matcher(s).replaceAll(""))
                // Preserve table column boundaries
                .replaceAll("</th>\\s*<th", "  |  </th><th")
                .replaceAll("</td>\\s*<td", "  |  </td><td")
                .replaceAll("<br\\s*/?>", "\n")
                .replaceAll("</p>", "\n")
                .replaceAll("</tr>", "\n")
                .replaceAll("</li>", "\n")
                .replaceAll("</h[1-6]>", "\n")
                .replaceAll("<[^>]+>", " ")
                // HTML entities
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"")
                .replaceAll("&#\\d+;", "")
                // Clean whitespace
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\n[ \\t]+", "\n")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }

    /**
     * Keeps only the first {@code maxLines} non-empty lines.
     * Used to cap histogram / package data at a token budget.
     */
    static String keepTopLines(String text, int maxLines) {
        String[] lines = text.split("\n");
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String line : lines) {
            if (!line.isBlank()) {
                sb.append(line.trim()).append("\n");
                count++;
                if (count >= maxLines) break;
            }
        }
        return sb.toString();
    }

    /**
     * Filters system-property lines, keeping only JVM-relevant settings
     * (heap flags, GC type, OS, Java version, etc.) and discarding noise.
     */
    static String filterJvmProperties(String text, Pattern jvmFlagPattern) {
        Set<String> relevantKeys = Set.of(
                "sun.java.command",
                "java.vm.name", "java.version", "java.runtime.version",
                "sun.arch.data.model",
                "os.name", "os.arch"
        );

        StringBuilder sb = new StringBuilder();
        for (String line : text.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isBlank()) continue;

            boolean relevant = relevantKeys.stream().anyMatch(trimmed::contains)
                    || jvmFlagPattern.matcher(trimmed).matches()
                    || trimmed.startsWith("-"); // JVM flags start with -

            if (relevant) sb.append(trimmed).append("\n");
        }

        return sb.isEmpty() ? text.lines().limit(10).collect(Collectors.joining("\n")) : sb.toString();
    }

    /** Truncates {@code text} to at most {@code maxChars} characters. */
    static String truncate(String text, int maxChars) {
        if (text.length() <= maxChars) return text;
        return text.substring(0, maxChars) + "\n[... truncated at " + maxChars + " chars ...]";
    }
}
