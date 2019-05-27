package ai.portals;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_HOUSING_ENTER_NEED_HOUSE;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.ActionItemNpcAI;

/**
 * @author Rolandas
 */
@AIName("studioportal")
public class StudioPortalAI extends ActionItemNpcAI {

	public StudioPortalAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		return true;
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		int ownerId = player.getPosition().getWorldMapInstance().getOwnerId();
		House studio = HousingService.getInstance().getPlayerStudio(player.getObjectId());
		if (studio == null && ownerId == 0) // Doesn't own studio and not in studio
		{
			PacketSendUtility.sendPacket(player, STR_HOUSING_ENTER_NEED_HOUSE());
			return;
		}

		int exitMapId = 0;
		float x = 0, y = 0, z = 0;
		byte heading = 0;
		int instanceId = 0;

		if (ownerId > 0) { // leaving
			studio = HousingService.getInstance().getPlayerStudio(ownerId);
			exitMapId = studio.getAddress().getExitMapId();
			instanceId = World.getInstance().getWorldMap(exitMapId).getMainWorldMapInstance().getInstanceId();
			x = studio.getAddress().getExitX();
			y = studio.getAddress().getExitY();
			z = studio.getAddress().getExitZ();
		} else if (studio == null) {
			return; // doesn't own studio
		} else { // entering own studio
			exitMapId = studio.getAddress().getMapId();
			WorldMapInstance instance = InstanceService.getPersonalInstance(exitMapId, player.getObjectId());
			if (instance == null) {
				instance = InstanceService.getNextAvailableInstance(exitMapId, player.getObjectId());
				instance.register(player.getObjectId());
			}
			instanceId = instance.getInstanceId();
			x = studio.getAddress().getX();
			y = studio.getAddress().getY();
			z = studio.getAddress().getZ();
			if (exitMapId == 710010000) {
				heading = 36;
			}
		}
		TeleportService.teleportTo(player, exitMapId, instanceId, x, y, z, heading, TeleportAnimation.FADE_OUT_BEAM);
	}
}
