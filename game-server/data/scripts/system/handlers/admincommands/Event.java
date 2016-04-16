package admincommands;

import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
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
 * @modified Estrayl 12.04.2016
 */
public class Event extends AdminCommand {

	public Event() {
		super("event");

		setParamInfo("//event setStatus [name] - disables ap gain/loss for the given player and sets him to event state",
			"//event setGroupStatus [name] - gets and sets the group of the given player to event state and disables ap gain/loss for them",
			"//event setEnemy <0|1|2> [name] {0:normal|1:group|2:ffa} - sets the specific state",
			"//event pvpspawn [asmo|elyos] - sets a ressurrection point for the given race",
			"//event clearInstance - clears the whole instance you have created",
			"//event announce <text> - sends a yellow message for all players in event state", "//event list - lists all players in event state",
			"//event removeAll - removes all players from event state");
	}

	private void clearInstance(Player admin) {
		WorldMapInstance map = admin.getPosition().getWorldMapInstance();
		if (!map.getParent().isInstanceType()) {
			PacketSendUtility.sendMessage(admin, "This map is not an instance!");
			return;
		}
		if (map.getSoloPlayerObj() != admin.getObjectId()) {
			PacketSendUtility.sendMessage(admin,
				"This instance was created by another player, you cannot delete NPCs here. Use //goto to create a new one!");
			return;
		}
		int count = 0;
		for (Npc npc : map.getNpcs()) {
			npc.getController().onDelete();
			count++;
		}
		for (StaticDoor door : map.getDoors().values())
			door.setOpen(true);

		PacketSendUtility.sendMessage(admin, "Deleted " + count + " NPCs.");
	}

