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
	public static <T> Predicate<T> alwaysTrue() {
		return ALWAYS_TRUE;
	}

	public static class Players {

		private Players() {
		}

		public static final Predicate<Player> ONLINE = Player::isOnline;

		public static final Predicate<Player> WITH_LOOT_PET = player -> player.getPet() != null
			&& player.getPet().getObjectTemplate().containsFunction(PetFunctionType.LOOT);

		public static Predicate<Player> sameRace(Player p) {
			return player -> p.getRace() == player.getRace();
		}

		public static Predicate<Player> allExcept(Player ignored) {
			return player -> !player.equals(ignored);
		}

		public static Predicate<Player> canBeMentoredBy(Player mentor) {
			return player -> player.getLevel() + 10 <= mentor.getLevel();
		}

	}
}
