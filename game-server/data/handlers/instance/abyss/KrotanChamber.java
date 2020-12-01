package instance.abyss;

import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * Created on June 23rd, 2016
 *
 * @author Estrayl
 * @since AION 4.8
 */
@InstanceID(300140000)
public class KrotanChamber extends AbstractInnerUpperAbyssInstance {

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
		switch (npc.getNpcId()) {
			case 215102:
				openDoor(10);
				break;
			case 215103:
				openDoor(5);
				break;
			case 215104:
				openDoor(30);
				break;
			case 215105:
				openDoor(6);
				break;
			case 215114:
				openDoor(32);
				break;
			case 215115:
				openDoor(31);
				break;
			case 215116:
				openDoor(12);
				break;
			case 215117:
				openDoor(29);
				break;
			case 215126:
				openDoor(13);
				break;
			case 215127:
				openDoor(8);
				break;
			case 215128:
				openDoor(7);
				break;
			case 215129:
				openDoor(9);
				break;
			case 215134:
				openDoor(16);
				break;
			case 215135:
			case 215136:
				spawnChests();
				break;
			case 215413:
				switchToEasyMode();
				break;
		}
	}
}
