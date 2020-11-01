package com.aionemu.chatserver.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ATracer, Neon
 */
public enum PlayerClass {

	// Implemented languages (in order of appearance): Russian, Turkish, NA English, GF English, German, Spanish, Italian, French, Polish
	WARRIOR(0),
	GLADIATOR(1, "Гладиатор", "Gladyatör", "Gladiator", "Gladiador", "Gladiatore", "Gladiateur"),
	TEMPLAR(2, "Страж", "Tapınakçı", "Templar", "Templer", "Templario", "Templare", "Templier", "Templariusz"),
	SCOUT(3),
	ASSASSIN(4, "Убийца", "Suikastçı", "Assassin", "Assassine", "Asesino", "Assassino", "Asasyn"),
	RANGER(5, "Стрелок", "Avcı", "Ranger", "Jäger", "Cazador", "Cacciatore", "Rôdeur", "Łowca"),
	MAGE(6),
	SORCERER(7, "Волшебник", "Sihirbaz", "Sorcerer", "Zauberer", "Hechicero", "Fattucchiere", "Sorcier", "Czarodziej"),
	SPIRIT_MASTER(8, "Заклинатель", "Ruh Çağırıcı", "Spiritmaster", "Beschwörer", "Invocador", "Incantatore", "Spiritualiste", "Zaklinacz"),
	PRIEST(9),
	CLERIC(10, "Целитель", "Ruhban", "Cleric", "Kleriker", "Clérigo", "Chierico", "Clerc", "Kleryk"),
	CHANTER(11, "Чародей", "Chanter", "Kantor", "Cantor", "Cantore", "Aède"),
	ENGINEER(12),
	RIDER(13, "Пилот", "Etertek", "Aethertech", "Äthertech", "Técnico del éter", "Tecnico dell'etere", "Éthertech", "EterTech"),
	GUNNER(14, "Снайпер", "Nişancı", "Gunslinger", "Gunner", "Schütze", "Tirador", "Tiratore", "Pistolero", "Strzelec"),
	ARTIST(15),
	BARD(16, "Бард", "Ozan", "Songweaver", "Bard", "Barde", "Bardo");

	private static final Map<String, PlayerClass> classIdentifiers = new HashMap<>();

	static {
		for (PlayerClass playerClass : values())
			for (String identifier : playerClass.getIdentifiers())
				classIdentifiers.put(identifier, playerClass);
	}

	private final byte classId;
	private final String[] identifiers;

	PlayerClass(int classId, String... identifiers) {
		this.classId = (byte) classId;
		this.identifiers = identifiers;
	}

	public static PlayerClass getClassByIdentifier(String classIdentifier) {
		return classIdentifiers.get(classIdentifier.split("\\[f:")[0]);
	}

	public byte getClassId() {
		return classId;
	}

	public String[] getIdentifiers() {
		return identifiers;
	}
}
