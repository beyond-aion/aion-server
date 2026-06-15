package com.aionemu.gameserver.model.team;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.common.legacy.LootGroupRules;
import com.aionemu.gameserver.model.team.common.legacy.LootRuleType;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SHOW_BRAND;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.collections.Predicates;

/**
 * @author ATracer
 */
public abstract class TemporaryPlayerTeam<TM extends TeamMember<Player>> extends GeneralTeam<Player, TM> {

	private LootGroupRules lootGroupRules = new LootGroupRules();
	protected final Map<Integer, Integer> targetIdsByBrandId = new ConcurrentHashMap<>();

	public TemporaryPlayerTeam(int objId, boolean autoReleaseObjectId) {
		super(objId, autoReleaseObjectId);
	}

	/**
	 * Level of the player with lowest exp
	 */
	public abstract int getMinExpPlayerLevel();

	/**
	 * Level of the player with highest exp
	 */
	public abstract int getMaxExpPlayerLevel();

	public void updateBrand(int brandId, int targetObjectId) {
		targetIdsByBrandId.put(brandId, targetObjectId);
		sendPackets(new SM_SHOW_BRAND(brandId, targetObjectId));
	}

	public void sendBrands(Player member) {
		PacketSendUtility.sendPacket(member, new SM_SHOW_BRAND(targetIdsByBrandId));
	}

	@Override
	public Race getRace() {
		return getLeader().getObject().getRace();
	}

	@Override
	public void sendPackets(AionServerPacket... packets) {
		sendPacket(Predicates.alwaysTrue(), packets);
	}

	@Override
	public void sendPacket(Predicate<Player> predicate, AionServerPacket... packets) {
		forEach(player -> {
			if (predicate.test(player)) {
				for (AionServerPacket packet : packets)
					PacketSendUtility.sendPacket(player, packet);
			}
		});
	}

	@Override
	public final List<Player> getOnlineMembers() {
		return filterMembers(Predicates.Players.ONLINE);
	}

	@Override
	public LootGroupRules getLootGroupRules() {
		return lootGroupRules;
	}

	public void setLootGroupRules(LootGroupRules lootGroupRules) {
		this.lootGroupRules = lootGroupRules;
		if (lootGroupRules != null && lootGroupRules.getLootRule() == LootRuleType.FREEFORALL)
			sendPacket(Predicates.Players.WITH_LOOT_PET, SM_SYSTEM_MESSAGE.STR_MSG_LOOTING_PET_MESSAGE03());
	}

}
