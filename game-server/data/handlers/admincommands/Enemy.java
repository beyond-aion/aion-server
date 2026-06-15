package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Neon
 */
public class Enemy extends AdminCommand {

	public Enemy() {
		super("enemy", "Modifies your enmity towards others.");

		// @formatter:off
		setSyntaxInfo(
			"all [players|npcs] - Sets your enmity (default: you're everyone's enemy, optional: you're an enemy to any player, or any NPC).",
			"none [players|npcs] - Disables your enmity (default: you're nobody's enemy, optional: you're not an enemy to any player, or any NPC).",
			"cancel - Resets your enmity to the default."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0) {
			sendInfo(player);
			return;
		}

		if (params[0].equalsIgnoreCase("all")) {
			if (params.length == 1) {
				player.unsetCustomState(CustomPlayerState.NEUTRAL_TO_EVERYONE);
				player.setCustomState(CustomPlayerState.ENEMY_OF_EVERYONE);
				sendInfo(player, "You are now an enemy to all.");
			} else if (params[1].equalsIgnoreCase("npcs")) {
				player.unsetCustomState(CustomPlayerState.ENEMY_OF_EVERYONE);
				player.unsetCustomState(CustomPlayerState.NEUTRAL_TO_ALL_NPCS);
				player.setCustomState(CustomPlayerState.ENEMY_OF_ALL_NPCS);
				sendInfo(player, "You are now an enemy to all NPCs.");
			} else if (params[1].equalsIgnoreCase("players")) {
				player.unsetCustomState(CustomPlayerState.ENEMY_OF_EVERYONE);
				player.unsetCustomState(CustomPlayerState.NEUTRAL_TO_ALL_PLAYERS);
				player.setCustomState(CustomPlayerState.ENEMY_OF_ALL_PLAYERS);
				sendInfo(player, "You are now an enemy to all players.");
			} else {
				sendInfo(player);
				return;
			}
		} else if (params[0].equalsIgnoreCase("none")) {
			if (params.length == 1) {
				player.unsetCustomState(CustomPlayerState.ENEMY_OF_EVERYONE);
				player.setCustomState(CustomPlayerState.NEUTRAL_TO_EVERYONE);
				sendInfo(player, "You are now neutral to everyone.");
			} else if (params[1].equalsIgnoreCase("npcs")) {
				player.unsetCustomState(CustomPlayerState.NEUTRAL_TO_EVERYONE);
				player.unsetCustomState(CustomPlayerState.ENEMY_OF_ALL_NPCS);
				player.setCustomState(CustomPlayerState.NEUTRAL_TO_ALL_NPCS);
				sendInfo(player, "You are now neutral to all NPCs.");
			} else if (params[1].equalsIgnoreCase("players")) {
				player.unsetCustomState(CustomPlayerState.NEUTRAL_TO_EVERYONE);
				player.unsetCustomState(CustomPlayerState.ENEMY_OF_ALL_PLAYERS);
				player.setCustomState(CustomPlayerState.NEUTRAL_TO_ALL_PLAYERS);
				sendInfo(player, "You are now neutral to all players.");
			} else {
				sendInfo(player);
				return;
			}
		} else if (params[0].equalsIgnoreCase("cancel")) {
			player.unsetCustomState(CustomPlayerState.ENEMY_OF_EVERYONE);
			player.unsetCustomState(CustomPlayerState.NEUTRAL_TO_EVERYONE);
			sendInfo(player, "You appear regular to everyone again.");
		} else {
			sendInfo(player);
			return;
		}
		player.getController().onChangedPlayerAttributes();
	}

}
