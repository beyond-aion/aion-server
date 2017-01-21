package admincommands;

import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.team2.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team2.group.PlayerGroupService;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_INFO;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Nathan
 * @modified Estrayl, Neon
 */
public class Event extends AdminCommand {

	public Event() {
		super("event", "Manages event functions and player event-states.");

		// @formatter:off
		setParamInfo(
			"<setStatus> [name] - Disables ap gain/loss for the given player and sets him to event state.",
			"<setGroupStatus> [name] - Gets and sets the group of the given player to event state and disables ap gain/loss for them.",
			"<setEnemy> <cancel|team|ffa> [name] - Sets the specific state (cancel: normal, team: everyone outside the players team is an enemy, ffa: everyone is an enemy).",
			"<pvpSpawn> [asmo|elyos] - Sets a ressurrection point for the given race.",
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
				TeleportService2.setEventPos(admin.getPosition(), Race.ASMODIANS);
				sendInfo(admin, "Eventspawn for Asmodians was set!");
			} else if (params[1].equalsIgnoreCase("elyos") || params[1].equalsIgnoreCase("ely")) {
				TeleportService2.setEventPos(admin.getPosition(), Race.ELYOS);
				sendInfo(admin, "Eventspawn for Elyos was set!");
			} else {
				sendInfo(admin, "Invalid race parameter!");
			}
		} else if (params[0].equalsIgnoreCase("clearInstance")) {
			clearInstance(admin);
		} else if (params[0].equalsIgnoreCase("announce")) {
			StringBuilder sb = new StringBuilder();
			sb.append(ChatUtil.name(admin) + ":");
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
			Player player = null;
			if (params.length > 1) {
				player = World.getInstance().findPlayer(params[1]);
				if (player == null)
					sendInfo(admin, "There was no player found with the name '" + params[1] + "'!");
			} else if (admin.getTarget() instanceof Player) {
				player = (Player) admin.getTarget();
			}
			if (player == null) {
				sendInfo(admin, "No valid target!");
				return;
			}
			setEventState(admin, player, false);
		} else if (params[0].equalsIgnoreCase("setGroupStatus")) {
			Player player = null;
			if (params.length > 1) {
				player = World.getInstance().findPlayer(params[1]);
				if (player == null)
					sendInfo(admin, "There was no player found with the name '" + params[1] + "'!");
			} else if (admin.getTarget() instanceof Player) {
				player = (Player) admin.getTarget();
			}
			if (player == null) {
				sendInfo(admin, "No valid target!");
				return;
			}
			TemporaryPlayerTeam<?> team = player.getCurrentTeam();
			if (team == null) {
				sendInfo(admin, "The target is not in a group or alliance!");
				return;
			}
			for (Player p : team.getOnlineMembers())
				setEventState(admin, p, false);
		} else if (params.length > 1 && params[0].equalsIgnoreCase("setEnemy")) {
			Player player = null;
			if (params.length > 2) {
				player = World.getInstance().findPlayer(params[2]);
				if (player == null)
					sendInfo(admin, "There was no player found with the name '" + params[2] + "'!");
			} else if (admin.getTarget() instanceof Player) {
				player = (Player) admin.getTarget();
			}
			if (player == null || !player.isInCustomState(CustomPlayerState.EVENT_MODE)) {
				sendInfo(admin, "No valid target! (Event state available?)");
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
			player.clearKnownlist();
			PacketSendUtility.sendPacket(player, new SM_PLAYER_INFO(player, false));
			PacketSendUtility.sendPacket(player, new SM_MOTION(player.getObjectId(), player.getMotions().getActiveMotions()));
			player.updateKnownlist();
			sendInfo(admin, ChatUtil.name(player) + " is " + msg);
			PacketSendUtility.sendMessage(player, "You are " + msg, ChatType.BRIGHT_YELLOW_CENTER);
		} else {
			sendInfo(admin);
		}
	}

	private void clearInstance(Player admin) {
		WorldMapInstance map = admin.getPosition().getWorldMapInstance();
		if (!map.getParent().isInstanceType()) {
			sendInfo(admin, "This map is not an instance!");
			return;
		}
		if (!admin.getObjectId().equals(map.getSoloPlayerObj())) {
			sendInfo(admin, "This instance was created by another player, you cannot delete NPCs here. Use //goto to create a new one!");
			return;
		}
		int count = 0;
		for (Npc npc : map.getNpcs()) {
			npc.getController().delete();
			count++;
		}
		for (StaticDoor door : map.getDoors().values())
			door.setOpen(true);

		sendInfo(admin, "Deleted " + count + " NPCs.");
	}

	private void setEventState(Player admin, Player player, boolean onlyRemove) {
		if (player.isInCustomState(CustomPlayerState.EVENT_MODE)) {
			player.unsetCustomState(CustomPlayerState.EVENT_MODE);
			player.unsetCustomState(CustomPlayerState.ENEMY_OF_ALL_PLAYERS);
			player.setInFfaTeamMode(false);
			player.clearKnownlist();
			PacketSendUtility.sendPacket(player, new SM_PLAYER_INFO(player, false));
			PacketSendUtility.sendPacket(player, new SM_MOTION(player.getObjectId(), player.getMotions().getActiveMotions()));
			player.updateKnownlist();
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
