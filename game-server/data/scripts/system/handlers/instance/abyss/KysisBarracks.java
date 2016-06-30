package instance.abyss;

import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * Created on June 23rd, 2016
 *
 * @author Estrayl
 * @since AION 4.8
 */
@InstanceID(301280000)
public class KysisBarracks extends AbstractInnerUpperAbyssInstance {

	@Override
	protected int getBossId() {
		return 233676;
	}

	@Override
	protected int getChestId() {
		return 702292;
	}

	@Override
	protected int getDoorId() {
		return 700546;
	}

	@Override
	protected int getKeymasterId() {
		return 233670;
	}

	@Override
	protected int getTimerNpcId() {
		return 206096;
	}

	@Override
	public void onDie(Npc npc) {
		switch (npc.getNpcId()) {
			case 233642:
				openDoor(5);
				break;
			case 233643:
				openDoor(10);
				break;
			case 233644:
				openDoor(30);
				break;
			case 233645:
				openDoor(6);
				break;
			case 233654:
				openDoor(29);
				break;
			case 233655:
				openDoor(31);
				break;
			case 233656:
				openDoor(12);
				break;
			case 233657:
				openDoor(32);
				break;
			case 233666:
				openDoor(8);
				break;
			case 233667:
				openDoor(9);
				break;
			case 233668:
				openDoor(13);
				break;
			case 233669:
				openDoor(7);
				break;
			case 233674:
				openDoor(16);
				break;
			case 233675:
			case 233676:
				spawnChests();
				break;
			case 215414:
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
