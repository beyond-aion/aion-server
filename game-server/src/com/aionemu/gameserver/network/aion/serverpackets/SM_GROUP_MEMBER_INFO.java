package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collection;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.stats.container.PlayerLifeStats;
import com.aionemu.gameserver.model.team2.common.legacy.GroupEvent;
import com.aionemu.gameserver.model.team2.group.PlayerGroup;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTargetSlot;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Lyahim, ATracer
 */
public class SM_GROUP_MEMBER_INFO extends AionServerPacket {

	private int groupId;
	private Player player;
	private GroupEvent event;
	private int slot;

	public SM_GROUP_MEMBER_INFO(PlayerGroup group, Player player, GroupEvent event, int slot) {
		this.groupId = group.getTeamId();
		this.player = player;
		this.event = event;
		this.slot = slot;
	}

	public SM_GROUP_MEMBER_INFO(PlayerGroup group, Player player, GroupEvent event) {
		this(group, player, event, 0);
	}

	@Override
	protected void writeImpl(AionConnection con) {
		PlayerLifeStats pls = player.getLifeStats();
		PlayerCommonData pcd = player.getCommonData();
		WorldPosition wp = player.getPosition();

		if (event == GroupEvent.ENTER && !player.isOnline()) {
			event = GroupEvent.ENTER_OFFLINE;
		}

		writeD(groupId);
		writeD(player.getObjectId());
		if (player.isOnline()) {
			writeD(pls.getMaxHp());
			writeD(pls.getCurrentHp());
			writeD(pls.getMaxMp());
			writeD(pls.getCurrentMp());
			writeD(pls.getMaxFp()); // maxflighttime
			writeD(pls.getCurrentFp()); // currentflighttime
		} else {
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
		}

		writeD(0);// unk 3.5
		writeD(wp.getMapId());
		writeD(wp.getMapId() + wp.getInstanceId() - 1);
		writeF(wp.getX());
		writeF(wp.getY());
		writeF(wp.getZ());
		writeC(pcd.getPlayerClass().getClassId()); // class id
		writeC(pcd.getGender().getGenderId()); // gender id
		writeC(pcd.getLevel()); // level

		writeC(event.getId()); // something events
		writeC(1); // unk 600020000 & 600030000 0x00, rest 0x01
		writeC(player.getFlyState()); // isFly
		writeC(player.isMentor() ? 0x01 : 0x00);

		switch (event) {
			case MOVEMENT:
			case DISCONNECTED:
			case LEAVE:
				break;
			case ENTER_OFFLINE:
			case JOIN:
				writeS(pcd.getName()); // name
				break;
			case UPDATE_EFFECTS:
				writeD(0x00); // unk
				writeD(0x00); // unk
				writeC(slot);
				Collection<Effect> _abnormalEffects = player.getEffectController().getAbnormalEffectsToTargetSlot(slot);
				writeH(_abnormalEffects.size()); // Abnormal effects
				for (Effect effect : _abnormalEffects) {
					writeD(effect.getEffectorId()); // casterid
					writeH(effect.getSkillId()); // spellid
					writeC(effect.getSkillLevel()); // spell level
					writeC(effect.getTargetSlot()); // unk ?
					writeD(effect.getRemainingTime()); // estimatedtime
				}

				for (SkillTargetSlot targetSlot : SkillTargetSlot.values()) {
					if ((slot & targetSlot.getId()) == 1)
						writeD(0x00); // TODO: remaining time ?
					else
						writeD(0x00);
				}
				break;
			default:
				writeS(pcd.getName()); // name
				writeD(0x00); // unk
				writeD(0x00); // unk
				writeC(SkillTargetSlot.FULLSLOTS);
				Collection<Effect> abnormalEffects = player.getEffectController().getAbnormalEffectsToShow();
				writeH(abnormalEffects.size()); // Abnormal effects
				for (Effect effect : abnormalEffects) {
					writeD(effect.getEffectorId()); // casterid
					writeH(effect.getSkillId()); // spellid
					writeC(effect.getSkillLevel()); // spell level
					writeC(effect.getTargetSlot()); // unk ?
					writeD(effect.getRemainingTime()); // estimatedtime
				}
				for (SkillTargetSlot targetSlot : SkillTargetSlot.values()) {
					if ((SkillTargetSlot.FULLSLOTS & targetSlot.getId()) == 1)
						writeD(0x00); // TODO: remaining time ?
					else
						writeD(0x00);
				}
				break;
		}
	}

}
