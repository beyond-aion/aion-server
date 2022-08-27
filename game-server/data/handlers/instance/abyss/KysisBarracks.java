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
@InstanceID(301280000)
public class KysisBarracks extends AbstractInnerUpperAbyssInstance {

	public KysisBarracks(WorldMapInstance instance) {
		super(instance);
	}

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
		super.onDie(npc);
		switch (npc.getNpcId()) {
			case 233642 -> openDoor(5);
			case 233643 -> openDoor(10);
			case 233644 -> openDoor(30);
			case 233645 -> openDoor(6);
			case 233654 -> openDoor(29);
			case 233655 -> openDoor(31);
			case 233656 -> openDoor(12);
			case 233657 -> openDoor(32);
			case 233666 -> openDoor(8);
			case 233667 -> openDoor(9);
			case 233668 -> openDoor(13);
			case 233669 -> openDoor(7);
			case 233674 -> openDoor(16);
			case 233675, 233676 -> spawnChests();
			case 215414 -> switchToEasyMode();
			case 235536 -> rewardStatueKill(3000);
			case 235537 -> rewardStatueKill(6000);
			case 235538 -> rewardStatueKill(12000);
		}
	}

}
