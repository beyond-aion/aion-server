package instance.rakes;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_CANNOT_OPEN_DOOR_NEED_NAMED_KEY_ITEM;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Cheatkiller, Bobobear
 *
 */
@InstanceID(301030000)
public class IDShulack_Rose_01Instance extends GeneralInstanceHandler {
	private AtomicBoolean teleportEnabled = new AtomicBoolean();
	private AtomicBoolean teleportEnabled2 = new AtomicBoolean();

    @Override
    public void onInstanceCreate(WorldMapInstance instance) {
        super.onInstanceCreate(instance);
        int rnd = Rnd.get(1, 3);
        switch (rnd) {
            case 1:
                spawn(230733, 462.325f, 512.27454f, 877.6181f, (byte) 90); //Badu The Lunatic.
                break;
            case 2:
                spawn(230734, 462.325f, 512.27454f, 877.6181f, (byte) 90); //Captain Mumu Kang.
                break;
            case 3:
                spawn(230735, 462.325f, 512.27454f, 877.6181f, (byte) 90); //Lampsprung Raon.
                break;
        }
    }

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch(npc.getNpcId()) {
			case 730767:
				if (!teleportEnabled.get()) {
					if (player.getInventory().getItemCountByItemId(185000155) != 0) {
						player.getInventory().decreaseByItemId(185000155, 1);
						teleportEnabled.compareAndSet(false, true);
						TeleportService2.teleportTo(player, player.getWorldId(), player.getInstanceId(),461.66025f, 496.11496f, 877.6181f, (byte) 30,
							TeleportAnimation.BEAM_ANIMATION);
					}
					else
						PacketSendUtility.sendPacket(player, STR_CANNOT_OPEN_DOOR_NEED_NAMED_KEY_ITEM(new DescriptionId(1622787)));
				}
				else
					TeleportService2.teleportTo(player, player.getWorldId(), player.getInstanceId(), 461.66025f, 496.11496f, 877.6181f, (byte) 30,
						TeleportAnimation.BEAM_ANIMATION);
				break;
			case 730763:
				if (!teleportEnabled2.get()) {
					if (player.getInventory().getItemCountByItemId(185000156) != 0) {
						player.getInventory().decreaseByItemId(185000156, 1);
						teleportEnabled2.compareAndSet(false, true);
						TeleportService2.teleportTo(player, player.getWorldId(), player.getInstanceId(),659.8103f, 509.08694f, 867.7978f, (byte) 0,
							TeleportAnimation.BEAM_ANIMATION);
					}
					else
						PacketSendUtility.sendPacket(player, STR_CANNOT_OPEN_DOOR_NEED_NAMED_KEY_ITEM(new DescriptionId(1622789)));
				}
				else
					TeleportService2.teleportTo(player, player.getWorldId(), player.getInstanceId(), 659.8103f, 509.08694f, 867.7978f, (byte) 0,
						TeleportAnimation.BEAM_ANIMATION);
				break;
			case 730768:
				TeleportService2.teleportTo(player, player.getWorldId(), player.getInstanceId(), 461.5f, 485f, 877.8f, (byte) 90,
					TeleportAnimation.BEAM_ANIMATION);
				break;
		}
	}

}
