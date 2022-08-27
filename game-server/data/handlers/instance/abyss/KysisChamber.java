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
@InstanceID(300120000)
public class KysisChamber extends AbstractInnerUpperAbyssInstance {

	public KysisChamber(WorldMapInstance instance) {
		super(instance);
	}

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
		super.onDie(npc);
		switch (npc.getNpcId()) {
			case 215145 -> openDoor(5);
			case 215146 -> openDoor(10);
			case 215147 -> openDoor(30);
			case 215148 -> openDoor(6);
			case 215157 -> openDoor(29);
			case 215158 -> openDoor(31);
			case 215159 -> openDoor(12);
			case 215160 -> openDoor(32);
			case 215169 -> openDoor(8);
			case 215170 -> openDoor(9);
			case 215171 -> openDoor(13);
			case 215172 -> openDoor(7);
			case 215177 -> openDoor(16);
			case 215178, 215179 -> spawnChests();
			case 215414 -> switchToEasyMode();
		}
	}

}
