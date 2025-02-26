package instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author xTz, Luzien, Majka
 */
@InstanceID(300160000)
public class LowerUdasTempleInstance extends GeneralInstanceHandler {

	private final static WorldPosition[] trap_positions = { new WorldPosition(300160000, 749.4821f, 880.4201f, 154.0f, (byte) 0),
		new WorldPosition(300160000, 745.0469f, 881.81305f, 154.0f, (byte) 0), new WorldPosition(300160000, 746.5357f, 894.3326f, 154.0f, (byte) 0),
		new WorldPosition(300160000, 744.6791f, 891.93774f, 154.0f, (byte) 0), new WorldPosition(300160000, 740.22205f, 889.76013f, 154.0f, (byte) 0),
		new WorldPosition(300160000, 747.434f, 892.34344f, 154.0f, (byte) 0), new WorldPosition(300160000, 742.4482f, 877.25726f, 154.0f, (byte) 0),
		new WorldPosition(300160000, 753.5256f, 887.18353f, 154.0f, (byte) 0), new WorldPosition(300160000, 737.72534f, 881.5906f, 154.0f, (byte) 0),
		new WorldPosition(300160000, 754.2026f, 891.1411f, 154.0f, (byte) 0), new WorldPosition(300160000, 752.56525f, 882.522f, 154.0f, (byte) 0),
		new WorldPosition(300160000, 747.5493f, 877.8989f, 154.0f, (byte) 0), new WorldPosition(300160000, 742.0631f, 880.5939f, 154.0f, (byte) 0),
		new WorldPosition(300160000, 743.5199f, 894.62134f, 154.0f, (byte) 0), new WorldPosition(300160000, 750.18896f, 888.14606f, 154.0f, (byte) 0),
		new WorldPosition(300160000, 734.8378f, 881.8151f, 154.0f, (byte) 0), new WorldPosition(300160000, 749.77515f, 890.7882f, 154.0f, (byte) 0),
		new WorldPosition(300160000, 736.18536f, 885.216f, 154.0f, (byte) 0), new WorldPosition(300160000, 747.9022f, 881.5243f, 154.0f, (byte) 0),
		new WorldPosition(300160000, 749.48315f, 884.58484f, 154.0f, (byte) 0), new WorldPosition(300160000, 737.6233f, 890.88306f, 154.0f, (byte) 0),
		new WorldPosition(300160000, 739.68243f, 879.1843f, 154.0f, (byte) 0), new WorldPosition(300160000, 738.9124f, 884.0289f, 154.0f, (byte) 0),
		new WorldPosition(300160000, 752.7236f, 884.03937f, 154.0f, (byte) 0), new WorldPosition(300160000, 753.46356f, 880.17993f, 154.0f, (byte) 0),
		new WorldPosition(300160000, 736.9874f, 878.4464f, 154.0f, (byte) 0), new WorldPosition(300160000, 737.6233f, 894.2839f, 154.0f, (byte) 0),
		new WorldPosition(300160000, 735.89667f, 888.10345f, 154.0f, (byte) 0), new WorldPosition(300160000, 740.22205f, 893.73846f, 154.0f, (byte) 0),
		new WorldPosition(300160000, 751.57184f, 893.03406f, 154.0f, (byte) 0), new WorldPosition(300160000, 742.10815f, 891.3489f, 154.0f, (byte) 0),
		new WorldPosition(300160000, 738.27075f, 887.3014f, 154.0f, (byte) 0), new WorldPosition(300160000, 744.6297f, 879.0218f, 154.0f, (byte) 0),
		new WorldPosition(300160000, 752.0531f, 889.8899f, 154.0f, (byte) 0), new WorldPosition(300160000, 750.2841f, 877.8855f, 154.0f, (byte) 0),
		new WorldPosition(300160000, 749.3269f, 895.6159f, 154.0f, (byte) 0) };

	private List<Npc> traps = new ArrayList<>();
	private AtomicBoolean wasSpawned = new AtomicBoolean();

	public LowerUdasTempleInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceCreate() {
		float chance = Rnd.chance();
		if (chance < 20) { // spawn named drop chests, 20% both, 30% epic, 50% fabled chest
			spawn(216150, 455.984f, 1192.506f, 190.221f, (byte) 116);
			spawn(216645, 435.664f, 1182.577f, 190.221f, (byte) 116);
		} else if (chance < 50) {
			spawn(216150, 455.984f, 1192.506f, 190.221f, (byte) 116);
		} else {
			spawn(216645, 435.664f, 1182.577f, 190.221f, (byte) 116);
		}
	}

	@Override
	public void onEnterInstance(Player player) {
		if (wasSpawned.compareAndSet(false, true)) {
			traps.add((Npc) spawn(216531, 744.7521f, 885.8238f, 152.7852f, (byte) 30)); // Zhanim The Librarian
			for (WorldPosition position : trap_positions)
				traps.add((Npc) spawn(216530, position.getX(), position.getY(), position.getZ(), (byte) 0)); // Ancient Trap
		}
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		for (Npc trap : traps) {
			if (trap != null && trap.getNpcId() != 216531)
				trap.getController().delete();
		}
	}
}
