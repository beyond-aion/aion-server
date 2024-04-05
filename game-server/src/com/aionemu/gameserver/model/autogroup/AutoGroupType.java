package com.aionemu.gameserver.model.autogroup;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.L10n;

/**
 * @author xTz, Estrayl
 */
public enum AutoGroupType implements L10n {

	BARANATH_DREDGION(1, 420000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvpInstance(this);
		}
	},
	CHANTRA_DREDGION(2, 420000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvpInstance(this);
		}
	},
	TERATH_DREDGION(3, 420000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvpInstance(this);
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
	KAMAR_BATTLEFIELD(107, 230000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvpInstance(this);
		}
	},
	ENGULFED_OPHIDIAN_BRIDGE(108, 230000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvpInstance(this);
		}
	},
	IRON_WALL_WARFRONT(109, 230000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvpInstance(this);
		}
	},
	IDGEL_DOME(111, 170000) {

		@Override
		AutoInstance newAutoInstance() {
			return new AutoPvpInstance(this);
		}
	};

	private final AutoGroup template;
	private final int maximumJoinTime;
	private byte difficultId;

	AutoGroupType(int instanceMaskId, int maximumJoinTime, int difficultId) {
		this(instanceMaskId, maximumJoinTime);
		this.difficultId = (byte) difficultId;
	}

	AutoGroupType(int instanceMaskId, int maximumJoinTime) {
		this.maximumJoinTime = maximumJoinTime;
		template = DataManager.AUTO_GROUP.getTemplateByInstanceMaskId(instanceMaskId);
		if (template == null)
			LoggerFactory.getLogger(AutoGroupType.class).warn("No auto group template for instanceMaskId={} found.", instanceMaskId);
	}

	public AutoGroup getTemplate() {
		return template;
	}

	@Override
	public int getL10nId() {
		return template.getL10nId();
	}

	public int getMaximumJoinTime() {
		return maximumJoinTime;
	}

	public boolean containNpcId(int npcId) {
		return template.getNpcIds().contains(npcId);
	}

	public boolean isPeriodicInstance() {
		return this == KAMAR_BATTLEFIELD || this == ENGULFED_OPHIDIAN_BRIDGE || this == IDGEL_DOME || this == IRON_WALL_WARFRONT
			|| this == CHANTRA_DREDGION || this == BARANATH_DREDGION || this == TERATH_DREDGION;
	}

	public static AutoGroupType getAGTByMaskId(int instanceMaskId) {
		for (AutoGroupType autoGroupType : values()) {
			if (autoGroupType.getTemplate().getMaskId() == instanceMaskId) {
				return autoGroupType;
			}
		}
		return null;
	}

	public static AutoGroupType getAutoGroup(int level, int npcId) {
		for (AutoGroupType agt : values()) {
			if (agt.isInLvlRange(level) && agt.containNpcId(npcId)) {
				return agt;
			}
		}
		return null;
	}

	public static AutoGroupType getAutoGroupByWorld(int level, int worldId) {
		for (AutoGroupType agt : values()) {
			if (agt.getTemplate().getInstanceMapId() == worldId && agt.isInLvlRange(level)) {
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
		return switch (this) {
			case ARENA_OF_DISCIPLINE_1, ARENA_OF_DISCIPLINE_2, ARENA_OF_DISCIPLINE_3, ARENA_OF_DISCIPLINE_4 -> true;
			default -> false;
		};
	}

	public boolean isTrainingPvPSoloArena() {
		return switch (this) {
			case DISCIPLINE_TRAINING_GROUNDS_1, DISCIPLINE_TRAINING_GROUNDS_2, DISCIPLINE_TRAINING_GROUNDS_3, DISCIPLINE_TRAINING_GROUNDS_4 -> true;
			default -> false;
		};
	}

	public boolean isPvPFFAArena() {
		return switch (this) {
			case ARENA_OF_CHAOS_1, ARENA_OF_CHAOS_2, ARENA_OF_CHAOS_3, ARENA_OF_CHAOS_4 -> true;
			default -> false;
		};
	}

	public boolean isTrainingPvPFFAArena() {
		return switch (this) {
			case CHAOS_TRAINING_GROUNDS_1, CHAOS_TRAINING_GROUNDS_2, CHAOS_TRAINING_GROUNDS_3, CHAOS_TRAINING_GROUNDS_4 -> true;
			default -> false;
		};
	}

	public boolean isTrainingHarmonyArena() {
		return switch (this) {
			case HARAMONIOUS_TRAINING_CENTER_1, HARAMONIOUS_TRAINING_CENTER_2, HARAMONIOUS_TRAINING_CENTER_3, HARAMONIOUS_TRAINING_CENTER_4 -> true;
			default -> false;
		};
	}

	public boolean isHarmonyArena() {
		return switch (this) {
			case ARENA_OF_HARMONY_1, ARENA_OF_HARMONY_2, ARENA_OF_HARMONY_3, ARENA_OF_HARMONY_4 -> true;
			default -> false;
		};
	}

	public boolean isGloryArena() {
		return switch (this) {
			case ARENA_OF_GLORY_1, ARENA_OF_GLORY_2 -> true;
			default -> false;
		};
	}

	public boolean isPvpArena() {
		return isTrainingPvPFFAArena() || isPvPFFAArena() || isTrainingPvPSoloArena() || isPvPSoloArena();
	}

	public boolean isInLvlRange(int level) {
		return level >= template.getMinLvl() && level <= template.getMaxLvl();
	}

	public byte getDifficultId() {
		return difficultId;
	}

	public AutoInstance createAutoInstance() {
		return newAutoInstance();
	}

	abstract AutoInstance newAutoInstance();
}
