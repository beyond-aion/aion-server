package ai.instance.illuminaryObelisk;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author Estrayl
 */
@AIName("southern_generator")
public class ShieldGeneratorSouthAI extends ShieldGeneratorAI {

	public ShieldGeneratorSouthAI(Npc owner) {
		super(owner);
	}

	@Override
	protected SM_SYSTEM_MESSAGE getAttackMsg() {
		return SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_DEFENCE_03_ATTACKED();
	}

	@Override
	protected SM_SYSTEM_MESSAGE getChargeMsg() {
		return SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_OBJ_CHARGE_03();
	}

	@Override
	protected SM_SYSTEM_MESSAGE getDestructionMsg() {
		return SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_OBJ_DESTROY_03();
	}

	@Override
	protected SM_SYSTEM_MESSAGE getGateMsg() {
		return SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_N_WAVE_03_BEGIN();
	}

	@Override
	protected void handleChargeComplete() {
		charges.add((Npc) spawn(702224 + chargeCount, 298.304f, 254.48207f, 295.95157f, (byte) 0));
	}

	@Override
	protected void handleVortexSpawn() {
		spawn(702016, 343.1202f, 254.10585f, 291.62302f, (byte) 0, 34); // south
		shout(SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_N_WAVE_01_BEGIN());
	}

	@Override
	protected void handleVortexDespawn() {
		getPosition().getWorldMapInstance().getNpcs(702016).forEach(npc -> npc.getController().delete());
	}
}
