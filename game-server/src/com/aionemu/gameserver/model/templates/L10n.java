package com.aionemu.gameserver.model.templates;

import com.aionemu.gameserver.utils.ChatUtil;

/**
 * This interface should be implemented by all templates that include a client description ID field (also known as name ID)
 * 
 * @author Neon
 */
public interface L10n {

	/**
	 * @return The ID of the given client string
	 */
	public int getL10nId();

	/**
	 * @return String identifier for a client message.
	 * @see ChatUtil#l10n(int)
	 */
	public default String getL10n() {
		return ChatUtil.l10n(getL10nId());
	}

}
