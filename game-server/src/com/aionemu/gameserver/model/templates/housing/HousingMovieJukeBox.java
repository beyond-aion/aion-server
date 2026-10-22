package com.aionemu.gameserver.model.templates.housing;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

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
