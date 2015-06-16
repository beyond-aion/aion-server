package com.aionemu.gameserver.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.summons.SkillOrder;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.utils.ThreadPoolManager;

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

	public CM_SUMMON_CASTSPELL(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		summonObjId = readD();
		skillId = readH();
		skillLvl = readC();
		targetObjId = readD();
		unk = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		final Summon summon = player.getSummon();
		if (summon == null) {
			log.warn("summon castspell without active summon on {}.", player.getName());
			return;
		}
		if (summon.getObjectId() != summonObjId) {
			log.warn("summon castspell from a different summon instance on {}.", player.getName());
			return;
		}

		Creature target = null;
		if (targetObjId != summon.getObjectId()) {
			VisibleObject obj = summon.getKnownList().getObject(targetObjId);
			if (obj instanceof Creature) {
				target = (Creature) obj;
			}
		}
		else {
			target = summon;
		}

		if (target != null) {
			final SkillOrder order = summon.retrieveNextSkillOrder();
			if (order != null && order.getTarget() == target) {
				if (order.getSkillId() != skillId || order.getSkillLevel() != skillLvl)
					log.warn("Player {} used summon order with a different skill: skillId {}->{}; skillLvl {}->{}.", player.getName(), skillId,
						order.getSkillId(), skillLvl, order.getSkillLevel());

				ThreadPoolManager.getInstance().execute(new Runnable() {

					@Override
					public void run() {
						summon.getController().useSkill(order);
					}
				});
			}
		}
		else
			log.warn("summon castspell on a wrong target on {}.", player.getName());
	}
}
