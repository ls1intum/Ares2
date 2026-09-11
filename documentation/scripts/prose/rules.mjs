/**
 * The writing rules this repository holds its documentation to, and the line between the
 * ones a machine may decide and the ones it may not.
 *
 * The rules themselves are the AET general writing rules. They were written for exercise
 * text that students read, so section "Decisions" of the contributor guide's writing-rules
 * page records how each one was translated for a two-audience reference site.
 *
 * Every rule carries a level:
 *
 *   - `enforced` fails `pnpm run lint:prose`. A rule may only be enforced when its
 *     forbidden form is decidable from the text alone, without knowing what the author
 *     meant. `doesn't` is always a contraction; there is no sentence in which it is not.
 *   - `advisory` is reported and never fails. `may` is permission, possibility or
 *     uncertainty depending on the sentence around it, and "the analysis may report false
 *     positives" is correct English that a lexical rule cannot tell from a violation.
 *     Turning that into `must` would state a guarantee the code does not make, which in
 *     documentation for a security tool is worse than the style problem it fixes.
 *
 * Promoting a rule from advisory to enforced is a decision about the standard, not about
 * the tool. It belongs in the writing-rules page first.
 */

/** Words the reader sees as a contraction, whatever the apostrophe character. */
const CONTRACTIONS = [
    'don', 'doesn', 'isn', 'aren', 'wasn', 'weren', 'can', 'won', 'didn', 'haven', 'hasn',
    'hadn', 'couldn', 'wouldn', 'shouldn', 'mustn', 'needn',
];

/**
 * The contraction pattern, built from the list above plus the pronoun forms, and matching
 * both the typewriter apostrophe and the typographic one, because a page can carry either
 * and a reader cannot tell them apart.
 */
const CONTRACTION_PATTERN = new RegExp(
    `\\b(?:(?:${CONTRACTIONS.join('|')})['’]t|`
        + `(?:it|that|there|here|what|who|he|she|let)['’]s|`
        + `(?:you|we|they|I)['’](?:re|ve|ll|d)|I['’]m)\\b`,
    'gi',
);

/**
 * American spellings that have one British form in every context.
 *
 * Deliberately not "British English", which is not a decidable rule: `licence` and
 * `license` are both British and differ by part of speech, so are `practice` and
 * `practise`, and `program` is the correct British spelling for software. Only spellings
 * whose British form never depends on the sentence belong here.
 *
 * `artefact` is included because this repository writes it that way in AGENTS.md and in the
 * pull request template. Where `artifact` names a Maven coordinate it sits in code, which is
 * never scanned.
 */
const AMERICAN_SPELLINGS = [
    'color', 'colors', 'colored', 'coloring',
    'behavior', 'behaviors', 'behavioral',
    'initialize', 'initializes', 'initialized', 'initializing', 'initialization',
    'organize', 'organizes', 'organized', 'organizing', 'organization',
    'analyze', 'analyzes', 'analyzed', 'analyzing',
    'recognize', 'recognizes', 'recognized', 'recognizing',
    'authorize', 'authorizes', 'authorized',
    'serialize', 'serializes', 'serialized', 'serializing', 'serialization',
    'normalize', 'normalizes', 'normalized', 'normalizing',
    'specialize', 'specializes', 'specialized',
    'customize', 'customizes', 'customized', 'customizing', 'customization',
    'optimize', 'optimizes', 'optimized', 'optimizing', 'optimization',
    'utilize', 'utilizes', 'utilized',
    'synchronize', 'synchronizes', 'synchronized',
    'minimize', 'minimizes', 'minimized',
    'maximize', 'maximizes', 'maximized',
    'prioritize', 'prioritizes', 'prioritized',
    'summarize', 'summarizes', 'summarized',
    'catalog', 'catalogs',
    'defense', 'defenses', 'offense',
    'center', 'centers', 'centered',
    'fulfill', 'fulfills', 'fulfilled',
    'modeling', 'modeled',
    'labeled', 'labeling',
    'canceled', 'canceling',
    'favor', 'favors', 'favored',
    'neighbor', 'neighbors',
    'artifact', 'artifacts',
    'gray',
];

