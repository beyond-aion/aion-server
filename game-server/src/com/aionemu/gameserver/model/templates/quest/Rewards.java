package com.aionemu.gameserver.model.templates.quest;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Rewards", propOrder = { "selectableRewardItem", "rewardItem" })
public class Rewards {

	@XmlElement(name = "selectable_reward_item")
	private List<QuestItems> selectableRewardItem;
	@XmlElement(name = "reward_item")
	private List<QuestItems> rewardItem;
	@XmlAttribute(name = "gold")
	private long kinah;
	@XmlAttribute
	private int exp;
	@XmlAttribute(name = "ap")
	private int abyssPoints;
	@XmlAttribute(name = "dp")
	private int divinePoints;
	@XmlAttribute(name = "gp")
	private int gloryPoints;
	@XmlAttribute
	private int title;
	@XmlAttribute(name = "extend_inventory")
	private int extendInventory;
	@XmlAttribute(name = "extend_stigma")
	private int extendStigma;
	@XmlAttribute(name = "ccheck")
	private List<Integer> collectItemChecks;
	@XmlAttribute(name = "icheck")
	private int inventoryItemCheck;

	public List<QuestItems> getSelectableRewardItem() {
		return selectableRewardItem == null ? Collections.emptyList() : selectableRewardItem;
	}

	public List<QuestItems> getRewardItem() {
		return rewardItem == null ? Collections.emptyList() : rewardItem;
	}

	public long getKinah() {
		return kinah;
	}

	public int getExp() {
		return exp;
	}

	public int getAp() {
		return abyssPoints;
	}

	public int getDp() {
		return divinePoints;
	}

	public int getGp() {
		return gloryPoints;
	}

	public int getTitle() {
		return title;
	}

	public int getExtendInventory() {
		return extendInventory;
	}

	public int getExtendStigma() {
		return extendStigma;
	}

	public List<Integer> getCollectItemChecks() {
		return collectItemChecks == null ? Collections.emptyList() : collectItemChecks;
	}

	public int getInventoryItemCheck() {
		return inventoryItemCheck;
	}

}
