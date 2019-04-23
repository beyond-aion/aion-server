package com.aionemu.gameserver.model.templates.mail;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ MailPart.class })
public class StringParamList {

	@XmlElement(name = "param")
	protected List<Param> params;

	public List<Param> getParams() {
		return params == null ? Collections.emptyList() : params;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "")
	public static class Param {

		@XmlAttribute(name = "id", required = true)
		protected String id;

		public String getId() {
			return id;
		}
	}

}
