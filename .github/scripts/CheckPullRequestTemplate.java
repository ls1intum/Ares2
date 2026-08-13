import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
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
 * <p>The required section headings, and the character limit of the sections that carry one, are
 * read from the template itself rather than duplicated here, so editing the template cannot leave
 * this check behind. The check validates shape, not substance: that every section exists, that none
 * was left empty, that none runs past its limit, and that no unfilled stub survived. It deliberately
 * does not require checkboxes to be ticked, which would only train contributors to tick them.
 */
public class CheckPullRequestTemplate {

    private static final Path TEMPLATE = Path.of(".github/PULL_REQUEST_TEMPLATE.md");

    private static final Pattern HEADING = Pattern.compile("^## .+$", Pattern.MULTILINE);

    /**
     * The constructs whose contents are not ordinary prose: an HTML comment, a fenced code block,
     * and an inline code span. The comment and fence alternatives run to the end of the text when
     * they are never closed, which is what Markdown renders and therefore what a reader sees.
     *
     * <p>They are matched by one pattern rather than one after the other, because each can contain
     * another's opening marker and only the one that starts first is real. Matching comments first
     * would let a {@code <!--} shown in code swallow everything up to the next genuine {@code -->},
     * and the text in between would then be counted by nobody although a reader sees all of it;
     * matching code first would let a fence quoted inside a comment do the same. A single
     * left-to-right scan asks the only question that has an answer: which one starts here?
     *
     * <p>The fence alternatives follow CommonMark: up to three spaces of indent, at least three
     * backticks or tildes, closed by a run of the same character at least as long as the opening
     * one. The length matters, because the usual way to show a fenced block is to wrap it in a
     * longer fence; a closing run shorter than the opening one is content, not a close. The opening
     * run is matched possessively so that it cannot be given back to let an inner, shorter run pass
     * as the close, and a backtick fence's info string may not contain a backtick, as CommonMark
     * requires.
     *
     * <p>The inline alternative is a code span: a whole run of backticks, closed by a whole run of
     * the same length. Both ends are guarded on both sides, because a run of two that opens nothing
     * would otherwise close on the last two backticks of a run of three further down, and swallow
     * the heading in between. It comes last, so a fence opening a line is
     * read as a fence. A span may wrap onto the next line, but the search for its close stops
     * where the block it sits in does: at a blank line, and at a heading, which needs no blank line
     * to interrupt a paragraph. A stray backtick above a heading must not swallow it. Both the
     * opening run and the search for the close are written
     * so that neither can be given back. A body is untrusted input on a fork pull request, and a
     * run of backticks that never closes must cost one scan rather than an exponential one.
     */
    private static final Pattern NOT_PROSE = Pattern.compile(
            "<!--.*?(?:-->|\\z)"
                    + "|^ {0,3}(?<backticks>`{3,}+)[^`\\n]*\\n.*?(?:^ {0,3}\\k<backticks>`*[ \\t]*$|\\z)"
                    + "|^ {0,3}(?<tildes>~{3,}+)[^\\n]*\\n.*?(?:^ {0,3}\\k<tildes>~*[ \\t]*$|\\z)"
                    + "|(?<!`)(?<span>`++)(?:(?!\\n(?:[ \\t]*\\n| {0,3}#{1,6}(?:[ \\t]|$))).)+?(?<!`)\\k<span>(?!`)",
            Pattern.DOTALL | Pattern.MULTILINE);

    /** The opening of the comment alternative of {@link #NOT_PROSE}, to tell the two apart. */
    private static final String COMMENT_START = "<!--";

    private static final Pattern BARE_LIST_MARKER = Pattern.compile("\\s*\\d+\\.\\s*");

    /**
     * The character limit a template section may declare, written as {@code Limit: 1000 characters}
     * inside that section's instruction comment. A section without such a line has no limit.
     */
    private static final Pattern DECLARED_LIMIT =
            Pattern.compile("Limit:\\s*(\\S+)\\s*characters", Pattern.CASE_INSENSITIVE);

    /** Any attempt at declaring a limit, so that a malformed one is reported instead of ignored. */
    private static final Pattern LIMIT_KEYWORD = Pattern.compile("Limit:", Pattern.CASE_INSENSITIVE);

