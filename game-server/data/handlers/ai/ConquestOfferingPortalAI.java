package ai;

import java.util.List;
import java.util.stream.Collectors;

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
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Yeats, Sykra
 */
@AIName("conquest_offering_portal")
public class ConquestOfferingPortalAI extends ActionItemNpcAI {

	private SpawnTemplate targetLocation;

	public ConquestOfferingPortalAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		targetLocation = findTargetLocation();
		getOwner().getController().addTask(TaskId.DESPAWN, ThreadPoolManager.getInstance().schedule(() -> getOwner().getController().delete(), 65000));
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		if (targetLocation != null)
			TeleportService.teleportTo(player, targetLocation.getWorldId(), targetLocation.getX(), targetLocation.getY(), targetLocation.getZ(),
				targetLocation.getHeading(), TeleportAnimation.FADE_OUT_BEAM);
	}

	private SpawnTemplate findTargetLocation() {
		int npcId = getNpcId() == 833018 ? 856412 : 856433;
		SpawnGroup spawnGroup = Rnd.get(DataManager.SPAWNS_DATA.getSpawnsForNpc(getOwner().getWorldId(), npcId));
		if (spawnGroup != null) {
			SpawnTemplate targetLocation = null;
			Npc creator = findCreatorNpc();
			if (creator != null) {
				SpawnTemplate creatorTemplate = creator.getSpawn();
				// exclude all teleport templates within a 50m range around the creator spawn template
				// to prevent teleportation to the killed conquest npc (creator of this npc)
				List<SpawnTemplate> spawnTemplates = spawnGroup.getSpawnTemplates().stream()
					.filter(teleportTemplate -> !PositionUtil.isInRange(teleportTemplate.getX(), teleportTemplate.getY(), teleportTemplate.getZ(),
						creatorTemplate.getX(), creatorTemplate.getY(), creatorTemplate.getZ(), 50))
					.collect(Collectors.toList());
				targetLocation = Rnd.get(spawnTemplates);
			}
			if (targetLocation != null)
				return targetLocation;
			return Rnd.get(spawnGroup.getSpawnTemplates());
		}
		return null;
	}

	private Npc findCreatorNpc() {
		if (getCreatorId() != 0 && getPosition().getWorldMapInstance().getObject(getCreatorId()) instanceof Npc npc)
			return npc;
		return null;
	}

}
