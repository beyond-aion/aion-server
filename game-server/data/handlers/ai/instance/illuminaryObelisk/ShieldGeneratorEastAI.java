package ai.instance.illuminaryObelisk;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author Estrayl
 */
@AIName("eastern_generator")
public class ShieldGeneratorEastAI extends ShieldGeneratorAI {

	public ShieldGeneratorEastAI(Npc owner) {
		super(owner);
	}

	@Override
	protected SM_SYSTEM_MESSAGE getAttackMsg() {
		return SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_DEFENCE_01_ATTACKED();
	}

	@Override
	protected SM_SYSTEM_MESSAGE getChargeMsg() {
		return SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_OBJ_CHARGE_01();
	}

	@Override
	protected SM_SYSTEM_MESSAGE getDestructionMsg() {
		return SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_OBJ_DESTROY_01();
	}

	@Override
	protected SM_SYSTEM_MESSAGE getGateMsg() {
		return SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_N_WAVE_01_BEGIN();
	}

	@Override
	protected void handleChargeComplete() {
		charges.add((Npc) spawn(702218 + chargeCount, 255.53876f, 297.46393f, 321.375f, (byte) 30));
	}

	@Override
	protected void handleVortexSpawn() {
		spawn(702014, 255.7926f, 338.22058f, 325.56473f, (byte) 0, 60); // east
		shout(SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_N_WAVE_01_BEGIN());
	}

	@Override
	protected void handleVortexDespawn() {
		getPosition().getWorldMapInstance().getNpcs(702014).forEach(npc -> npc.getController().delete());
	}
}
