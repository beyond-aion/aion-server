package admincommands;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Neon
 */
public class Debug extends AdminCommand {

	public Debug() {
		super("debug", "Helps fixing runtime problems.", """
			connections - Displays all connected game clients.
			connectedPlayers - Displays information about connected players.
			dcBuggedPlayers - Disconnects and attempts to save bugged players.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}
		if ("connections".equalsIgnoreCase(params[0])) {
			Stream<AionConnection> connections = GameServer.findClientConnections();
			sendInfo(admin, "Online clients:\n\t" + connections.map(AionConnection::toString).collect(Collectors.joining("\n\t")));
		} else if ("connectedPlayers".equalsIgnoreCase(params[0])) {
			List<Player> connectedPlayers = findConnectedPlayers();
			String message = "Connected players (" + connectedPlayers.size() + "):";
			for (Player player : connectedPlayers) {
				String details = player.getPosition().toCoordString() + ", spawned: " + player.isSpawned();
				if (!player.isInWorld()) {
					details += ", " + ChatUtil.color("not in world", Color.RED);
				}
				message += "\n\t" + name(player) + " - " + ChatUtil.position("Location", player.getPosition()) + ": " + details;
			}
			sendInfo(admin, message);
		} else if ("dcBuggedPlayers".equalsIgnoreCase(params[0])) {
			List<Player> buggedPlayers = findConnectedPlayers().stream().filter(p -> !p.isInWorld()).toList();
			if (buggedPlayers.isEmpty()) {
				sendInfo(admin, "No bugged players found.");
			} else {
				for (Player player : buggedPlayers) {
					player.getController().cancelAllTasks(); // ensure to cancel item update task etc
					player.getCommonData().setOnline(false);
					PlayerService.storePlayer(player);
					player.getClientConnection().setActivePlayer(null);
					player.getClientConnection().close();
					player.setClientConnection(null);
				}
				sendInfo(admin, "Saved most data and disconnected the following players:\n" + buggedPlayers);
			}
		} else {
			sendInfo(admin);
		}
	}

	private List<Player> findConnectedPlayers() {
		Stream<AionConnection> connections = GameServer.findClientConnections();
		return connections.map(AionConnection::getActivePlayer).filter(Objects::nonNull).sorted(Comparator.comparing(Player::getName)).toList();
	}
}
