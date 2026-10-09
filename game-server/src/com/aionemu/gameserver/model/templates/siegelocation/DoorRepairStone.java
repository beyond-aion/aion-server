package com.aionemu.gameserver.model.templates.siegelocation;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DoorRepairStone")
public class DoorRepairStone {

    @XmlAttribute(name = "static_id")
    protected int staticId;
    @XmlAttribute(name = "door_id")
    protected int doorId;

    public int getStaticId() {
        return staticId;
    }

    public int getDoorId() {
        return doorId;
    }
}
