package com.aionemu.gameserver.model.templates.pet;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.services.toypet.PetFeedCalculator;
import com.aionemu.gameserver.services.toypet.PetFeedProgress;
import com.aionemu.gameserver.services.toypet.PetHungryLevel;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PetFlavour", propOrder = { "food" })
public class PetFlavour {

	@XmlElement(required = true)
	protected List<PetRewards> food;

	@XmlAttribute(required = true)
	protected int id;

	@XmlAttribute(name = "full_count")
	protected int fullCount = 1;

	@XmlAttribute(name = "loved_limit")
	protected int lovedFoodLimit = 0;

	@XmlAttribute(name = "cd", required = true)
	protected int cooldown = 0;

	public List<PetRewards> getFood() {
		if (food == null) {
			food = new ArrayList<>();
		}
		return this.food;
	}

	/**
	 * Returns a food group for the itemId. Null if doesn't match
	 * 
	 * @param itemId
	 */
	public FoodType getFoodType(int itemId) {
		for (PetRewards rewards : getFood()) {
			if (DataManager.ITEM_GROUPS_DATA.isFood(itemId, rewards.getType()))
				return rewards.getType();
		}
		return null;
	}

	/**
	 * Returns reward details if earned, otherwise null. Updates progress automatically
	 * 
	 * @param progress
	 * @param itemId
	 * @return
	 */
	public PetFeedResult processFeedResult(PetFeedProgress progress, FoodType foodType, int itemLevel, int playerLevel) {
		PetRewards rewardGroup = null;
		for (PetRewards rewards : getFood()) {
			if (rewards.getType() == foodType) {
				rewardGroup = rewards;
				break;
			}
		}
		if (rewardGroup == null)
			return null;

		int maxFeedCount = 1;
		if (rewardGroup.isLoved()) {
			progress.setIsLovedFeeded();
		} else {
			maxFeedCount = fullCount;
		}

		PetFeedCalculator.updatePetFeedProgress(progress, itemLevel, maxFeedCount);
		if (progress.getHungryLevel() != PetHungryLevel.FULL)
			return null;

		return PetFeedCalculator.getReward(maxFeedCount, rewardGroup, progress, playerLevel);
	}

	public boolean isLovedFood(FoodType foodType, int itemId) {
		PetRewards rewardGroup = null;
		for (PetRewards rewards : getFood()) {
			if (rewards.getType() == foodType) {
				rewardGroup = rewards;
				break;
			}
		}
		if (rewardGroup == null)
			return false;
		return rewardGroup.isLoved();
	}

	public int getId() {
		return id;
	}

	public int getFullCount() {
		return fullCount;
	}

	public int getLovedFoodLimit() {
		return lovedFoodLimit;
	}

	public int getCooldDown() {
		return cooldown;
	}

}
