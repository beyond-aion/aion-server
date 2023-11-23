package com.aionemu.gameserver.model.templates.mail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.Race;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MailTemplate")
public class MailTemplate {

	@XmlElements({ @XmlElement(name = "sender", type = Sender.class), @XmlElement(name = "title", type = Title.class),
		@XmlElement(name = "header", type = Header.class), @XmlElement(name = "body", type = Body.class), @XmlElement(name = "tail", type = Tail.class) })
	private List<MailPart> mailParts;

	@XmlAttribute(name = "name", required = true)
	protected String name;

	@XmlAttribute(name = "race", required = true)
	protected Race race;

	@XmlTransient
	private Map<MailPartType, MailPart> mailPartsMap = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (MailPart part : mailParts) {
			mailPartsMap.put(((IMailFormatter) part).getType(), part);
		}
		mailParts = null;
	}

	public MailPart getSender() {
		return mailPartsMap.get(MailPartType.SENDER);
	}

	public MailPart getTitle() {
		return mailPartsMap.get(MailPartType.TITLE);
	}

	public MailPart getHeader() {
		return mailPartsMap.get(MailPartType.HEADER);
	}

	public MailPart getBody() {
		return mailPartsMap.get(MailPartType.BODY);
	}

	public MailPart getTail() {
		return mailPartsMap.get(MailPartType.TAIL);
	}

	public String getName() {
		return name;
	}

	public Race getRace() {
		return race;
	}

	public String getFormattedTitle(IMailFormatter customFormatter) {
		return getTitle().getFormattedString(customFormatter);
	}

	public String getFormattedMessage(IMailFormatter customFormatter) {
		String headerStr = getHeader().getFormattedString(customFormatter);
		String bodyStr = getBody().getFormattedString(customFormatter);
		String tailStr = getTail().getFormattedString(customFormatter);
		String message = headerStr;
		if (message.isEmpty())
			message = bodyStr;
		else if (!bodyStr.isEmpty()) {
			message += "," + bodyStr;
		}
		if (message.isEmpty())
			message = tailStr;
		else if (!tailStr.isEmpty()) {
			message += "," + tailStr;
		}
		return message;
	}

}
