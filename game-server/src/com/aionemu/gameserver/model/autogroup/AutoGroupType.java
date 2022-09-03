package com.aionemu.gameserver.model.autogroup;

import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.L10n;

/**
 * @author xTz
 */
public enum AutoGroupType implements L10n {
	BARANATH_DREDGION(1, 600000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoDredgionInstance(this);
		}
	},
	CHANTRA_DREDGION(2, 600000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoDredgionInstance(this);
		}
	},
	TERATH_DREDGION(3, 600000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoDredgionInstance(this);
		}
	},
	ELYOS_FIRE_TEMPLE(4, 300000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoGeneralInstance(this);
		}
	},
	NOCHSANA_TRAINING_CAMP(5, 600000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoGeneralInstance(this);
		}
	},
	DARK_POETA(6, 1200000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoGeneralInstance(this);
		}
	},
	// STEEL_RAKE(7, 1200000, 6) { @Override AutoInstance newAutoInstance() { return new AutoGeneralInstance(this); } },
	UDAS_TEMPLE(8, 600000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoGeneralInstance(this);
		}
	},
	LOWER_UDAS_TEMPLE(9, 600000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoGeneralInstance(this);
		}
	},
	EMPYREAN_CRUCIBLE(11, 600000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoGeneralInstance(this);
		}
	},
	ASMODIANS_FIRE_TEMPLE(14, 300000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoGeneralInstance(this);
		}
	},
	ARENA_OF_CHAOS_1(21, 110000, 1) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvPFFAInstance(this);
		}
	},
	ARENA_OF_CHAOS_2(22, 110000, 2) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvPFFAInstance(this);
		}
	},
	ARENA_OF_CHAOS_3(23, 110000, 3) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvPFFAInstance(this);
		}
	},
	ARENA_OF_DISCIPLINE_1(24, 110000, 1) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvPFFAInstance(this);
		}
	},
	ARENA_OF_DISCIPLINE_2(25, 110000, 2) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvPFFAInstance(this);
		}
	},
	ARENA_OF_DISCIPLINE_3(26, 110000, 3) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvPFFAInstance(this);
		}
	},
	CHAOS_TRAINING_GROUNDS_1(27, 110000, 1) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvPFFAInstance(this);
		}
	},
	CHAOS_TRAINING_GROUNDS_2(28, 110000, 2) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvPFFAInstance(this);
		}
	},
	CHAOS_TRAINING_GROUNDS_3(29, 110000, 3) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvPFFAInstance(this);
		}
	},
	DISCIPLINE_TRAINING_GROUNDS_1(30, 110000, 1) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvPFFAInstance(this);
		}
	},
	DISCIPLINE_TRAINING_GROUNDS_2(31, 110000, 2) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvPFFAInstance(this);
		}
	},
	DISCIPLINE_TRAINING_GROUNDS_3(32, 110000, 3) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvPFFAInstance(this);
		}
	},
	ARENA_OF_HARMONY_1(33, 110000, 1) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoHarmonyInstance(this);
		}
	},
	ARENA_OF_HARMONY_2(34, 110000, 2) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoHarmonyInstance(this);
		}
	},
	ARENA_OF_HARMONY_3(35, 110000, 3) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoHarmonyInstance(this);
		}
	},
	ARENA_OF_GLORY_1(38, 110000, 1) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvPFFAInstance(this);
		}
	},
	ARENA_OF_CHAOS_4(39, 110000, 4) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvPFFAInstance(this);
		}
	},
	ARENA_OF_DISCIPLINE_4(40, 110000, 4) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvPFFAInstance(this);
		}
	},
	ARENA_OF_HARMONY_4(41, 110000, 4) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoHarmonyInstance(this);
		}
	},
	ARENA_OF_GLORY_2(42, 110000, 2) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvPFFAInstance(this);
		}
	},
	CHAOS_TRAINING_GROUNDS_4(43, 110000, 4) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvPFFAInstance(this);
		}
	},
	DISCIPLINE_TRAINING_GROUNDS_4(44, 110000, 4) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvPFFAInstance(this);
		}
	},
	HARAMONIOUS_TRAINING_CENTER_4(45, 110000, 4) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoHarmonyInstance(this);
		}
	},
	HARAMONIOUS_TRAINING_CENTER_1(101, 110000, 1) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoHarmonyInstance(this);
		}
	},
	HARAMONIOUS_TRAINING_CENTER_2(102, 110000, 2) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoHarmonyInstance(this);
		}
	},
	HARAMONIOUS_TRAINING_CENTER_3(103, 110000, 3) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoHarmonyInstance(this);
		}
	},
	KAMAR_BATTLEFIELD(107, 600000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoKamarBattlefieldInstance(this);
		}
	},
	ENGULFED_OPHIDIAN_BRIDGE(108, 600000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoEngulfedOphidanBridgeInstance(this);
		}
	},
	IRON_WALL_WARFRONT(109, 600000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoIronWallWarfrontInstance(this);
		}
	},
	IDGEL_DOME(111, 600000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoIdgelDomeInstance(this);
		}
	},
	STEEL_RAKE_ELYOS(308, 600000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoGeneralInstance(this);
		}
	},
	STEEL_RAKE_ASMODIANS(401, 600000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoGeneralInstance(this);
		}
	};

	private int instanceMaskId;
	private int time;
	private byte difficultId;
	private AutoGroup template;

	AutoGroupType(int instanceMaskId, int time, int difficultId) {
		this(instanceMaskId, time);
		this.difficultId = (byte) difficultId;
	}

	AutoGroupType(int instanceMaskId, int time) {
		this.instanceMaskId = instanceMaskId;
		this.time = time;
		template = DataManager.AUTO_GROUP.getTemplateByInstaceMaskId(this.instanceMaskId);
	}

	public int getInstanceMapId() {
		return template.getInstanceId();
	}

	public int getInstanceMaskId() {
		return instanceMaskId;
	}

	@Override
	public int getL10nId() {
		return template.getL10nId();
	}

	public int getTitleId() {
		return template.getTitleId();
	}

	public int getTime() {
		return time;
	}

	public int getMinLevel() {
		return template.getMinLvl();
	}

	public int getMaxLevel() {
		return template.getMaxLvl();
	}

	public boolean hasRegisterGroup() {
		return template.hasRegisterGroup();
	}

	public boolean hasRegisterQuick() {
		return template.hasRegisterQuick();
	}

	public boolean hasRegisterNew() {
		return template.hasRegisterNew();
	}

	public boolean containNpcId(int npcId) {
		return template.getNpcIds().contains(npcId);
	}

	public List<Integer> getNpcIds() {
		return template.getNpcIds();
	}

	public boolean isDredgion() {
		switch (this) {
			case BARANATH_DREDGION:
			case CHANTRA_DREDGION:
			case TERATH_DREDGION:
				return true;
		}
		return false;
	}

	public boolean isIconInvite() {
		return isDredgion() || isKamarBattlefield() || isEngulfedOphidanBridge() || isIronWallWarfront() || isIdgelDome();
	}

	public boolean isKamarBattlefield() {
		return this.equals(AutoGroupType.KAMAR_BATTLEFIELD);
	}

	public boolean isEngulfedOphidanBridge() {
		return this.equals(AutoGroupType.ENGULFED_OPHIDIAN_BRIDGE);
	}

	public boolean isIronWallWarfront() {
		return this.equals(AutoGroupType.IRON_WALL_WARFRONT);
	}

	public boolean isIdgelDome() {
		return this.equals(AutoGroupType.IDGEL_DOME);
	}

	public static AutoGroupType getAGTByMaskId(int instanceMaskId) {
		for (AutoGroupType autoGroupsType : values()) {
			if (autoGroupsType.getInstanceMaskId() == instanceMaskId) {
				return autoGroupsType;
			}
		}
		return null;
	}

	public static AutoGroupType getAutoGroup(int level, int npcId) {
		for (AutoGroupType agt : values()) {
			if (agt.hasLevelPermit(level) && agt.containNpcId(npcId)) {
				return agt;
			}
		}
		return null;
	}

	public static AutoGroupType getAutoGroupByWorld(int level, int worldId) {
		for (AutoGroupType agt : values()) {
			if (agt.getInstanceMapId() == worldId && agt.hasLevelPermit(level)) {
				return agt;
			}
		}
		return null;
	}

	public static AutoGroupType getAutoGroup(int npcId) {
		for (AutoGroupType agt : values()) {
			if (agt.containNpcId(npcId)) {
				return agt;
			}
		}
		return null;
	}

	public boolean isPvPSoloArena() {
		switch (this) {
			case ARENA_OF_DISCIPLINE_1:
			case ARENA_OF_DISCIPLINE_2:
			case ARENA_OF_DISCIPLINE_3:
			case ARENA_OF_DISCIPLINE_4:
				return true;
		}
		return false;
	}

	public boolean isTrainigPvPSoloArena() {
		switch (this) {
			case DISCIPLINE_TRAINING_GROUNDS_1:
			case DISCIPLINE_TRAINING_GROUNDS_2:
			case DISCIPLINE_TRAINING_GROUNDS_3:
			case DISCIPLINE_TRAINING_GROUNDS_4:
				return true;
		}
		return false;
	}

	public boolean isPvPFFAArena() {
		switch (this) {
			case ARENA_OF_CHAOS_1:
			case ARENA_OF_CHAOS_2:
			case ARENA_OF_CHAOS_3:
			case ARENA_OF_CHAOS_4:
				return true;
		}
		return false;
	}

	public boolean isTrainigPvPFFAArena() {
		switch (this) {
			case CHAOS_TRAINING_GROUNDS_1:
			case CHAOS_TRAINING_GROUNDS_2:
			case CHAOS_TRAINING_GROUNDS_3:
			case CHAOS_TRAINING_GROUNDS_4:
				return true;
		}
		return false;
	}

	public boolean isTrainigHarmonyArena() {
		switch (this) {
			case HARAMONIOUS_TRAINING_CENTER_1:
			case HARAMONIOUS_TRAINING_CENTER_2:
			case HARAMONIOUS_TRAINING_CENTER_3:
			case HARAMONIOUS_TRAINING_CENTER_4:
				return true;
		}
		return false;
	}

	public boolean isHarmonyArena() {
		switch (this) {
			case ARENA_OF_HARMONY_1:
			case ARENA_OF_HARMONY_2:
			case ARENA_OF_HARMONY_3:
			case ARENA_OF_HARMONY_4:
				return true;
		}
		return false;
	}

	public boolean isGloryArena() {
		switch (this) {
			case ARENA_OF_GLORY_1:
			case ARENA_OF_GLORY_2:
				return true;
		}
		return false;
	}

	public boolean isPvpArena() {
		return isTrainigPvPFFAArena() || isPvPFFAArena() || isTrainigPvPSoloArena() || isPvPSoloArena();
	}

	public boolean hasLevelPermit(int level) {
		return level >= getMinLevel() && level <= getMaxLevel();
	}

	public byte getDifficultId() {
		return difficultId;
	}

	public AutoInstance getAutoInstance() {
		return newAutoInstance();
	}

	abstract AutoInstance newAutoInstance();
}
