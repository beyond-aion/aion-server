package instance.abyss;

import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * Created on June 23rd, 2016
 *
 * @author Estrayl
 * @since AION 4.8
 */
@InstanceID(300140000)
public class KrotanChamber extends AbstractInnerUpperAbyssInstance {

	public KrotanChamber(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	protected int getBossId() {
		return 215136;
	}

	@Override
	protected int getChestId() {
		return 700539;
	}

	@Override
	protected int getDoorId() {
		return 700545;
	}

	@Override
	protected int getKeymasterId() {
		return 215130;
	}

	@Override
	protected int getTimerNpcId() {
		return 206095;
	}

	@Override
	public void onDie(Npc npc) {
		super.onDie(npc);
		switch (npc.getNpcId()) {
			case 215102 -> openDoor(10);
			case 215103 -> openDoor(5);
			case 215104 -> openDoor(30);
			case 215105 -> openDoor(6);
			case 215114 -> openDoor(32);
			case 215115 -> openDoor(31);
			case 215116 -> openDoor(12);
			case 215117 -> openDoor(29);
			case 215126 -> openDoor(13);
			case 215127 -> openDoor(8);
			case 215128 -> openDoor(7);
			case 215129 -> openDoor(9);
			case 215134 -> openDoor(16);
			case 215135, 215136 -> spawnChests();
			case 215413 -> switchToEasyMode();
		}
	}
}