    /** No section declares a limit of zero, so zero is free to mean "unlimited" here. */
    private static final int NO_LIMIT = 0;

    /**
     * The largest limit a section may declare. Well past any section anyone would write, and low
     * enough that a typo such as an extra digit reads as the mistake it is rather than as no limit.
     */
    private static final int MAX_LIMIT = 100_000;

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

        String maskedTemplate = masked(template);
        List<String> required = headings(maskedTemplate);
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

        // Matched against the headings the body really has rather than with contains, and rejected
        // when a heading occurs twice. A heading merely quoted inside a sentence would otherwise
        // count as present while sections() never finds it, and of two real occurrences only the
        // last would be inspected, which leaves everything above it, its length included,
        // unchecked.
        String maskedBody = masked(body);
        List<String> found = headings(maskedBody);
        List<String> present = new ArrayList<>();
        for (String heading : required) {
            int occurrences = Collections.frequency(found, heading);
            if (occurrences == 1) {
                present.add(heading);
            } else if (occurrences == 0) {
                problems.add("Missing section heading: '" + heading + "'");
            } else {
                problems.add("Section heading '" + heading + "' appears " + occurrences
                        + " times. Keep exactly one of each, in the order of " + TEMPLATE + ".");
            }
        }
        problems.addAll(outOfOrder(required, found));

        Map<String, Integer> limits = limits(sections(template, maskedTemplate, required), problems);
        for (Map.Entry<String, String> section : sections(body, maskedBody, present).entrySet()) {
            String heading = section.getKey();
            problems.addAll(inspect(heading, section.getValue(), limits.getOrDefault(heading, NO_LIMIT)));
        }

