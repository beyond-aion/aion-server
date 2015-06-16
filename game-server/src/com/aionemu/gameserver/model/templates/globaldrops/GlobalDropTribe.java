package com.aionemu.gameserver.model.templates.globaldrops;

import com.aionemu.gameserver.model.TribeClass;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author AionCool
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropTribe")
public class GlobalDropTribe {

    @XmlAttribute(name = "tribe", required = true)
    protected TribeClass tribe;
 
    public TribeClass getTribe() {
		return tribe;
    }
}
