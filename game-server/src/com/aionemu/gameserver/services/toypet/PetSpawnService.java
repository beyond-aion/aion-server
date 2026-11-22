package com.aionemu.gameserver.services.toypet;

import static com.aionemu.gameserver.skillengine.effect.AbnormalState.*;

import com.aionemu.gameserver.controllers.PetController;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.PetSpecialFunction;
import com.aionemu.gameserver.model.gameobjects.player.PetCommonData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PET;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.spawnengine.VisibleObjectSpawner;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author ATracer
 */
public class PetSpawnService {

	private static final long SUMMON_COOLDOWN_MILLIS = 5000;
	private static final long MOOD_TICK_MILLIS = 60_000;
	private static final int SUMMON_BLOCKING_ABNORMALS = CANCEL_ITEM_USE.getId() | STUNLIKE.getId() | CANT_MOVE_OR_ATTACK.getId();

	public static void summonPet(Player player, int templateId) {
		if (!canSummonPet(player))
			return;

		PetCommonData petCommonData = player.getPetList().getPet(templateId);
		if (petCommonData == null) {
			AuditLogger.log(player, "tried to spawn invalid pet with id " + templateId);
			return;
		}
		if (player.getPet() != null) {
			if (player.getPet().getObjectTemplate().getTemplateId() == templateId)
				return;
			player.getPet().getController().delete();
		}
		PetMoodService.onSummon(player, petCommonData);

		Pet pet = VisibleObjectSpawner.spawnPet(player, templateId);
		if (pet == null) {
			AuditLogger.log(player, "tried to spawn invalid pet with id " + templateId);
			return;
		}
		player.getPetList().setLastSummonTime(System.currentTimeMillis());
		player.getController().addTask(TaskId.PET_UPDATE,
			ThreadPoolManager.getInstance().scheduleAtFixedRate(new PetController.PetUpdateTask(player), MOOD_TICK_MILLIS, MOOD_TICK_MILLIS));

		if (petCommonData.getRefeedDelay() > 0) {
			petCommonData.scheduleRefeed(petCommonData.getRefeedDelay());
		} else if (petCommonData.getFeedProgress() != null)
			petCommonData.getFeedProgress().setHungryLevel(PetHungryLevel.HUNGRY);
		if (petCommonData.isLooting())
			PacketSendUtility.sendPacket(player, SM_PET.specialFunction(PetSpecialFunction.AUTOLOOT, true));
		if (petCommonData.isSelling())
			PacketSendUtility.sendPacket(player, SM_PET.specialFunction(PetSpecialFunction.AUTOSELL, true));
		PacketSendUtility.sendPacket(player, SM_PET.moodUpdate(petCommonData));
	}

	/**
	 * @return True if the client allows spawning a pet right now and there are no map restrictions. A message is sent where the client does not already
	 *         send one or block the UI.
	 */
	private static boolean canSummonPet(Player player) {
		// Stricter than retail: it doesn't check death here, but a pet summoned while dead or between maps would escape the dismissal on death
		// and be spawned without its owner. Remove this check to follow retail.
		if (player.isDead() || !player.isSpawned())
			return false;
		if (System.currentTimeMillis() < player.getPetList().getLastSummonTime() + SUMMON_COOLDOWN_MILLIS)
			return false;
		if (player.isFlying() || player.isUsingFlightTransporterOrWindstream())
			return false;
		if ((player.getEffectController().getAbnormals() & SUMMON_BLOCKING_ABNORMALS) != 0)
			return false;
		if (player.getWorldId() == WorldMapType.LIVE_PARTY_CONCERT_HALL.getId()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PET_CANT_SUMMON_HERE());
			return false;
		}
		return true;
	}

}
