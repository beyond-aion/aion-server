package com.aionemu.gameserver.utils.xml;

import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventHandler;
import jakarta.xml.bind.ValidationEventLocator;

/**
 * @author Rolandas
 */
public class XmlValidationHandler implements ValidationEventHandler {

	@Override
	public boolean handleEvent(ValidationEvent event) {
		if (event.getSeverity() == ValidationEvent.FATAL_ERROR || event.getSeverity() == ValidationEvent.ERROR) {
			ValidationEventLocator locator = event.getLocator();
			String message = event.getMessage();
			String file = locator.getURL() == null ? "" : "file=" + locator.getURL().toString() + ", ";
			int line = locator.getLineNumber();
			int column = locator.getColumnNumber();
			throw new RuntimeException("Error at [" + file + "line=" + line + ", column=" + column + "]: " + message, event.getLinkedException());
		}
		return true;
	}
}
