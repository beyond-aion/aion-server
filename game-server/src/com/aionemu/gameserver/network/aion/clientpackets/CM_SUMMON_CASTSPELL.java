package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.summons.SkillOrder;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer, KID
 */
public class CM_SUMMON_CASTSPELL extends AionClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_SUMMON_CASTSPELL.class);
	private int summonObjId;
	private int targetObjId;
	private int skillId;
	private int skillLvl;
	@SuppressWarnings("unused")
	private int unk; // probably related to release

	public CM_SUMMON_CASTSPELL(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		summonObjId = readD();
		skillId = readUH();
		skillLvl = readUC();
		targetObjId = readD();
		unk = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		Creature summonOrMercenary = player.getSummonOrMercenary(summonObjId);
		if (summonOrMercenary == null || summonOrMercenary instanceof Summon summon && !summon.isPet()) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_SKILL_NOT_NEED_PET());
			return;
		}

		Creature target;
		if (targetObjId != summonOrMercenary.getObjectId()) {
			VisibleObject obj = summonOrMercenary.getKnownList().getObject(targetObjId);
			if (obj instanceof Creature) {
				target = (Creature) obj;
			} else { // null or not a creature (attack should be client restricted)
				if (obj != null) // may be null due to lags while the target runs out of sight
					AuditLogger.log(player, "tried to cast a summon spell on a wrong target: " + obj);
				return;
			}
		} else {
			target = summonOrMercenary;
		}

		if (summonOrMercenary instanceof Summon summon) {
			final SkillOrder order = summon.retrieveNextSkillOrder();
			if (order != null && order.getTarget().equals(target)) {
				if (order.getSkillId() != skillId || order.getSkillLevel() != skillLvl)
					log.warn(player + " used summon order with a different skill: skillId {}->{}; skillLvl {}->{}.", skillId, order.getSkillId(), skillLvl,
						order.getSkillLevel());
				summon.getController().useSkill(order);
			}
		} else {
			summonOrMercenary.setTarget(target);
			if (DataManager.PET_SKILL_DATA.petHasSkill(summonOrMercenary.getObjectTemplate().getTemplateId(), skillId))
				summonOrMercenary.getController().useSkill(skillId, skillLvl);
			else
				AuditLogger.log(player, "tried to use invalid mercenary skill " + skillId);
		}
	}
}