/** The British form to suggest, where it is not simply the `s` for `z` swap. */
const BRITISH_FORM = {
    catalog: 'catalogue', defense: 'defence', offense: 'offence', center: 'centre',
    fulfill: 'fulfil', modeling: 'modelling', modeled: 'modelled', labeled: 'labelled',
    labeling: 'labelling', canceled: 'cancelled', canceling: 'cancelling', favor: 'favour',
    neighbor: 'neighbour', artifact: 'artefact', gray: 'grey', color: 'colour',
    behavior: 'behaviour',
};

/** Returns the British spelling to suggest for one matched American one. */
export function britishFormOf(word) {
    const lower = word.toLowerCase();
    for (const [american, british] of Object.entries(BRITISH_FORM)) {
        if (lower.startsWith(american)) {
            return british + lower.slice(american.length);
        }
    }
    return lower.replace(/z/g, 's');
}

/**
 * The rules that match a word or phrase anywhere in eligible prose.
 *
 * `allow` holds the exceptions, each with the reason it exists. An exception is a named
 * phrase, never a general escape: the moment one is written as "anything containing X" it
 * has become an undocumented classifier of its own.
 */
export const WORD_RULES = [
    {
        id: 'no-contractions',
        level: 'enforced',
        pattern: CONTRACTION_PATTERN,
        message: (hit) => `"${hit}" is a contraction. Write it out.`,
        allow: [],
    },
    {
        id: 'no-american-spellings',
        level: 'enforced',
        pattern: new RegExp(`\\b(?:${AMERICAN_SPELLINGS.join('|')})\\b`, 'gi'),
        message: (hit) => `"${hit}" is the American spelling. Write "${britishFormOf(hit)}".`,
        allow: [
            // The Gradle feature is called a version catalog, and renaming it in prose would
            // send a reader looking for something Gradle does not have.
            /version catalog/i,
            // The standard's own name, which the rules exempt as an API name.
            /unified modeling language/i,
        ],
    },
    {
        id: 'no-always-filler',
        // The Outline rules give one list of filler words and call it absolute. Every word on
        // that list is here: `additional`, `furthermore`, `moreover`, `also`, `actually`,
        // `clearly` and `obviously`. The first three were enforced from the start; `also` and
        // `actually` sat under `context-filler` until the corpus was clear of them, because
        // enforcing a word 139 places still used it would have meant a baseline instead of a
        // rule.
        //
        // Where the word carries meaning, the sentence gets a word that carries it plainly:
        // `and` for a list entry, `further` for one more of something, `as well` or `too` for
        // an addition. None of those is filler, and none needs a suppression.
        level: 'enforced',
        pattern: /\b(?:additionally|additional|of\s+course|furthermore|moreover|also|actually|obviously|clearly)\b/gi,
        message: (hit) => `"${hit}" adds nothing. Delete it, or say what it means: "and" for `
            + 'another entry, "further" for one more, "as well" for an addition.',
        allow: [],
    },
    {
        id: 'canonical-terms',
        level: 'enforced',
        pattern: /\b(?:restricted|trusted)\s+code\b/gi,
        message: (hit) => (/^restricted/i.test(hit)
            ? `"${hit}" is the same concept as "student code". Write "student code".`
            : `"${hit}" is the same concept as "test code". Write "test code".`),
        // "Supervised code" is deliberately absent. It names the package boundary Ares
        // reserves for itself, which is its own concept with its own section, not a third
        // name for student code.
        allow: [],
    },
    {
        id: 'prefer-must',
        level: 'advisory',
        pattern: /\b(?:should|may)\b/gi,
        message: (hit) => `"${hit}" states an obligation weakly. Where the behaviour is `
            + 'guaranteed, write "must". Where it is a possibility, leave it and suppress this.',
        allow: [],
    },
    {
        id: 'present-tense',
        level: 'advisory',
        pattern: /\b(?:will|would)\b/gi,
        message: (hit) => `"${hit}" is not the present tense. Where the sentence describes `
            + 'what happens, write it in the present. Where it is a counterfactual, leave it.',
        allow: [],
    },
    {
        id: 'context-filler',
        level: 'advisory',
        // `also`, `actually` and `additional` moved to the enforced list above once the corpus
        // was clear of them. What is left is the words that genuinely turn on the sentence.
        pattern: /\b(?:just|simply|basically|essentially|in fact)\b/gi,
        message: (hit) => `"${hit}" is usually filler. Delete it unless it carries meaning, `
            + 'as "just" does when it means "a moment ago".',
        allow: [
            // The compiler is named after the phrase, so the "just" in it is not a word the
            // sentence could do without.
            /just-in-time/i,
        ],
    },
    {
        id: 'address-the-reader',
        level: 'advisory',
        pattern: /\b(?:we|our|ours|us)\b/gi,
        message: (hit) => `"${hit}" addresses the reader as a group. Write "you", meaning `
            + 'whoever this guide is for. Where it means the Ares project itself, leave it.',
        allow: [
            // The country in the name of a data-protection framework, not the first person.
            /EU-US/i,
        ],
    },
    {
        id: 'no-intensifiers',
        level: 'advisory',
        pattern: /\b(?:very|extremely|highly|really|quite|optimal|optimally|best|greatest|worst|perfect|perfectly)\b/gi,
        message: (hit) => `"${hit}" is an intensifier or a superlative. Say the thing plainly.`,
        allow: [
            // A term of art for a deliberately incomplete guarantee, not a superlative.
            /best-effort/i,
        ],
    },
];

