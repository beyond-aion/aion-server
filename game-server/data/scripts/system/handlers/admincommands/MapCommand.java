package admincommands;

import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.taskmanager.tasks.MovementNotifyTask;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Rolandas
 */
public class MapCommand extends AdminCommand {

	public MapCommand() {
		super("map");
	}

	@Override
	public void execute(Player admin, String... params) {
		WorldMapInstance instance = admin.getPosition().getWorldMapInstance();
		if ("freeze".equalsIgnoreCase(params[0])) {
			for (Npc npc : instance.getNpcs())
				npc.getAi2().onGeneralEvent(AIEventType.FREEZE);
			PacketSendUtility.sendMessage(admin, "World map is frozen!");
		} else if ("unfreeze".equalsIgnoreCase(params[0])) {
			for (Npc npc : instance.getNpcs())
				npc.getAi2().onGeneralEvent(AIEventType.UNFREEZE);
			PacketSendUtility.sendMessage(admin, "World map is unfrozen!");
		} else if ("stats".equalsIgnoreCase(params[0])) {
			for (String line : MovementNotifyTask.getInstance().dumpBroadcastStats())
				PacketSendUtility.sendMessage(admin, line);
		}
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "usage: //map freeze | unfreeze | stats");
	}
}
