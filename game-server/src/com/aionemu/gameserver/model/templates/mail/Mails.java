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
@XmlType(name = "", propOrder = { "sysMailTemplates" })
@XmlRootElement(name = "mails")
public class Mails {

	@XmlElement(name = "mail")
	private List<SysMail> sysMailTemplates;

	@XmlTransient
	private Map<String, SysMail> sysMailByName = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (SysMail template : sysMailTemplates) {
			String sysMailName = template.getName().toLowerCase();
			sysMailByName.put(sysMailName, template);
		}
		sysMailTemplates = null;
	}

	public MailTemplate getMailTemplate(String name, String eventName, Race playerRace) {
		SysMail template = sysMailByName.get(name.toLowerCase());
		if (template == null)
			return null;
		return template.getTemplate(eventName, playerRace);
	}

	public int size() {
		return sysMailByName.values().size();
	}

}
