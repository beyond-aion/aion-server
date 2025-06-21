package com.aionemu.gameserver.services;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npc.AbyssNpcType;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawnTemplate;

/**
 * @author Cheatkiller
 */
public class TribeRelationService {

	public static boolean isAggressive(Creature creature1, Creature creature2) {
		switch (creature1.getTribe()) {
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
		if (creature2 instanceof Player p && p.getPanesterraFaction() != null && creature1.getTribe().name().startsWith("GAB1_")) {
			if (creature1.getTribe() == p.getPanesterraFaction().getTribe())
				return false;
			return DataManager.TRIBE_RELATIONS_DATA.isAggressiveRelation(creature1.getTribe(), p.getPanesterraFaction().getTribe());
		}

		return DataManager.TRIBE_RELATIONS_DATA.isAggressiveRelation(creature1.getTribe(), creature2.getTribe());
	}

	public static boolean isFriend(Creature creature1, Creature creature2) {
		if (creature1.getTribe() == creature2.getTribe()) // OR BASE ????
			return true;
		if (creature1.getTribe() == TribeClass.IDF5U2_SHULACK && creature2.getTribe() == TribeClass.FIELD_OBJECT_ALL_HOSTILEMONSTER)
			return true;
		switch (creature1.getBaseTribe()) {
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
		if (creature2 instanceof Player p && p.getPanesterraFaction() != null && creature1.getTribe().name().startsWith("GAB1_")) {
			if (creature1.getTribe() == p.getPanesterraFaction().getTribe())
				return true;
			return DataManager.TRIBE_RELATIONS_DATA.isFriendlyRelation(creature1.getTribe(), p.getPanesterraFaction().getTribe());
		}

		return DataManager.TRIBE_RELATIONS_DATA.isFriendlyRelation(creature1.getTribe(), creature2.getTribe());
	}

	public static boolean isSupport(Creature creature1, Creature creature2) {
		if (creature1.getTribe() == creature2.getTribe() || creature1.getBaseTribe() == creature2.getTribe()
			|| creature1.getTribe() == creature2.getBaseTribe() || creature1.getBaseTribe() == creature2.getBaseTribe()) {
			return true;
		}
		switch (creature1.getBaseTribe()) {
			case GUARD_DARK:
				if (creature2.getBaseTribe() == TribeClass.PC_DARK)
					return true;
				break;
			case GUARD:
				if (creature2.getBaseTribe() == TribeClass.PC)
					return true;
				break;
		}
		if (creature2 instanceof Player p && p.getPanesterraFaction() != null && creature1.getTribe().name().startsWith("GAB1_")) {
			if (creature1.getTribe() == p.getPanesterraFaction().getTribe())
				return true;
			return DataManager.TRIBE_RELATIONS_DATA.isSupportRelation(creature1.getTribe(), p.getPanesterraFaction().getTribe());
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
			case GAB1_PEACE:
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
		if (creature1.getBaseTribe() == TribeClass.MONSTER) {
			switch (creature2.getBaseTribe()) {
				case PC_DARK:
				case PC:
					return true;
			}
		}

		if (creature2 instanceof Player p && p.getPanesterraFaction() != null && creature1.getTribe().name().startsWith("GAB1_")) {
			if (creature1.getTribe() == p.getPanesterraFaction().getTribe())
				return false;
			return DataManager.TRIBE_RELATIONS_DATA.isHostileRelation(creature1.getTribe(), p.getPanesterraFaction().getTribe());
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

	public static boolean canHelpCreature(Creature creature1, Creature creature2) {
		return creature1.getTribe() == creature2.getTribe()
			|| DataManager.TRIBE_RELATIONS_DATA.isSupportRelation(creature1.getTribe(), creature2.getTribe())
			|| DataManager.TRIBE_RELATIONS_DATA.isFriendlyRelation(creature1.getTribe(), creature2.getTribe());
	}
}
