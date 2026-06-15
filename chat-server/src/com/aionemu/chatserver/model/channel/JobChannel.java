package com.aionemu.chatserver.model.channel;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.aionemu.chatserver.model.ChannelType;
import com.aionemu.chatserver.model.Race;

/**
 * @author ATracer, Neon
 */
public class JobChannel extends RaceChannel {

	private static final List<Set<String>> aliasSets = List.of(
		// Order of languages: NA English, GF English, German, Spanish, Italian, French, Polish, Turkish, Russian, Chinese, Korean
		newOrderedSet("Gladiator", "Gladiador", "Gladiatore", "Gladiateur", "Gladyatör", "Гладиатор", "剑星", "검성"),
		newOrderedSet("Templar", "Templer", "Templario", "Templare", "Templier", "Templariusz", "Tapınakçı", "Страж", "守护星", "수호성"),
		newOrderedSet("Assassin", "Assassine", "Asesino", "Assassino", "Asasyn", "Suikastçı", "Убийца", "杀星", "살성"),
		newOrderedSet("Ranger", "Jäger", "Cazador", "Cacciatore", "Rôdeur", "Łowca", "Avcı", "Стрелок", "弓星", "궁성"),
		newOrderedSet("Sorcerer", "Zauberer", "Hechicero", "Fattucchiere", "Sorcier", "Czarodziej", "Sihirbaz", "Волшебник", "魔道星", "마도성"),
		newOrderedSet("Spiritmaster", "Beschwörer", "Invocador", "Incantatore", "Spiritualiste", "Zaklinacz", "Ruh Çağırıcı", "Заклинатель", "精灵星", "정령성"),
		newOrderedSet("Cleric", "Kleriker", "Clérigo", "Chierico", "Clerc", "Kleryk", "Ruhban", "Целитель", "治愈星", "치유성"),
		newOrderedSet("Chanter", "Kantor", "Cantor", "Cantore", "Aède", "Чародей", "护法星", "호법성"),
		newOrderedSet("Aethertech", "Äthertech", "Técnico del éter", "Tecnico dell'etere", "Éthertech", "EterTech", "Etertek", "Пилот", "机甲星", "기갑성"),
		newOrderedSet("Gunslinger", "Gunner", "Schütze", "Tirador", "Tiratore", "Pistolero", "Strzelec", "Nişancı", "Снайпер", "枪炮星", "사격성"),
		newOrderedSet("Songweaver", "Bard", "Barde", "Bardo", "Ozan", "Бард", "吟游星", "음유성")
	);

	private final Set<String> classIdentifiers;

	public JobChannel(int gameServerId, Race race, String classIdentifier) {
		super(ChannelType.JOB, gameServerId, race);
		this.classIdentifiers = withAliases(classIdentifier.split("\\[f:")[0]);
	}

	public boolean hasAliases() {
		return classIdentifiers.size() > 1;
	}

	@Override
	public boolean matches(ChannelType channelType, int gameServerId, Race race, String classIdentifier) {
		return super.matches(channelType, gameServerId, race, classIdentifier) && classIdentifiers.contains(classIdentifier);
	}

	@Override
	public String name() {
		return classIdentifiers.iterator().next() + " (" + getRace().name().charAt(0) + ")";
	}

	private Set<String> withAliases(String classIdentifier) {
		return aliasSets.stream().filter(aliases -> aliases.contains(classIdentifier)).findFirst()
			.orElseGet(() -> Collections.singleton(classIdentifier));
	}

	private static Set<String> newOrderedSet(String... values) {
		return new LinkedHashSet<>(List.of(values));
	}
}
