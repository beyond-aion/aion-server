package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer
 */
public class CM_SUMMON_ATTACK extends AionClientPacket {

	private int summonObjId;
	private int targetObjId;
	@SuppressWarnings("unused")
	private byte unk1;
	private int time;
	@SuppressWarnings("unused")
	private byte unk3;

	public CM_SUMMON_ATTACK(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		summonObjId = readD();
		targetObjId = readD();
		unk1 = readC();
		time = readUH();
		unk3 = readC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		Summon summon = player.getSummon();
		if (summon == null) // commonly due to lags when the pet dies
			return;

		if (summon.getObjectId() != summonObjId) {
			AuditLogger.log(player, "tried to use summon attack from a different summon instance");
			return;
		}

		VisibleObject obj = summon.getKnownList().getObject(targetObjId); // may be null due to lags during movement
		if (obj instanceof Creature)
			summon.getController().attackTarget((Creature) obj, time, false);
		else if (obj != null) // not a creature (attack should be client restricted)
			AuditLogger.log(player, "tried to use summon attack on a wrong target: " + obj);
	}
}
