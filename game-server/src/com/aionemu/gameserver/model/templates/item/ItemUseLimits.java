package com.aionemu.gameserver.model.templates.item;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UseLimits")
public class ItemUseLimits {

	@XmlAttribute(name = "usedelay")
	private int useDelay;

	@XmlAttribute(name = "usedelayid")
	private int useDelayId;

	@XmlAttribute(name = "ownership_world")
	private int ownershipWorldId;

	@XmlAttribute(name = "usearea")
	private String usearea;

	@XmlAttribute(name = "gender")
	private Gender genderPermitted;

	@XmlAttribute(name = "ride_usable")
	private Boolean rideUsable;

	@XmlAttribute(name = "rank_min")
	private int minRank;

	@XmlAttribute(name = "rank_max")
	private int maxRank = AbyssRankEnum.SUPREME_COMMANDER.getId();

	@XmlAttribute(name = "purchable_rank_min")
	private int minRankPuchable;

	@XmlAttribute(name = "recommend_rank")
	private int recommendRank;

	@XmlAttribute(name = "guild_level")
	private int guildLevel;

	@XmlAttribute(name = "pack_count")
	private int packCount = -1;

	public int getDelayId() {
		return useDelayId;
	}

	public void setDelayId(int delayId) {
		useDelayId = delayId;
	}

	public int getDelayTime() {
		return useDelay;
	}

	public void setDelayTime(int useDelay) {
		this.useDelay = useDelay;
	}

	public ZoneName getUseArea() {
		if (this.usearea == null)
			return null;

		try {
			return ZoneName.createOrGet(this.usearea);
		} catch (Exception e) {
			return null;
		}
	}

	public int getOwnershipWorld() {
		return ownershipWorldId;
	}

	public Gender getGenderPermitted() {
		return genderPermitted;
	}

	public boolean isRideUsable() {
		if (rideUsable == null)
			return false;
		return rideUsable;
	}

	public int getMinRank() {
		return minRank;
	}

	public int getMaxRank() {
		return maxRank;
	}

	public int getMinRankPurchable() {
		return minRankPuchable;
	}

	public int getRecommendRank() {
		return recommendRank;
	}

	public boolean verifyRank(int rank) {
		return minRank <= rank && maxRank >= rank;
	}

	public int getGuildLevelPermitted() {
		return guildLevel;
	}

	public int getPackCount() {
		return packCount;
	}
}
