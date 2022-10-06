package instance;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author xTz, Ritsu, Tibald
 */
@InstanceID(310110000)
public class TheobomosLabInstance extends GeneralInstanceHandler {

	private boolean isInstanceDestroyed;
	private boolean isDead1 = false;
	private boolean isDead2 = false;

	public TheobomosLabInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onDie(Npc npc) {
		if (isInstanceDestroyed)
			return;

		if (npc.getMaster() instanceof Player)
			return;

		switch (npc.getNpcId()) {
			case 214669: // Triroan
				spawn(730178, 571.15f, 490.6607f, 196.7324f, (byte) 60);
				break;
			case 280971:
			case 280972:
				if (guardDie(npc))
					removeBuff();
		}
	}

	/**
	 * @Override public void onEnterInstance(Player player) { final QuestState qs = player.getQuestStateList().getQuestState(1094); if (qs != null &&
	 *           qs.getStatus() == QuestStatus.COMPLETE) doors.get(37).setOpen(true); else doors.get(37).setOpen(false); }//this door is static door, so
	 *           we cant control it.
	 */
	@Override
	public void onInstanceDestroy() {
		isDead1 = false;
		isDead2 = false;
		isInstanceDestroyed = true;
	}

	private boolean guardDie(Npc npc) {
		WorldPosition p = npc.getPosition();
		int npcId = npc.getNpcId();
		Npc orb = getNpc(280973);
		if (orb != null && PositionUtil.getDistance(orb, npc) <= 7) {
			switch (npcId) {
				case 280971 -> isDead1 = true;
				case 280972 -> isDead2 = true;
			}
			return true;
		} else {
			npc.getController().delete();
			spawn(npcId, p.getX(), p.getY(), p.getZ(), (byte) 41);
			return false;
		}
	}

	private void removeBuff() {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isInstanceDestroyed && isDead1 && isDead2) {
				getNpc(214668).getEffectController().removeEffect(18481);
				deleteAliveNpcs(280973);
			}
		}, 1000);
	}

	@Override
	public void onInstanceCreate() {
		if (Rnd.nextBoolean()) {
			switch (Rnd.get(1, 3)) {
				case 1 -> spawn(798223, 256.26215f, 512.9742f, 187.79453f, (byte) 0);
				case 2 -> spawn(798223, 360.16098f, 526.9446f, 186.20251f, (byte) 105);
				case 3 -> spawn(798223, 476.77145f, 541.5697f, 187.79453f, (byte) 90);
			}
		}
	}
}
