package com.aionemu.commons.configuration.transformers;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.commons.configuration.TransformationTypeInfo;

/**
 * @author Neon
 */
public abstract class CommaSeparatedValueTransformer<T> extends PropertyTransformer<T> {

	@Override
	protected final T parseObject(String value, TransformationTypeInfo typeInfo) throws Exception {
		return parseObject(splitAndTrimValues(value), typeInfo);
	}

	protected abstract T parseObject(List<String> value, TransformationTypeInfo typeInfo) throws Exception;

	/**
	 * Modified version of http://stackoverflow.com/a/24078092<br>
	 * Splits strings on every comma outside quotes.<br>
	 * Example: {@code "a,b,\"c,d\", e , \" f \" " = ["a", "b", "c,d", "e", " f "]}
	 * 
	 * @return List of trimmed strings, separated by comma. Leading+trailing quotes are removed if both were present.
	 */
	protected final List<String> splitAndTrimValues(String value) {
		List<String> tokensList = new ArrayList<>();
		boolean inQuotes = false;
		StringBuilder b = new StringBuilder(value.length());
		for (char c : value.toCharArray()) {
			switch (c) {
				case ',':
					if (inQuotes)
						break;
					tokensList.add(trim(b));
					b.setLength(0);
					continue;
				case '\"':
					inQuotes = !inQuotes;
			}
			b.append(c);
		}
		String lastValue = trim(b);
		if (!lastValue.isEmpty()) // don't add empty strings if it's the only element (no comma present) or if it's the last one
			tokensList.add(lastValue);
		return tokensList;
	}

	private String trim(StringBuilder input) {
		String output = input.toString().trim();
		// strip quotes if string starts AND ends with one
		if (output.length() > 1 && output.charAt(0) == '\"' && output.charAt(output.length() - 1) == '\"')
			output = output.substring(1, output.length() - 1);
		return output;
	}
}
