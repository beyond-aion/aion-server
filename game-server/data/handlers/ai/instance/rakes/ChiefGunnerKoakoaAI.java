package ai.instance.rakes;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.ai.Percentage;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.SummonerAI;

/**
 * @author xTz
 */
@AIName("gunnerkoakoa")
public class ChiefGunnerKoakoaAI extends SummonerAI {

	public ChiefGunnerKoakoaAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleIndividualSpawnedSummons(Percentage percent) {
		if (getEffectController().hasAbnormalEffect(18552)) {
			checkAbnormalEffect();
		}
		randomSpawn(Rnd.get(1, 3));
	}

	private void checkAbnormalEffect() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				getEffectController().removeEffect(18552);
				// to do remove pause
			}
		}, 21000);
	}

	private void randomSpawn(int i) {
		// to do pause boss
		spawn(281212, 757.39746f, 508.70383f, 1012.30084f, (byte) 0);
		switch (i) {
			case 1:
				spawn(281212, 726.1167f, 503.28836f, 1012.6846f, (byte) 0);
				spawn(281212, 736.4446f, 505.3141f, 1012.1576f, (byte) 0);
				spawn(281212, 746.9261f, 503.50122f, 1012.68335f, (byte) 0);
				spawn(281212, 728.9705f, 492.59402f, 1012.68335f, (byte) 0);
				spawn(281212, 739.9526f, 491.54123f, 1011.692f, (byte) 0);
				spawn(281212, 749.754f, 491.74677f, 1011.8663f, (byte) 0);
				spawn(281212, 756.9996f, 500.01736f, 1011.692f, (byte) 0);
				spawn(281213, 736.9722f, 514.6446f, 1011.8599f, (byte) 0);
				spawn(281213, 747.5162f, 514.51715f, 1011.692f, (byte) 0);
				spawn(281213, 726.8303f, 514.5155f, 1012.6845f, (byte) 0);
				spawn(281213, 727.9019f, 524.578f, 1012.68365f, (byte) 0);
				spawn(281213, 738.52844f, 525.0482f, 1011.692f, (byte) 0);
				spawn(281213, 758.3127f, 520.59143f, 1011.692f, (byte) 0);
				spawn(281213, 748.7474f, 525.84f, 1011.859f, (byte) 0);
				break;
			case 2:
				spawn(281213, 726.1167f, 503.28836f, 1012.6846f, (byte) 0);
				spawn(281213, 736.4446f, 505.3141f, 1012.1576f, (byte) 0);
				spawn(281212, 746.9261f, 503.50122f, 1012.68335f, (byte) 0);
				spawn(281213, 728.9705f, 492.59402f, 1012.68335f, (byte) 0);
				spawn(281213, 739.9526f, 491.54123f, 1011.692f, (byte) 0);
				spawn(281212, 749.754f, 491.74677f, 1011.8663f, (byte) 0);
				spawn(281212, 756.9996f, 500.01736f, 1011.692f, (byte) 0);
				spawn(281212, 736.9722f, 514.6446f, 1011.8599f, (byte) 0);
				spawn(281213, 747.5162f, 514.51715f, 1011.692f, (byte) 0);
				spawn(281212, 726.8303f, 514.5155f, 1012.6845f, (byte) 0);
				spawn(281212, 727.9019f, 524.578f, 1012.68365f, (byte) 0);
				spawn(281212, 738.52844f, 525.0482f, 1011.692f, (byte) 0);
				spawn(281213, 758.3127f, 520.59143f, 1011.692f, (byte) 0);
				spawn(281213, 748.7474f, 525.84f, 1011.859f, (byte) 0);
				break;
			case 3:
				spawn(281212, 726.1167f, 503.28836f, 1012.6846f, (byte) 0);
				spawn(281212, 736.4446f, 505.3141f, 1012.1576f, (byte) 0);
				spawn(281213, 746.9261f, 503.50122f, 1012.68335f, (byte) 0);
				spawn(281212, 728.9705f, 492.59402f, 1012.68335f, (byte) 0);
				spawn(281212, 739.9526f, 491.54123f, 1011.692f, (byte) 0);
				spawn(281213, 749.754f, 491.74677f, 1011.8663f, (byte) 0);
				spawn(281213, 756.9996f, 500.01736f, 1011.692f, (byte) 0);
				spawn(281213, 736.9722f, 514.6446f, 1011.8599f, (byte) 0);
				spawn(281212, 747.5162f, 514.51715f, 1011.692f, (byte) 0);
				spawn(281213, 726.8303f, 514.5155f, 1012.6845f, (byte) 0);
				spawn(281213, 727.9019f, 524.578f, 1012.68365f, (byte) 0);
				spawn(281213, 738.52844f, 525.0482f, 1011.692f, (byte) 0);
				spawn(281212, 758.3127f, 520.59143f, 1011.692f, (byte) 0);
				spawn(281212, 748.7474f, 525.84f, 1011.859f, (byte) 0);
				break;
		}
	}
}
