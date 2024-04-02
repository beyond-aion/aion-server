package admincommands;

import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Nathan, Estrayl, Neon
 */
public class Event extends AdminCommand {

	public Event() {
		super("event", "Manages event functions and player event-states.");

		// @formatter:off
		setSyntaxInfo(
			"<setStatus> [name] - Disables ap gain/loss for the given player and sets him to event state.",
			"<setGroupStatus> [name] - Gets and sets the group of the given player to event state and disables ap gain/loss for them.",
			"<setEnemy> <cancel|team|ffa> [name] - Sets the specific state (cancel: normal, team: everyone outside the players team is an enemy, ffa: everyone is an enemy).",
			"<pvpSpawn> [asmo|elyos] - Sets a resurrection point for the given race.",
			"<clearInstance> - Clears the whole instance you have created.",
			"<announce> <text> - Sends a yellow message for all players in event state.",
			"<list> - Lists all players in event state.",
			"<removeAll> - Removes all players from event state."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		if (params[0].equalsIgnoreCase("pvpSpawn")) {
			if (params[1].equalsIgnoreCase("asmo")) {
				TeleportService.setEventPos(admin.getPosition(), Race.ASMODIANS);
				sendInfo(admin, "Eventspawn for Asmodians was set!");
			} else if (params[1].equalsIgnoreCase("elyos") || params[1].equalsIgnoreCase("ely")) {
				TeleportService.setEventPos(admin.getPosition(), Race.ELYOS);
				sendInfo(admin, "Eventspawn for Elyos was set!");
			} else {
				sendInfo(admin, "Invalid race parameter!");
			}
		} else if (params[0].equalsIgnoreCase("clearInstance")) {
			clearInstance(admin);
		} else if (params[0].equalsIgnoreCase("announce")) {
			StringBuilder sb = new StringBuilder();
			sb.append(ChatUtil.name(admin)).append(':');
			for (int i = 1; i < params.length; i++)
				sb.append(" ").append(params[i]);

			World.getInstance().forEachPlayer(p -> {
				if (p.isInCustomState(CustomPlayerState.EVENT_MODE) || p == admin)
					PacketSendUtility.sendMessage(p, sb.toString(), ChatType.BRIGHT_YELLOW_CENTER);
			});
		} else if (params[0].equalsIgnoreCase("list")) {
			StringBuilder sb = new StringBuilder("Players in event state:");
			World.getInstance().getAllPlayers().stream().filter(p -> p.isInCustomState(CustomPlayerState.EVENT_MODE))
				.forEach(p -> sb.append("\n\t").append(ChatUtil.name(p)));
			sendInfo(admin, sb.toString());
		} else if (params[0].equalsIgnoreCase("removeAll")) {
			for (Player player : World.getInstance().getAllPlayers())
				setEventState(admin, player, true);
		} else if (params[0].equalsIgnoreCase("setStatus")) {
			Player player = getPlayer(admin, params.length > 1 ? params[1] : null);
			if (player == null)
				return;
			setEventState(admin, player, false);
		} else if (params[0].equalsIgnoreCase("setGroupStatus")) {
			Player player = getPlayer(admin, params.length > 1 ? params[1] : null);
			if (player == null)
				return;
			TemporaryPlayerTeam<?> team = player.getCurrentTeam();
			if (team == null) {
				sendInfo(admin, "The target is not in a group or alliance!");
				return;
			}
			for (Player p : team.getOnlineMembers())
				setEventState(admin, p, false);
		} else if (params.length > 1 && params[0].equalsIgnoreCase("setEnemy")) {
			Player player = getPlayer(admin, params.length > 2 ? params[2] : null);
			if (player == null)
				return;
			if (!player.isInCustomState(CustomPlayerState.EVENT_MODE)) {
				sendInfo(admin, player.getName() + " is not in event state");
				return;
			}
			boolean ffaTeamMode = false;
			String msg = "no longer in FFA state.";
			if (params[1].equalsIgnoreCase("cancel")) {
				player.unsetCustomState(CustomPlayerState.ENEMY_OF_ALL_PLAYERS);
			} else if (params[1].equalsIgnoreCase("team")) {
				player.setCustomState(CustomPlayerState.ENEMY_OF_ALL_PLAYERS);
				msg = "in Team-FFA state now.";
				ffaTeamMode = true;
			} else if (params[1].equalsIgnoreCase("ffa")) {
				player.setCustomState(CustomPlayerState.ENEMY_OF_ALL_PLAYERS);
				msg = "in FFA state now.";
				PlayerGroupService.removePlayer(player);
				PlayerAllianceService.removePlayer(player);
			} else {
				sendInfo(admin);
				return;
			}
			player.setInFfaTeamMode(ffaTeamMode);
			player.getController().onChangedPlayerAttributes();
			sendInfo(admin, ChatUtil.name(player) + " is " + msg);
			PacketSendUtility.sendMessage(player, "You are " + msg, ChatType.BRIGHT_YELLOW_CENTER);
		} else {
			sendInfo(admin);
		}
	}

	private Player getPlayer(Player admin, String name) {
		Player player = null;
		if (name != null) {
			String playerName = Util.convertName(name);
			player = World.getInstance().getPlayer(playerName);
			if (player == null) {
				PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_NO_SUCH_USER(playerName));
			}
		} else if (admin.getTarget() instanceof Player target) {
			player = target;
		} else {
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
		}
		return player;
	}

	private void clearInstance(Player admin) {
		WorldMapInstance map = admin.getPosition().getWorldMapInstance();
		if (!map.getParent().isInstanceType()) {
			sendInfo(admin, "This map is not an instance!");
			return;
		}
		if (map.getRegisteredCount() != 1 || !map.isRegistered(admin.getObjectId())) {
			sendInfo(admin, "This instance was not created by you, you cannot delete NPCs here. Use //goto to create a new one!");
			return;
		}
		AtomicInteger count = new AtomicInteger();
		map.forEachNpc(npc -> {
			npc.getController().delete();
			count.getAndIncrement();
		});
		map.forEachDoor(door -> door.setOpen(true));

		sendInfo(admin, "Deleted " + count + " NPCs.");
	}

	private void setEventState(Player admin, Player player, boolean onlyRemove) {
		if (player.isInCustomState(CustomPlayerState.EVENT_MODE)) {
			player.unsetCustomState(CustomPlayerState.EVENT_MODE);
			player.unsetCustomState(CustomPlayerState.ENEMY_OF_ALL_PLAYERS);
			player.setInFfaTeamMode(false);
			player.getController().onChangedPlayerAttributes();
			sendInfo(admin, ChatUtil.name(player) + " was removed from event state.");
			PacketSendUtility.sendMessage(player, "You were removed from event state!", ChatType.BRIGHT_YELLOW_CENTER);
		} else if (!onlyRemove) {
			player.setCustomState(CustomPlayerState.EVENT_MODE);
			sendInfo(admin, ChatUtil.name(player) + " was set in event state.");
			PacketSendUtility.sendMessage(player,
				"You are in event state now. Please notice that you are not allowed to leave the event without removal of this state!",
				ChatType.BRIGHT_YELLOW_CENTER);
		}
	}

}
