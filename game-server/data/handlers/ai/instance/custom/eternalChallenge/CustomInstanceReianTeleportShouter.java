package ai.instance.custom.eternalChallenge;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.custom.instance.CustomInstanceService;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.geo.GeoService;

import ai.GeneralNpcAI;

/**
 * @author Estrayl
 */
@AIName("custom_instance_reian_teleport_shouter")
public class CustomInstanceReianTeleportShouter extends GeneralNpcAI {

	private static final Map<Integer, Long> lastShoutToPlayer = new ConcurrentHashMap<>();

	public CustomInstanceReianTeleportShouter(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		if (!(creature instanceof Player player) || creature.isDead() || !getOwner().canSee(creature) || !getOwner().getPosition().isMapRegionActive())
			return;
		if (lastShoutToPlayer.get(creature.getObjectId()) != null
			&& System.currentTimeMillis() - lastShoutToPlayer.get(creature.getObjectId()) <= 1800000) // only allow 1 shout half an hour
			return;

		if (PositionUtil.isInRange(getOwner(), creature, 15) && GeoService.getInstance().canSee(getOwner(), creature)) {
			lastShoutToPlayer.put(creature.getObjectId(), System.currentTimeMillis());
			if (CustomInstanceService.getInstance().canEnter(creature.getObjectId()))
				PacketSendUtility.sendPacket(player,
					new SM_MESSAGE(getOwner(), "Hey you know, use this device to teleport into the 'Eternal Challenge'.", ChatType.NPC));
			else
				PacketSendUtility.sendPacket(player, new SM_MESSAGE(getOwner(),
					"I see... you already have shown your potential today. Come again tomorrow if you want to improve.", ChatType.NPC));
		}
	}
}