/** The openers a sentence may not start with, because they bury the subject. */
export const OPENER_RULE = {
    id: 'no-back-loaded-opener',
    level: 'enforced',
    pattern: /^(As|Since|To|In order to|Because)\b/,
    message: (hit) => `A sentence opens with "${hit}". Open with the subject instead.`,
};

/**
 * Words that end in `ed` or `en` and are not participles.
 *
 * The pattern below cannot conjugate; it recognises a suffix. Without this list "the grass is
 * green" and "it is often wrong" are reported as passive, which is a rule reporting something
 * nobody wrote.
 */
const NOT_PARTICIPLES = [
    'green', 'often', 'open', 'even', 'seven', 'keen', 'golden', 'wooden', 'sudden', 'hidden',
    'token', 'when', 'then', 'children', 'women', 'garden', 'red', 'indeed', 'unseen',
];

/**
 * The passive constructions that hide who acts, reported and never failed.
 *
 * A form of "to be" followed by a past participle is passive far more often than not, but not
 * always: "is interested" and "is related" are adjectives wearing the same clothes, and this
 * pattern cannot tell them apart. It is therefore advisory, like every other rule that needs
 * the sentence read.
 *
 * Deliberately narrow. Only the finite forms are matched, so "has been dropped" and "may be
 * blocked" are left alone; a rule that caught every participle in the corpus would report so
 * much that nobody would read it. The irregular participles that end in neither `ed` nor `en`,
 * "is read" and "is built" among them, are missed on purpose: recognising them means a verb
 * list, and a verb list is a dictionary this repository would then have to maintain.
 */
export const PASSIVE_RULE = {
    id: 'active-voice',
    level: 'advisory',
    pattern: new RegExp('\\b(?:is|are|was|were)\\s+(?:\\w+ly\\s+)?'
        + `(?!(?:${NOT_PARTICIPLES.join('|')})\\b)\\w{2,}(?:ed|en)\\b`, 'gi'),
    message: (hit) => `"${hit}" is passive and does not say who acts. Name the actor, or leave `
        + 'it where the actor is genuinely unknown.',
    allow: [
        // Participles this corpus uses as adjectives, naming a state rather than an act.
        // "A path is forbidden" says what the path is, not that somebody forbade it just now,
        // and rewriting it to name an actor states a event that never happened.
        new RegExp('\\b(?:is|are|was|were)\\s+(?:\\w+ly\\s+)?(?:'
            + 'allowed|permitted|forbidden|denied|required|needed|trusted|untrusted|'
            + 'based|related|interested|involved|limited|located|intended|expected|'
            + 'supported|unsupported|deprecated|sealed|hidden|nested|closed|fixed'
            + ')\\b', 'i'),
    ],
};

