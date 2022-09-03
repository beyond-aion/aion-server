package instance.pvparenas;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.instancescore.InstanceScore;
import com.aionemu.gameserver.model.instance.instancescore.PvPArenaScore;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.abyss.GloryPointsService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.DispelCategoryType;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
public abstract class PvPArenaInstance extends GeneralInstanceHandler {

	private boolean isInstanceDestroyed;
	protected PvPArenaScore instanceReward;
	protected int killBonus;
	protected int deathFine;

	public PvPArenaInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public boolean onDie(Player player, Creature lastAttacker) {
		PvPArenaPlayerReward ownerReward = getPlayerReward(player);
		ownerReward.endBoostMoraleEffect(player);
		ownerReward.applyBoostMoraleEffect(player);
		sendPacket();
		PacketSendUtility.sendPacket(player, new SM_DIE(false, false, 0, 8));

		if (lastAttacker != null && lastAttacker != player) {
			if (lastAttacker instanceof Player winner) {
				PvPArenaPlayerReward reward = getPlayerReward(winner);
				reward.addPvPKillToPlayer();

				// notify Kill-Quests
				int worldId = winner.getWorldId();
				QuestEngine.getInstance().onKillInWorld(new QuestEnv(player, winner, 0), worldId);
			}
		}

		updatePoints(player);

		return true;
	}

	private void updatePoints(Creature victim) {

		if (!instanceReward.isStartProgress()) {
			return;
		}

		int bonus;
		// Decrease victim points
		if (victim instanceof Player player) {
			PvPArenaPlayerReward victimFine = getPlayerReward(player);
			victimFine.addPoints(deathFine);
			bonus = killBonus;
			if (instanceReward.getRound() == 3 && instanceReward.getRank(victimFine.getPoints()) == 0)
				bonus *= 3;
		} else
			bonus = getNpcBonus(((Npc) victim).getNpcId());

		if (bonus == 0)
			return;

		int totalDamage = victim.getAggroList().getTotalDamage();
		// Reward all damagers
		for (AggroInfo aggroInfo : victim.getAggroList().getFinalDamageList(false)) {
			if (aggroInfo.getDamage() == 0) // e.g. when victim got killed by lava, but attacker only debuffed him without dealing damage
				continue;
			if (aggroInfo.getAttacker() instanceof Creature creature && creature.getMaster() instanceof Player attacker) {
				int rewardPoints = bonus * aggroInfo.getDamage() / totalDamage;
				getPlayerReward(attacker).addPoints(rewardPoints);
				sendSystemMsg(attacker, victim, rewardPoints);
			}
		}
		if (instanceReward.hasCapPoints()) {
			instanceReward.setInstanceProgressionType(InstanceProgressionType.END_PROGRESS);
			reward();
		}
		sendPacket();
	}

