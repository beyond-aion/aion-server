package ai.instance.drakenspire;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.NoActionAI;

/**
 * @author Estrayl
 */
@AIName("wave_portal")
public class WavePortalAI extends NoActionAI {

	private List<Future<?>> tasks = new ArrayList<>();

	public WavePortalAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		int staticId = getOwner().getSpawn().getStaticId();
		tasks.add(ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			switch (staticId) {
				case 84 -> {// Top Left
					spawn(236204, 581.92f, 823.65f, 1609.64f, (byte) 15, "301390000_Wave_Top_Left_01");
					spawn(236204, 581.92f, 823.65f, 1609.64f, (byte) 15, "301390000_Wave_Top_Left_02");
				}
				case 405 -> {// Central
					spawn(Rnd.get(236216, 236220), 635.17f, 811.90f, 1598.50f, (byte) 30, "301390000_Wave_Commander_Left", 1000);
					spawn(Rnd.get(236216, 236220), 635.17f, 811.90f, 1598.50f, (byte) 30, "301390000_Wave_Commander_Middle", 0);
					spawn(Rnd.get(236216, 236220), 635.17f, 811.90f, 1598.50f, (byte) 30, "301390000_Wave_Commander_Right", 1000);
				}
				case 548 -> {// Bottom Left
					spawn(236204, 687.29f, 825.78f, 1609.66f, (byte) 45, "301390000_Wave_Bottom_Left_01");
					spawn(236204, 687.29f, 825.78f, 1609.66f, (byte) 45, "301390000_Wave_Bottom_Left_02");
				}
				case 398 -> {// Bottom Central
					spawn(236205, 704.21f, 877.60f, 1604.55f, (byte) 60, "301390000_Wave_Bottom_Central_01");
					spawn(236205, 704.21f, 877.60f, 1604.55f, (byte) 60, "301390000_Wave_Bottom_Central_02");
				}
				case 401 -> {// Top Central
					spawn(236205, 575.15f, 877.52f, 1600.89f, (byte) 0, "301390000_Wave_Top_Central_01");
					spawn(236205, 575.15f, 877.52f, 1600.89f, (byte) 0, "301390000_Wave_Top_Central_02");
				}
				case 399 -> {// Bottom Right
					spawn(236206, 690.59f, 932.85f, 1618.27f, (byte) 75, "301390000_Wave_Bottom_Right_01");
					spawn(236206, 690.59f, 932.85f, 1618.27f, (byte) 75, "301390000_Wave_Bottom_Right_02");
				}
				case 407 -> {// Top Right
					spawn(236206, 576.92f, 936.11f, 1620.33f, (byte) 104, "301390000_Wave_Top_Right_01");
					spawn(236206, 576.92f, 936.11f, 1620.33f, (byte) 104, "301390000_Wave_Top_Right_02");
				}
			}
		}, 8000, staticId == 405 ? 40000 : 22000));
	}

	private void spawn(int npcId, float x, float y, float z, int heading, String walkerId) {
		spawn(npcId, x, y, z, heading, walkerId, 0);
	}

	private void spawn(int npcId, float x, float y, float z, int heading, String walkerId, int delay) {
		tasks.add(ThreadPoolManager.getInstance().schedule(() -> {
			Npc npc = (Npc) spawn(npcId, x, y, z, (byte) heading);
			npc.getSpawn().setWalkerId(walkerId);
		}, delay));
	}

	@Override
	protected void handleDespawned() {
		for (Future<?> task : tasks)
			if (task != null && !task.isCancelled())
				task.cancel(true);
		super.handleDespawned();
	}
}
