package instance.abyss;

import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * Created on June 23rd, 2016
 *
 * @author Estrayl
 * @since AION 4.8
 */
@InstanceID(301290000)
public class MirenBarracks extends AbstractInnerUpperAbyssInstance {

	@Override
	protected int getBossId() {
		return 233719;
	}

	@Override
	protected int getChestId() {
		return 702296;
	}

	@Override
	protected int getDoorId() {
		return 700547;
	}

	@Override
	protected int getKeymasterId() {
		return 233713;
	}

	@Override
	protected int getTimerNpcId() {
		return 206097;
	}

	@Override
	public void onDie(Npc npc) {
		switch (npc.getNpcId()) {
			case 233685:
				openDoor(78);
				break;
			case 233686:
				openDoor(77);
				break;
			case 233687:
				openDoor(15);
				break;
			case 233688:
				openDoor(11);
				break;
			case 233697:
				openDoor(12);
				break;
			case 233698:
				openDoor(7);
				break;
			case 233699:
				openDoor(81);
				break;
			case 233700:
				openDoor(8);
				break;
			case 233709:
				openDoor(13);
				break;
			case 233710:
				openDoor(75);
				break;
			case 233711:
				openDoor(16);
				break;
			case 233712:
				openDoor(82);
				break;
			case 233717:
				openDoor(73);
				break;
			case 233718:
			case 233719:
				spawnChests();
				break;
			case 215415:
				switchToEasyMode();
				break;
			case 235536:
				rewardStatueKill(3000);
				break;
			case 235537:
				rewardStatueKill(6000);
				break;
			case 235538:
				rewardStatueKill(12000);
				break;
		}
	}

}
