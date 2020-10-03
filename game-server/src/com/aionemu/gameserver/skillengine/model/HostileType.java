package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "HostileType")
@XmlEnum
public enum HostileType {
    NONE,
    DIRECT,
    INDIRECT
}
