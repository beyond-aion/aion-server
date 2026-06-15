package com.aionemu.gameserver.utils;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.VisibleObjectTemplate;
import com.aionemu.gameserver.network.aion.clientpackets.AbstractGmCommandPacket;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMap;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author antness, Neon
 */
public class ChatUtil {

	private static final DecimalFormat DF = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
	private static final char ASMO_NAME_PREFIX = '\uE053';
	private static final char ELYOS_NAME_PREFIX = '\uE052';

	static {
		DF.applyPattern(".##");
	}

	/**
	 * @see #color(String, int, int, int)
	 */
	public static String color(String message, Color color) {
		if (color == null)
			color = Color.WHITE;

		return color(message, color.getRed(), color.getGreen(), color.getBlue());
	}

	/**
	 * @see #color(String, int, int, int)
	 */
	public static String color(String message, int rgb) {
		return color(message, (rgb & 0xFF0000) >> 16, (rgb & 0xFF00) >> 8, rgb & 0xFF);
	}

	/**
	 * This creates a colored text message.<br>
	 * Does not work in public chats. The valid message length is limited. In fact the message limit depends on the length of the internal link
	 * parameters, the whole link becomes invalid when exceeding x characters.
	 * 
	 * @param message
	 * @param r
	 *          red component (0-255)
	 * @param g
	 *          green component (0-255)
	 * @param b
	 *          blue component (0-255)
	 * @return The color encoded message.
	 */
	public static String color(String message, int r, int g, int b) {
		return String.format("[color:%s;%s %s %s]", message, DF.format(r / 255f), DF.format(g / 255f), DF.format(b / 255f));
	}

	/**
	 * @return Formatted string for the client to display the corresponding text for male/female characters. The provided male string must not contain
	 *         spaces, otherwise only the part after the last space will be replaced. Does not work in {@link SM_MESSAGE}
	 */
	public static String genderize(String wordForMales, String textForFemales) {
		return String.format("%s[f:\"%s\"]", wordForMales, textForFemales);
	}

	/**
	 * @see #name(String)
	 */
	public static String name(Player player) {
		return name(player.getName(true));
	}

	/**
	 * @return A clickable (for system {@link ChatType}s) character name.<br>
	 */
	public static String name(String name) {
		return String.format("[charname:%s;1 1 1]", name); // the 3 parameters are color values, but client doesn't render them anyways
	}

	/**
	 * @return String identifier for the localized client string with the given ID.<br/>
	 *         The client will display the corresponding localized message instead of the string identifier. This works with {@link SM_MESSAGE} (only if
	 *         the senderObjectId is 0), {@link SM_SYSTEM_MESSAGE} and {@link SM_QUESTION_WINDOW},
	 */
	public static String l10n(int l10nId) {
		if (l10nId == 0)
			return null;
		l10nId = l10nId << 1 | 1; // client wants the rightmost bit = 1, followed by the id (effectively = l10nId * 2 + 1)
		String idAsFourBytesString = new String(new char[] { (char) (l10nId & 0xFFFF), (char) ((l10nId >>> 16) & 0xFFFF) });
		return "$" + idAsFourBytesString;
	}

	/**
	 * @see #path(String, int)
	 */
	public static String path(VisibleObject object, boolean withIdInName) {
		return path(object.getObjectTemplate(), withIdInName);
	}

	/**
	 * @see #path(String, int)
	 */
	public static String path(int npcId, boolean withIdInName) {
		VisibleObjectTemplate template = DataManager.NPC_DATA.getNpcTemplate(npcId);
		if (template != null)
			return path(template, withIdInName);
		else
			return path(withIdInName ? "Unknown ID " + npcId : "Unknown ID", npcId);
	}

	private static String path(VisibleObjectTemplate template, boolean withIdInName) {
		String name = template.getL10n();
		if (name == null)
			name = StringUtils.capitalize(template.getName());
		if (withIdInName)
			name = name + " | " + template.getTemplateId();
		return path(name, template.getTemplateId());
	}

