package com.aionemu.gameserver.utils;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author antness
 */
public class ChatUtil {

	public static String position(String label, WorldPosition pos) {
		return position(label, pos.getMapId(), pos.getX(), pos.getY(), pos.getZ());
	}

	public static String position(String label, long worldId, float x, float y, float z) {
		// TODO: need rework for abyss map
		return String.format("[pos:%s;%d %f %f %f -1]", label, worldId, x, y, z);
	}

	public static String item(long itemId) {
		return String.format("[item: %d]", itemId);
	}

	public static String recipe(long recipeId) {
		return String.format("[recipe: %d]", recipeId);
	}

	public static String quest(int questId) {
		return String.format("[quest: %d]", questId);
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
