package admincommands;

import java.util.List;

import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Rolandas, Neon
 */
public class MapCommand extends AdminCommand {

	public MapCommand() {
		super("map", "Offers different functions for the current map instance.");

		// @formatter:off
		setSyntaxInfo(
			"freeze - Freezes all NPCs on this map instance.",
			"unfreeze - Unfreezes all NPCs on this map instance."
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
			List<Npc> npcs = admin.getPosition().getWorldMapInstance().getNpcs();
			npcs.forEach(npc -> npc.getAi().onGeneralEvent(AIEventType.FREEZE));
			sendInfo(admin, "World map is frozen!");
			long walkerCount = npcs.stream().filter(o -> o.getAi().getState() == AIState.WALKING).count();
			sendInfo(admin, "There are " + walkerCount + " walkers remaining on map " + admin.getPosition().getWorldMapInstance().getMapId());
		} else if ("unfreeze".equalsIgnoreCase(params[0])) {
			admin.getPosition().getWorldMapInstance().forEachNpc(npc -> npc.getAi().onGeneralEvent(AIEventType.UNFREEZE));
			sendInfo(admin, "World map is unfrozen!");
		}
	}
}
