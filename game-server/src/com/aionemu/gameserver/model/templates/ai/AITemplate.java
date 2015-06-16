package com.aionemu.gameserver.model.templates.ai;

import com.aionemu.gameserver.model.ai.Ai;
import com.aionemu.gameserver.model.ai.Bombs;
import com.aionemu.gameserver.model.ai.Summons;

/**
 * @author xTz
 */
public class AITemplate {

	private int npcId;
	private Summons summons;
	private Bombs bombs;

	public AITemplate() {
	}

	public AITemplate(Ai template) {
		this.summons = template.getSummons();
		this.bombs = template.getBombs();
		this.npcId = template.getNpcId();
	}

	public int getNpcId() {
		return npcId;
	}

	public Summons getSummons() {
		return summons;
	}

	public Bombs getBombs() {
		return bombs;
	}
}
