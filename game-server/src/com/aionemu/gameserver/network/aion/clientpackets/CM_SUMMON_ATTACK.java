package com.aionemu.gameserver.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author ATracer
 */
public class CM_SUMMON_ATTACK extends AionClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_SUMMON_ATTACK.class);

	private int summonObjId;
	private int targetObjId;
	@SuppressWarnings("unused")
	private int unk1;

	private int time;
	@SuppressWarnings("unused")
	private int unk3;

	public CM_SUMMON_ATTACK(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		summonObjId = readD();
		targetObjId = readD();
		unk1 = readC();
		time = readH();
		unk3 = readC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		Summon summon = player.getSummon();
		if (summon == null) {
			log.warn("summon attack without active summon on " + player.getName() + ".");
			return;
		}

		if (summon.getObjectId() != summonObjId) {
			log.warn("summon attack from a different summon instance on " + player.getName() + ".");
			return;
		}

		VisibleObject obj = summon.getKnownList().getObject(targetObjId);
		if (obj instanceof Creature) {
			summon.getController().attackTarget((Creature) obj, time, false);
		} else
			log.warn("summon attack on a wrong target on " + player.getName());
	}
}
