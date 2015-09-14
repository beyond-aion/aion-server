package instance.abyss;

import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author Cheatkiller
 */
@InstanceID(301280000)
public class KysisBattleInstance extends KysisBattleInstance_L {

	@Override
	public void onDie(Npc npc) {
		super.onDie(npc);
	}

	@Override
	protected void spawnChests(Npc npc) {
		if (!rewarded) {
			rewarded = true; // safety mechanism
			spawn(702292, 575.6636f, 853.2475f, 199.37367f, (byte) 63);
			spawn(702292, 571.56036f, 869.93604f, 199.37367f, (byte) 69);
			spawn(702292, 560.082f, 882.97943f, 199.37367f, (byte) 76);
			spawn(702292, 545.4042f, 892.1157f, 199.37367f, (byte) 83);
			spawn(702292, 528.2692f, 895.10614f, 199.37366f, (byte) 89);
			spawn(702292, 511.3643f, 891.9409f, 199.37367f, (byte) 96);
			spawn(702292, 496.22192f, 883.09937f, 199.37367f, (byte) 103);
			spawn(702292, 484.93253f, 869.89667f, 199.37427f, (byte) 109);
			spawn(702292, 479.12225f, 853.5763f, 199.37297f, (byte) 116);
			spawn(702292, 478.9662f, 836.40704f, 199.37367f, (byte) 2);
			spawn(702292, 485.365f, 820.3497f, 199.45955f, (byte) 9);
			spawn(702295, 576.4634f, 837.3374f, 199.7f, (byte) 56); // Treasure Room Chest
		}
	}

	@Override
	protected void artifactSpawns(Npc npc) {
		// Only for legion instance
	}

}
