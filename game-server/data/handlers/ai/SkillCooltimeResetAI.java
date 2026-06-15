package ai;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIRequest;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.custom.pvpmap.PvpMapService;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemCooldown;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.ItemUseLimits;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.skillengine.model.SkillTargetSlot;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.skillengine.model.TransformType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author Yeats, Neon
 */
@AIName("customcdreset")
public class SkillCooltimeResetAI extends NpcAI {

	private static final int PRICE = 50_000;
	private static final int MAX_SKILL_COOLDOWN_TIME = 3060; // = 5min 6sec
	private static final int MAX_ITEM_COOLDOWN_SECONDS = 300; // excludes items like Fine Bracing Water, Leader's Recovery Scroll, Recovery Crystal etc.

	private final Map<Integer, Long> playersInSight = new ConcurrentHashMap<>();

	public SkillCooltimeResetAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (PvpMapService.getInstance().isOnPvPMap(getOwner())) {
			getOwner().getController().addTask(TaskId.DESPAWN, ThreadPoolManager.getInstance().schedule(() -> getOwner().getController().delete(), 30000));
			ThreadPoolManager.getInstance().schedule(() -> getOwner().getKnownList().forEachPlayer(this::tryNotify), 1000);
		}
	}

	@Override
	protected void handleDialogStart(Player player) {
		playersInSight.values().removeIf(time -> System.currentTimeMillis() > time + 300000); // remove players if they are already 5 mins+ in the map
		if (player.getLifeStats().isAboutToDie() || player.isDead())
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_USE_IN_DEAD_STATE());
		else if (!PvpMapService.getInstance().isOnPvPMap(player) && player.getController().isInCombat())
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CANT_CAST_IN_COMBAT_STATE());
		else if (player.isTransformed() && player.getTransformModel().getType() == TransformType.AVATAR)
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_ACT_WHILE_IN_ABNORMAL_STATE());
		else if (collectResettableSkillCooldownIds(player).isEmpty() && collectResettableItemCooldownIds(player).isEmpty())
			PacketSendUtility.sendPacket(player, new SM_MESSAGE(getOwner(), "Daeva has no skill cooldowns to reset, yang.", ChatType.NPC));
		else
			sendRequest(player);
	}

	@Override
	public void handleCreatureMoved(Creature creature) {
		if (creature instanceof Player player)
			tryNotify(player);
	}

	private void tryNotify(Player player) {
		if (player.isDead())
			return;
		if (!getOwner().canSee(player))
			return;
		if (playersInSight.containsKey(player.getObjectId()))
			return;
		if (PositionUtil.isInRange(getOwner(), player, 8) && GeoService.getInstance().canSee(getOwner(), player)) {
			playersInSight.put(player.getObjectId(), System.currentTimeMillis());
			PacketSendUtility.sendPacket(player,
				new SM_MESSAGE(getOwner(), String.format("I can heal you and reset your skill cooldowns for %,d Kinah, yang yang.", PRICE), ChatType.NPC));
		}
	}

	private void sendRequest(Player player) {
		AIActions.addRequest(this, player, 1300765, getObjectTemplate().getTalkDistance(), new AIRequest() {

			@Override
			public void acceptRequest(Creature requester, Player responder, int requestId) {
				if (responder.getInventory().getKinah() < PRICE)
					PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_KINA(PRICE));
				else if (responder.getLifeStats().isAboutToDie() || responder.isDead())
					PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_CANNOT_USE_IN_DEAD_STATE());
				else if (responder.getController().isInCombat())
					PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_SKILL_CANT_CAST_IN_COMBAT_STATE());
				else if (responder.isTransformed() && responder.getTransformModel().getType() == TransformType.AVATAR)
					PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_ACT_WHILE_IN_ABNORMAL_STATE());
				else {
					Set<Integer> skillCooldownIds = collectResettableSkillCooldownIds(responder);
					Set<Integer> itemCooldownIds = collectResettableItemCooldownIds(responder);
					if (!skillCooldownIds.isEmpty() || !itemCooldownIds.isEmpty()) {
						if (responder.getInventory().tryDecreaseKinah(PRICE)) {
							responder.getLifeStats().increaseHp(SM_ATTACK_STATUS.TYPE.HP, responder.getLifeStats().getMaxHp(), getOwner());
							responder.getLifeStats().increaseMp(SM_ATTACK_STATUS.TYPE.HEAL_MP, responder.getLifeStats().getMaxMp(), 0, SM_ATTACK_STATUS.LOG.MPHEAL);
							if (!skillCooldownIds.isEmpty()) {
								skillCooldownIds.forEach(responder::removeSkillCoolDown);
								PacketSendUtility.sendPacket(responder, new SM_SKILL_COOLDOWN(responder, skillCooldownIds));
							}
							if (!itemCooldownIds.isEmpty()) {
								Map<Integer, ItemCooldown> dummyCds = new HashMap<>(); // 4.8 client ignores reuseTime <= currentTime, but sending old cds + useDelay 0 works
								itemCooldownIds.forEach(itemCooldownId -> {
									dummyCds.put(itemCooldownId, new ItemCooldown(responder.getItemReuseTime(itemCooldownId), 0));
									responder.removeItemCoolDown(itemCooldownId);
								});
								PacketSendUtility.sendPacket(responder, new SM_ITEM_COOLDOWN(dummyCds));
							}
							if (PvpMapService.getInstance().isOnPvPMap(getOwner())) {
								getOwner().getController().delete();
							}
						} else {
							PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_KINA(PRICE));
						}
					}
				}
			}
		});
	}

	private Set<Integer> collectResettableItemCooldownIds(Player player) {
		Set<Integer> itemCooldownIds = player.getItemCoolDowns().entrySet().stream()
			.filter(e -> e.getValue().getUseDelay() <= MAX_ITEM_COOLDOWN_SECONDS && e.getValue().getReuseTime() > System.currentTimeMillis())
			.map(Map.Entry::getKey)
			.collect(Collectors.toSet());
		if (!itemCooldownIds.isEmpty())
			itemCooldownIds.retainAll(collectBuffItemAndPotionCooldownIds(player));
		return itemCooldownIds;
	}

	private Set<Integer> collectBuffItemAndPotionCooldownIds(Player player) {
		Set<Integer> cooldownIds = new HashSet<>();
		for (Item item : player.getInventory().getItems()) {
			ItemTemplate itemTemplate = item.getItemTemplate();
			ItemUseLimits useLimits = itemTemplate.getUseLimits();
			if (useLimits == null || useLimits.getDelayId() == 0)
				continue;
			if (itemTemplate.getActions() == null || itemTemplate.getActions().getSkillUseAction() == null)
				continue;
			SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(itemTemplate.getActions().getSkillUseAction().getSkillId());
			if (skillTemplate == null || skillTemplate.getTargetSlot() != SkillTargetSlot.BUFF && !skillTemplate.getStack().startsWith("ITEM_POTION_")
				&& !skillTemplate.getStack().startsWith("ITEM_ARENA_POTION_"))
				continue;
			cooldownIds.add(useLimits.getDelayId());
		}
		return cooldownIds;
	}

	private Set<Integer> collectResettableSkillCooldownIds(Player player) {
		Set<Integer> cooldownIds = new HashSet<>();
		for (PlayerSkillEntry skill : player.getSkillList().getAllSkills()) {
			SkillTemplate st = DataManager.SKILL_DATA.getSkillTemplate(skill.getSkillId());
			if (st == null || st.getCooldown() > MAX_SKILL_COOLDOWN_TIME)
				continue;
			if (st.isDeityAvatar())
				continue;
			if (player.getSkillCoolDown(st.getCooldownId()) < System.currentTimeMillis())
				continue;
			cooldownIds.add(st.getCooldownId());
		}
		return cooldownIds;
	}
}
