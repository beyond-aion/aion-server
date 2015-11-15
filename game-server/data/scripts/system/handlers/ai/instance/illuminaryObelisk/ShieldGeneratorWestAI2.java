package ai.instance.illuminaryObelisk;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author Estrayl
 */
@AIName("western_generator")
public class ShieldGeneratorWestAI2 extends ShieldGeneratorAI2 {

	@Override
	protected int getAttackMsg() {
		return 1402221;
	}

	@Override
	protected int getChargeMsg() {
		if (chargeCount == 2)
			return 1402199;
		return 1402136;
	}

	@Override
	protected int getGateMsg() {
		return 1402225;
	}

	@Override
	protected int getDestructionMsg() {
		return 1402140;
	}

	@Override
	protected void handleChargeComplete() {
		switch (chargeCount) {
			case 0:
				support.add((Npc) spawn(702221, 255.38824f, 211.9726f, 321.37753f, (byte) 89));
				break;
			case 1:
				support.add((Npc) spawn(702222, 255.38824f, 211.9726f, 321.37753f, (byte) 89));
				break;
			case 2:
				support.add((Npc) spawn(702223, 255.38824f, 211.9726f, 321.37753f, (byte) 89));
				break;
		}
	}

	@Override
	protected void phaseAttack() {
		sp(233729, 253.31f, 180.35f, 325.00f, (byte) 30, 2000, "2_left_301230000");
		sp(233730, 257.56f, 180.41f, 325.00f, (byte) 30, 2500, "2_right_301230000");
		sp(233731, 255.39f, 182.25f, 325.00f, (byte) 30, 3000, "2_center_301230000");
		sp(233732, 253.31f, 180.35f, 325.00f, (byte) 30, 11000, "2_left_301230000");
		sp(233733, 257.56f, 180.41f, 325.00f, (byte) 30, 11500, "2_right_301230000");
		sp(233734, 255.39f, 182.25f, 325.00f, (byte) 30, 12000, "2_center_301230000");
		sp(233735, 253.31f, 180.35f, 325.00f, (byte) 30, 21000, "2_left_301230000");
		sp(233736, 257.56f, 180.41f, 325.00f, (byte) 30, 21500, "2_right_301230000");
		sp(233737, 255.39f, 182.25f, 325.00f, (byte) 30, 22000, "2_center_301230000");
	}
}
