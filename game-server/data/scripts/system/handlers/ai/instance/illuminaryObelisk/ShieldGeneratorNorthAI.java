package ai.instance.illuminaryObelisk;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author Estrayl
 */
@AIName("northern_generator")
public class ShieldGeneratorNorthAI extends ShieldGeneratorAI {

	@Override
	protected int getAttackMsg() {
		return 1402223;
	}

	@Override
	protected int getChargeMsg() {
		if (chargeCount == 2)
			return 1402201;
		return 1402138;
	}

	@Override
	protected int getGateMsg() {
		return 1402227;
	}

	@Override
	protected int getDestructionMsg() {
		return 1402142;
	}

	@Override
	protected void handleChargeComplete() {
		switch (chargeCount) {
			case 0:
				support.add((Npc) spawn(702227, 212.64922f, 254.5639f, 295.94763f, (byte) 60));
				break;
			case 1:
				support.add((Npc) spawn(702228, 212.64922f, 254.5639f, 295.94763f, (byte) 60));
				break;
			case 2:
				support.add((Npc) spawn(702229, 212.64922f, 254.5639f, 295.94763f, (byte) 60));
				break;
		}
	}

	@Override
	protected void phaseAttack() {
		sp(233723, 181.01f, 257.40f, 291.83f, (byte) 119, 2000, "4_left_301230000");
		sp(233724, 180.83f, 252.54f, 291.83f, (byte) 119, 2500, "4_right_301230000");
		sp(233725, 183.05f, 254.72f, 291.83f, (byte) 119, 3000, "4_center_301230000");
		sp(233726, 181.01f, 257.40f, 291.83f, (byte) 119, 11000, "4_left_301230000");
		sp(233727, 180.83f, 252.54f, 291.83f, (byte) 119, 11500, "4_right_301230000");
		sp(233728, 183.05f, 254.72f, 291.83f, (byte) 119, 12000, "4_center_301230000");
		sp(233729, 181.01f, 257.40f, 291.83f, (byte) 119, 21000, "4_left_301230000");
		sp(233722, 180.83f, 252.54f, 291.83f, (byte) 119, 21500, "4_right_301230000");
		sp(233721, 183.05f, 254.72f, 291.83f, (byte) 119, 22000, "4_center_301230000");
	}
}
