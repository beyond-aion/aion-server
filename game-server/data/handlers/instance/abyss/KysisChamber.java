package instance.abyss;

import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * Created on June 23rd, 2016
 *
 * @author Estrayl
 * @since AION 4.8
 */
@InstanceID(300120000)
public class KysisChamber extends AbstractInnerUpperAbyssInstance {

	@Override
	protected int getBossId() {
		return 215179;
	}

	@Override
	protected int getChestId() {
		return 700541;
	}

	@Override
	protected int getDoorId() {
		return 700546;
	}

	@Override
	protected int getKeymasterId() {
		return 215173;
	}

	@Override
	protected int getTimerNpcId() {
		return 206096;
	}

	@Override
	public void onDie(Npc npc) {
		switch (npc.getNpcId()) {
			case 215145:
				openDoor(5);
				break;
			case 215146:
				openDoor(10);
				break;
			case 215147:
				openDoor(30);
				break;
			case 215148:
				openDoor(6);
				break;
			case 215157:
				openDoor(29);
				break;
			case 215158:
				openDoor(31);
				break;
			case 215159:
				openDoor(12);
				break;
			case 215160:
				openDoor(32);
				break;
			case 215169:
				openDoor(8);
				break;
			case 215170:
				openDoor(9);
				break;
			case 215171:
				openDoor(13);
				break;
			case 215172:
				openDoor(7);
				break;
			case 215177:
				openDoor(16);
				break;
			case 215178:
			case 215179:
				spawnChests();
				break;
			case 215414:
				switchToEasyMode();
				break;
		}
	}

}
