package admincommands;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.aionemu.gameserver.model.Announcement;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.AnnouncementService;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Divinity
 */
public class Announcements extends AdminCommand {

	public Announcements() {
		super("announcements", "Manages automatic announcements.");

		// @formatter:off
		setSyntaxInfo(
			"<list> - Shows all announcements including their ID.",
			"<reload> - Reloads all announcements from DB.",
			"<add> <elyos|asmodians|all> <chatType> <delay> <message> - Adds the specified message (delay is in seconds, chatType can be system, white, orange, shout or yellow).",
			"<delete> <id> - Deletes the announcement with the specified ID."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0) {
			sendInfo(player);
			return;
		}

		if (params[0].equals("list")) {
			Collection<Announcement> announcements = AnnouncementService.getInstance().getAnnouncements();
			String msg;
			if (announcements.isEmpty()) {
				msg = "There are no active announcements.";
			} else {
				msg = "Announcements:";
				for (Announcement announce : announcements) {
					msg += "\nID: " + announce.getId() + " (chat type: " + announce.getType() + ", delay: " + announce.getDelay() + "s";
					if (announce.getFaction() != null)
						msg += ", faction: " + announce.getFaction();
					msg += ")\n\t\"" + announce.getAnnounce() + "\"";
				}
			}
			sendInfo(player, msg);
		} else if (params[0].equals("reload")) {
			AnnouncementService.getInstance().reload();
			sendInfo(player, "Reloaded " + AnnouncementService.getInstance().getAnnouncements().size() + " announcements.");
		} else if (params[0].equals("add")) {
			if (params.length < 4) {
				sendInfo(player);
				return;
			}

			String faction = params[1].toUpperCase();
			if (!Arrays.asList("ELYOS", "ASMODIANS", "ALL").contains(faction)) {
				sendInfo(player, "Please specify a valid faction parameter.");
				return;
			}

			String chatType = params[2].toUpperCase();
			if (!Arrays.asList("SYSTEM", "WHITE", "ORANGE", "SHOUT", "YELLOW").contains(chatType)) {
				sendInfo(player, "Please specify a valid chat type parameter.");
				return;
			}

			int delay;
			try {
				delay = Integer.parseInt(params[3]);
				if (delay < 300)
					throw new IllegalArgumentException("Delay must be at least 300s (5 minutes).");
			} catch (IllegalArgumentException e) {
				sendInfo(player, e instanceof NumberFormatException ? "Delay must be specified in seconds." : e.getMessage());
				return;
			}

			String message = StringEscapeUtils.unescapeJava(StringUtils.join(params, ' ', 4, params.length));
			if (message.isEmpty()) {
				sendInfo(player, "The message cannot be empty.");
				return;
			}

			if (AnnouncementService.getInstance().addAnnouncement(message, faction, chatType, delay))
				sendInfo(player, "The announcement has been created successfully");
			else
				sendInfo(player, "The announcement could not be created");
		} else if (params[0].equals("delete")) {
			if (params.length < 2) {
				sendInfo(player, "Please specify the ID of the announcement to delete.");
				return;
			}

			int id;

			try {
				id = Integer.parseInt(params[1]);
			} catch (NumberFormatException e) {
				sendInfo(player, "Illegal announcement ID.");
				return;
			}

			// Delete the announcement from the database
			if (AnnouncementService.getInstance().delAnnouncement(id))
				sendInfo(player, "The announcement has been deleted successfully.");
			else
				sendInfo(player, "The announcement could not be deleted.");
		} else {
			sendInfo(player);
		}
	}

}
