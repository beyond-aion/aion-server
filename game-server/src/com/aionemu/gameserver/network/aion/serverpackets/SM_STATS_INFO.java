package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.stats.container.PlayerGameStats;
import com.aionemu.gameserver.model.stats.container.PlayerLifeStats;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.GameTimeService;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * In this packet Server is sending User Info?
 * 
 * @author -Nemesiss-, Luno, ginho1
 */
public class SM_STATS_INFO extends AionServerPacket {

	private Player player;
	private PlayerGameStats pgs;
	private PlayerLifeStats pls;
	private PlayerCommonData pcd;

	public SM_STATS_INFO(Player player) {
		this.player = player;
		this.pcd = player.getCommonData();
		this.pgs = player.getGameStats();
		this.pls = player.getLifeStats();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(player.getObjectId());// Player ObjectId
		writeD(GameTimeService.getInstance().getGameTime().getTime());// Minutes since 1/1/00 00:00:00

		writeH(pgs.getPower().getCurrent());// [current power]
		writeH(pgs.getHealth().getCurrent());// [current health]
		writeH(pgs.getAccuracy().getCurrent());// [current accuracy]
		writeH(pgs.getAgility().getCurrent());// [current agility]
		writeH(pgs.getKnowledge().getCurrent());// [current knowledge]
		writeH(pgs.getWill().getCurrent());// [current will]

		writeH(pgs.getStat(StatEnum.WATER_RESISTANCE, 0).getCurrent());// [current water]
		writeH(pgs.getStat(StatEnum.WIND_RESISTANCE, 0).getCurrent());// [current wind]
		writeH(pgs.getStat(StatEnum.EARTH_RESISTANCE, 0).getCurrent());// [current earth]
		writeH(pgs.getStat(StatEnum.FIRE_RESISTANCE, 0).getCurrent());// [current fire]
		writeH(pgs.getStat(StatEnum.ELEMENTAL_RESISTANCE_LIGHT, 0).getCurrent());// [current light resistance]
		writeH(pgs.getStat(StatEnum.ELEMENTAL_RESISTANCE_DARK, 0).getCurrent());// [current dark resistance]

		writeH(player.getLevel());// [level]

		// something like very dynamic
		writeH(0);// [unk]
		writeH(0);// [unk]
		writeH(0);// [unk]

		writeQ(pcd.getExpNeed());// [xp till next lv]
		writeQ(pcd.getExpRecoverable());// [recoverable exp]
		writeQ(pcd.getExpShown());// [current xp]

		writeD(0);// [unk]

		writeD(pgs.getMaxHp().getCurrent());// [max hp]
		writeD(pls.getCurrentHp());// [current hp]

		writeD(pgs.getMaxMp().getCurrent());// [max mana]
		writeD(pls.getCurrentMp());// [current mana]

		writeH(pgs.getMaxDp().getCurrent());// [max dp]
		writeH(pcd.getDp());// [current dp]

		writeD(pgs.getFlyTime().getCurrent());// [max fly time]
		writeD(pls.getCurrentFp());// [current fly time]

		writeC(player.getFlyState());// [fly state]
		writeC(player.getMoveController().getMovementMask());// [movementMask]

		writeH(pgs.getMainHandPAttack(CalculationType.DISPLAY).getCurrent());// [current main hand attack]
		writeH(pgs.getOffHandPAttack(CalculationType.DISPLAY).getCurrent());// [current off hand attack]

		writeH(0);// unk 3.0

		writeD(pgs.getPDef().getCurrent());// [current pdef]
		writeH(pgs.getMainHandMAttack(CalculationType.DISPLAY).getCurrent());// [current magic attack]
		writeH(pgs.getOffHandMAttack(CalculationType.DISPLAY).getCurrent());// [current off hand magic attack]
		writeD(pgs.getMDef().getCurrent()); // [Current magic def]
		writeH(pgs.getMResist().getCurrent());// [current mres]
		writeH(0);// unk 3.0
		writeF(pgs.getAttackRange().getCurrent() / 1000f);// attack range
		writeH(pgs.getAttackSpeed().getCurrent());// attack speed
		writeH(pgs.getEvasion().getCurrent());// [current evasion]
		writeH(pgs.getParry().getCurrent());// [current parry]
		writeH(pgs.getBlock().getCurrent());// [current block]
		writeH(pgs.getMainHandPCritical().getCurrent());// [current main hand crit rate]
		writeH(pgs.getOffHandPCritical().getCurrent());// [current off hand crit rate]
		writeH(pgs.getMainHandPAccuracy().getCurrent());// [current main_hand_accuracy]
		writeH(pgs.getOffHandPAccuracy().getCurrent());// [current off_hand_accuracy]

		writeH(1);// [unk]

		writeH(pgs.getMAccuracy().getCurrent());// [current magic accuracy]
		writeH(pgs.getMCritical().getCurrent());// [current crit spell]

		writeH(0);// [unk]

		writeF(pgs.getReverseStat(StatEnum.BOOST_CASTING_TIME, 1000).getCurrent() / 1000f);// [current casting speed]
		writeH(0); // [unk 3.5]
		writeH(pgs.getStat(StatEnum.CONCENTRATION, 0).getCurrent());// [current concetration]
		writeH(pgs.getMBoost().getCurrent());// [current magic boost]
		writeH(pgs.getMBResist().getCurrent());// [current magic suppression]
		writeH(pgs.getStat(StatEnum.HEAL_BOOST, 0).getCurrent());// [current heal_boost]
		writeH(0);// unk
		writeH(pgs.getPCR().getCurrent()); // [current strike resist]
		writeH(pgs.getMCR().getCurrent());// [current spell resist]
		writeH(pgs.getStat(StatEnum.PHYSICAL_CRITICAL_DAMAGE_REDUCE, 0).getCurrent());// [current strike fortitude]
		writeH(pgs.getStat(StatEnum.MAGICAL_CRITICAL_DAMAGE_REDUCE, 0).getCurrent());// [current spell fortitude]
		writeD(player.getInventory().getLimit());
		writeD(player.getInventory().size());
		writeD(0);// [unk]
		writeD(0);// [unk]
		writeD(pcd.getPlayerClass().getClassId());// [Player Class id]

		writeH(0);// unk 3.0
		writeH(0);// unk 3.0
		writeH(0); // [unk 3.5]
		writeH(0); // [unk 3.5]
		writeQ(pcd.getCurrentReposeEnergy());
		writeQ(pcd.getMaxReposeEnergy());
		writeQ(pcd.getCurrentSalvationPercent());

		writeH(0); // 4.3 NA
		writeH(0); // 4.3 NA
		writeH(1); // 4.3 NA
		writeH(0); // 4.3 NA
		writeH(0); //4.8
		writeH(0); //4.8
		writeH(0); //4.8
		writeH(0); //4.8
		writeH(pgs.getPower().getBase());// [base power]
		writeH(pgs.getHealth().getBase());// [base health]
		writeH(pgs.getAccuracy().getBase());// [base accuracy]
		writeH(pgs.getAgility().getBase());// [base agility]
		writeH(pgs.getKnowledge().getBase());// [base knowledge]
		writeH(pgs.getWill().getBase());// [base will]
		writeH(pgs.getStat(StatEnum.WATER_RESISTANCE, 0).getBase());// [base water res]
		writeH(pgs.getStat(StatEnum.WIND_RESISTANCE, 0).getBase());// [base water res]
		writeH(pgs.getStat(StatEnum.EARTH_RESISTANCE, 0).getBase());// [base earth resist]
		writeH(pgs.getStat(StatEnum.FIRE_RESISTANCE, 0).getBase());// [base water res]
		writeH(pgs.getStat(StatEnum.ELEMENTAL_RESISTANCE_LIGHT, 0).getBase());// [base light resistance]
		writeH(pgs.getStat(StatEnum.ELEMENTAL_RESISTANCE_DARK, 0).getBase());// [base dark resistance]
		writeD(pgs.getMaxHp().getBase());// [base hp]
		writeD(pgs.getMaxMp().getBase());// [base mana]
		writeH(pgs.getMaxDp().getBase());// [base dp]
		writeH(21592);// to do display_max_point
		writeD(pgs.getFlyTime().getBase());// [fly time]
		writeH(pgs.getMainHandPAttack(CalculationType.DISPLAY).getBase());// [base main hand attack]
		writeH(pgs.getOffHandPAttack(CalculationType.DISPLAY).getBase());// [base off hand attack]
		writeH(pgs.getMainHandMAttack(CalculationType.DISPLAY).getBase());// [base main hand magic attack]
		writeH(pgs.getOffHandMAttack(CalculationType.DISPLAY).getBase());// [base off hand magic attack]
		writeD(pgs.getPDef().getBase()); // [base pdef]
		writeD(pgs.getMDef().getBase());// [base magic def]
		writeH(pgs.getMResist().getBase());// [base magic res]
		writeF(pgs.getAttackRange().getBase() / 1000f);// [base attack range]
		writeH(0); // [unk 3.5]
		writeH(pgs.getEvasion().getBase());// [base evasion]
		writeH(pgs.getParry().getBase());// [base parry]
		writeH(pgs.getBlock().getBase());// [base block]
		writeH(pgs.getMainHandPCritical().getBase());// [base main hand crit rate]
		writeH(pgs.getOffHandPCritical().getBase());// [base off hand crit rate]
		writeH(pgs.getMCritical().getBase());// [base magical crit rate]

		writeH(0);// [unk]
		writeH(pgs.getMainHandPAccuracy().getBase());// [base main hand accuracy]
		writeH(pgs.getOffHandPAccuracy().getBase());// [base off hand accuracy]

		writeH(0);// [unk]
		writeH(pgs.getMAccuracy().getBase());// [base magic accuracy]
		writeH(pgs.getStat(StatEnum.CONCENTRATION, 0).getBase());// [base concentration]
		writeH(pgs.getMBoost().getBase());// [base magic boost]
		writeH(pgs.getMBResist().getBase()); // [base magic suppression]
		writeH(pgs.getStat(StatEnum.HEAL_BOOST, 0).getBase());// [base healboost]
		writeH(0);// unk
		writeH(pgs.getPCR().getBase());// [base strike resist]
		writeH(pgs.getMCR().getBase());// [base spell resist]
		writeH(pgs.getStat(StatEnum.PHYSICAL_CRITICAL_DAMAGE_REDUCE, 0).getBase());// [base strike fortitude]
		writeH(pgs.getStat(StatEnum.MAGICAL_CRITICAL_DAMAGE_REDUCE, 0).getBase());// [base spell fortitude]
	}

}
