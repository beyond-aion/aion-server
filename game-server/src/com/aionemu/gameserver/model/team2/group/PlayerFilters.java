package com.aionemu.gameserver.model.team2.group;

import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.pet.PetFunctionType;
import com.google.common.base.Predicate;

/**
 * @author ATracer
 */
public class PlayerFilters {

	public static final Predicate<Player> ONLINE = new Predicate<Player>() {

		@Override
		public boolean apply(Player member) {
			return member.isOnline();
		}
	};

	public static final class MentorSuiteFilter implements Predicate<Player> {

		private final Player player;

		public MentorSuiteFilter(Player player) {
			this.player = player;
		}

		@Override
		public boolean apply(Player member) {
			return member.getLevel() + 9 < player.getLevel();
		}

	}

	public static final class SameInstanceFilter implements Predicate<Player> {

		private final Player player;

		public SameInstanceFilter(Player player) {
			this.player = player;
		}

		@Override
		public boolean apply(Player member) {
			return member.getInstanceId() == player.getInstanceId();
		}

	}

	public static final Predicate<Player> HAS_LOOT_PET = new Predicate<Player>() {

		@Override
		public boolean apply(Player member) {
			Pet pet = member.getPet();
			if (pet == null)
				return false;
			return pet.getPetTemplate().getPetFunction(PetFunctionType.LOOT) != null;
		}
	};

	public static final class ExcludePlayerFilter implements Predicate<Player> {

		private final Player player;

		public ExcludePlayerFilter(Player player) {
			this.player = player;
		}

		@Override
		public boolean apply(Player member) {
			return !player.equals(member);
		}
	}
}
