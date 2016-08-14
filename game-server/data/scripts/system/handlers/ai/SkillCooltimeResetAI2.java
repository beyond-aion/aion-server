package ai;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AI2Request;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.custom.pvpmap.PvpMapService;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.model.skill.PlayerSkillList;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_COOLDOWN;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.action.Action;
import com.aionemu.gameserver.skillengine.action.DpUseAction;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.skillengine.model.TransformType;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author Yeats
 * @modified Neon
 */
@AIName("customcdreset")
public class SkillCooltimeResetAI2 extends NpcAI2 {

	private Map<Integer, Long> playersInSight = new ConcurrentHashMap<>();
	private final int price = 50000; // = 50.000 Kinah
	private final int maxCooldownTime = 3000; // = 5min -> skills with a cd >5min are ignored

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (PvpMapService.getInstance().isOnPvPMap(getOwner())) {
			getOwner().getController().addTask(TaskId.DESPAWN,
				ThreadPoolManager.getInstance().schedule(() -> getOwner().getController().onDelete(), 30000));
			ThreadPoolManager.getInstance().schedule(() -> {
				getOwner().getKnownList().forEachPlayer(p -> {
					if (p.getLifeStats().isAlreadyDead() || !getOwner().canSee(p) || playersInSight.containsKey(p.getObjectId()))
						return;
					if (MathUtil.isIn3dRange(getOwner(), p, 8) && GeoService.getInstance().canSee(getOwner(), p)) {
						playersInSight.put(p.getObjectId(), System.currentTimeMillis());
						PacketSendUtility.sendPacket(p, new SM_MESSAGE(getOwner(),
							String.format("I can heal you and reset your skill cooldowns for %,d Kinah, yang yang.", price), ChatType.NPC));
					}
				});
			}, 1000);
		}
	}

	@Override
	protected void handleDialogStart(Player player) {
		playersInSight.values().removeIf(time -> System.currentTimeMillis() > time + 300000); // remove players if they are already 5 mins+ in the map
		if (player.getLifeStats().isAboutToDie() || player.getLifeStats().isAlreadyDead())
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_USE_IN_DEAD_STATE());
		else if (!PvpMapService.getInstance().isOnPvPMap(player) && player.getController().isInCombat())
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CANT_CAST_IN_COMBAT_STATE());
		else if (player.isTransformed() && player.getTransformModel().getType() == TransformType.AVATAR)
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_ACT_WHILE_IN_ABNORMAL_STATE());
		else if (player.getSkillCoolDowns().isEmpty() || checkCooldowns(player))
			PacketSendUtility.sendPacket(player, new SM_MESSAGE(getOwner(), "Daeva has no skill cooldowns to reset, yang.", ChatType.NPC));
		else
			sendRequest(player);
	}

	@Override
	public void handleCreatureMoved(Creature creature) {
		if (!(creature instanceof Player))
			return;

		if (creature.getLifeStats().isAlreadyDead())
			return;

		if (!getOwner().canSee(creature))
			return;

		if (!getOwner().getActiveRegion().isMapRegionActive())
			return;

		if (playersInSight.containsKey(creature.getObjectId()))
			return;

		if (MathUtil.isIn3dRange(getOwner(), creature, 8) && GeoService.getInstance().canSee(getOwner(), creature)) {
			playersInSight.put(creature.getObjectId(), System.currentTimeMillis());
			PacketSendUtility.sendPacket((Player) creature,
				new SM_MESSAGE(getOwner(), String.format("I can heal you and reset your skill cooldowns for %,d Kinah, yang yang.", price), ChatType.NPC));
		}
	}

	private boolean checkCooldowns(Player player) {
		PlayerSkillList skillList = player.getSkillList();
		for (PlayerSkillEntry skill : skillList.getAllSkills()) {
			SkillTemplate st = DataManager.SKILL_DATA.getSkillTemplate(skill.getSkillId());
			if (st != null && st.getCooldown() <= maxCooldownTime && (player.getSkillCoolDown(st.getCooldownId()) - System.currentTimeMillis()) > 0) {
				return false;
			}
		}
		return true;
	}

	private void sendRequest(final Player player) {
		int distance = 5;
		AI2Actions.addRequest(this, player, 1300765, getObjectId(), distance, new AI2Request() {

			@Override
			public void acceptRequest(Creature requester, Player responder, int requestId) {
				if (!MathUtil.isIn3dRange(requester, responder, distance))
					PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_WAREHOUSE_TOO_FAR_FROM_NPC());
				else if (responder.getInventory().getKinah() < price)
					PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_KINA(price));
				else if (player.getLifeStats().isAboutToDie() || player.getLifeStats().isAlreadyDead())
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_USE_IN_DEAD_STATE());
				else if (player.getController().isInCombat())
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CANT_CAST_IN_COMBAT_STATE());
				else if (responder.isTransformed() && responder.getTransformModel().getType() == TransformType.AVATAR)
					PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_ACT_WHILE_IN_ABNORMAL_STATE());
				else {
					Map<Integer, Long> resetSkillCoolDowns = new HashMap<>();

					PlayerSkillList skillList = responder.getSkillList();
					for (PlayerSkillEntry skill : skillList.getAllSkills()) {
						skillId = skill.getSkillId();
						SkillTemplate st = DataManager.SKILL_DATA.getSkillTemplate(skillId);

						if (st != null && st.getCooldown() <= maxCooldownTime) {
							if (!st.isDeityAvatar()) {
								boolean hasDpAction = false;

								if (st.getActions() != null) {
									for (Action ac : st.getActions().getActions()) {
										if (ac instanceof DpUseAction) {
											hasDpAction = true;
											break;
										}
									}
								}

								if (!hasDpAction) {
									if ((responder.getSkillCoolDown(st.getCooldownId()) - System.currentTimeMillis()) > 0) {
										resetSkillCoolDowns.put(st.getCooldownId(), System.currentTimeMillis());
									}
									responder.removeSkillCoolDown(st.getCooldownId());
								}
							}
						}
					}

					if (resetSkillCoolDowns.size() > 0) {
						if (responder.getInventory().tryDecreaseKinah(price)) {
							responder.getLifeStats().increaseHp(SM_ATTACK_STATUS.TYPE.HP, responder.getLifeStats().getMaxHp(), 0, SM_ATTACK_STATUS.LOG.REGULAR);
							responder.getLifeStats().increaseMp(SM_ATTACK_STATUS.TYPE.HEAL_MP, responder.getLifeStats().getMaxMp(), 0, SM_ATTACK_STATUS.LOG.MPHEAL);
							PacketSendUtility.sendPacket(responder, new SM_SKILL_COOLDOWN(resetSkillCoolDowns));
							if (PvpMapService.getInstance().isOnPvPMap(getOwner())) {
								getOwner().getController().onDelete();
							}
						} else {
							PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_KINA(price));
						}
					}
				}
			}
		});
	}
}
