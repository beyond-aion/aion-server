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

	public abstract void loadPlayerPunishments(final Player player, final PunishmentType punishmentType);

	public abstract void storePlayerPunishments(final Player player, final PunishmentType punishmentType);
	
	public abstract void punishPlayer(final int playerId, final PunishmentType punishmentType, final long expireTime, final String reason);

	public abstract void punishPlayer(final Player player, final PunishmentType punishmentType, final String reason);

	public abstract void unpunishPlayer(final int playerId, final PunishmentType punishmentType);
	
	public abstract CharacterBanInfo getCharBanInfo(final int playerId);
}
