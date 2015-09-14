package com.aionemu.gameserver.model.gameobjects.player.title;

import com.aionemu.gameserver.model.IExpirable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.TitleTemplate;

/**
 * @author Mr. Poke
 */
public class Title implements IExpirable {

	private TitleTemplate template;
	private int id;
	private int dispearTime;

	/**
	 * @param template
	 * @param id
	 * @param dispearTime
	 */
	public Title(TitleTemplate template, int id, int dispearTime) {
		this.template = template;
		this.id = id;
		this.dispearTime = dispearTime;
	}

	/**
	 * @return Returns the template.
	 */
	public TitleTemplate getTemplate() {
		return template;
	}

	/**
	 * @return Returns the id.
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return Returns the dispearTime.
	 */
	public int getRemainingTime() {
		if (dispearTime == 0)
			return 0;
		return dispearTime - (int) (System.currentTimeMillis() / 1000);
	}

	@Override
	public int getExpireTime() {
		return dispearTime;
	}

	@Override
	public void expireEnd(Player player) {
		player.getTitleList().removeTitle(id);
	}

	@Override
	public void expireMessage(Player player, int time) {
	}

	@Override
	public boolean canExpireNow() {
		return true;
	}
}
