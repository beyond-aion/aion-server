package com.aionemu.gameserver.model.autogroup;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.instancescore.PvPArenaScore;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AUTO_GROUP;
import com.aionemu.gameserver.services.AutoGroupService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
public class AutoPvPFFAInstance extends AutoInstance {

	public AutoPvPFFAInstance(AutoGroupType agt) {
		super(agt);
	}

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		PvPArenaScore score = (PvPArenaScore) instance.getInstanceHandler().getInstanceScore();
		score.setDifficultyId(agt.getDifficultId());
	}

	@Override
	public AGQuestion addLookingForParty(LookingForParty lookingForParty) {
		super.writeLock();
		try {
			if (isRegistrationDisabled(lookingForParty) || lookingForParty.getMembers().size() > 1
				|| registeredAGPlayers.size() >= getMaxPlayers()) {
				return AGQuestion.FAILED;
			}

			registeredAGPlayers.putAll(lookingForParty.getMembers());
			return instance == null && registeredAGPlayers.size() == getMaxPlayers() ? AGQuestion.READY : AGQuestion.ADDED;
		} finally {
			super.writeUnlock();
		}
	}

	@Override
	public void onPressEnter(Player player) {
		if (agt.isPvPFFAArena() || agt.isPvPSoloArena() || agt.isGloryArena()) {
			long size = 1;
			int itemId = 186000135;
			if (agt.isGloryArena()) {
				size = 3;
				itemId = 186000185;
			}
			if (!removeItem(player, itemId, size)) {
				registeredAGPlayers.remove(player.getObjectId());
				PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(agt.getTemplate().getMaskId(), 5));
				if (registeredAGPlayers.isEmpty())
					AutoGroupService.getInstance().destroyIfPossible(this);
				return;
			}
		}
		((PvPArenaScore) instance.getInstanceHandler().getInstanceScore()).portToPosition(player);
		instance.register(player.getObjectId());
	}

	@Override
	public void onLeaveInstance(Player player) {
		super.unregister(player);
	}

}
