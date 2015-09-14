package com.aionemu.gameserver.model.templates.globaldrops;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Bobobear
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropExcludedNpcs")
public class GlobalDropExcludedNpcs {

    @XmlElement(name = "gd_excluded_npc")
    protected List<GlobalDropExcludedNpc> gdExcludedNpcs;

    public List<GlobalDropExcludedNpc> getGlobalDropExcludedNpcs() {
        if (gdExcludedNpcs == null) {
            gdExcludedNpcs = new ArrayList<GlobalDropExcludedNpc>();
        }
        return this.gdExcludedNpcs;
    }

}
