package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author alexa026, rhys2002
 */
public class CM_CASTSPELL extends AionClientPacket {

	private final long receiveTime = System.currentTimeMillis();
	private int spellid;
	// 0 - obj id, 1 - point location, 2 - unk, 3 - object not in sight(skill 1606)? 4 - unk
	private int targetType;
	private float x, y, z;

	@SuppressWarnings("unused")
	private int targetObjectId;
	private int hitTime;
	private int level;
	@SuppressWarnings("unused")
	private int unk;

	public CM_CASTSPELL(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		spellid = readUH();
		level = readUC();

		targetType = readUC();

		switch (targetType) {
			case 0:
			case 3:
			case 4:
				targetObjectId = readD();
				break;
			case 1:
				x = readF();
				y = readF();
				z = readF();
				break;
			case 2:
				x = readF();
				y = readF();
				z = readF();
				readF();// unk1
				readF();// unk2
				readF();// unk3
				readF();// unk4
				readF();// unk5
				readF();// unk6
				readF();// unk7
				readF();// unk8
				break;
		}

		hitTime = readUH();
		unk = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		if (player.isDead()) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_SKILL_CANT_CAST(ChatUtil.l10n(1400059)));
			return;
		}

		if (spellid == 0) {
			player.getController().cancelCurrentSkill(null);
			return;
		}
		if (DataManager.PET_SKILL_DATA.isPetOrderSkill(spellid) && (player.getSummon() == null || !player.getSummon().isPet())) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_SKILL_NOT_NEED_PET());
			return;
		}

		SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(spellid);
		if (template == null || template.isPassive())
			return;

		if (player.isProtectionActive())
			player.getController().stopProtectionActiveTask();
		player.getController().cancelUseItem();

		if (player.getNextSkillUse() > receiveTime) {
			int lastSkillId = player.getLastSkill().getSkillId(); // lastSkill cannot be null, as nextSkillUse is zero on the first cast
			AuditLogger.log(player, "tried to use skill " + spellid + " " + (player.getNextSkillUse() - receiveTime) + " ms too early. Previous skill: " + lastSkillId);
			if (player.getNextSkillUse() > System.currentTimeMillis()) {
				sendPacket(SM_SYSTEM_MESSAGE.STR_SKILL_NOT_READY());
				return;
			}
		}

		player.getController().useSkill(template, targetType, x, y, z, hitTime, level);
	}
}
