package com.aionemu.gameserver.dataholders;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * AP and DP multipliers for killed players by the level difference between killer and victim.
 */
@XmlRootElement(name = "pvp_mod_table")
@XmlAccessorType(XmlAccessType.NONE)
public class PvpModTable extends LevelDiffModTable {

}
