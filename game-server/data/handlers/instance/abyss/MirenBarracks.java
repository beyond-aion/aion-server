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
@InstanceID(301290000)
public class MirenBarracks extends AbstractInnerUpperAbyssInstance {

	public MirenBarracks(WorldMapInstance instance) {
		super(instance);
	}

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
		super.onDie(npc);
		switch (npc.getNpcId()) {
			case 233685 -> openDoor(78);
			case 233686 -> openDoor(77);
			case 233687 -> openDoor(15);
			case 233688 -> openDoor(11);
			case 233697 -> openDoor(12);
			case 233698 -> openDoor(7);
			case 233699 -> openDoor(81);
			case 233700 -> openDoor(8);
			case 233709 -> openDoor(13);
			case 233710 -> openDoor(75);
			case 233711 -> openDoor(16);
			case 233712 -> openDoor(82);
			case 233717 -> openDoor(73);
			case 233718, 233719 -> spawnChests();
			case 215415 -> switchToEasyMode();
			case 235536 -> rewardStatueKill(3000);
			case 235537 -> rewardStatueKill(6000);
			case 235538 -> rewardStatueKill(12000);
		}
	}

}
