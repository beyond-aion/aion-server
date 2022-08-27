package instance;

import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Yeats
 */
@InstanceID(301360000)
public class InfernalDanuarReliquaryInstance extends DanuarReliquaryInstance {

	public InfernalDanuarReliquaryInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	protected int getExitId() {
		return 730843;
	}

	@Override
	protected int getTreasureBoxId() {
		return 701795;
	}

	@Override
	protected int getEnragedModorId() {
		return 234691;
	}

	@Override
	protected int getCursedModorId() {
		return 234690;
	}

	@Override
	protected int getRealCloneId() {
		return 855244;
	}

	@Override
	protected int getFakeCloneId() {
		return 855245;
	}

	@Override
	protected void onInstanceEnd(boolean successful) {
		cancelWipeTask();
		if (!successful) {
			Npc modor = getNpc(getEnragedModorId());
			if (modor == null) {
				modor = getNpc(getCursedModorId());
			}
			if (modor != null) {
				PacketSendUtility.broadcastMessage(modor, 1500739);
			}
			instance.forEachNpc(npc -> npc.getController().delete());
		} else {
			Npc modor = getNpc(getEnragedModorId());
			if (modor != null) {
				PacketSendUtility.broadcastMessage(modor, 343629);
			}
			instance.forEachNpc(npc -> {
				if (npc.getNpcId() != getEnragedModorId()) {
					npc.getController().delete();
				}
			});
		}
		spawn(getExitId(), 255.66669f, 263.78525f, 241.7986f, (byte) 86); // Spawn exit portal
	}

}
