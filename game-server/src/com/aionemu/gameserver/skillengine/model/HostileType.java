package com.aionemu.gameserver.skillengine.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "HostileType")
@XmlEnum
public enum HostileType {
    NONE,
    DIRECT,
    INDIRECT
}
