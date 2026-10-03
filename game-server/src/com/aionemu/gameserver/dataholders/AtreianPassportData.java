package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.model.templates.event.AtreianPassport;

/**
 * @author Alcapwnd, ViAl
 */
@XmlRootElement(name = "login_events")
@XmlAccessorType(XmlAccessType.FIELD)
public class AtreianPassportData {

	@XmlElement(name = "login_event")
	private List<AtreianPassport> list;

	@XmlTransient
	private Map<Integer, AtreianPassport> passportData = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (AtreianPassport passport : list) {
			passportData.put(passport.getId(), passport);
		}
		list = null;
	}

	public int size() {
		return passportData.size();
	}

	public Map<Integer, AtreianPassport> getAll() {
		return passportData;
	}

	public AtreianPassport getAtreianPassportId(int id) {
		return passportData.get(id);
	}

}
