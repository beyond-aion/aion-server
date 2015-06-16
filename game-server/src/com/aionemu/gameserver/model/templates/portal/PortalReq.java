package com.aionemu.gameserver.model.templates.portal;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.configs.main.GSConfig;
/**
 *
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PortalReq")
public class PortalReq {

    @XmlElement(name = "quest_req")
    protected List<QuestReq> questReq;
    @XmlElement(name = "item_req")
    protected List<ItemReq> itemReq;
    @XmlAttribute(name = "min_level")
    protected int minLevel;
    @XmlAttribute(name = "max_level")
    protected int maxLevel = GSConfig.PLAYER_MAX_LEVEL;
    @XmlAttribute(name = "kinah_req")
    protected int kinahReq;
	@XmlAttribute(name = "title_id")
    protected int titleId;
	@XmlAttribute(name = "err_level")
    protected int errLevel;

    public List<QuestReq> getQuestReq() {
        return this.questReq;
    }

    public List<ItemReq> getItemReq() {
        return this.itemReq;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(int value) {
        this.minLevel = value;
    }

    public int getMaxLevel() {
		return maxLevel;
    }

    public void setMaxLevel(int value) {
        this.maxLevel = value;
    }

    public int getKinahReq() {
        return kinahReq;
    }

    public void setKinahReq(int value) {
        this.kinahReq = value;
    }

	public int getTitleId() {
		return titleId;
	}

	public int getErrLevel() {
		return errLevel;
	}
}