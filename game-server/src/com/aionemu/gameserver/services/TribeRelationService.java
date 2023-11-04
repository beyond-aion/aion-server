package com.aionemu.gameserver.services;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.templates.npc.AbyssNpcType;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawnTemplate;

/**
 * @author Cheatkiller
 */
public class TribeRelationService {

	public static boolean isAggressive(Creature creature1, Creature creature2) {
		switch (creature1.getTribe()) {
			case GAB1_MONSTER:
				if (creature2 instanceof Player)
					return true;
				break;
			case GAB1_MONSTER_NONAGGRE:
				if (creature2 instanceof Player)
					return false;
				break;
			case AGGRESSIVESINGLEMONSTER:
				if (creature2.getTribe() == TribeClass.YUN_GUARD)
					return true;
				break;
			case IDF5U2_SHULACK:
				if (creature2.getTribe() == TribeClass.FIELD_OBJECT_ALL_HOSTILEMONSTER)
					return false;
				break;
		}
		switch (creature1.getBaseTribe()) {
			case GAB1_01_POINT_01:
			case GAB1_01_POINT_02:
			case GAB1_01_POINT_03:
			case GAB1_01_POINT_04:
			case GAB1_01_POINT_05:
			case GAB1_02_POINT_01:
			case GAB1_02_POINT_02:
			case GAB1_02_POINT_03:
			case GAB1_02_POINT_04:
			case GAB1_02_POINT_05:
			case GAB1_03_POINT_01:
			case GAB1_03_POINT_02:
			case GAB1_03_POINT_03:
			case GAB1_03_POINT_04:
			case GAB1_03_POINT_05:
			case GAB1_04_POINT_01:
			case GAB1_04_POINT_02:
			case GAB1_04_POINT_03:
			case GAB1_04_POINT_04:
			case GAB1_04_POINT_05:
			case GAB1_SUB_DEST_69_AGGRESSIVE:
			case GAB1_SUB_DEST_70_AGGRESSIVE:
			case GAB1_SUB_DEST_71_AGGRESSIVE:
			case GAB1_SUB_DEST_72_AGGRESSIVE:
				if (creature2 instanceof Player)
					return !checkPanesterraRelation(creature1, (Player) creature2);
				break;
			case GAB1_SUB_DRAKAN:
			case GAB1_SUB_KILLER:
				if (creature2.getBaseTribe() != TribeClass.GAB1_SUB_DRAKAN && creature2.getBaseTribe() != TribeClass.GAB1_SUB_KILLER
					&& creature2.getBaseTribe() != TribeClass.GAB1_SUB_ATTACKABLE_FOBJ) {
					return true;
				}
				break;
			case GUARD_DARK:
				switch (creature2.getBaseTribe()) {
					case PC:
					case GUARD:
					case GENERAL:
					case GUARD_DRAGON:
						return true;
				}
				break;
			case GUARD:
				switch (creature2.getBaseTribe()) {
					case PC_DARK:
					case GUARD_DARK:
					case GENERAL_DARK:
					case GUARD_DRAGON:
						return true;
				}
				break;
			case GUARD_DRAGON:
				switch (creature2.getBaseTribe()) {
					case PC_DARK:
					case PC:
					case GUARD:
					case GUARD_DARK:
					case GENERAL_DARK:
					case GENERAL:
						return true;
				}
				break;
		}
		return DataManager.TRIBE_RELATIONS_DATA.isAggressiveRelation(creature1.getTribe(), creature2.getTribe());
	}

	public static boolean isFriend(Creature creature1, Creature creature2) {
		if (creature1.getTribe() == creature2.getTribe()) // OR BASE ????
			return true;
		if (creature1.getTribe() == TribeClass.IDF5U2_SHULACK && creature2.getTribe() == TribeClass.FIELD_OBJECT_ALL_HOSTILEMONSTER)
			return true;
		switch (creature1.getBaseTribe()) {
			case GAB1_01_POINT_01:
			case GAB1_01_POINT_02:
			case GAB1_01_POINT_03:
			case GAB1_01_POINT_04:
			case GAB1_01_POINT_05:
			case GAB1_02_POINT_01:
			case GAB1_02_POINT_02:
			case GAB1_02_POINT_03:
			case GAB1_02_POINT_04:
			case GAB1_02_POINT_05:
			case GAB1_03_POINT_01:
			case GAB1_03_POINT_02:
			case GAB1_03_POINT_03:
			case GAB1_03_POINT_04:
			case GAB1_03_POINT_05:
			case GAB1_04_POINT_01:
			case GAB1_04_POINT_02:
			case GAB1_04_POINT_03:
			case GAB1_04_POINT_04:
			case GAB1_04_POINT_05:
			case GAB1_SUB_DEST_69:
			case GAB1_SUB_DEST_70:
			case GAB1_SUB_DEST_71:
			case GAB1_SUB_DEST_72:
			case GAB1_SUB_DEST_69_AGGRESSIVE:
			case GAB1_SUB_DEST_70_AGGRESSIVE:
			case GAB1_SUB_DEST_71_AGGRESSIVE:
			case GAB1_SUB_DEST_72_AGGRESSIVE:
				if (creature2 instanceof Player)
					return checkPanesterraRelation(creature1, (Player) creature2);
				break;
			case GAB1_MONSTER:
				if (creature2 instanceof Player)
					return false;
				break;
			case USEALL:
			case FIELD_OBJECT_ALL:
				return true;
			case GENERAL_DARK:
				if (creature1.getTribe() != TribeClass.DRAMA_EVE_NONPC_DARKA && creature1.getTribe() != TribeClass.DRAMA_EVE_NONPC_DARKB) {
					switch (creature2.getBaseTribe()) {
						case PC_DARK:
						case GUARD_DARK:
							return true;
					}
				}
				break;
			case GENERAL:
				if (creature1.getTribe() != TribeClass.DRAMA_EVE_NONPC_A && creature1.getTribe() != TribeClass.DRAMA_EVE_NONPC_B) {
					switch (creature2.getBaseTribe()) {
						case PC:
						case GUARD:
							return true;
					}
				}
				break;
			case FIELD_OBJECT_LIGHT:
				if (creature2.getBaseTribe() == TribeClass.PC)
					return true;
				break;
			case FIELD_OBJECT_DARK:
				if (creature2.getBaseTribe() == TribeClass.PC_DARK)
					return true;
				break;
		}
		return DataManager.TRIBE_RELATIONS_DATA.isFriendlyRelation(creature1.getTribe(), creature2.getTribe());
	}

