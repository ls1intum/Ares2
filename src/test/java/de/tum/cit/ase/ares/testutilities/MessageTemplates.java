package de.tum.cit.ase.ares.testutilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Reads a message back against its catalogue text, so a test can check that a
 * message has exactly the shape of its key and look at each filled-in value, in
 * whatever language {@link Messages} currently uses.
 */
public final class MessageTemplates {

	/** Marks where a value stands; no real message contains it. */
	private static final String MARK = "\0";

	/** Finds a value marker and the position of its value. */
	private static final Pattern MARKER = Pattern.compile(MARK + "(\\d+)" + MARK);

	/** Not to be created: only the static method is used. */
	private MessageTemplates() {
	}

	/**
	 * Reads the values out of a text written from a catalogue key.
	 *
	 * @param key       the catalogue key the text should come from
	 * @param arguments how many values the key takes
	 * @param text      the text to read
	 * @return the values in the key's argument order, {@code null} for one the text
	 *         does not show, or empty when the text does not have the key's shape
	 */
	public static Optional<List<String>> argumentsOf(String key, int arguments, String text) {
		Object[] markers = IntStream.rangeClosed(1, arguments).mapToObj(index -> MARK + index + MARK).toArray();
		String rendered = Messages.localized(key, markers);
		StringBuilder regex = new StringBuilder("(?s)");
		List<Integer> order = new ArrayList<>();
		Matcher marker = MARKER.matcher(rendered);
		int end = 0;
		while (marker.find()) {
			regex.append(Pattern.quote(rendered.substring(end, marker.start()))).append("(.+?)");
			order.add(Integer.parseInt(marker.group(1)));
			end = marker.end();
		}
		regex.append(Pattern.quote(rendered.substring(end)));
		Matcher match = Pattern.compile(regex.toString()).matcher(text);
		if (!match.matches()) {
			return Optional.empty();
		}
		List<String> values = new ArrayList<>(Collections.nCopies(arguments, (String) null));
		for (int group = 1; group <= order.size(); group++) {
			values.set(order.get(group - 1) - 1, match.group(group));
		}
		return Optional.of(values);
	}
}
