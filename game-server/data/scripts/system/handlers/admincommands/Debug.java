package admincommands;

import java.awt.Color;
import java.lang.reflect.Field;
import java.nio.channels.SelectionKey;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.aionemu.commons.network.NioServer;
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
		super("debug", "Helps fixing runtime problems.");

		// @formatter:off
		setSyntaxInfo(
			"<connections> - Displays all connected game clients.",
			"<connectedPlayers> - Displays information about connected players.",
			"<dcBuggedPlayers> - Disconnects and attempts to save bugged players."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		if ("connections".equalsIgnoreCase(params[0])) {
			List<AionConnection> connections = findAionConnections(admin);
			if (connections != null) {
				sendInfo(admin, "Online clients:\n\t" + connections.stream().map(AionConnection::toString).collect(Collectors.joining("\n\t")));
			}
		} else if ("connectedPlayers".equalsIgnoreCase(params[0])) {
			List<Player> connectedPlayers = findConnectedPlayers(admin);
			if (connectedPlayers != null) {
				String message = "Connected players (" + connectedPlayers.size() + "):";
				for (Player player : connectedPlayers) {
					String details = "position: " + player.getPosition().toCoordString() + ", spawned: " + player.isSpawned();
					if (!player.isInWorld()) {
						details += ", " + ChatUtil.color("not in world", Color.RED);
					}
					message += "\n\t" + player.getName() + " [" + details + "]";
				}
				sendInfo(admin, message);
			}
		} else if ("dcBuggedPlayers".equalsIgnoreCase(params[0])) {
			List<Player> buggedPlayers = findConnectedPlayers(admin).stream().filter(p -> !p.isInWorld()).collect(Collectors.toList());
			if (buggedPlayers != null) {
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
			}
		}
	}

	private List<Player> findConnectedPlayers(Player admin) {
		List<AionConnection> connections = findAionConnections(admin);
		if (connections != null) {
			return connections.stream().map(AionConnection::getActivePlayer).filter(Objects::nonNull)
				.sorted(Comparator.comparing(Player::getName)).collect(Collectors.toList());
		}
		return null;
	}

	private List<AionConnection> findAionConnections(Player admin) {
		try {
			Field nioServerField = GameServer.class.getDeclaredField("nioServer");
			boolean oldAccessible = nioServerField.isAccessible();
			nioServerField.setAccessible(true);
			NioServer nioServer = (NioServer) nioServerField.get(null);
			nioServerField.setAccessible(oldAccessible);
			java.util.Set<SelectionKey> keys = nioServer.getReadWriteDispatcher().selector().keys();
			return keys.stream().map(SelectionKey::attachment).filter(o -> o instanceof AionConnection).map(o -> (AionConnection) o)
				.collect(Collectors.toList());
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			sendInfo(admin, e.toString());
			return null;
		}
	}

}
