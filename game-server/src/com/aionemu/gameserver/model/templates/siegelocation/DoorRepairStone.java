package com.aionemu.gameserver.model.templates.siegelocation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

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
