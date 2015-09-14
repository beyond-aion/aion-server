package com.aionemu.gameserver.model.templates.mail;

/**
 * @author Rolandas
 */
public interface IMailFormatter {

	MailPartType getType();

	String getFormattedString(MailPartType partType);

	String getParamValue(String name);
}
