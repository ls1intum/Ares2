import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks a pull request body against {@code .github/PULL_REQUEST_TEMPLATE.md}.
 *
 * <p>Run with {@code java .github/scripts/CheckPullRequestTemplate.java} (single-file source-code
 * mode, Java 11 and later). The body arrives through the {@code PR_BODY} environment variable. It
 * is untrusted input on fork pull requests and must never be interpolated into a shell command.
 *
 * <p>The required section headings are read from the template itself rather than duplicated here,
 * so editing the template cannot leave this check behind. The check validates shape, not substance:
 * that every section exists, that none was left empty, and that no unfilled stub survived. It
 * deliberately does not require checkboxes to be ticked, which would only train contributors to
 * tick them.
 */
public class CheckPullRequestTemplate {

    private static final Path TEMPLATE = Path.of(".github/PULL_REQUEST_TEMPLATE.md");

    private static final Pattern HEADING = Pattern.compile("^## .+$", Pattern.MULTILINE);

    private static final Pattern HTML_COMMENT = Pattern.compile("<!--.*?-->", Pattern.DOTALL);

    private static final Pattern FENCED_BLOCK = Pattern.compile("^```.*?^```", Pattern.DOTALL | Pattern.MULTILINE);

    private static final Pattern BARE_LIST_MARKER = Pattern.compile("\\s*\\d+\\.\\s*");

    /**
     * A table row whose every cell is blank. The template's separator row ({@code | --- | ---: |})
     * never matches this, because its cells are not whitespace, so no separate exclusion is needed.
     */
    private static final Pattern EMPTY_TABLE_ROW = Pattern.compile("\\s*\\|(\\s*\\|)+\\s*");

    /**
     * Escape hatches the template itself documents. A section carrying one of these is complete by
     * definition and is exempt from the leftover-stub scan.
     */
    private static final List<String> ESCAPE_HATCHES = List.of(
            "no production java code changed",
            "not reproducible from an exercise",
            "no mode-specific behaviour changed",
            "no improvement");

    public static void main(String[] args) throws IOException {
        System.exit(run());
    }

    private static int run() throws IOException {
        String template;
        try {
            template = normalise(Files.readString(TEMPLATE, StandardCharsets.UTF_8));
        } catch (IOException error) {
            System.out.println("::error::cannot read " + TEMPLATE + ": " + error.getMessage());
            return 1;
        }

        List<String> required = headings(template);
        if (required.isEmpty()) {
            System.out.println("::error::" + TEMPLATE + " declares no '## ' headings; nothing to enforce");
            return 1;
        }

        String body = normalise(System.getenv().getOrDefault("PR_BODY", ""));
        List<String> problems = new ArrayList<>();

        if (visible(body).isEmpty()) {
            problems.add("The pull request body is empty. Start from " + TEMPLATE + " and fill in every section.");
            report(problems, required);
            return 1;
        }

        List<String> present = new ArrayList<>();
        for (String heading : required) {
            if (body.contains(heading)) {
                present.add(heading);
            } else {
                problems.add("Missing section heading: '" + heading + "'");
            }
        }

        for (Map.Entry<String, String> section : sections(body, present).entrySet()) {
            problems.addAll(inspect(section.getKey(), section.getValue()));
        }

        report(problems, required);
        return problems.isEmpty() ? 0 : 1;
    }

    /** Collects the problems in a single section, or an empty list when it is well formed. */
    private static List<String> inspect(String heading, String content) {
        List<String> problems = new ArrayList<>();
        String prose = visible(content);

        if (prose.isEmpty()) {
            problems.add("Section '" + heading
                    + "' is empty. The template states what to write when it does not apply.");
            return problems;
        }

        String lowered = prose.toLowerCase(Locale.ROOT);
        for (String hatch : ESCAPE_HATCHES) {
            if (lowered.contains(hatch)) {
                return problems;
            }
        }

        String scannable = FENCED_BLOCK.matcher(HTML_COMMENT.matcher(content).replaceAll("")).replaceAll("");
        for (String line : scannable.split("\n", -1)) {
            if (BARE_LIST_MARKER.matcher(line).matches()) {
                problems.add("Section '" + heading + "' still contains an unfilled list stub ('"
                        + line.trim() + "' with nothing after it).");
                break;
            }
        }
        for (String line : scannable.split("\n", -1)) {
            if (EMPTY_TABLE_ROW.matcher(line).matches()) {
                problems.add("Section '" + heading + "' still contains the empty template table row. "
                        + "Fill it in, or use the documented escape hatch.");
                break;
            }
        }
        return problems;
    }

    /** The '## ' headings of a document, in order, trimmed. */
    private static List<String> headings(String text) {
        List<String> found = new ArrayList<>();
        Matcher matcher = HEADING.matcher(text);
        while (matcher.find()) {
            found.add(matcher.group().trim());
        }
        return found;
    }

    /** Splits text into heading to body, for the given headings only, in document order. */
    private static Map<String, String> sections(String text, List<String> wanted) {
        List<int[]> bounds = new ArrayList<>();
        List<String> names = new ArrayList<>();
        Matcher matcher = HEADING.matcher(text);
        while (matcher.find()) {
            String heading = matcher.group().trim();
            if (wanted.contains(heading)) {
                bounds.add(new int[] { matcher.start(), matcher.end() });
                names.add(heading);
            }
        }

        Map<String, String> result = new LinkedHashMap<>();
        for (int index = 0; index < names.size(); index++) {
            int end = index + 1 < bounds.size() ? bounds.get(index + 1)[0] : text.length();
            result.put(names.get(index), text.substring(bounds.get(index)[1], end));
        }
        return result;
    }

    /** The prose a reader actually sees: HTML comments removed, surrounding whitespace trimmed. */
    private static String visible(String text) {
        return HTML_COMMENT.matcher(text).replaceAll("").trim();
    }

    /**
     * Normalises line endings. The repository checks Markdown out with CRLF (see .gitattributes)
     * and GitHub delivers pull request bodies with CRLF, so every pattern here would otherwise have
     * to tolerate a stray carriage return.
     */
    private static String normalise(String text) {
        return text.replace("\r\n", "\n").replace("\r", "\n");
    }

    private static void report(List<String> problems, List<String> required) throws IOException {
        List<String> lines = new ArrayList<>();
        if (problems.isEmpty()) {
            lines.add("## Pull request template check passed");
            lines.add("");
            lines.add("All " + required.size() + " required sections are present and filled in.");
            System.out.println("All " + required.size() + " required sections are present and filled in.");
        } else {
            lines.add("## Pull request template check failed");
            lines.add("");
            lines.add("The body does not follow [" + TEMPLATE + "](" + TEMPLATE + "). It declares "
                    + required.size() + " required sections.");
            lines.add("");
            for (String problem : problems) {
                lines.add("- " + problem);
                System.out.println("::error::" + problem);
            }
            lines.add("");
            lines.add("Copy the template into the body, fill in every section, and the check re-runs "
                    + "automatically when the description is edited.");
        }

        String summaryPath = System.getenv("GITHUB_STEP_SUMMARY");
        if (summaryPath != null && !summaryPath.isBlank()) {
            Files.writeString(Path.of(summaryPath), String.join("\n", lines) + "\n", StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
    }
}
