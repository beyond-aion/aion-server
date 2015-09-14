package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collection;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.stats.container.PlayerLifeStats;
import com.aionemu.gameserver.model.team2.alliance.PlayerAllianceMember;
import com.aionemu.gameserver.model.team2.common.legacy.PlayerAllianceEvent;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTargetSlot;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Sarynth (Thx Rhys2002 for Packets)
 */
public class SM_ALLIANCE_MEMBER_INFO extends AionServerPacket {

	private Player player;
	private PlayerAllianceEvent event;
	private final int allianceId;
	private final int objectId;
	private final int slot;

	public SM_ALLIANCE_MEMBER_INFO(PlayerAllianceMember member, PlayerAllianceEvent event, int slot) {
		this.player = member.getObject();
		this.event = event;
		this.allianceId = member.getAllianceId();
		this.objectId = member.getObjectId();
		this.slot = slot;
	}

	public SM_ALLIANCE_MEMBER_INFO(PlayerAllianceMember member, PlayerAllianceEvent event) {
		this(member, event, 0);
	}

	@Override
	protected void writeImpl(AionConnection con) {
		PlayerCommonData pcd = player.getCommonData();
		WorldPosition wp = player.getPosition();

		/**
		 * Required so that when member is disconnected, and his playerAllianceGroup slot is changed, he will continue to appear as disconnected to the
		 * alliance.
		 */
		if (event == PlayerAllianceEvent.ENTER && !player.isOnline())
			event = PlayerAllianceEvent.ENTER_OFFLINE;

		writeD(allianceId);
		writeD(objectId);
		if (player.isOnline()) {
			PlayerLifeStats pls = player.getLifeStats();
			writeD(pls.getMaxHp());
			writeD(pls.getCurrentHp());
			writeD(pls.getMaxMp());
			writeD(pls.getCurrentMp());
			writeD(pls.getMaxFp());
			writeD(pls.getCurrentFp());
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
		writeC(pcd.getPlayerClass().getClassId());
		writeC(pcd.getGender().getGenderId());
		writeC(pcd.getLevel());
		writeC(this.event.getId());
		writeC(1); // unk 600020000 & 600030000 0x00, rest 0x01
		writeC(player.getFlyState()); // isFly
		writeC(0x0);
		switch (this.event) {
			case LEAVE:
			case LEAVE_TIMEOUT:
			case BANNED:
			case MOVEMENT:
			case DISCONNECTED:
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

				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				break;
			case JOIN:
			case ENTER:
			case ENTER_OFFLINE:
			case UPDATE:
			case RECONNECT:
			case APPOINT_VICE_CAPTAIN: // Unused maybe...
			case DEMOTE_VICE_CAPTAIN:
			case APPOINT_CAPTAIN:
				writeS(pcd.getName());
				writeD(0x00); // unk
				writeD(0x00); // unk
				if (player.isOnline()) {
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
					writeD(0x00);
					writeD(0x00);
					writeD(0x00);
					writeD(0x00);
					writeD(0x00);
					writeD(0x00);
					writeD(0x00);
					writeD(0x00);
				} else {
					writeH(0);
				}
				break;
			case MEMBER_GROUP_CHANGE:
				writeS(pcd.getName());
				break;
			default:
				break;
		}
	}

}
