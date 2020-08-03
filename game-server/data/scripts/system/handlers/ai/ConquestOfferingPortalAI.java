package ai;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Yeats 16.03.2016.
 */
@AIName("conquest_offering_portal")
public class ConquestOfferingPortalAI extends ActionItemNpcAI {

	public ConquestOfferingPortalAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		getOwner().getController().addTask(TaskId.DESPAWN, ThreadPoolManager.getInstance().schedule(() -> getOwner().getController().delete(), 65000));
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		int npcId = getNpcId() == 833018 ? 856412 : 856433;
		SpawnGroup spawnGroup = Rnd.get(DataManager.SPAWNS_DATA.getSpawnsForNpc(getOwner().getWorldId(), npcId));
		if (spawnGroup != null) {
			SpawnTemplate template = Rnd.get(spawnGroup.getSpawnTemplates());
			if (template != null)
				TeleportService.teleportTo(player, player.getWorldId(), template.getX(), template.getY(), template.getZ(), template.getHeading(),
					TeleportAnimation.FADE_OUT_BEAM);
		}
	}
}
