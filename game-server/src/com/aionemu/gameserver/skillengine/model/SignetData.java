package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "signet_data")
@XmlAccessorType(XmlAccessType.FIELD)
public class SignetData {

    @XmlAttribute(name = "lvl", required = true)
    private int level;
    @XmlAttribute(name = "add_effect_prob", required = true)
    private int addEffectProb = 1;
    @XmlAttribute(name = "dmg_multi", required = true)
    private float damageMultiplier;

    public int getLevel() {
        return level;
    }

    public int getAddEffectProb() {
        return addEffectProb;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }
}
