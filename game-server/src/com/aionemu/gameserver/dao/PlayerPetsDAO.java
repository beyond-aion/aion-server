package com.aionemu.gameserver.dao;

import java.util.List;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.PetCommonData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.pet.PetDopingBag;

/**
 * @author Xitanium, Kamui, Rolandas
 */
public abstract class PlayerPetsDAO implements DAO {

	@Override
	public final String getClassName() {
		return PlayerPetsDAO.class.getName();
	}

	public abstract void insertPlayerPet(PetCommonData petCommonData);

	public abstract void removePlayerPet(Player player, int petId);

	public abstract void updatePetName(PetCommonData petCommonData);

	public abstract List<PetCommonData> getPlayerPets(Player player);

	public abstract void setTime(Player player, int petId, long time);
	
	public abstract void saveFeedStatus(Player player, int petId, int hungryLevel, int feedProgress, long reuseTime);

	public abstract boolean savePetMoodData(PetCommonData petCommonData);
	
	public abstract void saveDopingBag(Player player, int petId, PetDopingBag bag);
}
