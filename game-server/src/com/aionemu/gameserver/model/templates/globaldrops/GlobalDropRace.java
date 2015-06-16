package com.aionemu.gameserver.model.templates.globaldrops;

import com.aionemu.gameserver.model.Race;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author AionCool
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropRace")
public class GlobalDropRace {

    @XmlAttribute(name = "race", required = true)
    protected Race race;
 
 	public Race getRace() {
        return race;
    }
}
