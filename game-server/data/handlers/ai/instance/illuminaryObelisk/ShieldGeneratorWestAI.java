package ai.instance.illuminaryObelisk;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author Estrayl
 */
@AIName("western_generator")
public class ShieldGeneratorWestAI extends ShieldGeneratorAI {

	public ShieldGeneratorWestAI(Npc owner) {
		super(owner);
	}

	@Override
	protected SM_SYSTEM_MESSAGE getAttackMsg() {
		return SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_DEFENCE_02_ATTACKED();
	}

	@Override
	protected SM_SYSTEM_MESSAGE getChargeMsg() {
		return SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_OBJ_CHARGE_02();
	}

	@Override
	protected SM_SYSTEM_MESSAGE getDestructionMsg() {
		return SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_OBJ_DESTROY_02();
	}

	@Override
	protected SM_SYSTEM_MESSAGE getGateMsg() {
		return SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_N_WAVE_02_BEGIN();
	}

	@Override
	protected void handleChargeComplete() {
		charges.add((Npc) spawn(702221 + chargeCount, 255.38824f, 211.9726f, 321.37753f, (byte) 90));
	}

	@Override
	protected void handleVortexSpawn() {
		spawn(702015, 255.7034f, 171.83853f, 325.81653f, (byte) 0, 18); // west
		shout(SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_N_WAVE_01_BEGIN());
	}

	@Override
	protected void handleVortexDespawn() {
		getPosition().getWorldMapInstance().getNpcs(702015).forEach(npc -> npc.getController().delete());
	}
}