/**
 * The point past which a sentence is long enough to be worth re-reading, reported and never
 * failed.
 *
 * The Outline rules ask for short sentences with the verb early. Neither half is decidable,
 * but length is measurable, and a sentence over the limit is where the clause-heavy German
 * construction the rules warn about turns up. Thirty-five words is roughly two lines of this
 * documentation; the limit is a prompt to look, never a verdict.
 */
export const SENTENCE_LENGTH_RULE = {
    id: 'long-sentence',
    level: 'advisory',
    limit: 35,
    message: (words) => `A sentence runs to ${words} words, past the ${35} the rules ask for. `
        + 'Split it, or move the verb forward.',
};

/**
 * The abbreviations a page spells out the first time it uses one, and what counts as having
 * spelled it out.
 *
 * Only abbreviations appear here. The label of the admonition every page opens with,
 * `:::tip[Simple Story]`, is two ordinary words and needs nothing.
 *
 * `expansion` is matched case-insensitively anywhere on the page. `write` is the wording the
 * message suggests, so one abbreviation is not expanded twenty-seven different ways.
 */
export const ABBREVIATIONS = [
    { short: 'JVM', expansion: /java virtual machine/i, write: 'Java Virtual Machine (JVM)' },
    { short: 'WALA', expansion: /watson libraries for analysis/i, write: 'T. J. Watson Libraries for Analysis (WALA)' },
    { short: 'AOP', expansion: /aspect[- ]oriented programming/i, write: 'aspect-oriented programming (AOP)' },
    { short: 'API', expansion: /application programming interface/i, write: 'application programming interface (API)' },
    { short: 'CI', expansion: /continuous integration/i, write: 'continuous integration (CI)' },
    { short: 'JDK', expansion: /java development kit/i, write: 'Java Development Kit (JDK)' },
    { short: 'AST', expansion: /abstract syntax tree/i, write: 'abstract syntax tree (AST)' },
    { short: 'UML', expansion: /unified modeling language/i, write: 'Unified Modeling Language (UML)' },
    { short: 'DFS', expansion: /depth[- ]first search/i, write: 'depth-first search (DFS)' },
    { short: 'DNS', expansion: /domain name system/i, write: 'Domain Name System (DNS)' },
    { short: 'TLS', expansion: /transport layer security/i, write: 'Transport Layer Security (TLS)' },
    { short: 'JSON', expansion: /javascript object notation/i, write: 'JavaScript Object Notation (JSON)' },
    { short: 'CLI', expansion: /command[- ]line interface/i, write: 'command-line interface (CLI)' },
    { short: 'SVG', expansion: /scalable vector graphics/i, write: 'Scalable Vector Graphics (SVG)' },
    { short: 'IDE', expansion: /integrated development environment/i, write: 'integrated development environment (IDE)' },
];

/** The rule the abbreviation check reports under. */
export const ABBREVIATION_RULE = {
    id: 'abbreviation-first-use',
    level: 'enforced',
};

/** Every rule identifier, which is what a suppression comment has to name. */
export const RULE_IDS = new Set([
    ...WORD_RULES.map((rule) => rule.id),
    OPENER_RULE.id,
    PASSIVE_RULE.id,
    SENTENCE_LENGTH_RULE.id,
    ABBREVIATION_RULE.id,
]);

/** Whether a rule fails the build, as opposed to being reported and left to a person. */
export function isEnforced(id) {
    const standalone = [OPENER_RULE, PASSIVE_RULE, SENTENCE_LENGTH_RULE, ABBREVIATION_RULE]
        .find((rule) => rule.id === id);
    if (standalone !== undefined) {
        return standalone.level === 'enforced';
    }
    return WORD_RULES.some((rule) => rule.id === id && rule.level === 'enforced');
}

/** Every rule, whatever shape it has, paired with the level it is held at. */
export function levelsById() {
    const levels = new Map();
    for (const rule of WORD_RULES) {
        levels.set(rule.id, rule.level);
    }
    for (const rule of [OPENER_RULE, PASSIVE_RULE, SENTENCE_LENGTH_RULE, ABBREVIATION_RULE]) {
        levels.set(rule.id, rule.level);
    }
    return levels;
}