        report(problems, required);
        return problems.isEmpty() ? 0 : 1;
    }

    /** Collects the problems in a single section, or an empty list when it is well formed. */
    private static List<String> inspect(String heading, String content, int limit) {
        List<String> problems = new ArrayList<>();
        String prose = visible(content);

        if (prose.isEmpty()) {
            problems.add("Section '" + heading
                    + "' is empty. The template states what to write when it does not apply.");
            return problems;
        }

        // Measured on the prose left once the instruction comments are removed, so the guidance a
        // contributor keeps in the body costs nothing, and on normalised line endings, so the same
        // text does not measure longer because GitHub delivered it with CRLF. Counted in Unicode
        // code points rather than in Java chars, so that a character outside the basic plane, an
        // emoji for instance, counts once rather than twice.
        if (limit != NO_LIMIT) {
            int length = prose.codePointCount(0, prose.length());
            if (length > limit) {
                problems.add("Section '" + heading + "' is " + length + " characters long and the limit is "
                        + limit + ". Shorten it to the part a reviewer needs; detail belongs in the code, "
                        + "in the testing manual or in the linked issue.");
            }
        }

        String lowered = prose.toLowerCase(Locale.ROOT);
        for (String hatch : ESCAPE_HATCHES) {
            if (lowered.contains(hatch)) {
                return problems;
            }
        }

        // Blanked rather than deleted, so that a '1.' left inside a fenced example, where it is
        // sample text rather than an unfilled stub, no longer reads as one: a blanked line holds no
        // digit and no table pipe, so neither stub pattern can match it.
        String scannable = masked(content);
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

    /**
     * The character limit each template section declares, for the sections that declare one. Read
     * from the raw section text rather than from {@link #visible(String)}, because the declaration
     * lives inside the instruction comment that a reader never sees.
     *
     * <p>A declaration that is present but unusable is reported rather than skipped. Skipping it
     * would fail open: the template would read as limited while nothing measured it, which is the
     * one outcome worse than having no limit at all. Such a problem is a defect of the template in
     * this repository, not of the pull request body being checked, and the message says so.
     */
    private static Map<String, Integer> limits(Map<String, String> templateSections, List<String> problems) {
        Map<String, Integer> found = new LinkedHashMap<>();
        for (Map.Entry<String, String> section : templateSections.entrySet()) {
            String heading = section.getKey();
            // Counted on the keyword, not on the readable form, so that a readable declaration next
            // to a mistyped one is reported rather than quietly deciding for the whole section.
            int attempts = 0;
            Matcher keyword = LIMIT_KEYWORD.matcher(section.getValue());
            while (keyword.find()) {
                attempts++;
            }
            if (attempts == 0) {
                continue;
            }
            if (attempts > 1) {
                problems.add(TEMPLATE + " section '" + heading + "' declares " + attempts
                        + " limits. Keep exactly one.");
                continue;
            }

            Matcher matcher = DECLARED_LIMIT.matcher(section.getValue());
            if (!matcher.find()) {
                problems.add(TEMPLATE + " section '" + heading + "' mentions a limit that this check cannot "
                        + "read. Write it as 'Limit: 1000 characters'.");
                continue;
            }
            String declared = matcher.group(1);

            int limit;
            try {
                limit = Integer.parseInt(declared);
            } catch (NumberFormatException error) {
                problems.add(TEMPLATE + " section '" + heading + "' declares the unreadable limit '" + declared
                        + "'. Write it as 'Limit: 1000 characters'.");
                continue;
            }
            if (limit < 1 || limit > MAX_LIMIT) {
                problems.add(TEMPLATE + " section '" + heading + "' declares the limit " + limit
                        + ", which is outside 1 to " + MAX_LIMIT + ".");
                continue;
            }
            found.put(heading, limit);
        }
        return found;
    }

    /**
     * The complaint about the first required section that sits in the wrong place, or an empty list
     * when the order matches the template. Sections read as a story, and a reviewer who has to hunt
     * for the testing manual above the problem it solves is reading a different document from the
     * one the template describes.
     */
    private static List<String> outOfOrder(List<String> required, List<String> found) {
        List<String> asWritten = new ArrayList<>();
        for (String heading : found) {
            if (required.contains(heading) && !asWritten.contains(heading)) {
                asWritten.add(heading);
            }
        }

        List<String> expected = new ArrayList<>(required);
        expected.retainAll(asWritten);
        for (int index = 0; index < asWritten.size(); index++) {
            if (!asWritten.get(index).equals(expected.get(index))) {
                return List.of("Section '" + asWritten.get(index) + "' is out of order: " + TEMPLATE
                        + " puts '" + expected.get(index) + "' in that place. Keep the order of the template.");
            }
        }
        return List.of();
    }

    /**
     * The text with every comment, fenced block and inline code span replaced by spaces of its own
     * length,
     * newlines kept. Headings are located in this copy and sections are then cut out of the
     * original at the same offsets, which the equal length guarantees stays exact.
     *
     * <p>Masking rather than deleting is what makes both directions safe: a heading written inside
     * a comment, or shown inside a fenced example, is no longer mistaken for a real one, while the
     * section content, and therefore the length that is measured, is still the original text.
     */
    private static String masked(String text) {
        StringBuilder result = new StringBuilder(text);
        Matcher matcher = NOT_PROSE.matcher(text);
        while (matcher.find()) {
            for (int index = matcher.start(); index < matcher.end(); index++) {
                if (result.charAt(index) != '\n') {
                    result.setCharAt(index, ' ');
                }
            }
        }
        return result.toString();
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

    /**
     * Splits text into heading to body, for the given headings only, in document order. The
     * headings are located in {@code masked}, the content is cut out of {@code text}; the two are
     * the same length, so an offset means the same thing in both.
     */
    private static Map<String, String> sections(String text, String masked, List<String> wanted) {
        List<int[]> bounds = new ArrayList<>();
        List<String> names = new ArrayList<>();
        Matcher matcher = HEADING.matcher(masked);
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

    /**
     * The text left once the comments are removed and the surrounding whitespace trimmed. A fenced
     * block is kept: a reader sees it, so it counts towards a length limit, and a {@code <!--}
     * shown inside one is text rather than the start of a comment.
     */
    private static String visible(String text) {
        StringBuilder result = new StringBuilder();
        Matcher matcher = NOT_PROSE.matcher(text);
        int cursor = 0;
        while (matcher.find()) {
            if (!text.startsWith(COMMENT_START, matcher.start())) {
                continue;
            }
            result.append(text, cursor, matcher.start());
            cursor = matcher.end();
        }
        result.append(text, cursor, text.length());
        return result.toString().trim();
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
