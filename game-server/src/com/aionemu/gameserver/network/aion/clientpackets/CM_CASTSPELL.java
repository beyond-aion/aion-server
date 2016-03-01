package com.aionemu.gameserver.network.aion.clientpackets;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_SKILL_CANT_CAST;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_SKILL_NOT_READY;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author alexa026
 * @author rhys2002
 */
public class CM_CASTSPELL extends AionClientPacket {

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

	/**
	 * Constructs new instance of <tt>CM_CM_REQUEST_DIALOG </tt> packet
	 * 
	 * @param opcode
	 */
	public CM_CASTSPELL(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		spellid = readH();
		level = readC();

		targetType = readC();

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

		hitTime = readH();
		unk = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		if (player.getLifeStats().isAlreadyDead()) {
			PacketSendUtility.sendPacket(player, STR_SKILL_CANT_CAST(1400059));
			return;
		}

		if (spellid == 0) {
			player.getController().cancelCurrentSkill(null);
			return;
		}

		SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(spellid);
		if (template == null || template.isPassive())
			return;

		if (player.isProtectionActive()) {
			player.getController().stopProtectionActiveTask();
		}

		long currentTime = System.currentTimeMillis();
		if (player.getNextSkillUse() > currentTime) {
			PacketSendUtility.sendPacket(player, STR_SKILL_NOT_READY);
			return;
		}

		player.getController().useSkill(template, targetType, x, y, z, hitTime, level);
	}
}
