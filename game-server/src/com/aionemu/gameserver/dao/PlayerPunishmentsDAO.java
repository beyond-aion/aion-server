package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.account.CharacterBanInfo;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.PunishmentService.PunishmentType;

/**
 * @author lord_rex
 */
public abstract class PlayerPunishmentsDAO implements DAO {

	@Override
	public final String getClassName() {
		return PlayerPunishmentsDAO.class.getName();
	}

	public abstract void loadPlayerPunishments(Player player);

	public abstract void storePlayerPunishment(Player player, PunishmentType punishmentType);

	public abstract void punishPlayer(int playerId, PunishmentType punishmentType, long expireTime, String reason);

	public abstract void punishPlayer(Player player, PunishmentType punishmentType, String reason);

	public abstract void unpunishPlayer(int playerId, PunishmentType punishmentType);

	public abstract CharacterBanInfo getCharBanInfo(int playerId);
}
