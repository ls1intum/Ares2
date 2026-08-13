import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks that a pull request description follows {@code .github/PULL_REQUEST_TEMPLATE.md}.
 *
 * <p>Run with {@code java .github/scripts/CheckPullRequestTemplate.java}, which needs no build step.
 * The description arrives in the {@code PR_BODY} environment variable. On a pull request from a fork
 * an outsider writes that text, so it must never be put into a shell command.
 *
 * <p>The rules live in {@link #SECTIONS} and the template says the same things in prose to whoever
 * fills it in. This program never reads the template, so <b>a section renamed, added, removed or
 * given a different limit or whole-section phrase has to change in both files in the same
 * commit</b>. Nothing notices if they drift apart.
 *
 * <p>It checks shape, not quality: every section present, once, in order, not empty, not too long,
 * and no blank left where the template shipped one. It does not ask for the boxes to be ticked,
 * which would only teach people to tick them.
 */
public class CheckPullRequestTemplate {

    /**
     * Where the template lives. It only names the file in the messages, so a contributor knows which one
     * to copy. This program never opens it: the rules are written out in {@link #SECTIONS} instead.
     */
    private static final String TEMPLATE = ".github/PULL_REQUEST_TEMPLATE.md";

    /**
     * Every section a description must have, in the order the template puts them, each with two rules: how
     * many characters it may hold, and the phrase that answers the whole section when it does not apply.
     * An empty string means it has no limit, or no phrase. One ordered map, so a rule cannot name a
     * section nobody requires, and so the order below is the order sections are expected in.
     */
    private static final Map<String, List<String>> SECTIONS;

    static {
        // A LinkedHashMap, because Map.of does not promise any order at all and the order check
        // reads this one.
        Map<String, List<String>> sections = new LinkedHashMap<>();
        sections.put("## Summary", List.of("500", ""));
        sections.put("## Linked issues", List.of("1000", "No linked issues"));
        sections.put("## 1. Problem", List.of("1000", ""));
        sections.put("## 2. Improvement from the user's perspective",
                List.of("1000", "No Improvement from the user's perspective"));
        sections.put("## 3. Improvement from the maintainer's perspective",
                List.of("1000", "No Improvement from the maintainer's perspective"));
        // This section has no whole-section phrase, and neither of the two the template offers here
        // belongs in that slot. "Not reproducible from an exercise" answers Steps and "No
        // mode-specific behaviour changed" answers the modes, both of which sit inside this
        // section, so neither may switch off the length and stub checks for the whole of it.
        // Nothing here requires a section to have steps at all; the slot is empty because these
        // phrases do not answer the section, not because a body saying only one of them is caught.
        sections.put("## 4. Testing manual", List.of("5000", ""));
        sections.put("## 5. Test case coverage regarding this PR", List.of("", "No production Java code changed"));
        sections.put("## Breaking changes and migration", List.of("1000", "No breaking changes or migration"));
        sections.put("## Checklist", List.of("", ""));
        sections.put("## Review progress", List.of("", ""));
        SECTIONS = Collections.unmodifiableMap(sections);
    }

    /** A section heading: a line that starts with {@code ## } and has something after it. */
    private static final Pattern HEADING = Pattern.compile("^## .+$", Pattern.MULTILINE);

    /**
     * Three things whose insides must not be read as ordinary text: an HTML comment, which never shows on
     * the page, a fenced code block, and code between backticks. One pattern finds all three, so that
     * whichever starts first wins, because each can hold another's opening marks. A comment or a fence
     * that is never closed runs to the end of the text, which is what GitHub shows.
     */
    private static final Pattern NOT_PROSE = Pattern.compile(
            "(?<comment><!--.*?(?:-->|\\z))"
                    // A fence closes only on a run of its own character at least as long as the one
                    // that opened it, which is how a longer fence shows a shorter one. The opening
                    // run cannot be given back, or an inner shorter run would close it early, and
                    // what follows the opening backticks may hold none, as CommonMark has it.
                    + "|^ {0,3}(?<backticks>`{3,}+)[^`\\n]*\\n.*?(?:^ {0,3}\\k<backticks>`*[ \\t]*$|\\z)"
                    + "|^ {0,3}(?<tildes>~{3,}+)[^\\n]*\\n.*?(?:^ {0,3}\\k<tildes>~*[ \\t]*$|\\z)"
                    // Guarded on both sides, so two backticks cannot close on the last two of three,
                    // and stopped at a blank line or a heading, so a stray one cannot swallow them.
                    // The opening run cannot be given back either: a run that never closes must cost
                    // one scan, since an outsider writes this text on a fork pull request.
                    + "|(?<!`)(?<span>`++)(?:(?!\\n(?:[ \\t]*\\n| {0,3}#{1,6}(?:[ \\t]|$))).)+?(?<!`)\\k<span>(?!`)",
            // Code indented four spaces, or fenced inside a list or a quote, is not recognised:
            // reading those means tracking the block above each line, which is a Markdown parser.
            Pattern.DOTALL | Pattern.MULTILINE);

    /**
     * The numbered blank the testing manual ships under Prerequisites and under Steps, left as it came: a
     * line holding a number and a full stop and nothing else. Looked for in every section, since a
     * leftover is a leftover wherever it sits.
     */
    private static final Pattern TESTING_MANUAL_STUB = Pattern.compile("\\s*\\d+\\.\\s*");

    /**
     * The blank row the coverage table ships, left as it came: a row whose cells are all empty. The line of
     * dashes under the header never matches, because dashes are not empty. Looked for in every section
     * too.
     */
    private static final Pattern TEST_CASE_COVERAGE_STUB = Pattern.compile("\\s*\\|(\\s*\\|)+\\s*");

    /**
     * Runs the check and exits with the verdict: 0 if it passed, 1 if either the description or this
     * file's own rules failed it. That number is what makes the check green or red.
     */
    public static void main(String[] args) throws IOException {
        System.exit(run());
    }

    /**
     * The whole check, in order: this file's own rules first, then the description, then the headings it
     * has, then each section against its rules. It gathers complaints rather than stopping at the first,
     * so one edit can answer several, but it gives up early if this file's own rules are broken or the
     * description is empty.
     */
    private static int run() throws IOException {
        List<String> required = List.copyOf(SECTIONS.keySet());
        List<String> problems = new ArrayList<>(miswritten());
        if (!problems.isEmpty()) {
            report(problems, required);
            return 1;
        }

        String body = normalise(System.getenv().getOrDefault("PR_BODY", ""));

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
        problems.addAll(missing(required, found));
        problems.addAll(duplicated(required, found));
        problems.addAll(outOfOrder(required, found));

        for (Map.Entry<String, String> section : sections(body, maskedBody, once(required, found)).entrySet()) {
            String heading = section.getKey();
            problems.addAll(inspect(heading, section.getValue(), SECTIONS.get(heading)));
        }

        report(problems, required);
        return problems.isEmpty() ? 0 : 1;
    }

    /**
     * The required headings the description does not have. Compared against the headings it really has, so
     * one quoted inside a sentence does not count as the section it names.
     */
    private static List<String> missing(List<String> required, List<String> found) {
        List<String> problems = new ArrayList<>();
        for (String heading : required) {
            if (!found.contains(heading)) {
                problems.add("Missing section heading: '" + heading + "'");
            }
        }
        return problems;
    }

    /**
     * The required headings the description has more than once. Of two, only the last would be looked at,
     * which would leave everything above it, its length included, unchecked.
     */
    private static List<String> duplicated(List<String> required, List<String> found) {
        List<String> problems = new ArrayList<>();
        for (String heading : required) {
            int occurrences = Collections.frequency(found, heading);
            if (occurrences > 1) {
                problems.add("Section heading '" + heading + "' appears " + occurrences
                        + " times. Keep exactly one of each, in the order of " + TEMPLATE + ".");
            }
        }
        return problems;
    }

    /**
     * The required headings that appear exactly once, which are the only ones worth cutting a section out
     * for. A missing one has nothing to cut, and a repeated one has been complained about already.
     */
    private static List<String> once(List<String> required, List<String> found) {
        List<String> usable = new ArrayList<>();
        for (String heading : required) {
            if (Collections.frequency(found, heading) == 1) {
                usable.add(heading);
            }
        }
        return usable;
    }

    /**
     * The complaints about this file rather than about a description: no sections listed at all, a heading
     * it could never find, a section not stating exactly a limit and a phrase, a limit that is not a whole
     * number of at least one that Java can hold, or a phrase of nothing but whitespace. Checked before any
     * description is judged, and every message says which file to fix.
     */
    private static List<String> miswritten() {
        List<String> problems = new ArrayList<>();
        if (SECTIONS.isEmpty()) {
            problems.add("This check is miswritten: it requires no sections at all, so it would pass any "
                    + "body. Fix CheckPullRequestTemplate.java, not the pull request body.");
        }
        for (Map.Entry<String, List<String>> section : SECTIONS.entrySet()) {
            String heading = section.getKey();
            List<String> rules = section.getValue();
            if (!HEADING.matcher(heading).matches() || !heading.equals(heading.trim())) {
                problems.add("This check is miswritten: the required heading '" + heading
                        + "' is not a heading it can ever match. Fix CheckPullRequestTemplate.java, "
                        + "not the pull request body.");
            }
            if (rules.size() != 2) {
                problems.add("This check is miswritten: the rules for '" + heading + "' are not the two "
                        + "expected, a limit and a phrase, either of which may be empty (found "
                        + rules.size() + "). Fix CheckPullRequestTemplate.java, not the pull request body.");
                continue;
            }
            problems.addAll(miswrittenLimit(heading, rules.get(0)));
            String phrase = rules.get(1);
            if (!phrase.isEmpty() && phrase.isBlank()) {
                problems.add("This check is miswritten: the phrase for '" + heading + "' is whitespace, "
                        + "which is neither a phrase nor the empty string that means it has none. Fix "
                        + "CheckPullRequestTemplate.java, not the pull request body.");
            }
        }
        return problems;
    }

    /**
     * Whether one section's limit can be used. Empty means the section has no limit. Anything else must be
     * a whole number of at least one, and small enough for Java to hold.
     */
    private static List<String> miswrittenLimit(String heading, String declared) {
        if (declared.isEmpty()) {
            return List.of();
        }
        int limit;
        try {
            limit = Integer.parseInt(declared);
        } catch (NumberFormatException error) {
            // Also the answer for a number too large to hold, which is a mistake either way: a
            // section bounded above two billion characters is a section nobody meant to bound.
            return List.of("This check is miswritten: the limit '" + declared + "' for '" + heading
                    + "' is not a whole number this program can hold. Fix CheckPullRequestTemplate.java, "
                    + "not the pull request body.");
        }
        if (limit < 1) {
            return List.of("This check is miswritten: the limit " + limit + " for '" + heading
                    + "' is not a length any section could keep to. Write an empty limit for a section "
                    + "that has none. Fix CheckPullRequestTemplate.java, not the pull request body.");
        }
        return List.of();
    }

    /**
     * The problems in one section, or none. Four questions in the order they stop mattering: an empty
     * section is only empty; a section that says just its phrase is finished; anything else has to fit its
     * limit, and must not still hold the blanks the template shipped.
     */
    private static List<String> inspect(String heading, String content, List<String> rules) {
        String prose = visible(content);
        if (prose.isEmpty()) {
            return List.of("Section '" + heading
                    + "' is empty. The template states what to write when it does not apply.");
        }
        if (answered(prose, rules.get(1))) {
            return List.of();
        }

        List<String> problems = new ArrayList<>(tooLong(heading, prose, rules.get(0)));
        problems.addAll(leftoverStubs(heading, content));
        return problems;
    }

    /**
     * Whether the section says its phrase and nothing else, which finishes it. A full stop at the end is
     * fine. The phrase has to be the whole answer rather than a few words inside one, and a section with
     * no phrase is never finished this way.
     */
    private static boolean answered(String prose, String phrase) {
        return !phrase.isEmpty() && stripped(prose).equalsIgnoreCase(stripped(phrase));
    }

    /** Text without the full stops and whitespace at the end, which carry no meaning here. */
    private static String stripped(String text) {
        return text.replaceAll("[.\\s]+$", "");
    }

    /**
     * The complaint about a section longer than its limit, or none. Counted on what is left once the
     * instruction notes are removed, so keeping them costs nothing, and counted in Unicode code points, so
     * that a character outside the basic set counts once rather than twice.
     */
    private static List<String> tooLong(String heading, String prose, String declared) {
        if (declared.isEmpty()) {
            return List.of();
        }
        int limit = Integer.parseInt(declared);
        int length = prose.codePointCount(0, prose.length());
        if (length <= limit) {
            return List.of();
        }
        return List.of("Section '" + heading + "' is " + length + " characters long and the limit is "
                + limit + ". Shorten it to the part a reviewer needs; detail belongs in the code, "
                + "in the testing manual or in the linked issue.");
    }

    /**
     * The complaints about blanks the template shipped and nobody filled in. Read from a copy with every
     * comment, fence and backtick run this program recognises painted over, so that their contents are
     * not mistaken for a blank somebody forgot.
     */
    private static List<String> leftoverStubs(String heading, String content) {
        List<String> problems = new ArrayList<>();
        String scannable = masked(content);
        for (String line : scannable.split("\n", -1)) {
            if (TESTING_MANUAL_STUB.matcher(line).matches()) {
                problems.add("Section '" + heading + "' still contains an unfilled list stub ('"
                        + line.trim() + "' with nothing after it).");
                break;
            }
        }
        for (String line : scannable.split("\n", -1)) {
            if (TEST_CASE_COVERAGE_STUB.matcher(line).matches()) {
                problems.add("Section '" + heading + "' still contains the empty template table row. "
                        + "Fill it in, or use the documented escape hatch.");
                break;
            }
        }
        return problems;
    }

    /**
     * The complaint about the first required section in the wrong place, or none. The sections read as a
     * story, and one that has moved sends a reader hunting for it.
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
     * The text with every comment, fence and backtick run this program recognises painted over in spaces,
     * the line breaks kept. Same length is the point: headings are found in this copy and the real text is
     * cut out of the original at the same places, so a heading inside a note, or shown in a fenced
     * example, is not taken for a real one.
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

    /** The {@code ## } headings of a text, in order, with the spaces around them trimmed. */
    private static List<String> headings(String text) {
        List<String> found = new ArrayList<>();
        Matcher matcher = HEADING.matcher(text);
        while (matcher.find()) {
            found.add(matcher.group().trim());
        }
        return found;
    }

    /**
     * Cuts a text into each wanted heading and the text under it. The headings are found in the painted
     * copy and the text is cut out of the original, which is the same length, so a place means the same
     * thing in both.
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
     * The text left once the comments this program recognises are removed, with the ends trimmed. Code is
     * kept, since a reader sees it. This is what empty and too long are measured on, which is why keeping
     * the template's instruction notes costs nothing.
     */
    private static String visible(String text) {
        StringBuilder result = new StringBuilder();
        Matcher matcher = NOT_PROSE.matcher(text);
        int cursor = 0;
        while (matcher.find()) {
            if (matcher.group("comment") == null) {
                continue;
            }
            result.append(text, cursor, matcher.start());
            cursor = matcher.end();
        }
        result.append(text, cursor, text.length());
        return result.toString().trim();
    }

    /**
     * Makes every line ending the same. GitHub sends Windows line endings and this repository stores
     * Markdown with them too, so without this the same text would measure longer than it reads.
     */
    private static String normalise(String text) {
        return text.replace("\r\n", "\n").replace("\r", "\n");
    }

    /**
     * Writes the verdict where people look: {@code ::error::} lines for GitHub's annotations when
     * something is wrong, and a short Markdown summary on the job page when the workflow provides one.
     */
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
