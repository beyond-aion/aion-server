package ai.instance.illuminaryObelisk;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author Estrayl
 */
@AIName("northern_generator")
public class ShieldGeneratorNorthAI extends ShieldGeneratorAI {

	public ShieldGeneratorNorthAI(Npc owner) {
		super(owner);
	}

	@Override
	protected SM_SYSTEM_MESSAGE getAttackMsg() {
		return SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_DEFENCE_04_ATTACKED();
	}

	@Override
	protected SM_SYSTEM_MESSAGE getChargeMsg() {
		return SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_OBJ_CHARGE_04();
	}

	@Override
	protected SM_SYSTEM_MESSAGE getDestructionMsg() {
		return SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_OBJ_DESTROY_04();
	}

	@Override
	protected SM_SYSTEM_MESSAGE getGateMsg() {
		return SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_N_WAVE_04_BEGIN();
	}

	@Override
	protected void handleChargeComplete() {
		charges.add((Npc) spawn(702227 + chargeCount, 212.64922f, 254.5639f, 295.94763f, (byte) 60));
	}

	@Override
	protected void handleVortexSpawn() {
		spawn(702017, 169.5563f, 254.52907f, 293.04276f, (byte) 0, 17); // north
		shout(SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_N_WAVE_04_BEGIN());
	}

	@Override
	protected void handleVortexDespawn() {
		getPosition().getWorldMapInstance().getNpcs(702017).forEach(npc -> npc.getController().delete());
	}
}