	public static boolean isSupport(Creature creature1, Creature creature2) {
		if (creature1.getTribe() == creature2.getTribe() || creature1.getBaseTribe() == creature2.getTribe()
			|| creature1.getTribe() == creature2.getBaseTribe() || creature1.getBaseTribe() == creature2.getBaseTribe()) {
			return true;
		}
		switch (creature1.getBaseTribe()) {
			case GAB1_01_POINT_01:
			case GAB1_01_POINT_02:
			case GAB1_01_POINT_03:
			case GAB1_01_POINT_04:
			case GAB1_01_POINT_05:
			case GAB1_02_POINT_01:
			case GAB1_02_POINT_02:
			case GAB1_02_POINT_03:
			case GAB1_02_POINT_04:
			case GAB1_02_POINT_05:
			case GAB1_03_POINT_01:
			case GAB1_03_POINT_02:
			case GAB1_03_POINT_03:
			case GAB1_03_POINT_04:
			case GAB1_03_POINT_05:
			case GAB1_04_POINT_01:
			case GAB1_04_POINT_02:
			case GAB1_04_POINT_03:
			case GAB1_04_POINT_04:
			case GAB1_04_POINT_05:
			case GAB1_SUB_DEST_69:
			case GAB1_SUB_DEST_70:
			case GAB1_SUB_DEST_71:
			case GAB1_SUB_DEST_72:
			case GAB1_SUB_DEST_69_AGGRESSIVE:
			case GAB1_SUB_DEST_70_AGGRESSIVE:
			case GAB1_SUB_DEST_71_AGGRESSIVE:
			case GAB1_SUB_DEST_72_AGGRESSIVE:
				if (creature2 instanceof Player)
					return checkPanesterraRelation(creature1, (Player) creature2);
				break;
			case GAB1_MONSTER:
				if (creature2 instanceof Player)
					return false;
				break;
			case GUARD_DARK:
				if (creature2.getBaseTribe() == TribeClass.PC_DARK)
					return true;
				break;
			case GUARD:
				if (creature2.getBaseTribe() == TribeClass.PC)
					return true;
				break;
		}
		return DataManager.TRIBE_RELATIONS_DATA.isSupportRelation(creature1.getTribe(), creature2.getTribe());
	}

	public static boolean isNone(Creature creature1, Creature creature2) {
		if (DataManager.TRIBE_RELATIONS_DATA.isAggressiveRelation(creature1.getTribe(), creature2.getTribe())
			|| creature1 instanceof Npc && checkSiegeRelation((Npc) creature1, creature2)
			|| DataManager.TRIBE_RELATIONS_DATA.isHostileRelation(creature1.getTribe(), creature2.getTribe())
			|| DataManager.TRIBE_RELATIONS_DATA.isNeutralRelation(creature1.getTribe(), creature2.getTribe())) {
			return false;
		}
		switch (creature1.getBaseTribe()) {
			case GENERAL_DRAGON:
				return true;
			case GENERAL:
			case FIELD_OBJECT_LIGHT:
				if (creature2.getBaseTribe() == TribeClass.PC_DARK)
					return true;

				break;
			case GENERAL_DARK:
			case FIELD_OBJECT_DARK:
				if (creature2.getBaseTribe() == TribeClass.PC)
					return true;
				break;
		}
		return DataManager.TRIBE_RELATIONS_DATA.isNoneRelation(creature1.getTribe(), creature2.getTribe());
	}

	public static boolean isNeutral(Creature creature1, Creature creature2) {
		return DataManager.TRIBE_RELATIONS_DATA.isNeutralRelation(creature1.getTribe(), creature2.getTribe());
	}