	private void setEventState(Player admin, Player player, boolean onlyRemove) {
		if (player.isInEvent()) {
			player.setIsInEvent(false);
			player.setEnemyState(0);
			player.clearKnownlist();
			PacketSendUtility.sendPacket(player, new SM_PLAYER_INFO(player, false));
			PacketSendUtility.sendPacket(player, new SM_MOTION(player.getObjectId(), player.getMotions().getActiveMotions()));
			player.updateKnownlist();
			PacketSendUtility.sendMessage(admin, "Player '" + player.getName() + "' removed from event state.");
			PacketSendUtility.sendMessage(player, "You were removed from event state!", ChatType.BRIGHT_YELLOW_CENTER);
		} else if (!onlyRemove) {
			player.setIsInEvent(true);
			PacketSendUtility.sendMessage(admin, "Player '" + player.getName() + "' set to event state!");
			PacketSendUtility.sendMessage(player,
				"You are in event state now. Please notice that you are not allowed to leave the event without removal of this state!",
				ChatType.BRIGHT_YELLOW_CENTER);
		}
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}
		if (params[0].equalsIgnoreCase("pvpspawn")) {
			if (params[1].equalsIgnoreCase("asmo") || params[1].equalsIgnoreCase("asmo")) {
				TeleportService2.setEventPos(admin.getPosition(), Race.ASMODIANS);
				PacketSendUtility.sendMessage(admin, "Eventspawn for Asmodians was set!");
			} else if (params[1].equalsIgnoreCase("elyos") || params[1].equalsIgnoreCase("ely")) {
				TeleportService2.setEventPos(admin.getPosition(), Race.ELYOS);
				PacketSendUtility.sendMessage(admin, "Eventspawn for Elyos was set!");
			} else {
				PacketSendUtility.sendMessage(admin, "Invalid race parameter!");
			}
		} else if (params[0].equalsIgnoreCase("clearInstance")) {
			clearInstance(admin);
		} else if (params[0].equalsIgnoreCase("announce")) {
			StringBuilder sb = new StringBuilder();
			sb.append(ChatUtil.name(admin) + ":");
			for (int i = 1; i < params.length; i++)
				sb.append(" ").append(params[i]);

			World.getInstance().doOnAllPlayers(p -> {
				if (p.isInEvent() || p == admin)
					PacketSendUtility.sendMessage(p, sb.toString(), ChatType.BRIGHT_YELLOW_CENTER);
			});
		} else if (params[0].equalsIgnoreCase("list")) {
			PacketSendUtility.sendMessage(admin, "-----------------------------------");
			PacketSendUtility.sendMessage(admin, "Players in event state:");
			for (Player p : World.getInstance().getAllPlayers())
				if (p.isInEvent())
					PacketSendUtility.sendMessage(admin, p.getName());
			PacketSendUtility.sendMessage(admin, "-----------------------------------");
		} else if (params[0].equalsIgnoreCase("removeAll")) {
			for (Player player : World.getInstance().getAllPlayers())
				setEventState(admin, player, true);
		} else if (params[0].equalsIgnoreCase("setStatus")) {
			Player player = null;
			if (params.length > 1) {
				player = World.getInstance().findPlayer(params[1]);
				if (player == null)
					PacketSendUtility.sendMessage(admin, "There was no player found with the name '" + params[1] + "'!");
			} else if (admin.getTarget() != null && admin.getTarget() instanceof Player) {
				player = (Player) admin.getTarget();
			} else {
				return;
			}
			if (player == null) {
				PacketSendUtility.sendMessage(admin, "No valid target!");
				return;
			}
			setEventState(admin, player, false);
		} else if (params[0].equalsIgnoreCase("setGroupStatus")) {
			Player player = null;
			if (params.length > 1) {
				player = World.getInstance().findPlayer(params[1]);
				if (player == null)
					PacketSendUtility.sendMessage(admin, "There was no player found with the name '" + params[1] + "'!");
			} else if (admin.getTarget() != null && admin.getTarget() instanceof Player) {
				player = (Player) admin.getTarget();
			} else {
				return;
			}
			if (player == null) {
				PacketSendUtility.sendMessage(admin, "No valid target!");
				return;
			}
			TemporaryPlayerTeam<?> team = player.getCurrentTeam();
			if (team == null) {
				PacketSendUtility.sendMessage(admin, "The target is not in a group or alliance!");
				return;
			}
			for (Player p : team.getOnlineMembers())
				setEventState(admin, p, false);
		} else if (params.length > 1 && params[0].equalsIgnoreCase("setEnemy")) {
			Player player = null;
			if (params.length > 2) {
				player = World.getInstance().findPlayer(params[2]);
				if (player == null)
					PacketSendUtility.sendMessage(admin, "There was no player found with the name '" + params[1] + "'!");
			} else if (admin.getTarget() != null && admin.getTarget() instanceof Player) {
				player = (Player) admin.getTarget();
			} else {
				return;
			}
			if (player == null || !player.isInEvent()) {
				PacketSendUtility.sendMessage(admin, "No valid target! (Event state available?)");
				return;
			}
			byte state = 0;
			try {
				state = Byte.parseByte(params[1]);
				if (state < 0 || state > 2)
					throw new IllegalArgumentException();
			} catch (Exception e) {
				PacketSendUtility.sendMessage(admin, "Choose state 0-2.");
				return;
			}
			player.setEnemyState(state);
			player.clearKnownlist();
			PacketSendUtility.sendPacket(player, new SM_PLAYER_INFO(player, false));
			PacketSendUtility.sendPacket(player, new SM_MOTION(player.getObjectId(), player.getMotions().getActiveMotions()));
			player.updateKnownlist();
			PacketSendUtility.sendMessage(admin, "Player '" + player.getName() + "' is in enemy state " + state + " now.");
			PacketSendUtility.sendMessage(player, "You are in FFA state " + state + " now.", ChatType.BRIGHT_YELLOW_CENTER);
			if (state == 2) {
				if (player.isInGroup2())
					PlayerGroupService.removePlayer(player);
				if (player.isInAlliance2())
					PlayerAllianceService.removePlayer(player);
			}
		} else {
			sendInfo(admin);
		}
	}
}
