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
import com.aionemu.gameserver.world.WorldMapType;

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
		WorldMapInstance instance;
		float x, y, z;
		byte heading = 0;
		WorldMapType mapType = WorldMapType.getWorld(player.getWorldId());
		if (mapType == WorldMapType.HOUSING_IDLF_PERSONAL || mapType == WorldMapType.HOUSING_IDDF_PERSONAL) { // leaving studio
			House studio = HousingService.getInstance().getPlayerStudio(player.getPosition().getWorldMapInstance().getOwnerId());
			if (studio == null) // should not happen unless this instance was custom spawned by admin
				return;
			instance = World.getInstance().getWorldMap(studio.getAddress().getExitMapId()).getMainWorldMapInstance();
			x = studio.getAddress().getExitX();
			y = studio.getAddress().getExitY();
			z = studio.getAddress().getExitZ();
		} else { // entering own studio
			House studio = HousingService.getInstance().getPlayerStudio(player.getObjectId());
			if (studio == null) { // doesn't own studio
				PacketSendUtility.sendPacket(player, STR_HOUSING_ENTER_NEED_HOUSE());
				return;
			}
			instance = InstanceService.getOrCreateHouseInstance(studio);
			x = studio.getAddress().getX();
			y = studio.getAddress().getY();
			z = studio.getAddress().getZ();
			heading = studio.getTeleportHeading();
		}
		TeleportService.teleportTo(player, instance, x, y, z, heading, TeleportAnimation.FADE_OUT_BEAM);
	}
}
