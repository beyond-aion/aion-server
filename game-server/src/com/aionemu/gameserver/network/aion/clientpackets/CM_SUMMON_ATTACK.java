package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.animations.AttackAnimation;
import com.aionemu.gameserver.model.animations.AttackTypeAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
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
	private byte attackIndex;
	private int time;
	private byte animation;

	public CM_SUMMON_ATTACK(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		summonObjId = readD();
		targetObjId = readD();
		attackIndex = readC();
		time = readUH();
		animation = readC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		Creature summonOrMercenary = player.getSummonOrMercenary(summonObjId);
		if (summonOrMercenary == null) // commonly due to lags when the pet dies
			return;

		VisibleObject obj = summonOrMercenary.getKnownList().getObject(targetObjId); // may be null due to lags during movement
		if (obj instanceof Creature creature)
			summonOrMercenary.getController().attackTarget(creature, time, AttackTypeAnimation.getById(attackIndex),
				AttackAnimation.getById(animation), false);
		else if (obj != null) // not a creature (attack should be client restricted)
			AuditLogger.log(player, "tried to use summon attack on a wrong target: " + obj);
	}
}
