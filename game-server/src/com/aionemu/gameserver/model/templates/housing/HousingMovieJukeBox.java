package com.aionemu.gameserver.model.templates.housing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HousingMovieJukeBox")
public class HousingMovieJukeBox extends HousingJukeBox {

	@Override
	public byte getTypeId() {
		return 0; // unknown
	}
}