	protected void sendSystemMsg(Player player, Creature creature, int rewardPoints) {
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GET_SCORE(creature.getObjectTemplate().getL10n(), rewardPoints));
	}

	@Override
	public void onDie(Npc npc) {
		if (npc.getAggroList().getMostPlayerDamage() == null) {
			return;
		}
		updatePoints(npc);
		final int npcId = npc.getNpcId();
		if (npcId == 701187 || npcId == 701188) {
			spawnRndRelics(30000);
		}
	}

	@Override
	public void onEnterInstance(Player player) {
		int objectId = player.getObjectId();
		if (instanceReward.regPlayerReward(player)) {
			getPlayerReward(player).applyBoostMoraleEffect(player);
			instanceReward.setRndPosition(objectId);
		} else {
			instanceReward.portToPosition(player);
		}
		sendPacket();
	}

	private void sendPacket(AionServerPacket packet) {
		PacketSendUtility.broadcastToMap(instance, packet);
	}

	private void spawnRndRelics(int time) {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isInstanceDestroyed && !instanceReward.isRewarded()) {
				spawn(Rnd.get(1, 2) == 1 ? 701187 : 701188, 1841.951f, 1733.968f, 300.242f, (byte) 0);
			}
		}, time);
	}

	private int getNpcBonus(int npcId) {
		switch (npcId) {
			case 701187: // Blessed Relics
			case 701188: // Cursed Relics
				return 1750;
			case 218690: // Pale Carmina
			case 218703: // Pale Carmina
			case 218716: // Pale Carmina
			case 218691: // Corrupt Casus
			case 218704: // Corrupt Casus
			case 218717: // Corrupt Casus
				return 1500;
			case 218682: // MuMu Rake Gatherer
			case 218695: // MuMu Rake Gatherer
			case 218708: // MuMu Rake Gatherer
				return 1250;
			case 218685: // Casus Manor Guard
			case 218698: // Casus Manor Guard
			case 218711: // Casus Manor Guard
			case 218687: // Casus Manor Maid
			case 218700: // Casus Manor Maid
			case 218713: // Casus Manor Maid
			case 218686: // Casus Manor Maidservant
			case 218699: // Casus Manor Maidservant
			case 218712: // Casus Manor Maidservant
				return 250;
			case 701172: // Plaza Flame Thrower
			case 701171: // Plaza Flame Thrower
			case 701170: // Plaza Flame Thrower
			case 701169: // Plaza Flame Thrower
			case 218806: // Casus Manor Chief Maid
			case 218807: // Casus Manor Chief Maid
			case 218808: // Casus Manor Chief Maid
			case 701317: // Blessed Relics
			case 701318: // Blessed Relics
			case 701319: // Blessed Relics
				return 500;
			case 218701: // Casus Manor Butler
			case 218688: // Casus Manor Butler
			case 218714: // Casus Manor Butler
				return 650;
			case 701181: // Cursed Relics
			case 701195: // Cursed Relics
			case 701209: // Cursed Relics
			case 218689: // Casus Manor Noble
			case 218702: // Casus Manor Noble
			case 218715: // Casus Manor Noble
				return 750;
			case 218683: // Black Claw Scratcher
			case 218696: // Black Claw Scratcher
			case 218709: // Black Claw Scratcher
			case 218684: // Mutated Drakan Fighter
			case 218697: // Mutated Drakan Fighter
			case 218710: // Mutated Drakan Fighter
			case 218693: // Red Sand Tog
			case 218706: // Red Sand Tog
			case 218719: // Red Sand Tog
			case 701215: // Blesed Relic
			case 701220: // Blesed Relic
			case 701225: // Blesed Relic
			case 701216: // Blesed Relic
			case 701221: // Blesed Relic
			case 701226: // Blesed Relic
				return 100;
			default:
				return 0;
		}
	}

	@Override
	public InstanceScore<?> getInstanceScore() {
		return instanceReward;
	}

	@Override
	public void onPlayerLogOut(Player player) {
		getPlayerReward(player).updateLogOutTime();
	}

	@Override
	public void onPlayerLogin(Player player) {
		getPlayerReward(player).updateBonusTime();
	}

	@Override
	public void onInstanceCreate() {
		instanceReward = new PvPArenaScore(instance);
		instanceReward.setInstanceProgressionType(InstanceProgressionType.PREPARING);
		spawnRings();
		if (!instanceReward.isSoloArena()) {
			spawnRndRelics(0);
		}
		instanceReward.setInstanceStartTime();
		ThreadPoolManager.getInstance().schedule(() -> {
			// start round 1
			if (!isInstanceDestroyed && !instanceReward.isRewarded() && canStart()) {
				instance.forEachDoor(door -> door.setOpen(true));
				sendPacket(new SM_SYSTEM_MESSAGE(1401058));
				instanceReward.setInstanceProgressionType(InstanceProgressionType.START_PROGRESS);
				sendPacket();
				ThreadPoolManager.getInstance().schedule(() -> {
					// start round 2
					if (!isInstanceDestroyed && !instanceReward.isRewarded()) {
						instanceReward.setRound(2);
						instanceReward.setRndZone();
						sendPacket();
						changeZone();
						ThreadPoolManager.getInstance().schedule(() -> {
							// start round 3
							if (!isInstanceDestroyed && !instanceReward.isRewarded()) {
								instanceReward.setRound(3);
								instanceReward.setRndZone();
								sendPacket(new SM_SYSTEM_MESSAGE(1401203));
								sendPacket();
								changeZone();
								ThreadPoolManager.getInstance().schedule(() -> {
									// end
									if (!isInstanceDestroyed && !instanceReward.isRewarded()) {
										instanceReward.setInstanceProgressionType(InstanceProgressionType.END_PROGRESS);
										reward();
										sendPacket();
									}
								}, 180000);
							}
						}, 180000);
					}
				}, 180000);
			}
		}, 120000);
	}

	private boolean canStart() {
		if (instance.getPlayersInside().size() < 2) {
			sendPacket(new SM_SYSTEM_MESSAGE(1401303));
			instanceReward.setInstanceProgressionType(InstanceProgressionType.END_PROGRESS);
			reward();
			sendPacket();
			return false;
		}
		return true;
	}

	@Override
	public void onExitInstance(Player player) {
		TeleportService.moveToInstanceExit(player, mapId, player.getRace());
	}

	protected PvPArenaPlayerReward getPlayerReward(Player player) {
		return instanceReward.getPlayerReward(player.getObjectId());
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PacketSendUtility.sendPacket(player, STR_REBIRTH_MASSAGE_ME());
		PlayerReviveService.revive(player, 100, 100, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		if (!isInstanceDestroyed) {
			instanceReward.portToPosition(player);
		}
		return true;
	}

	@Override
	public void onLeaveInstance(Player player) {
		clearDebuffs(player);
		PvPArenaPlayerReward playerReward = getPlayerReward(player);
		if (playerReward != null) {
			playerReward.endBoostMoraleEffect(player);
			instanceReward.clearPosition(playerReward.getPosition(), Boolean.FALSE);
			instanceReward.removePlayerReward(playerReward);
		}
	}

	private void clearDebuffs(Player player) {
		for (Effect ef : player.getEffectController().getAbnormalEffects()) {
			DispelCategoryType category = ef.getSkillTemplate().getDispelCategory();
			if (category == DispelCategoryType.DEBUFF || category == DispelCategoryType.DEBUFF_MENTAL || category == DispelCategoryType.DEBUFF_PHYSICAL
				|| category == DispelCategoryType.ALL) {
				ef.endEffect();
				player.getEffectController().clearEffect(ef);
			}
		}
	}

	protected abstract void sendPacket();

	@Override
	public void onInstanceDestroy() {
		isInstanceDestroyed = true;
		instanceReward.clear();
	}

	private void changeZone() {
		ThreadPoolManager.getInstance().schedule(() -> {
			for (Player player : instance.getPlayersInside()) {
				instanceReward.portToPosition(player);
			}
			sendPacket();
		}, 1000);
	}

	protected void reward() {
		for (Player player : instance.getPlayersInside()) {
			if (player.isDead())
				PlayerReviveService.duelRevive(player);
			PvPArenaPlayerReward reward = getPlayerReward(player);
			if (!reward.isRewarded()) {
				reward.setRewarded();
				AbyssPointsService.addAp(player, reward.getBasicAP() + reward.getRankingAP() + reward.getScoreAP());
				int gpToAdd = reward.getBasicGP() + reward.getRankingGP() + reward.getScoreGP();
				if (gpToAdd > 0)
					GloryPointsService.increaseGpBy(player.getObjectId(), gpToAdd, false, true); // already added arena rates
				int courage = reward.getBasicCourage() + reward.getRankingCourage() + reward.getScoreCourage();
				if (courage != 0) {
					ItemService.addItem(player, 186000137, courage);
				}
				int crucible = reward.getBasicCrucible() + reward.getRankingCrucible() + reward.getScoreCrucible();
				if (crucible != 0) {
					ItemService.addItem(player, 186000130, crucible);
				}
				int opportunity = reward.getOpportunity();
				if (opportunity != 0) {
					ItemService.addItem(player, 186000165, opportunity);
				}
				int gloryTicket = reward.getGloryTicket();
				if (gloryTicket != 0) {
					ItemService.addItem(player, 186000185, gloryTicket);
				}
				int ceraniumMedal = reward.getCeramiumMedal();
				if (ceraniumMedal != 0) {
					ItemService.addItem(player, 186000242, ceraniumMedal);
				}
				int mithrilMedal = reward.getMithrilMedal();
				if (mithrilMedal != 0) {
					ItemService.addItem(player, 186000147, mithrilMedal);
				}
				int platinumMedal = reward.getPlatinumMedal();
				if (platinumMedal != 0) {
					ItemService.addItem(player, 186000096, platinumMedal);
				}

				int gloriousInsignia = reward.getGloriousInsignia();
				if (gloriousInsignia != 0) {
					ItemService.addItem(player, 182213259, gloriousInsignia);
				}
				int lifeSerum = reward.getLifeSerum();
				if (lifeSerum != 0) {
					ItemService.addItem(player, 162000077, lifeSerum);
				}
			}
		}
		instance.forEachNpc(npc -> npc.getController().delete());
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isInstanceDestroyed) {
				for (Player player : instance.getPlayersInside()) {
					onExitInstance(player);
				}
			}
		}, 10000);
	}

	protected void spawnRings() {
	}

	protected Npc getNpc(float x, float y, float z) {
		if (!isInstanceDestroyed) {
			for (Npc npc : instance.getNpcs()) {
				SpawnTemplate st = npc.getSpawn();
				if (st.getX() == x && st.getY() == y && st.getZ() == z) {
					return npc;
				}
			}
		}
		return null;
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		if (!instanceReward.isStartProgress()) {
			return;
		}
		int rewardetPoints = getNpcBonus(npc.getNpcId());
		int skill = instanceReward.getNpcBonusSkill(npc.getNpcId());
		if (skill != 0) {
			useSkill(npc, player, skill >> 8, skill & 0xFF);
		}
		getPlayerReward(player).addPoints(rewardetPoints);
		sendSystemMsg(player, npc, rewardetPoints);
		sendPacket();
	}

	protected void useSkill(Npc npc, Player player, int skillId, int level) {
		SkillEngine.getInstance().getSkill(npc, skillId, level, player).useNoAnimationSkill();
	}

}
