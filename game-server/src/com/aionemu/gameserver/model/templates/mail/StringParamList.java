package com.aionemu.gameserver.model.templates.mail;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StringParamList", propOrder = { "param" })
@XmlSeeAlso({ MailPart.class })
public class StringParamList {

	protected List<StringParamList.Param> param;

	public List<StringParamList.Param> getParam() {
		if (param == null)
			param = new ArrayList<>();
		return this.param;
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
