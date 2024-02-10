package com.aionemu.gameserver.model.autogroup;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
public interface AutoInstanceHandler {

	void onInstanceCreate(WorldMapInstance instance);

	AGQuestion addLookingForParty(LookingForParty lookingForParty);

	void onEnterInstance(Player player);

	void onLeaveInstance(Player player);

	void onPressEnter(Player player);

	void unregister(Player player);
}
