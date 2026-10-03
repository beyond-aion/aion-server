package com.aionemu.gameserver.model.templates.item;

import java.util.List;

/**
 * A weighted alternative of a {@link DecomposableSet}, granting either a single item or a bundle of them.
 */
public interface DecomposedReward {

	float getChance();

	List<DecomposedItem> getItems();
}
