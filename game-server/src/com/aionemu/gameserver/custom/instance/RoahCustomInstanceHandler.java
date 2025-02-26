package com.aionemu.gameserver.custom.instance;

import static com.aionemu.gameserver.custom.instance.CustomInstanceService.REWARD_COIN_ID;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_START_IDABRE;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.custom.instance.neuralnetwork.PlayerModel;
import com.aionemu.gameserver.custom.instance.neuralnetwork.PlayerModelController;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.drop.DropItem;
import com.aionemu.gameserver.model.flyring.FlyRing;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.stats.calc.functions.StatSetFunction;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.flyring.FlyRingTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Jo, Estrayl
 */
public class RoahCustomInstanceHandler extends GeneralInstanceHandler {

	private static final int CENTER_ARTIFACT_ID = 234029;
	private static final int TRASH_MOB_ID = 218483;
	private static final int BULKY_MOB_ID = 282757;
	private static final int DOMINATOR_MOB_ID = 284203;
	private static final int BOSS_MOB_A_M_ID = 231919; // asmodian male
	private static final int BOSS_MOB_A_F_ID = 231747; // asmodian female
	private static final int BOSS_MOB_E_M_ID = 231918; // elyos male
	private static final int BOSS_MOB_E_F_ID = 231717; // elyos female
	private static final int BOSS_MOB_AT_ID = 217221;

	private static final int TIME_LIMIT = 900; // 15 minutes

	private static final float REWARD_SCALE = 2f;

	private final AtomicLong startTime = new AtomicLong();
	private final AtomicBoolean isInitialized = new AtomicBoolean();
	private final AtomicBoolean isCompleted = new AtomicBoolean();

	private int avgClassDps, achievedDps, playerObjId;
	private PlayerModel model;
	private List<Integer> skillSet;

	private boolean isBossPhase, isPrebuffed;
	private int rank;
	private Future<?> despawnTask, trashMobSpawnTask, bulkyMobSpawnTask, dominatorMobSpawnTask;