	public static boolean isHostile(Creature creature1, Creature creature2) {
		if (creature1 instanceof Npc && checkSiegeRelation((Npc) creature1, creature2))
			return true;
		if (creature1.getTribe() == TribeClass.IDF5U2_SHULACK && creature2.getTribe() == TribeClass.FIELD_OBJECT_ALL_HOSTILEMONSTER)
			return false;
		switch (creature1.getBaseTribe()) {
			case GAB1_01_POINT_01:
			case GAB1_01_POINT_02:
			case GAB1_01_POINT_03:
			case GAB1_01_POINT_04:
			case GAB1_01_POINT_05:
			case GAB1_02_POINT_01:
			case GAB1_02_POINT_02:
			case GAB1_02_POINT_03:
			case GAB1_02_POINT_04:
			case GAB1_02_POINT_05:
			case GAB1_03_POINT_01:
			case GAB1_03_POINT_02:
			case GAB1_03_POINT_03:
			case GAB1_03_POINT_04:
			case GAB1_03_POINT_05:
			case GAB1_04_POINT_01:
			case GAB1_04_POINT_02:
			case GAB1_04_POINT_03:
			case GAB1_04_POINT_04:
			case GAB1_04_POINT_05:
			case GAB1_SUB_DEST_69:
			case GAB1_SUB_DEST_70:
			case GAB1_SUB_DEST_71:
			case GAB1_SUB_DEST_72:
				if (creature2 instanceof Player)
					return !checkPanesterraRelation(creature1, (Player) creature2);
				break;
			case GAB1_MONSTER_NONAGGRE:
				if (creature2 instanceof Player)
					return true;
				break;
			case GAB1_SUB_ATTACKABLE_FOBJ:
			case GAB1_SUB_NONAGGRESSIVE_DRAKAN:
			case GAB1_MONSTER:
			case MONSTER:
				switch (creature2.getBaseTribe()) {
					case PC_DARK:
					case PC:
						return true;
				}
				break;
		}
		return DataManager.TRIBE_RELATIONS_DATA.isHostileRelation(creature1.getTribe(), creature2.getTribe());
	}

	public static boolean checkSiegeRelation(Npc npc, Creature creature) {
		return ((npc.getObjectTemplate().getAbyssNpcType() != AbyssNpcType.ARTIFACT && npc.getObjectTemplate().getAbyssNpcType() != AbyssNpcType.NONE)
			|| npc.getSpawn() instanceof BaseSpawnTemplate)
			&& ((npc.getBaseTribe() == TribeClass.GENERAL && creature.getTribe() == TribeClass.PC_DARK)
				|| (npc.getBaseTribe() == TribeClass.GENERAL_DARK && creature.getTribe() == TribeClass.PC))
			|| npc.getBaseTribe() == TribeClass.GENERAL_DRAGON && npc.getObjectTemplate().getAbyssNpcType() != AbyssNpcType.ARTIFACT;
	}

	/**
	 * returns true if the player's faction holds the respective fortress and has a resulting friendly relation to the NPC.
	 * <br>
	 * <br>
	 * TODO: If Panesterra sieges will be implemented it is necessary to distinguish between each _POINT_ type.
	 * <br>
	 * So each _POINT_01 represents the fortress owner and the following four the possible attackers.
	 */
	public static boolean checkPanesterraRelation(Creature npc, Player player) {
		int panesterraFortressId = switch (npc.getBaseTribe()) {
			case GAB1_01_POINT_01, GAB1_01_POINT_02, GAB1_01_POINT_03, GAB1_01_POINT_04, GAB1_01_POINT_05, GAB1_SUB_DEST_69, GAB1_SUB_DEST_69_AGGRESSIVE -> 10111;
			case GAB1_02_POINT_01, GAB1_02_POINT_02, GAB1_02_POINT_03, GAB1_02_POINT_04, GAB1_02_POINT_05, GAB1_SUB_DEST_70, GAB1_SUB_DEST_70_AGGRESSIVE -> 10211;
			case GAB1_03_POINT_01, GAB1_03_POINT_02, GAB1_03_POINT_03, GAB1_03_POINT_04, GAB1_03_POINT_05, GAB1_SUB_DEST_71, GAB1_SUB_DEST_71_AGGRESSIVE -> 10311;
			case GAB1_04_POINT_01, GAB1_04_POINT_02, GAB1_04_POINT_03, GAB1_04_POINT_04, GAB1_04_POINT_05, GAB1_SUB_DEST_72, GAB1_SUB_DEST_72_AGGRESSIVE -> 10411;
			default -> 0;
		};
		SiegeRace occupier = SiegeService.getInstance().getFortress(panesterraFortressId).getRace();
		return SiegeRace.getByRace(player.getRace()) == occupier && npc.getRace() != Race.DRAKAN;
	}

	public static boolean canHelpCreature(Creature creature1, Creature creature2) {
		return creature1.getTribe() == creature2.getTribe()
			|| DataManager.TRIBE_RELATIONS_DATA.isSupportRelation(creature1.getTribe(), creature2.getTribe())
			|| DataManager.TRIBE_RELATIONS_DATA.isFriendlyRelation(creature1.getTribe(), creature2.getTribe());
	}
}
