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
@InstanceID(300130000)
public class MirenChamber extends AbstractInnerUpperAbyssInstance {

	public MirenChamber(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	protected int getBossId() {
		return 215222;
	}

	@Override
	protected int getChestId() {
		return 700543;
	}

	@Override
	protected int getDoorId() {
		return 700547;
	}

	@Override
	protected int getKeymasterId() {
		return 215216;
	}

	@Override
	protected int getTimerNpcId() {
		return 206097;
	}

	@Override
	public void onDie(Npc npc) {
		super.onDie(npc);
		switch (npc.getNpcId()) {
			case 215188 -> openDoor(78);
			case 215189 -> openDoor(77);
			case 215190 -> openDoor(15);
			case 215191 -> openDoor(11);
			case 215200 -> openDoor(12);
			case 215201 -> openDoor(7);
			case 215202 -> openDoor(81);
			case 215203 -> openDoor(8);
			case 215212 -> openDoor(13);
			case 215213 -> openDoor(75);
			case 215214 -> openDoor(16);
			case 215215 -> openDoor(82);
			case 215220 -> openDoor(73);
			case 215221, 215222 -> spawnChests();
			case 215415 -> switchToEasyMode();
		}
	}
}
