package instance.abyss;

import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * Created on June 23rd, 2016
 *
 * @author Estrayl
 * @since AION 4.8
 */
@InstanceID(301300000)
public class KrotanBarracks extends AbstractInnerUpperAbyssInstance {

	@Override
	protected int getBossId() {
		return 233633;
	}

	@Override
	protected int getChestId() {
		return 702288;
	}

	@Override
	protected int getDoorId() {
		return 700545;
	}

	@Override
	protected int getKeymasterId() {
		return 233627;
	}

	@Override
	protected int getTimerNpcId() {
		return 206095;
	}

	@Override
	public void onDie(Npc npc) {
		switch (npc.getNpcId()) {
			case 233599:
				openDoor(10);
				break;
			case 233600:
				openDoor(5);
				break;
			case 233601:
				openDoor(30);
				break;
			case 233602:
				openDoor(6);
				break;
			case 233611:
				openDoor(32);
				break;
			case 233612:
				openDoor(31);
				break;
			case 233613:
				openDoor(12);
				break;
			case 233614:
				openDoor(29);
				break;
			case 233623:
				openDoor(13);
				break;
			case 233624:
				openDoor(8);
				break;
			case 233625:
				openDoor(7);
				break;
			case 233626:
				openDoor(9);
				break;
			case 233631:
				openDoor(16);
				break;
			case 233632:
			case 233633:
				spawnChests();
				break;
			case 215413:
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
