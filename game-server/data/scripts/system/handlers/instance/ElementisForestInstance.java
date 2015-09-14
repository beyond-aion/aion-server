package instance;

import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Luzien
 */
@InstanceID(300260000)
public class ElementisForestInstance extends GeneralInstanceHandler {

	private AtomicInteger spawned = new AtomicInteger();

	@Override
	public void onDie(Npc npc) {
		switch (npc.getNpcId()) {
			case 217233:
				spawn(700999, 303.07858f, 768.25012f, 204.34013f, (byte) 7);
				deleteNpc(700998);
				deleteNpc(282362);
				break;
			case 217238:
				spawn(282204, 472.9886f, 798.10944f, 129.94006f, (byte) 90);
				sendJurdinDialog();
				break;
			case 217234:
				spawn(730378, 574.359f, 429.351f, 125.533f, (byte) 0, 82);
				break;
		}
	}

	@Override
	public void onEnterZone(Player player, ZoneInstance zone) {
		if (zone.getAreaTemplate().getZoneName() == ZoneName.get("CANYONGUARDS_RAVINE_300260000")) {
			if (spawned.compareAndSet(0, 1)) {
				spawn(217233, 301.77118f, 765.36951f, 193.03818f, (byte) 90);
			}
		} else if (zone.getAreaTemplate().getZoneName() == ZoneName.get("JURDINS_DOMAIN_300260000")) {
			if (spawned.compareAndSet(1, 2)) {
				sendMsg(1500242);
			}
		}
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 282440:
				SkillEngine.getInstance().getSkill(npc, 19402, 60, player).useNoAnimationSkill();
				npc.getController().onDelete();
				break;
			case 799637:
			case 799639:
			case 799641:
			case 799643:
			case 799645:
			case 799647:
				SkillEngine.getInstance().getSkill(npc, 19692, 60, player).useNoAnimationSkill();
				npc.getController().onDelete();
				break;
			case 282308:
				SkillEngine.getInstance().getSkill(npc, 19517, 40, player).useNoAnimationSkill();
				WorldPosition p = npc.getPosition();
				if (p != null && p.getWorldMapInstance() != null) {
					spawn(282441, p.getX(), p.getY(), p.getZ(), p.getHeading());
					Npc smoke = (Npc) spawn(282465, p.getX(), p.getY(), p.getZ(), p.getHeading());
					NpcActions.delete(smoke);
				}
				NpcActions.delete(npc);
				break;
		}
	}

	private void sendJurdinDialog() {
		sendMsg(1500243, getNpc(282204).getObjectId(), false, 0, 5000);
		sendMsg(1500244, getNpc(282204).getObjectId(), false, 0, 8000);
	}

	private void deleteNpc(int npcId) {
		if (getNpc(npcId) != null) {
			getNpc(npcId).getController().onDelete();
		}
	}
}
