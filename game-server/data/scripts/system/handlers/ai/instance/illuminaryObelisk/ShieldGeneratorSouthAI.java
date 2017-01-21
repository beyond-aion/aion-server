package ai.instance.illuminaryObelisk;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author Estrayl
 */
@AIName("southern_generator")
public class ShieldGeneratorSouthAI extends ShieldGeneratorAI {

	@Override
	protected int getAttackMsg() {
		return 1402222;
	}

	@Override
	protected int getChargeMsg() {
		if (chargeCount == 2)
			return 1402200;
		return 1402137;
	}

	@Override
	protected int getGateMsg() {
		return 1402226;
	}

	@Override
	protected int getDestructionMsg() {
		return 1402141;
	}

	@Override
	protected void handleChargeComplete() {
		switch (chargeCount) {
			case 0:
				support.add((Npc) spawn(702224, 298.304f, 254.48207f, 295.95157f, (byte) 119));
				break;
			case 1:
				support.add((Npc) spawn(702225, 298.304f, 254.48207f, 295.95157f, (byte) 119));
				break;
			case 2:
				support.add((Npc) spawn(702226, 298.304f, 254.48207f, 295.95157f, (byte) 119));
				break;
		}
	}

	@Override
	protected void phaseAttack() {
		sp(233738, 329.78f, 251.68f, 291.83f, (byte) 60, 2000, "3_left_301230000");
		sp(233739, 329.84f, 256.80f, 291.83f, (byte) 60, 2500, "3_right_301230000");
		sp(233730, 328.09f, 254.24f, 291.83f, (byte) 60, 3000, "3_center_301230000");
		sp(233731, 329.78f, 251.68f, 291.83f, (byte) 60, 11000, "3_left_301230000");
		sp(233732, 329.84f, 256.80f, 291.83f, (byte) 60, 11500, "3_right_301230000");
		sp(233733, 328.09f, 254.24f, 291.83f, (byte) 60, 12000, "3_center_301230000");
		sp(233734, 329.78f, 251.68f, 291.83f, (byte) 60, 21000, "3_left_301230000");
		sp(233735, 329.84f, 256.80f, 291.83f, (byte) 60, 21500, "3_right_301230000");
		sp(233736, 328.09f, 254.24f, 291.83f, (byte) 60, 22000, "3_center_301230000");
	}
}
