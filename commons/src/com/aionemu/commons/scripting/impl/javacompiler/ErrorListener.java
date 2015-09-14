package com.aionemu.commons.scripting.impl.javacompiler;

import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is simple compiler error listener that forwards errors to log4j logger
 * 
 * @author SoulKeeper
 */
public class ErrorListener implements DiagnosticListener<JavaFileObject> {

	private static final Logger log = LoggerFactory.getLogger(ErrorListener.class);

	/**
	 * Reports compilation errors to log4j
	 * 
	 * @param diagnostic
	 *          compiler errors
	 */
	@Override
	public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
		StringBuilder sb = new StringBuilder();
		sb.append("Java Compiler ");
		sb.append(diagnostic.getKind());
		sb.append(": ");
		sb.append(diagnostic.getMessage(Locale.ENGLISH));
		if (diagnostic.getSource() != null) {
			sb.append("\n");
			sb.append("Source: ");
			sb.append(diagnostic.getSource().getName());
			sb.append("\n");
			sb.append("Line: ");
			sb.append(diagnostic.getLineNumber());
			sb.append("\n");
			sb.append("Column: ");
			sb.append(diagnostic.getColumnNumber());
		}
		log.error(sb.toString());
	}
}
