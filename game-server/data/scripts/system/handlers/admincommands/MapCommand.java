package admincommands;

import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.taskmanager.tasks.MovementNotifyTask;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Rolandas
 * @modified Neon
 */
public class MapCommand extends AdminCommand {

	public MapCommand() {
		super("map", "Offers different functions for the current map instance.");

		// @formatter:off
		setParamInfo(
			"<freeze|unfreeze> - (Un)freezes all npcs on this map instance.",
			"<stats> - Shows peak movement broadcast counts for all maps."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		if ("freeze".equalsIgnoreCase(params[0])) {
			for (Npc npc : admin.getPosition().getWorldMapInstance().getNpcs())
				npc.getAi().onGeneralEvent(AIEventType.FREEZE);
			sendInfo(admin, "World map is frozen!");
			long walkerCount = admin.getPosition().getWorldMapInstance().getNpcs().stream().filter(o -> o.getAi().getState() == AIState.WALKING).count();
			sendInfo(admin, "There are " + walkerCount + " walkers remaining on map " + admin.getPosition().getWorldMapInstance().getMapId());
		} else if ("unfreeze".equalsIgnoreCase(params[0])) {
			for (Npc npc : admin.getPosition().getWorldMapInstance().getNpcs())
				npc.getAi().onGeneralEvent(AIEventType.UNFREEZE);
			sendInfo(admin, "World map is unfrozen!");
		} else if ("stats".equalsIgnoreCase(params[0])) {
			for (String line : MovementNotifyTask.getInstance().dumpBroadcastStats())
				sendInfo(admin, line);
		}
	}
}
