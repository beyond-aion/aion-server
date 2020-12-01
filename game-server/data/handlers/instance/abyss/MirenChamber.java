package instance.abyss;

import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * Created on June 23rd, 2016
 *
 * @author Estrayl
 * @since AION 4.8
 */
@InstanceID(300130000)
public class MirenChamber extends AbstractInnerUpperAbyssInstance {

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
		switch (npc.getNpcId()) {
			case 215188:
				openDoor(78);
				break;
			case 215189:
				openDoor(77);
				break;
			case 215190:
				openDoor(15);
				break;
			case 215191:
				openDoor(11);
				break;
			case 215200:
				openDoor(12);
				break;
			case 215201:
				openDoor(7);
				break;
			case 215202:
				openDoor(81);
				break;
			case 215203:
				openDoor(8);
				break;
			case 215212:
				openDoor(13);
				break;
			case 215213:
				openDoor(75);
				break;
			case 215214:
				openDoor(16);
				break;
			case 215215:
				openDoor(82);
				break;
			case 215220:
				openDoor(73);
				break;
			case 215221:
			case 215222:
				spawnChests();
				break;
			case 215415:
				switchToEasyMode();
				break;
		}
	}
}
