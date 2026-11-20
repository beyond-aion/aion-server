package com.aionemu.gameserver.dataholders;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * PvP XP multipliers by the level difference between killer and victim.
 *
 * @author SVDNESS
 */
@XmlRootElement(name = "pvp_exp_mod_table")
@XmlAccessorType(XmlAccessType.NONE)
public class PvpExpModTable extends LevelDiffModTable {

}
