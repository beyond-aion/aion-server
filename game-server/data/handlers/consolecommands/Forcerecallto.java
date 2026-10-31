package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * Recalls the alliance of the targeted player to the given coordinates on the GM's map.
 */
public class Forcerecallto extends RecallToCommand {

	public Forcerecallto() {
		super("forcerecallto", "Recalls the alliance of your target to the given coordinates on your map.", """
			<x> <y> <z> - Coordinates on your current map. The target must be within 30 m.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		WorldPosition destination = parseDestination(admin, params);
		if (destination == null)
			return;
		Player target = getTargetPlayer(admin, 30);
		if (target == null)
			return;
		PlayerAlliance alliance = target.getPlayerAlliance();
		if (alliance == null) {
			sendInfo(admin, name(target) + " is not in an alliance.");
			return;
		}
		recall(admin, alliance, destination, "alliance member");
	}
}
