package com.aionemu.gameserver.utils.collections;

import java.util.function.Predicate;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.pet.PetFunctionType;

/**
 * @author ATracer, Neon
 */
public class Predicates {

	private Predicates() {
	}

	@SuppressWarnings("rawtypes")
	private static final Predicate ALWAYS_TRUE = x -> true;

	@SuppressWarnings("unchecked")
	public static final <T> Predicate<T> alwaysTrue() {
		return ALWAYS_TRUE;
	}

	public static class Players {

		private Players() {
		}

		public static final Predicate<Player> ONLINE = player -> player.isOnline();

		public static final Predicate<Player> WITH_LOOT_PET = player -> player.getPet() != null
			&& player.getPet().getObjectTemplate().getPetFunction(PetFunctionType.LOOT) != null;

		public static final Predicate<Player> sameRace(Player p) {
			return player -> p.getRace() == player.getRace();
		}
		public static final Predicate<Player> allExcept(Player skipped) {
			return player -> !player.equals(skipped);
		}

		public static final Predicate<Player> canBeMentoredBy(Player mentor) {
			return player -> player.getLevel() + 10 <= mentor.getLevel();
		}

		public static final class SameInstanceFilter implements Predicate<Player> {

			private final Player player;

			public SameInstanceFilter(Player player) {
				this.player = player;
			}

			@Override
			public boolean test(Player member) {
				return member.getInstanceId() == player.getInstanceId();
			}

		}
	}
}
