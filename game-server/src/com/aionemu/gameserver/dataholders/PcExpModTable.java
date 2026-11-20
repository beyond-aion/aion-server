package com.aionemu.gameserver.dataholders;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * XP multipliers for defeated NPCs by the level difference between player and NPC.
 */
@XmlRootElement(name = "pcexp_mod_table")
@XmlAccessorType(XmlAccessType.NONE)
public class PcExpModTable extends LevelDiffModTable {

	@Override
	protected int getLevelStep() {
		return 5;
	}
}
