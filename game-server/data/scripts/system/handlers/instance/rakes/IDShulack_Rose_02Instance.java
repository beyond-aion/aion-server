package instance.rakes;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_CANNOT_OPEN_DOOR_NEED_NAMED_KEY_ITEM;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;


/**
 * @author Cheatkiller, Bobobear
 *
 */
@InstanceID(301040000)
public class IDShulack_Rose_02Instance extends GeneralInstanceHandler {
	private AtomicBoolean teleportEnabled = new AtomicBoolean();

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 730764:
				if (!teleportEnabled.get()) {
					if (player.getInventory().getItemCountByItemId(185000162) != 0) {
						player.getInventory().decreaseByItemId(185000162, 1);
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

}
