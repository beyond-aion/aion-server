package admincommands;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.services.mail.MailFormatter;
import com.aionemu.gameserver.services.mail.SystemMailService;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author xTz
 */
public class SysMail extends AdminCommand {

	public SysMail() {
		super("sysmail", "Sends a system mail.", """
			[%|$$<sender>] <recipient> <type> <item> <count> <kinah> [|title|] [|message|] - Sends the mail.
			The sender name must start with % or $$ and can be omitted.
			The recipient is a player name, or @all, @elyos or @asmodians for everyone online.
			Mail types: 0 is regular, 1 is express (online players only), 2 is black cloud.
			An item or count of 0 sends no item, kinah 0 sends no kinah.
			The optional title and message must be enclosed in pipe chars.
			""");
	}

	enum RecipientType {

		ELYOS,
		ASMO,
		ALL,
		PLAYER;

		public boolean isAllowed(Race race) {
			switch (this) {
				case ELYOS:
					return race == Race.ELYOS;
				case ASMO:
					return race == Race.ASMODIANS;
				case ALL:
					return race == Race.ELYOS || race == Race.ASMODIANS;
				default:
					return false;
			}
		}
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 5) {
			sendInfo(admin);
			return;
		}

		String[] paramValues = new String[params.length];
		System.arraycopy(params, 0, paramValues, 0, params.length);

		RecipientType recipientType = null;
		String sender = null;
		String recipient = null;

		if (paramValues[0].startsWith("$$") || paramValues[0].startsWith("%")) {
			if (params.length < 6) {
				sendInfo(admin);
				return;
			}
			sender = paramValues[0];
			paramValues = new String[params.length - 1];
			System.arraycopy(params, 1, paramValues, 0, params.length - 1);
		} else {
			sender = "Admin";
		}

		if (paramValues[0].startsWith("@")) {
			if ("@all".startsWith(paramValues[0]))
				recipientType = RecipientType.ALL;
			else if ("@elyos".startsWith(paramValues[0]))
				recipientType = RecipientType.ELYOS;
			else if ("@asmodians".startsWith(paramValues[0]))
				recipientType = RecipientType.ASMO;
			else {
				sendInfo(admin, "Recipient must be Player name, @all, @elyos or @asmodians.");
				return;
			}
		} else {
			recipientType = RecipientType.PLAYER;
			recipient = Util.convertName(paramValues[0]);
		}

		int item = 0, count = 0, kinah = 0;
		LetterType letterType;

		item = Integer.parseInt(paramValues[2]);
		count = Integer.parseInt(paramValues[3]);
		kinah = Integer.parseInt(paramValues[4]);
		letterType = LetterType.getLetterTypeById(Integer.parseInt(paramValues[1]));

		if (letterType == LetterType.BLACKCLOUD)
			sender = "$$CASH_ITEM_MAIL";

		if (!validate(admin, item, count, kinah, recipient, recipientType, letterType))
			return;

		if (item <= 0)
			item = 0;

		if (count <= 0)
			count = -1;

		String title = "System Mail";
		String message = " ";

		if (paramValues.length > 5) {
			String[] words = new String[paramValues.length - 5];
			System.arraycopy(paramValues, 5, words, 0, words.length);
			String[] outText = new String[1];
			int wordCount = extractText(words, outText);
			if (wordCount > 0) {
				title = outText[0];
				String[] msgWords = new String[words.length - wordCount];
				System.arraycopy(words, wordCount, msgWords, 0, msgWords.length);
				wordCount = extractText(msgWords, outText);
				if (wordCount > 0)
					message = outText[0];
			}
		}

		if (recipientType == RecipientType.PLAYER) {
			if (letterType == LetterType.BLACKCLOUD)
				MailFormatter.sendBlackCloudMail(recipient, item, count);
			else
				SystemMailService.sendMail(sender, recipient, title, message, item, count, kinah, letterType);
		} else {
			for (Player player : World.getInstance().getAllPlayers()) {
				if (recipientType.isAllowed(player.getRace())) {
					if (letterType == LetterType.BLACKCLOUD)
						MailFormatter.sendBlackCloudMail(player.getName(), item, count);
					else
						SystemMailService.sendMail(sender, player.getName(), title, message, item, count, kinah, letterType);
				}
			}
		}

		if (item != 0) {
			sendInfo(admin, "Sent to " + recipientType + (recipientType == RecipientType.PLAYER ? " " + recipient : "") + "\n"
				+ "[item:" + item + "] Count:" + count + " Kinah:" + kinah + "\n" + "Mail sent successfully.");
		} else if (kinah > 0) {
			sendInfo(admin, "Sent to " + recipientType + (recipientType == RecipientType.PLAYER ? " " + recipient : "") + "\n"
				+ " Kinah:" + kinah + "\n" + "Mail sent successfully.");
		}
	}

	private int extractText(String[] words, String[] outText) {
		if (words.length == 0 || outText.length == 0)
			return 0;

		if (!words[0].startsWith("|"))
			return 0;

		int wordCount = 1;

		String enclosedText = words[0].substring(1);
		if (enclosedText.endsWith("|")) {
			outText[0] = enclosedText.substring(0, enclosedText.length() - 1);
		} else {
			List<String> titleWords = new ArrayList<>();
			titleWords.add(enclosedText);
			for (; wordCount < words.length; wordCount++) {
				String word = words[wordCount];
				if (word.endsWith("|")) {
					word = word.substring(0, word.length() - 1);
					titleWords.add(word);
					wordCount++;
					break;
				} else
					titleWords.add(word);
			}

			outText[0] = String.join(" ", titleWords);
		}

		return wordCount;
	}

	private static boolean validate(Player admin, int item, int count, int kinah, String recipient, RecipientType recipientType,
		LetterType letterType) {
		if (recipientType == RecipientType.PLAYER) {
			if (letterType == LetterType.EXPRESS) {
				if (World.getInstance().getPlayer(recipient) == null) {
					sendInfo(admin, "Express mail can only be sent to online players.");
					return false;
				}
			} else if (!PlayerDAO.isNameUsed(recipient)) {
				sendInfo(admin, "Could not find a recipient by that name.");
				return false;
			}
		}

		if (item == 0 && count != 0) {
			sendInfo(admin, "Please insert an item ID.");
			return false;
		}

		if (count == 0 && item != 0) {
			sendInfo(admin, "Please insert an item count.");
			return false;
		}

		if (count <= 0 && item <= 0 && kinah <= 0) {
			sendInfo(admin, "Nothing to send: item, count and kinah are all 0.");
			return false;
		}

		ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(item);
		if (item != 0) {
			if (itemTemplate == null) {
				sendInfo(admin, "Item id is incorrect: " + item);
				return false;
			}
			long maxStackCount = itemTemplate.getMaxStackCount();
			if (count > maxStackCount && maxStackCount != 0) {
				sendInfo(admin, "The item count exceeds the max stack count of " + maxStackCount + ".");
				return false;
			}
		}

		if (kinah < 0) {
			sendInfo(admin, "Kinah value must be >= 0.");
			return false;
		} else if (kinah > 0 && letterType == LetterType.BLACKCLOUD) {
			sendInfo(admin, "Black cloud mail cannot contain kinah.");
			return false;
		}
		return true;
	}

}
