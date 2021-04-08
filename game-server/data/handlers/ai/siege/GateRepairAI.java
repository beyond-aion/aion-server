package ai.siege;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.*;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.animations.ActionAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.team.legion.LegionPermissionsMask;
import com.aionemu.gameserver.model.templates.siegelocation.DoorRepairData;
import com.aionemu.gameserver.model.templates.siegelocation.DoorRepairStone;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ACTION_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.siege.SiegeException;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Source
 */
@AIName("siege_gaterepair")
public class GateRepairAI extends NpcAI {

	private static final Logger log = LoggerFactory.getLogger("SIEGE_LOG");
	private AtomicLong nextActivationTime = new AtomicLong(0);
	public GateRepairAI(Npc owner) {
		super(owner);
	}

	@Override
	protected SiegeSpawnTemplate getSpawnTemplate() {
		return (SiegeSpawnTemplate) super.getSpawnTemplate();
	}

	@Override
	protected void handleDialogStart(final Player player) {
		DoorRepairData repairData = SiegeService.getInstance().getDoorRepairData(getSpawnTemplate().getSiegeId());
		if (repairData == null)
			return;

		RequestResponseHandler<Npc> gaterepair = new RequestResponseHandler<Npc>(getOwner()) {
			@Override
			public void acceptRequest(Npc requester, Player responder) {
				RequestResponseHandler<Npc> repairstone = new RequestResponseHandler<Npc>(requester) {

					@Override
					public void acceptRequest(Npc requester, Player responder) {
						if (canActivate(responder, repairData)) {
							onActivate(responder, repairData);
						}
					}

				};
				if (player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_ASK_DOOR_REPAIR_DO_YOU_ACCEPT_REPAIR, repairstone)) {
					PacketSendUtility.sendPacket(player, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_ASK_DOOR_REPAIR_DO_YOU_ACCEPT_REPAIR,
							getObjectId(), 5,  DataManager.ITEM_DATA.getItemTemplate(repairData.getItemId()).getL10n(), repairData.getCount()));
				}
			}

		};
		if (player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_ASK_DOOR_REPAIR_POPUPDIALOG, gaterepair))
			PacketSendUtility.sendPacket(player, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_ASK_DOOR_REPAIR_POPUPDIALOG, ((SiegeNpc) getOwner()).getSiegeId(), getCooldown()));
	}

	@Override
	protected void handleDialogFinish(Player player) {
	}

	public void onActivate(Player player, DoorRepairData repairData) {
		DoorRepairStone repairStone = getSpawnTemplate() == null ? null : repairData.getRepairStone(getSpawnTemplate().getStaticId());
		if (repairStone == null)
			return;
		if (getPosition().getWorldMapInstance().getObjectByStaticId(repairStone.getDoorId()) instanceof Creature door) {
			int healValue = (int) Math.round(door.getLifeStats().getMaxHp() * SiegeConfig.DOOR_REPAIR_HEAL_PERCENT);
			if (door.getLifeStats().getCurrentHp() + healValue > door.getLifeStats().getMaxHp()) {
				healValue = door.getLifeStats().getMaxHp() - door.getLifeStats().getCurrentHp();
			}

			if (LoggingConfig.LOG_SIEGE)
				log.info("Gate Repair Stone with staticId: " + getSpawnTemplate().getStaticId() + " siege: " + getSpawnTemplate().getSiegeId() + " activated by " + player + " (race: " + player.getRace() + ") to heal door with staticId: " + (door.getSpawn().getStaticId()) + " by " + healValue);
			nextActivationTime.set(System.currentTimeMillis() + repairData.getCd());
			PacketSendUtility.broadcastPacket(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_REPAIR_ABYSS_DOOR(player.getName(), "" + healValue));
			PacketSendUtility.broadcastPacket(getOwner(), new SM_ACTION_ANIMATION(getObjectId(), ActionAnimation.REPAIR_GATE, door.getObjectId()));
			door.getLifeStats().increaseHp(SM_ATTACK_STATUS.TYPE.DOOR_REPAIR, healValue);
		} else {
			throw new SiegeException("Could not find a door to repair for siege " + getSpawnTemplate().getSiegeId() + " for npc_id = " + getNpcId() + " with static_id = " + getSpawnTemplate().getStaticId());
		}
	}

	private int getCooldown() {
		return nextActivationTime.get() <= 0 ? 0 : (int) Math.max(0, (nextActivationTime.get() - System.currentTimeMillis())/1000);
	}

	private boolean canActivate(Player player, DoorRepairData repairData) {
		FortressLocation loc  = SiegeService.getInstance().getFortress(getSpawnTemplate().getSiegeId());
		if (getCooldown() > 0) {
			PacketSendUtility.sendPacket(player, STR_CANNOT_USE_DOOR_REPAIR_OUT_OF_COOLTIME());
			return false;
		}
		if (loc.getRace().getRaceId() != player.getRace().getRaceId()) {
			return false;
		}
		if (loc.getLegionId() > 0 && (!player.isLegionMember() || player.getLegion().getLegionId() != loc.getLegionId()
				|| !player.getLegionMember().hasRights(LegionPermissionsMask.ARTIFACT))) {
			PacketSendUtility.sendPacket(player, STR_CANNOT_USE_DOOR_REPAIR_HAVE_NO_AUTHORITY());
		} else if (player.getInventory().getItemCountByItemId(repairData.getItemId()) >= repairData.getCount() && player.getInventory().decreaseByItemId(repairData.getItemId(), repairData.getCount())) {
			return true;
		} else {
			PacketSendUtility.sendPacket(player, STR_CANNOT_USE_DOOR_REPAIR_NOT_ENOUGH_FEE(DataManager.ITEM_DATA.getItemTemplate(repairData.getItemId()).getL10n(), "" + repairData.getCount()));
		}
		return false;
	}
}
