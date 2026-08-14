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
 * <p>The description arrives in {@code PR_BODY}. An outsider writes it on a fork, so it must never
 * be interpolated into a shell command. The rules are in {@link #SECTIONS}, not the template, so a
 * heading, a limit or a whole-section phrase changed in one file has to change in the other too.
 *
 * <p>It checks shape, not quality, and never asks for a box to be ticked.
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
     * An empty string means it has no limit, or no phrase. A LinkedHashMap, because Map.of promises no
     * order, and this one is read as the order sections come in.
     */
    private static final Map<String, List<String>> SECTIONS;

    static {
        Map<String, List<String>> sections = new LinkedHashMap<>();
        sections.put("## Summary", List.of("500", ""));
        sections.put("## Linked issues", List.of("1000", "No linked issues"));
        sections.put("## 1. Problem", List.of("1000", ""));
        sections.put("## 2. Improvement from the user's perspective",
                List.of("1000", "No Improvement from the user's perspective"));
        sections.put("## 3. Improvement from the maintainer's perspective",
                List.of("1000", "No Improvement from the maintainer's perspective"));
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
     * The two block constructs whose insides are not markup for this check: an HTML comment, which never
     * shows on the page, and a fenced code block. Shared by the two patterns below so that both know a
     * block by the same rules.
     */
    private static final String BLOCK_ALTERNATIVES =
            "(?<comment><!--.*?(?:-->|\\z))"
                    // A fence closes only on a run of its own character at least as long as the one
                    // that opened it, which is how a longer fence shows a shorter one. The opening
                    // run cannot be given back, or an inner shorter run would close it early, and
                    // what follows the opening backticks may hold none, as CommonMark has it.
                    + "|^ {0,3}(?<backticks>`{3,}+)[^`\\n]*\\n.*?(?:^ {0,3}\\k<backticks>`*[ \\t]*$|\\z)"
                    + "|^ {0,3}(?<tildes>~{3,}+)[^\\n]*\\n.*?(?:^ {0,3}\\k<tildes>~*[ \\t]*$|\\z)";

    /**
     * Those two and code between backticks, which is the third thing whose insides are not markup here.
     * Code still counts towards a section's length, but a comment marker, a heading or a numbered blank
     * inside it is an example rather than markup, and obeying it would hide text or invent a section
     * nobody wrote. One pattern finds all three, so the one starting first wins.
     */
    private static final Pattern NOT_PROSE = Pattern.compile(BLOCK_ALTERNATIVES
                    // Guarded on both sides, so two backticks cannot close on the last two of three,
                    // and stopped at a blank line or a heading, so a stray one cannot swallow them.
                    // The opening run cannot be given back either: a run that never closes must cost
                    // one scan, since an outsider writes this text on a fork pull request.
                    + "|(?<!`)(?<span>`++)(?:(?!\\n(?:[ \\t]*\\n| {0,3}#{1,6}(?:[ \\t]|$))).)+?(?<!`)\\k<span>(?!`)",
            // A fence indented against its container rather than the margin, inside a list or a
            // quote, is not recognised: reading those means tracking each container, a Markdown parser.
            Pattern.DOTALL | Pattern.MULTILINE);

    /**
     * The same without code between backticks, so that a span cannot swallow a fence or a comment
     * that starts inside it. Skipping spans after matching them would not do: the scan resumes past
     * the whole span, so whatever it covered is never looked at again.
     */
    private static final Pattern BLOCKS = Pattern.compile(BLOCK_ALTERNATIVES,
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
        List<String> problems = new ArrayList<>(miswrittenSectionList());
        for (Map.Entry<String, List<String>> section : SECTIONS.entrySet()) {
            problems.addAll(miswrittenSection(section.getKey(), section.getValue()));
        }
        return problems;
    }

    /**
     * Whether this file lists any section at all. An empty list would pass every description, silently,
     * since a check with nothing to require has nothing to complain about.
     */
    private static List<String> miswrittenSectionList() {
        if (!SECTIONS.isEmpty()) {
            return List.of();
        }
        return List.of("This check is miswritten: it requires no sections at all, so it would pass any "
                + "body. Fix CheckPullRequestTemplate.java, not the pull request body.");
    }

    /**
     * The complaints about one entry of {@link #SECTIONS}: its heading, then its two rules. The rules are
     * read only when there are exactly two, which is the shape {@link #inspect} expects of them.
     */
    private static List<String> miswrittenSection(String heading, List<String> rules) {
        List<String> problems = new ArrayList<>(miswrittenHeading(heading));
        if (rules.size() != 2) {
            problems.add("This check is miswritten: the rules for '" + heading + "' are not the two "
                    + "expected, a limit and a phrase, either of which may be empty (found "
                    + rules.size() + "). Fix CheckPullRequestTemplate.java, not the pull request body.");
            return problems;
        }
        problems.addAll(miswrittenLimit(heading, rules.get(0)));
        problems.addAll(miswrittenPhrase(heading, rules.get(1)));
        return problems;
    }

    /**
     * Whether a required heading is one this program could ever find in a description. A heading it cannot
     * match would fail every description with a missing section nobody can do anything about.
     */
    private static List<String> miswrittenHeading(String heading) {
        if (HEADING.matcher(heading).matches() && heading.equals(heading.trim())) {
            return List.of();
        }
        return List.of("This check is miswritten: the required heading '" + heading
                + "' is not a heading it can ever match. Fix CheckPullRequestTemplate.java, "
                + "not the pull request body.");
    }

    /**
     * Whether one section's phrase can be used. Empty means the section has none, and whitespace is
     * neither that nor a phrase.
     */
    private static List<String> miswrittenPhrase(String heading, String phrase) {
        if (phrase.isEmpty() || !phrase.isBlank()) {
            return List.of();
        }
        return List.of("This check is miswritten: the phrase for '" + heading + "' is whitespace, "
                + "which is neither a phrase nor the empty string that means it has none. Fix "
                + "CheckPullRequestTemplate.java, not the pull request body.");
    }

    /**
     * Whether one section's limit can be used. Empty means the section has no limit. Anything else must be
     * a whole number of at least one that Java can hold; a number too large to hold is reported the same
     * way as one that is not a number, since either is a mistake in this file.
     */
    private static List<String> miswrittenLimit(String heading, String declared) {
        if (declared.isEmpty()) {
            return List.of();
        }
        int limit;
        try {
            limit = Integer.parseInt(declared);
        } catch (NumberFormatException error) {
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
     * instruction notes this program recognises are removed, so keeping those costs nothing, and in
     * Unicode code points, so a character outside the basic set counts once rather than twice.
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
     * The complaints about blanks the template shipped and nobody filled in. Read from a copy with
     * the comments and fenced blocks painted over, so their contents are not mistaken for a blank
     * somebody forgot. Code between backticks stays: a step is often a number and a command, and
     * painting the command out would leave what an unfilled blank looks like.
     */
    private static List<String> leftoverStubs(String heading, String content) {
        String scannable = blocksOnly(content);
        List<String> problems = new ArrayList<>();
        String stub = firstLineMatching(scannable, TESTING_MANUAL_STUB);
        if (stub != null) {
            problems.add("Section '" + heading + "' still contains an unfilled list stub ('"
                    + stub.trim() + "' with nothing after it).");
        }
        if (firstLineMatching(scannable, TEST_CASE_COVERAGE_STUB) != null) {
            problems.add("Section '" + heading + "' still contains the empty template table row. "
                    + "Fill it in, or use the documented escape hatch.");
        }
        return problems;
    }

    /**
     * The first line of a text that is nothing but the given pattern, or null when no line is. Whole lines,
     * because a blank the template shipped stands on its own; a number in a sentence is not one.
     */
    private static String firstLineMatching(String text, Pattern pattern) {
        for (String line : text.split("\n", -1)) {
            if (pattern.matcher(line).matches()) {
                return line;
            }
        }
        return null;
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
     * cut out of the original at the same places, so a heading inside a note it recognises, or shown
     * in a fenced example, is not taken for a real one.
     */
    private static String masked(String text) {
        return painted(text, NOT_PROSE);
    }

    /**
     * The same, except that code between backticks is left alone. A line that is a number and a
     * command, {@code 1. `mvn test`}, is a step somebody wrote, and painting its command out would
     * leave a number and a full stop, which is what an unfilled blank looks like. The same goes for
     * a table row whose cells hold code.
     */
    private static String blocksOnly(String text) {
        return painted(text, BLOCKS);
    }

    /**
     * The text with everything the given pattern matches replaced by spaces of its own length, the
     * line breaks kept. A comment on a line indented four columns is left alone, since a reader
     * sees it there.
     */
    private static String painted(String text, Pattern pattern) {
        StringBuilder result = new StringBuilder(text);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            if (matcher.group("comment") != null && treatedAsIndentedCode(text, matcher.start())) {
                continue;
            }
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
     * kept, since a reader sees it. This is what empty and too long are measured on, which is why the
     * template's instruction notes cost nothing when they are left in place.
     */
    private static String visible(String text) {
        StringBuilder result = new StringBuilder();
        Matcher matcher = NOT_PROSE.matcher(text);
        int cursor = 0;
        while (matcher.find()) {
            if (matcher.group("comment") == null || treatedAsIndentedCode(text, matcher.start())) {
                continue;
            }
            result.append(text, cursor, matcher.start());
            cursor = matcher.end();
        }
        result.append(text, cursor, text.length());
        return result.toString().trim();
    }

    /**
     * Whether this place is preceded on its line by four columns of nothing but indentation, counting a
     * tab to the next multiple of four. This check then treats a comment marker there as code shown to
     * a reader rather than as markup. It approximates Markdown, which also asks what the line above
     * was, so four columns under a paragraph are called code here where Markdown continues the
     * paragraph and keeps the comment hidden.
     */
    private static boolean treatedAsIndentedCode(String text, int place) {
        int column = 0;
        for (int index = text.lastIndexOf(10, place - 1) + 1; index < place; index++) {
            char character = text.charAt(index);
            if (character == 32) {
                column++;
            } else if (character == 9) {
                column += 4 - column % 4;
            } else {
                return false;
            }
            if (column >= 4) {
                return true;
            }
        }
        return false;
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
        List<String> lines = problems.isEmpty() ? passed(required) : failed(problems, required);
        problems.forEach(problem -> System.out.println("::error::" + problem));
        if (problems.isEmpty()) {
            System.out.println("All " + required.size() + " required sections are present and filled in.");
        }
        writeSummary(lines);
    }

    /**
     * The summary written when the description is in order.
     */
    private static List<String> passed(List<String> required) {
        return List.of("## Pull request template check passed", "",
                "All " + required.size() + " required sections are present and filled in.");
    }

    /**
     * The summary written when it is not: what the body should follow, then every complaint, then how to
     * put it right. Editing the description is the fix, since the check runs again when it is edited.
     */
    private static List<String> failed(List<String> problems, List<String> required) {
        List<String> lines = new ArrayList<>(List.of("## Pull request template check failed", "",
                "The body does not follow [" + TEMPLATE + "](" + TEMPLATE + "). It declares "
                        + required.size() + " required sections.", ""));
        problems.forEach(problem -> lines.add("- " + problem));
        lines.add("");
        lines.add("Copy the template into the body, fill in every section, and the check re-runs "
                + "automatically when the description is edited.");
        return lines;
    }

    /**
     * Appends the summary to the page GitHub shows for the job, when the workflow gives one to write to.
     * Run from a terminal there is none, and the {@code ::error::} lines have already said everything.
     */
    private static void writeSummary(List<String> lines) throws IOException {
        String summaryPath = System.getenv("GITHUB_STEP_SUMMARY");
        if (summaryPath == null || summaryPath.isBlank()) {
            return;
        }
        Files.writeString(Path.of(summaryPath), String.join("\n", lines) + "\n", StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
