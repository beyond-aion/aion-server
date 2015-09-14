package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.event.AtreianPassport;

/**
 * @author Alcapwnd
 * @author ViAl
 */
@XmlRootElement(name = "login_events")
@XmlAccessorType(XmlAccessType.FIELD)
public class AtreianPassportData {

	@XmlElement(name = "login_event")
	private List<AtreianPassport> list;

	private Map<Integer, AtreianPassport> passportData = new HashMap<Integer, AtreianPassport>();

	/**
	 * @param u
	 * @param parent
	 */
	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (AtreianPassport passport : list) {
			passportData.put(passport.getId(), passport);
		}
		list.clear();
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