	/**
	 * @return The objects spawn point link, based on its localized name (client does a lookup in its current language)
	 */
	public static String path(String npcName) {
		return String.format("[where:%s]", npcName);
	}

	/**
	 * @param linkName
	 * @param npcId
	 * @return The objects spawn point link, based on its ID.
	 */
	public static String path(String linkName, int npcId) {
		return String.format("[where:%s;%d]", linkName, npcId);
	}

	public static String position(String label, WorldPosition pos) {
		return position(label, pos.getMapId(), pos.getX(), pos.getY(), pos.getZ());
	}

	public static String position(String label, long worldId, float x, float y, float z) {
		byte subId = 0;
		if (worldId == 400010000) { // abyss map
			if (z > 1800 && z < 2800 && x > 1600 && x < 2700 && y > 1400 && y < 2500)
				subId = 2; // core
			else if (z > 2250)
				subId = 3; // upper
			else
				subId = 1; // lower
		}
		return String.format("[pos:%s;%d %s %s %s %d]", label, worldId, DF.format(x), DF.format(y), DF.format(z), subId);
	}

	/**
	 * @param posLink
	 *          can be an Aion link like "{@code [pos:Teleporter;0 400010000 2128.8 1924.3 0.0 2]}"
	 * @return The {@link WorldPosition} or null if input was invalid.
	 */
	public static WorldPosition getPosition(String posLink) {
		if (posLink == null || !posLink.startsWith("[pos:"))
			return null;

		int startIndex = posLink.indexOf(";");
		int endIndex = posLink.indexOf("]");
		if (startIndex < 0 || startIndex >= endIndex)
			return null;
		String[] posStr = posLink.substring(startIndex, endIndex).trim().split("\\h+");
		if (NumberUtils.toInt(posStr[0]) <= 1) // if present, strip ely/asmo language restriction flag (0 = ely only, 1 = asmo only)
			posStr = ArrayUtils.subarray(posStr, 1, posStr.length);

		if (posStr.length < 3)
			return null;

		int mapAndInstanceId = NumberUtils.toInt(posStr[0]);
		float x = NumberUtils.toFloat(posStr[1]);
		float y = NumberUtils.toFloat(posStr[2]);
		float z = posStr.length > 3 ? NumberUtils.toFloat(posStr[3]) : 0; // client always creates position links with z = 0
		int layer = posStr.length > 4 ? NumberUtils.toInt(posStr[4]) : 0;
		Integer zSearchOffset = null;
		if (layer > 0 && z == 0 && mapAndInstanceId == 400010000) { // abyss
			switch (layer) {
				case 1: // lower
					z = 1700f;
					break;
				case 2: // core
					z = 2350f;
					break;
				case 3: // upper
					z = 2950f;
					break;
			}
			zSearchOffset = -250;
		}

		return parsedCoordsToWorldPosition(mapAndInstanceId, x, y, z == 0 ? null : z, zSearchOffset);
	}

	public static WorldPosition parsedCoordsToWorldPosition(int mapAndInstanceId, float x, float y, Float z, Integer zSearchOffset) {
		int instanceId = mapAndInstanceId % 10000;
		int mapId = mapAndInstanceId;
		if (instanceId > 0)
			mapId -= instanceId;
		WorldMap map = World.getInstance().getWorldMap(mapId);
		if (map == null)
			return null;
		instanceId += 1; // client counts instanceIds starting at 0, but on server side it starts at 1 (TODO change server side)
		if (instanceId > 1 && !map.getAvailableInstanceIds().contains(instanceId))
			instanceId = 1;
		float geoZ;
		if (z == null)
			geoZ = GeoService.getInstance().getZ(mapId, x, y, 4000, 0, instanceId);
		else if (zSearchOffset == null)
			geoZ = GeoService.getInstance().getZ(mapId, x, y, z, instanceId); // search relative to input z (max diff = z ±2)
		else
			geoZ = GeoService.getInstance().getZ(mapId, x, y, z, z + zSearchOffset, instanceId);

		if (!Float.isNaN(geoZ))
			z = geoZ;

		return z == null ? null : World.getInstance().createPosition(mapId, x, y, z, (byte) 0, instanceId);
	}

