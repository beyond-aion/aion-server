package com.aionemu.gameserver.model.templates.item.purification;

import com.aionemu.gameserver.model.stats.calc.StatOwner;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Ranastic
 * @rework Navyan
 */
@XmlRootElement(name = "ItemPurification")
@XmlAccessorType(XmlAccessType.FIELD)
public class ItemPurificationTemplate implements StatOwner {

    protected List<PurificationResultItem> purification_result_item;
    @XmlAttribute(name = "base_item")
    private int purification_base_item_id;

    /**
     * @param u
     * @param parent
     */
    void afterUnmarshal(Unmarshaller u, Object parent) {
    }

    /**
     * @return the purification_result_item
     */
    public List<PurificationResultItem> getPurification_result_item() {
        return purification_result_item;
    }

    /**
     * @return the purification_base_item_id
     */
    public int getPurification_base_item_id() {
        return purification_base_item_id;
    }
}
