package ai.instance.idgelDome;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

import ai.ActionItemNpcAI2;

/**
 * @author Ritsu
 */
@AIName("flame_vent")
public class FlameVentAI2 extends ActionItemNpcAI2 {

	@Override
	protected void handleUseItemFinish(Player player) {
		if (getOwner().getNpcId() == 802548) // Elyos Switch
		{
			Npc repelling1 = (Npc) spawn(855010, 234.4599f, 194.21619f, 79.589996f, (byte) 119);
			Npc fx1 = (Npc) spawn(702405, 232.19067f, 185.85762f, 80.199997f, (byte) 75);
			Npc fx2 = (Npc) spawn(702405, 238.54938f, 200.84813f, 80f, (byte) 15);
			getOwner().getController().die();
			World.getInstance().forEachPlayer(new Visitor<Player>() {

				@Override
				public void visit(Player receiver) {
					PacketSendUtility.sendPacket(receiver, new SM_SYSTEM_MESSAGE(1402368)); // Msg Asmodians are trapped
				}
			});
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					repelling1.getController().onDelete();
					fx1.getController().onDelete();
					fx2.getController().onDelete();
				}

			}, 60000);
		} else if (getOwner().getNpcId() == 802549) // Asmodians Switch
		{
			Npc repelling2 = (Npc) spawn(855010, 294.62436f, 324.11783f, 79.790443f, (byte) 119);
			Npc fx3 = (Npc) spawn(702405, 290.67102f, 317.26324f, 80.099998f, (byte) 75);
			Npc fx4 = (Npc) spawn(702405, 297.08356f, 332.35382f, 80.099998f, (byte) 15);
			getOwner().getController().die();
			World.getInstance().forEachPlayer(new Visitor<Player>() {

				@Override
				public void visit(Player receiver) {
					PacketSendUtility.sendPacket(receiver, new SM_SYSTEM_MESSAGE(1402369)); // Msg Elyos are trapped
				}
			});
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					repelling2.getController().onDelete();
					fx3.getController().onDelete();
					fx4.getController().onDelete();
				}

			}, 60000);
		}
	}
}