	public static String item(int itemId) {
		return String.format("[item:%d]", itemId);
	}

	public static String itemName(int itemId) {
		return String.format("[item_ex:%d]", itemId);
	}

	public static String recipe(int recipeId) {
		return String.format("[recipe:%d]", recipeId);
	}

	public static String quest(int questId) {
		return String.format("[quest:%d]", questId);
	}

	/**
	 * @param itemStr
	 *          can be ID string or Aion link like "{@code [item: 100000094]}"
	 * @return The item ID or 0 if {@code itemStr} did not contain a valid ID.
	 */
	public static int getItemId(String itemStr) {
		return getIdFromString(itemStr, "item", "1[0-9]{8}");
	}

	/**
	 * @param questStr
	 *          can be ID string or Aion link like "{@code [quest: 1006]}"
	 * @return The quest ID or 0 if {@code questStr} did not contain a valid ID.
	 */
	public static int getQuestId(String questStr) {
		return getIdFromString(questStr, "quest", "[1-9][0-9]{3,4}");
	}

	private static int getIdFromString(String input, String linkAccessor, String validationPattern) {
		if (input == null)
			return 0;

		if (input.startsWith("[" + linkAccessor + ":"))
			input = input.substring(linkAccessor.length() + 2).trim();

		Matcher m = Pattern.compile("^(" + validationPattern + ")(?:[^\\d][^\\[]*\\]?$|$)").matcher(input);
		if (m.find())
			return NumberUtils.toInt(m.group(1));

		return 0;
	}

	/**
	 * @return The character name without custom tags.
	 */
	public static String getRealCharName(String name) {
		return getRealCharName(name, false);
	}

	public static String getRealCharName(String name, boolean nameIsFromGMCommand) {
		// don't perform expensive checks if name is already qualified
		if (name.matches("^[A-Za-z]+$"))
			return Util.convertName(name);

		boolean replaceUnsupportedCommandChars = nameIsFromGMCommand && name.contains(AbstractGmCommandPacket.UNSUPPORTED_COMMAND_CHAR_PLACEHOLDER);
		char firstChar = name.charAt(0);
		if (firstChar == ASMO_NAME_PREFIX || firstChar == ELYOS_NAME_PREFIX)
			name = name.substring(1);

		String nameFlag = "%s";
		for (String nameFormat : AdminConfig.NAME_TAGS) {
			int nameStartIndex = nameFormat.indexOf(nameFlag);
			if (nameStartIndex == -1)
				continue;
			String namePrefix = nameFormat.substring(0, nameStartIndex > 0 ? nameStartIndex : 0);
			String nameSuffix = nameFormat.substring(nameStartIndex + nameFlag.length());
			if (replaceUnsupportedCommandChars) {
				namePrefix = AbstractGmCommandPacket.replaceUnsupportedCommandChars(namePrefix);
				nameSuffix = AbstractGmCommandPacket.replaceUnsupportedCommandChars(nameSuffix);
			}
			if ((namePrefix + nameSuffix).length() > 0 && name.startsWith(namePrefix) && name.endsWith(nameSuffix)) {
				int endIndex = name.indexOf(nameSuffix) - 1;
				name = name.substring(namePrefix.length(), endIndex > 0 ? endIndex : name.length());
				break;
			}
		}

		return Util.convertName(name);
	}

	/**
	 * @return The player name with an icon in front to distinguish between elyos and asmodians. Only added if the reader is a staff member.
	 */
	public static String toFactionPrefixedName(Player reader, Player player) {
		String name = player.getName(true);
		if (reader.isStaff())
			name = (player.getRace() == Race.ELYOS ? ELYOS_NAME_PREFIX : ASMO_NAME_PREFIX) + name;
		return name;
	}
}
