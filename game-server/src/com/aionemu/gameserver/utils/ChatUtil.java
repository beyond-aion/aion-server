package com.aionemu.gameserver.utils;

import java.awt.Color;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author antness, Neon
 */
public class ChatUtil {

	/**
	 * @see #color(String, int, int, int)
	 */
	public static String color(String message, Color color) {
		if (color == null)
			color = Color.WHITE;

		return color(message, color.getRed(), color.getGreen(), color.getBlue());
	}

	/**
	 * This creates a colored text message.<br>
	 * Does not work in public chats. The valid message length is limited. In fact the message limit depends on the length of the internal link
	 * parameters, the whole
	 * link becomes invalid when exceeding x characters.
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
		return String.format("[color:%s;%.2g %.2g %.2g]", message, r / 255d, g / 255d, b / 255d);
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
		// TODO: need rework for abyss map
		return String.format("[pos:%s;%d %f %f %f -1]", label, worldId, x, y, z);
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
	 * @param itemLink
	 * @return The item ID or 0 if itemLink did not contain a valid ID.
	 */
	public static int getItemId(String itemLink) {
		if (itemLink == null)
			return 0;

		if (itemLink.startsWith("[item:"))
			itemLink = itemLink.substring("[item:".length()).trim();

		if (itemLink.matches("^1[0-9]{8}.*$"))
			return NumberUtils.toInt(itemLink.substring(0, 9));

		return 0;
	}

	public static String getRealAdminName(String name) {
		// don't perform expensive checks if name is already qualified
		if (name.matches("^[A-Za-z]+$"))
			return name;

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

		return name;
	}
}
