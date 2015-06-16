package com.aionemu.gameserver.model.templates.item.purification;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ranastic
 * @rework Navyan
 */
@XmlRootElement(name = "NeedKinah")
@XmlAccessorType(XmlAccessType.FIELD)
public class NeedKinah {

    @XmlAttribute(name = "count")
    private int count;

    /**
     * @return the count
     */
    public int getCount() {
        return count;
    }
}
