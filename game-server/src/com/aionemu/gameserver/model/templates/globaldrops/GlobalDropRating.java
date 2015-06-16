package com.aionemu.gameserver.model.templates.globaldrops;

import com.aionemu.gameserver.model.templates.npc.NpcRating;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author AionCool
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropRating")
public class GlobalDropRating {

    @XmlAttribute(name = "rating", required = true)
    protected NpcRating rating;
 
    public NpcRating getRating() {
        return rating;
    }
}
