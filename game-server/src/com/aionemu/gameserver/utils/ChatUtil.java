package com.aionemu.gameserver.utils;

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
		int index = name.lastIndexOf(" ");
		if (index == -1)
			return name;
		return name.substring(index + 1);	
	}
	
}
