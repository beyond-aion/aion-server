package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "alias_pos")
public class SkillAliasPosition {

    @XmlAttribute(name = "x", required = true)
    private float x;
    @XmlAttribute(name = "y", required = true)
    private float y;
    @XmlAttribute(name = "z", required = true)
    private float z;

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }
}