	public RoahCustomInstanceHandler(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceCreate() {
		spawnRings();
		spawn(730588, 505.2431f, 377.0414f, 93.8944f, (byte) 30, 33); // Exit
	}

	@Override
	public void onInstanceDestroy() {
		cancelAllTasks();
	}

	private void spawnRings() {
		// transparent entry gate
		new FlyRing(new FlyRingTemplate("ROAH_WING_1", mapId, new Point3D(501.77, 409.53, 94.12), new Point3D(503.93, 409.65, 98.9),
			new Point3D(506.26, 409.7, 94.15), 10), instance.getInstanceId()).spawn();
	}

	@Override
	public boolean onPassFlyingRing(Player player, String flyingRing) {
		if (flyingRing.equals("ROAH_WING_1") && startTime.compareAndSet(0, System.currentTimeMillis())) {
			Npc artifact = (Npc) spawn(CENTER_ARTIFACT_ID, 504.1977f, 481.5051f, 87.2790f, (byte) 30);
			if (artifact != null)
				adaptNPC(artifact, rank);

			PacketSendUtility.broadcastToMap(instance, STR_MSG_INSTANCE_START_IDABRE());
			PacketSendUtility.broadcastToMap(instance, new SM_QUEST_ACTION(0, TIME_LIMIT));
			PacketSendUtility.broadcastToMap(instance, new SM_MESSAGE(0, null,
				"You have 15 minutes to demolish the central Aether Stone. Beware for its protectors!", ChatType.BRIGHT_YELLOW_CENTER));

			despawnTask = ThreadPoolManager.getInstance().schedule(() -> {
				setResult(false);
				deleteAliveNpcs(CENTER_ARTIFACT_ID, TRASH_MOB_ID, BULKY_MOB_ID, DOMINATOR_MOB_ID, BOSS_MOB_A_M_ID, BOSS_MOB_A_F_ID, BOSS_MOB_E_M_ID,
					BOSS_MOB_E_F_ID, BOSS_MOB_AT_ID);
			}, TIME_LIMIT * 1000);

			// spawn bulky mob: 1/min after 1:20min
			bulkyMobSpawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
				Npc bulky = null;
				for (Npc n : instance.getNpcs(BULKY_MOB_ID)) {
					if (!n.isDead() && !n.getLifeStats().isAboutToDie()) {
						bulky = n;
						break;
					}
				}
				if (bulky != null) {
					bulky.getLifeStats().setCurrentHp(bulky.getLifeStats().getMaxHp());
					PacketSendUtility.broadcastToMap(instance, new SM_MESSAGE(0, null, "Protector Vord recovered energy!", ChatType.BRIGHT_YELLOW_CENTER));
				} else {
					bulky = (Npc) spawn(BULKY_MOB_ID, 493.923f, 488.16794f, 87.18341f, (byte) 0);
					adaptNPC(bulky, rank);
					bulky.getAggroList().addHate(player, 1000);
					PacketSendUtility.broadcastToMap(instance, new SM_MESSAGE(0, null, "A tough protector has appeared!", ChatType.BRIGHT_YELLOW_CENTER));
				}
			}, 80000, 60000);

			if (rank >= CustomInstanceRankEnum.BRONZE.getMinRank()) {
				// spawn trash mobs: 1 wave/min after 1min
				trashMobSpawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
					int newTrash = 6;
					for (Npc n : instance.getNpcs(TRASH_MOB_ID)) // always spawn until 6 total
						if (!n.isDead())
							newTrash--;

					if (newTrash > 0) {
						for (int i = 0; i < newTrash; i++) {
							Npc mob = (Npc) spawn(TRASH_MOB_ID, 500.1f + Rnd.nextFloat(8f), 460f + Rnd.nextFloat(4f), 86.7112f, (byte) 30);
							adaptNPC(mob, rank);
							mob.getAggroList().addHate(player, 1000);
						}
					}
				}, 60000, 60000 - rank * 1000L); // 60s ... 30s
			}

			if (rank >= CustomInstanceRankEnum.SILVER.getMinRank()) {
				// spawn dominator mob: 1/min after 1:40min
				dominatorMobSpawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
					Npc dominator = null;
					for (Npc n : instance.getNpcs(DOMINATOR_MOB_ID)) {
						if (!n.isDead() && !n.getLifeStats().isAboutToDie()) {
							dominator = n;
							break;
						}
					}
					if (dominator != null) {
						dominator.getLifeStats().setCurrentHp(dominator.getLifeStats().getMaxHp());
						PacketSendUtility.broadcastToMap(instance, new SM_MESSAGE(0, null, "Protector Vala recovered energy!", ChatType.BRIGHT_YELLOW_CENTER));

					} else {
						dominator = (Npc) spawn(DOMINATOR_MOB_ID, 515.23114f, 487.94998f, 87.176056f, (byte) 60);
						adaptNPC(dominator, rank);
						dominator.getAggroList().addHate(player, 1000);
						PacketSendUtility.broadcastToMap(instance, new SM_MESSAGE(0, null, "A vile protector has appeared!", ChatType.BRIGHT_YELLOW_CENTER));
					}
				}, 100000, 60000);
			}
		}
		return false;
	}

	@Override
	public void onDie(Npc npc) {
		switch (npc.getNpcId()) {
			case TRASH_MOB_ID, BULKY_MOB_ID, DOMINATOR_MOB_ID -> npc.getController().delete();
			case CENTER_ARTIFACT_ID -> {
				cancelAllTasks();
				// use pcd for rare cases in which the artifact got destroyed by dots/servants and the player got DC'ed before
				PlayerCommonData pcd = PlayerService.getOrLoadPlayerCommonData(playerObjId);
				float usedTime = (System.currentTimeMillis() - startTime.get()) / 1000f;
				achievedDps = Math.round(npc.getLifeStats().getMaxHp() / usedTime);
				log.info(String.format(
					"[CI_ROAH] Player [id=%d, name=%s, class=%s, rank=%s(%d), isPrebuffed=%b] succeeded in destroying the artifact in %.3fs (DPS:%d).",
					pcd.getPlayerObjId(), pcd.getName(), pcd.getPlayerClass(), CustomInstanceRankEnum.getRankDescription(rank), rank, isPrebuffed, usedTime,
					achievedDps));
				deleteAliveNpcs(TRASH_MOB_ID, BULKY_MOB_ID, DOMINATOR_MOB_ID);
				PacketSendUtility.broadcastToMap(instance,
					new SM_MESSAGE(0, null, "You feel a shadowy presence from the throne room.", ChatType.BRIGHT_YELLOW_CENTER));
				PacketSendUtility.broadcastToMap(instance, new SM_QUEST_ACTION(0, 0));
				int bossID = BOSS_MOB_AT_ID;
				if (pcd.getPlayerClass() != PlayerClass.RIDER) {
					switch (pcd.getRace()) {
						case ASMODIANS -> bossID = switch (pcd.getGender()) {
								case MALE -> BOSS_MOB_A_M_ID;
								case FEMALE -> BOSS_MOB_A_F_ID;
							};
						case ELYOS -> bossID = switch (pcd.getGender()) {
								case MALE -> BOSS_MOB_E_M_ID;
								case FEMALE -> BOSS_MOB_E_F_ID;
							};
					}
				}
				spawn(bossID, 503.96774f, 631.935f, 104.548775f, (byte) 90); // spawn bossMob
				isBossPhase = true;
				npc.getController().delete();
			}
			case BOSS_MOB_A_M_ID, BOSS_MOB_A_F_ID, BOSS_MOB_E_M_ID, BOSS_MOB_E_F_ID, BOSS_MOB_AT_ID -> {
				calcBossDrop(npc.getObjectId());
				setResult(true);
				PacketSendUtility.broadcastToMap(instance, new SM_QUEST_ACTION(0, 0));
			}
		}
	}

	@Override
	public void onEnterInstance(Player player) {
		if (isInitialized.compareAndSet(false, true)) {
			isPrebuffed = checkForPrebuffOnEntry(player); // check for Word of Wind and Symphony of Destruction
			setAverageDpsByPlayerClass(player.getPlayerClass());
			playerObjId = instance.getRegisteredObjects().iterator().next();
			rank = CustomInstanceService.getInstance().loadOrCreateRank(playerObjId).getRank();
			PacketSendUtility.broadcastToMap(instance, new SM_MESSAGE(0, null,
				"Welcome to the 'Eternal Challenge', " + CustomInstanceRankEnum.getRankDescription(rank) + " challenger!", ChatType.BRIGHT_YELLOW_CENTER));
			spawnUnlockableNpcs(player);
			// train player model from previous boss runs
			skillSet = PlayerModelController.getSkillSetForPlayer(playerObjId);
			model = PlayerModelController.trainModelForPlayer(playerObjId, skillSet);
		}
	}

	private boolean checkForPrebuffOnEntry(Player player) {
		return player.getEffectController().findBySkillId(1810) != null || player.getEffectController().findBySkillId(4469) != null;
	}

	private void setAverageDpsByPlayerClass(PlayerClass playerClass) {
		avgClassDps = switch (playerClass) {
			case TEMPLAR, CHANTER -> 2500;
			case GUNNER -> 4500;
			case SORCERER, ASSASSIN -> 5000;
			case SPIRIT_MASTER -> 6000;
			default -> 4000;
		};
	}

	private void calcBossDrop(int npcObjId) {
		Set<DropItem> dropItems = DropRegistrationService.getInstance().getCurrentDropMap().get(npcObjId);
		if (dropItems != null) { // dropItems can possibly be null if the player died right before the boss
			dropItems.clear();
			dropItems.add(DropRegistrationService.getInstance().regDropItem(0, playerObjId, npcObjId, REWARD_COIN_ID, getRewardCoinAmount(rank)));
		}
	}

	private int getRewardCoinAmount(int rank) {
		CustomInstanceRankEnum rankEnum = CustomInstanceRankEnum.getByRank(rank);
		if (rankEnum != CustomInstanceRankEnum.ANCIENT_PLUS)
			return rankEnum.getMinReward();
		else
			return (int) (rankEnum.getMinReward() + (rank - rankEnum.getMinRank()) * REWARD_SCALE);
	}

	private void adaptNPC(Npc npc, int rank) {
		List<StatSetFunction> functions = new ArrayList<>();

		switch (npc.getNpcId()) { // IRON ... ANCIENT+
			case CENTER_ARTIFACT_ID -> { // ~5 ... 10min
				int maxHP = 300 * avgClassDps + rank * 10 * avgClassDps;
				if (rank > CustomInstanceRankEnum.ANCIENT.getMinRank())
					maxHP += (rank - CustomInstanceRankEnum.ANCIENT.getMinRank()) * 150000;
				functions.add(new StatSetFunction(StatEnum.MAXHP, maxHP));
			}
			case TRASH_MOB_ID -> { // ~1s fix (AoE-able)
				functions.add(new StatSetFunction(StatEnum.MAXHP, 1));
				functions.add(new StatSetFunction(StatEnum.SPEED, 6000 + rank * 100));
			}
			case BULKY_MOB_ID -> // ~10 ... 25s
				functions.add(new StatSetFunction(StatEnum.MAXHP, 10 * avgClassDps + rank * avgClassDps / 2));
			case DOMINATOR_MOB_ID -> { // ~10s fix
				functions.add(new StatSetFunction(StatEnum.MAXHP, 10 * avgClassDps));
				functions.add(new StatSetFunction(StatEnum.PHYSICAL_ATTACK, 0)); // only debuffs, no dmg
				functions.add(new StatSetFunction(StatEnum.ATTACK_SPEED, 2000 - rank * 30));
			}
		}

		npc.getGameStats().addEffect(null, functions);
		npc.getLifeStats().setCurrentHp(npc.getLifeStats().getMaxHp());
	}

	private void setResult(boolean success) {
		cancelAllTasks();
		if (isCompleted.compareAndSet(false, true)) {
			int oldRank = rank;
			Player player = instance.getPlayer(playerObjId);
			if (success) {
				rank++;
				PacketSendUtility.broadcastToMap(instance,
					new SM_MESSAGE(0, null, "Your rank has been increased to " + CustomInstanceRankEnum.getRankDescription(rank)
						+ ". Stay steadfast for tougher challenges and higher rewards!", ChatType.BRIGHT_YELLOW_CENTER));
			} else {
				rank = Math.max(0, --rank);

				if (player != null) {
					PacketSendUtility.sendPacket(player, new SM_MESSAGE(0, null,
						"Your rank has been decreased to " + CustomInstanceRankEnum.getRankDescription(rank) + ".", ChatType.BRIGHT_YELLOW_CENTER));

					if (rank > 0) // prevent suicide abuse || loss rewards
						ItemService.addItem(player, REWARD_COIN_ID, getRewardCoinAmount(oldRank), true);
				}
			}

			CustomInstanceService.getInstance().saveNewPlayerModelEntries(playerObjId);
			if (CustomInstanceService.getInstance().changePlayerRank(playerObjId, rank, achievedDps)) {
				String name = player != null ? player.getName() : PlayerDAO.getPlayerNameByObjId(playerObjId);
				log.info(String.format("[CI_ROAH] Rank changed for Player [id=%d, name=%s, oldRank=%s(%d), newRank=%s(%d)]", playerObjId, name,
					CustomInstanceRankEnum.getRankDescription(oldRank), oldRank, CustomInstanceRankEnum.getRankDescription(rank), rank));
			}
		}
	}

	private void cancelAllTasks() {
		if (despawnTask != null)
			despawnTask.cancel(true);
		if (trashMobSpawnTask != null)
			trashMobSpawnTask.cancel(true);
		if (bulkyMobSpawnTask != null)
			bulkyMobSpawnTask.cancel(true);
		if (dominatorMobSpawnTask != null)
			dominatorMobSpawnTask.cancel(true);
	}

	private void spawnUnlockableNpcs(Player player) {
		if (rank >= CustomInstanceRankEnum.BRONZE.getMinRank()) // + General Goods Merchant
			if (player.getRace() == Race.ASMODIANS)
				spawn(205848, 510.84058f, 407.1751f, 93.91156f, (byte) 87);
			else
				spawn(205826, 510.84058f, 407.1751f, 93.91156f, (byte) 87);

		if (rank >= CustomInstanceRankEnum.SILVER.getMinRank()) // + Warehouse Manager
			spawn(205711, 496.6516f, 407.25f, 93.85523f, (byte) 99);

		if (rank >= CustomInstanceRankEnum.GOLD.getMinRank()) // + Legion Warehouse
			if (player.getRace() == Race.ASMODIANS)
				spawn(805253, 494.753f, 405.53464f, 93.87636f, (byte) 104);
			else
				spawn(805245, 494.753f, 405.53464f, 93.87636f, (byte) 104);

		if (rank >= CustomInstanceRankEnum.PLATINUM.getMinRank()) // + Mail
			if (player.getRace() == Race.ASMODIANS)
				spawn(700079, 499.6871f, 406.73828f, 93.84634f, (byte) 91);
			else
				spawn(700000, 499.6871f, 406.73828f, 93.84634f, (byte) 91);

		if (rank >= CustomInstanceRankEnum.MITHRIL.getMinRank()) // + Soul Healer
			spawn(205727, 513.6179f, 405.60223f, 93.91156f, (byte) 82);

		if (rank >= CustomInstanceRankEnum.CERANIUM.getMinRank()) // + Trade Broker
			if (player.getRace() == Race.ASMODIANS)
				spawn(205853, 516.24396f, 402.30844f, 93.91156f, (byte) 68);
			else
				spawn(798912, 516.24396f, 402.30844f, 93.91156f, (byte) 68);

		if (rank >= CustomInstanceRankEnum.ANCIENT.getMinRank()) // + Stigma Master
			if (player.getRace() == Race.ASMODIANS)
				spawn(800765, 493.39554f, 403.7841f, 93.88781f, (byte) 106);
			else
				spawn(800760, 493.39554f, 403.7841f, 93.88781f, (byte) 106);

		if (rank >= CustomInstanceRankEnum.ANCIENT_PLUS.getMinRank()) // + AP Shugo
			spawn(804833, 508.1798f, 407.87378f, 93.8523f, (byte) 89);
	}

	private int getElapsedTimeMillis() {
		return (int) (System.currentTimeMillis() - startTime.get());
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(0, TIME_LIMIT * 1000 - getElapsedTimeMillis()));
		return super.onReviveEvent(player);
	}

	@Override
	public boolean onDie(Player player, Creature lastAttacker) {
		if (!isCompleted.get()) {
			if (isBossPhase) {
				PacketSendUtility.sendMessage(player, "At last! I have become .. your greatest nightmare!", ChatType.BRIGHT_YELLOW_CENTER);
				deleteAliveNpcs(BOSS_MOB_A_M_ID, BOSS_MOB_A_F_ID, BOSS_MOB_E_M_ID, BOSS_MOB_E_F_ID, BOSS_MOB_AT_ID);
			} else {
				PacketSendUtility.sendMessage(player, "You shall not pass!", ChatType.BRIGHT_YELLOW_CENTER);
				deleteAliveNpcs(CENTER_ARTIFACT_ID, TRASH_MOB_ID, BULKY_MOB_ID, DOMINATOR_MOB_ID);
			}
			setResult(false);
		}
		return true;
	}

	@Override
	public void onEndCastSkill(Skill skill) {
		if (isBossPhase && skill.getEffector() instanceof Player effector && instance.isRegistered(effector.getObjectId()))
			CustomInstanceService.getInstance().recordPlayerModelEntry(effector, skill, effector.getTarget());
	}

	public PlayerModel getPlayerModel() {
		return model;
	}

	public List<Integer> getSkillSet() {
		return skillSet;
	}

	@Override
	public boolean allowSelfReviveBySkill() {
		return false;
	}

	@Override
	public boolean allowSelfReviveByItem() {
		return false;
	}

	@Override
	public boolean allowInstanceRevive() {
		return false;
	}
}
