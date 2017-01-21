package ai.instance.illuminaryObelisk;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author Estrayl
 */
@AIName("eastern_generator")
public class ShieldGeneratorEastAI extends ShieldGeneratorAI {

	@Override
	protected int getAttackMsg() {
		return 1402220;
	}

	@Override
	protected int getChargeMsg() {
		if (chargeCount == 2)
			return 1402198;
		return 1402135;
	}

	@Override
	protected int getDestructionMsg() {
		return 1402139;
	}

	@Override
	protected int getGateMsg() {
		return 1402224;
	}

	@Override
	protected void handleChargeComplete() {
		switch (chargeCount) {
			case 0:
				support.add((Npc) spawn(702218, 255.53876f, 297.46393f, 321.375f, (byte) 30));
				break;
			case 1:
				support.add((Npc) spawn(702219, 255.53876f, 297.46393f, 321.375f, (byte) 30));
				break;
			case 2:
				support.add((Npc) spawn(702220, 255.53876f, 297.46393f, 321.375f, (byte) 30));
				break;
		}
	}

	@Override
	protected void phaseAttack() {
		sp(233720, 257.31f, 328.03f, 325.00f, (byte) 91, 2000, "1_left_301230000");
		sp(233721, 253.57f, 328.10f, 325.00f, (byte) 91, 2500, "1_right_301230000");
		sp(233722, 255.40f, 326.54f, 325.00f, (byte) 91, 3000, "1_center_301230000");
		sp(233723, 257.31f, 328.03f, 325.00f, (byte) 91, 11000, "1_left_301230000");
		sp(233724, 253.57f, 328.10f, 325.00f, (byte) 91, 11500, "1_right_301230000");
		sp(233725, 255.40f, 326.54f, 325.00f, (byte) 91, 12000, "1_center_301230000");
		sp(233726, 257.31f, 328.03f, 325.00f, (byte) 91, 21000, "1_left_301230000");
		sp(233727, 253.57f, 328.10f, 325.00f, (byte) 91, 21500, "1_right_301230000");
		sp(233728, 255.40f, 326.54f, 325.00f, (byte) 91, 22000, "1_center_301230000");
	}
}
