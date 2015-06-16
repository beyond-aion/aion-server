package com.aionemu.gameserver.model.templates.item.purification;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ranastic
 * @rework Navyan
 */
@XmlRootElement(name = "PurificationResultItem")
@XmlAccessorType(XmlAccessType.FIELD)
public class PurificationResultItem {

    @XmlAttribute(name = "item_id")
    private int item_id;
    @XmlAttribute(name = "check_enchant_count")
    private int check_enchant_count;
    private RequiredMaterials required_materials;
    private NeedAbyssPoint abyss_point_needed;
    private NeedKinah kinah_needed;

    /**
     * @return the check_enchant_count
     */
    public int getCheck_enchant_count() {
        return check_enchant_count;
    }

    /**
     * @return the item_id
     */
    public int getItem_id() {
        return item_id;
    }

    /**
     * @return the required_materials
     */
    public RequiredMaterials getUpgrade_materials() {
        return required_materials;
    }

    /**
     * @return the abyss_point_needed
     */
    public NeedAbyssPoint getNeed_abyss_point() {
        return abyss_point_needed;
    }

    /**
     * @return the kinah_needed
     */
    public NeedKinah getNeed_kinah() {
        return kinah_needed;
    }
}
