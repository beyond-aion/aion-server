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
@InstanceID(301300000)
public class KrotanBarracks extends AbstractInnerUpperAbyssInstance {

	public KrotanBarracks(WorldMapInstance instance) {
		super(instance);
	}

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
		super.onDie(npc);
		switch (npc.getNpcId()) {
			case 233599 -> openDoor(10);
			case 233600 -> openDoor(5);
			case 233601 -> openDoor(30);
			case 233602 -> openDoor(6);
			case 233611 -> openDoor(32);
			case 233612 -> openDoor(31);
			case 233613 -> openDoor(12);
			case 233614 -> openDoor(29);
			case 233623 -> openDoor(13);
			case 233624 -> openDoor(8);
			case 233625 -> openDoor(7);
			case 233626 -> openDoor(9);
			case 233631 -> openDoor(16);
			case 233632, 233633 -> spawnChests();
			case 215413 -> switchToEasyMode();
			case 235536 -> rewardStatueKill(3000);
			case 235537 -> rewardStatueKill(6000);
			case 235538 -> rewardStatueKill(12000);
		}
	}

}
