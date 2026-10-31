package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * Recalls the party of the targeted player to the given coordinates on the GM's map.
 */
public class Partyrecallto extends RecallToCommand {

	public Partyrecallto() {
		super("partyrecallto", "Recalls the party of your target to the given coordinates on your map.", """
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
		PlayerGroup group = target.getPlayerGroup();
		if (group == null) {
			sendInfo(admin, name(target) + " is not in a party.");
			return;
		}
		recall(admin, group, destination, "party member");
	}
}
