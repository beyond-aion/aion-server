package instance.rakes;

import com.aionemu.commons.utils.Rnd;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_CANNOT_OPEN_DOOR_NEED_NAMED_KEY_ITEM;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;


/**
 * @author Cheatkiller, Bobobear
 *
 */
@InstanceID(301020000)
public class IDShulackRoseSolo02Instance extends GeneralInstanceHandler {

	private AtomicBoolean teleportEnabled = new AtomicBoolean();

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch(npc.getNpcId()) {
			case 730764:
				if (!teleportEnabled.get()) {
					if (player.getInventory().getItemCountByItemId(185000148) != 0) {
						player.getInventory().decreaseByItemId(185000148, 1);
						teleportEnabled.compareAndSet(false, true);
						TeleportService2.teleportTo(player, player.getWorldId(), player.getInstanceId(),704.3273f, 500.8049f, 939.6262f, (byte) 0,
							TeleportAnimation.BEAM_ANIMATION);
					}
					else
						PacketSendUtility.sendPacket(player, STR_CANNOT_OPEN_DOOR_NEED_NAMED_KEY_ITEM(new DescriptionId(1622801)));
				}
				else
					TeleportService2.teleportTo(player, player.getWorldId(), player.getInstanceId(), 704.3273f, 500.8049f, 939.6262f, (byte) 0,
						TeleportAnimation.BEAM_ANIMATION);
				break;
			case 730770:
				SkillEngine.getInstance().applyEffectDirectly(19272, player, player, 0);
				npc.getController().onDelete();
				break;
		}
	}
        
        @Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		if (Rnd.get(1, 100) > 75) { 
			spawn(230649, 460.9086f, 513.1888f, 952.549f, (byte) 1); // Nerukiki the Timid
		}
	}
        
        @Override
	public boolean onDie(final Player player, Creature lastAttacker) {
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.DIE, 0,
			player.equals(lastAttacker) ? 0 : lastAttacker.getObjectId()), true);

		PacketSendUtility.sendPacket(player, new SM_DIE(false, false, 0, 8));
		return true;
	}
}
