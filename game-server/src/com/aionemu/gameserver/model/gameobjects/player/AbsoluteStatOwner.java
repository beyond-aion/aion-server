package com.aionemu.gameserver.model.gameobjects.player;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.templates.stats.ModifiersTemplate;

/**
 * @author Rolandas
 */
public class AbsoluteStatOwner implements StatOwner {

	Player target;
	ModifiersTemplate template;
	boolean isActive = false;

	public AbsoluteStatOwner(Player player, int templateId) {
		this.target = player;
		setTemplate(templateId);
	}

	public boolean isActive() {
		return isActive;
	}

	public void setTemplate(int templateId) {
		if (isActive)
			cancel();
		this.template = DataManager.ABSOLUTE_STATS_DATA.getTemplate(templateId);
	}

	public void apply() {
		if (template == null)
			return;
		target.getGameStats().addEffect(this, template.getModifiers());
		isActive = true;
	}

	public void cancel() {
		if (template == null)
			return;
		target.getGameStats().endEffect(this);
		isActive = false;
	}

}
