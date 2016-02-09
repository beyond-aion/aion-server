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
import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapType;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author antness, Neon
 */
public class ChatUtil {

	private static final DecimalFormat DF = (DecimalFormat) DecimalFormat.getInstance(Locale.US);

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
	 * @see #name(String)
	 */
	public static String name(Player player) {
		return name(player.getName(AdminConfig.CUSTOMTAG_ENABLE));
	}

	/**
	 * This creates a click-able character name.<br>
	 * Does not work in public chats.
	 */
	public static String name(String name) {
		return String.format("[charname:%s;1 1 1]", name); // the 3 parameters are color values, but client doesn't render them anyways
	}

	/**
	 * @see #path(String, int)
	 */
	public static String path(Npc npc) {
		return path(StringUtils.capitalize(npc.getName()), npc.getObjectTemplate().getTemplateId());
	}

	/**
	 * @see #path(String, int)
	 */
	public static String path(int npcId) {
		NpcTemplate template = DataManager.NPC_DATA.getNpcTemplate(npcId);
		return path(template == null ? "Npc" : StringUtils.capitalize(template.getName()), npcId);
	}

	/**
	 * @see #path(String, int)
	 */
	public static String path(String npcName) {
		return String.format("[where:%s]", npcName);
	}

	/**
	 * @param linkName
	 * @param npcId
	 * @return The NPC's spawn point link.
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
	 * @param posStr
	 *          can be a string array of {mapId, x, y[, z[, layer]]} or Aion link like "{@code [pos:Teleporter;0 400010000 2128.8 1924.3 0.0 2]}"
	 * @return The {@link WorldPosition} or null if input was invalid.
	 */
	public static WorldPosition getPosition(String... posStr) {
		if (posStr == null || posStr.length == 0)
			return null;

		if (posStr[0].startsWith("[pos:")) {
			int startIndex = posStr[0].indexOf(";");
			int endIndex = posStr[0].indexOf("]");
			if (endIndex <= 0 || startIndex < 0 || startIndex > endIndex)
				return null;
			posStr = posStr[0].substring(startIndex, endIndex).trim().split("\\h+");
			if (NumberUtils.toInt(posStr[0]) <= 1) // if present, strip ely/asmo language restriction flag (0 = ely only, 1 = asmo only)
				posStr = ArrayUtils.subarray(posStr, 1, posStr.length);
		}

		if (posStr == null || posStr.length < 3)
			return null;

		int mapId = NumberUtils.toInt(posStr[0]);
		float x = NumberUtils.toFloat(posStr[1]);
		float y = NumberUtils.toFloat(posStr[2]);
		float z = posStr.length > 3 ? NumberUtils.toFloat(posStr[3]) : 0;
		int layer = posStr.length > 4 ? NumberUtils.toInt(posStr[4]) : 0;

		if (mapId == 0)
			mapId = WorldMapType.getMapId(posStr[0]);

		if (WorldMapType.getWorld(mapId) == null)
			return null;

		if (layer > 0 && z <= 0 && mapId == 400010000) { // abyss
			switch (layer) {
				case 1: // lower
					z = 1700;
					break;
				case 2: // core
					z = 2350;
					break;
				case 3: // upper
					if (!GeoDataConfig.GEO_ENABLE) // only with deactivated geo, since z collisions are checked from top to bottom anyways
						z = 2900;
					break;
			}
		}

		if (z > 0)
			z = GeoService.getInstance().getZ(mapId, x, y, z, 0, 0); // search relative to input z (max diff = z +2/-100)
		else
			z = GeoService.getInstance().getZ(mapId, x, y);

		return World.getInstance().createPosition(mapId, x, y, z, (byte) 0, 0);
	}

	public static String item(int itemId) {
		return String.format("[item:%d]", itemId);
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

	public static String getRealAdminName(String name) {
		// don't perform expensive checks if name is already qualified
		if (name.matches("^[A-Za-z]+$"))
			return Util.convertName(name);

		String nameFormat;
		String nameFlag = "%s";
		for (int i = 1; i < 99; i++) {
			try {
				nameFormat = AdminConfig.class.getField("CUSTOMTAG_ACCESS" + i).get(null).toString();
				if (nameFormat != null && nameFormat.contains(nameFlag)) {
					int nameStartIndex = nameFormat.indexOf(nameFlag);
					String namePrefix = nameFormat.substring(0, nameStartIndex > 0 ? nameStartIndex : 0);
					String nameSuffix = nameFormat.substring(nameStartIndex + nameFlag.length(), nameFormat.length());
					if ((namePrefix + nameSuffix).length() > 0 && name.startsWith(namePrefix) && name.endsWith(nameSuffix)) {
						int endIndex = name.indexOf(nameSuffix) - 1;
						name = name.substring(namePrefix.length(), endIndex > 0 ? endIndex : name.length());
						break;
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				break; // (expecting NoSuchFieldException to trigger loop break)
			}
		}

		return Util.convertName(name);
	}
}
