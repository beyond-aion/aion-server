package admincommands;

import static com.aionemu.gameserver.world.WorldMapType.*;
import static java.util.stream.Collectors.*;

import java.awt.*;
import java.util.*;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;

import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author Dwarfpicker, Imaginary, Neon
 */
public class GoTo extends AdminCommand {

	private final Map<String, Location> locations = new LinkedHashMap<>();

	public GoTo() {
		super("goto", "Teleports you to regions by name.");

		// @formatter:off
		setSyntaxInfo(
			" - Shows a list of locations to teleport to.",
			"<location name> - Teleports you to the given location."
		);
		// @formatter:on
		addLocations();
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0) {
			listLocations(player);
			return;
		}

		String destination = String.join(" ", params).trim().toLowerCase();
		Location location = locations.get(destination);
		if (location == null) {
			Map<String, Location> matches = findPossibleMatch(destination);
			if (isSingleSureMatch(matches, destination)) {
				location = matches.values().iterator().next();
			} else {
				StringBuilder msg = new StringBuilder("Could not find the specified destination.");
				if (!matches.isEmpty()) {
					msg.append(" Possible matches:");
					for (Map.Entry<String, Location> e : matches.entrySet())
						msg.append("\n\t- ").append(ChatUtil.color(e.getKey(), Color.WHITE)).append(" (").append(getName(e.getValue().mapType)).append(")");
				}
				sendInfo(player, msg.toString());
			}
			if (location == null)
				return;
		}

		goTo(player, location);
	}

	private boolean isSingleSureMatch(Map<String, Location> matches, String query) {
		return matches.size() == 1 && (query.length() >= 5 || matches.keySet().iterator().next().toLowerCase().startsWith(query));
	}

	private Map<String, Location> findPossibleMatch(String destination) {
		Map<String, Location> possibleMatches = new LinkedHashMap<>();
		if (destination.length() >= 2) {
			for (Location loc : locations.values()) {
				for (String identifier : loc.identifiers) {
					if (identifier.toLowerCase().contains(destination)) {
						possibleMatches.put(identifier, loc);
						break;
					}
				}
			}
		}
		return possibleMatches;
	}

	private static void goTo(Player player, Location location) {
		WorldMapInstance instance;
		if (player.getWorldId() == location.mapType.getId())
			instance = player.getPosition().getWorldMapInstance();
		else if (World.getInstance().getWorldMap(location.mapType.getId()).isInstanceType())
			instance = InstanceService.getOrRegisterInstance(location.mapType.getId(), player);
		else
			instance = World.getInstance().getWorldMap(location.mapType.getId()).getMainWorldMapInstance();
		TeleportService.teleportTo(player, instance, location.x, location.y, location.z, location.h != 0 ? location.h : player.getHeading(),
			TeleportAnimation.NONE);
	}

	private void listLocations(Player player) {
		Map<WorldMapType, Collection<Location>> locsByWorld = locations.entrySet().stream()
			.collect(groupingBy(e -> e.getValue().mapType, LinkedHashMap::new, mapping(Map.Entry::getValue, toCollection(LinkedHashSet::new))));
		StringBuilder sb = new StringBuilder();
		if (locsByWorld.size() == 1)
			sb.append("Locations for ");
		else
			sb.append("List of locations per map:\n");
		locsByWorld.forEach((worldMapType, locs) -> appendLocationsForMap(sb, worldMapType, locs));
		sendInfo(player, sb.toString());
	}

	private void appendLocationsForMap(StringBuilder sb, WorldMapType worldMapType, Collection<Location> locs) {
		sb.append(getName(worldMapType)).append(':');
		if (locs.size() > 1)
			sb.append('\n');
		for (Location loc : locs) {
			sb.append('\t');
			if (locs.size() > 1)
				sb.append("- ");
			appendLocNames(sb, loc.identifiers);
			sb.append('\n');
		}
	}

	private String getName(WorldMapType worldMapType) {
		return WordUtils.capitalizeFully(worldMapType.name().replace('_', ' '));
	}

	private void appendLocNames(StringBuilder sb, List<String> locNames) {
		for (int i = 0; i < locNames.size();) {
			sb.append(ChatUtil.color(locNames.get(i++), Color.WHITE));
			if (i != locNames.size())
				sb.append(" // ");
		}
	}

	private void addLocations() {
		/*
		 * Elysea
		 */
		addLocation(SANCTUM, 1322, 1511, 568, "Sanctum");
		addLocation(KAISINEL, 2155, 1567, 1205, "Kaisinel", "Kaisinels Kloster");
		addLocation(WISPLIGHT_ABBEY, 247, 228, 130, "Wisplight Abbey", "Zuflucht der Rückkehrer Elyos", "Zuflucht Elyos");
		addLocation(POETA, 806, 1242, 119, "Poeta");
		addLocation(POETA, 426, 1740, 119, "Melponeh", "Melponehs Lager");
		addLocation(VERTERON, 1643, 1500, 119, "Verteron");
		addLocation(VERTERON, 2384, 788, 102, "Cantas Coast", "Kantas Küste");
		addLocation(VERTERON, 2333, 1817, 193, "Ardus Shrine", "Ardus Schrein");
		addLocation(VERTERON, 2063, 2412, 274, "Pilgrims Respite", "Pilgers Rast");
		addLocation(VERTERON, 1291, 2206, 142, "Tolbas Village");
		addLocation(ELTNEN, 343, 2724, 264, "Eltnen");
		addLocation(ELTNEN, 688, 431, 332, "Golden", "Golden Bough Garrison", "Garnison der Goldbogenlegion");
		addLocation(ELTNEN, 1779, 883, 422, "Eltnen Observatory", "Observatorium von Eltnen");
		addLocation(ELTNEN, 947, 2215, 252, "Novan", "Novans Kreuzung");
		addLocation(ELTNEN, 1921, 2045, 361, "Agairon");
		addLocation(ELTNEN, 2411, 2724, 361, "Kuriullu");
		addLocation(THEOBOMOS, 1398, 1557, 31, "Theobomos");
		addLocation(THEOBOMOS, 458, 1257, 127, "Jamanok Inn", "Jamanoks Gasthaus");
		addLocation(THEOBOMOS, 1396, 1560, 31, "Meniherk");
		addLocation(THEOBOMOS, 2234, 2284, 50, "Obsvillage", "Observatoriumsdorf");
		addLocation(THEOBOMOS, 901, 2774, 62, "Josnack", "Josnacks Zelt");
		addLocation(THEOBOMOS, 2681, 847, 138, "Anangke");
		addLocation(HEIRON, 2540, 343, 411, "Heiron");
		addLocation(HEIRON, 1423, 1334, 175, "Heiron Observatory", "Heiron Observatorium");
		addLocation(HEIRON, 971, 686, 135, "Senemonea", "Seneas Lager");
		addLocation(HEIRON, 1635, 2693, 115, "Jeiaparan");
		addLocation(HEIRON, 916, 2256, 157, "Changarnerk", "Changarnerks Lager");
		addLocation(HEIRON, 1999, 1391, 118, "Kishar");
		addLocation(HEIRON, 170, 1662, 120, "Arbolu", "Arbolus Oase");

		/*
		 * Asmodae
		 */
		addLocation(PANDAEMONIUM, 1679, 1400, 195, "Pandaemonium", "Pandämonium");
		addLocation(MARCHUTAN, 1557, 1429, 266, "Marchutan", "Marchutans Konvent");
		addLocation(FATEBOUND_ABBEY, 281, 266, 97, "Fatebound Abbey", "Zuflucht der Rückkehrer Asmo", "Zuflucht Asmo");
		addLocation(ISHALGEN, 529, 2449, 281, "Ishalgen");
		addLocation(ISHALGEN, 940, 1707, 259, "Anturoon", "Anturoon Wachposten");
		addLocation(ALTGARD, 1748, 1807, 254, "Altgard");
		addLocation(ALTGARD, 1903, 696, 260, "Basfelt");
		addLocation(ALTGARD, 2680, 1024, 311, "Trader", "Handelshafen");
		addLocation(ALTGARD, 2643, 1658, 324, "Impetusiom", "Herz des Impetusiums");
		addLocation(ALTGARD, 1468, 2560, 299, "Altgard Observatory", "Observatorium von Altgard");
		addLocation(MORHEIM, 308, 2274, 449, "Morheim");
		addLocation(MORHEIM, 634, 900, 360, "Desert", "Wüstengarnison");
		addLocation(MORHEIM, 1772, 1662, 197, "Slag", "Schlackenbollwerk");
		addLocation(MORHEIM, 1070, 2486, 239, "Kellan", "Kellans Hütte");
		addLocation(MORHEIM, 2387, 1742, 102, "Alsig");
		addLocation(MORHEIM, 2794, 1122, 171, "Morheim Observatory", "Observatorium von Morheim");
		addLocation(MORHEIM, 2346, 2219, 127, "Halabana");
		addLocation(BRUSTHONIN, 2917, 2421, 15, "Brusthonin");
		addLocation(BRUSTHONIN, 1413, 2013, 51, "Baltasar");
		addLocation(BRUSTHONIN, 840, 2016, 307, "Bollu", "Iollu");
		addLocation(BRUSTHONIN, 1523, 374, 231, "Edge", "Saum der Qualen");
		addLocation(BRUSTHONIN, 526, 848, 76, "Bubu", "Bubu Dorf");
		addLocation(BRUSTHONIN, 2917, 2417, 15, "Settlers", "Siedlerlager");
		addLocation(BELUSLAN, 398, 400, 222, "Beluslan");
		addLocation(BELUSLAN, 533, 1866, 262, "Besfer");
		addLocation(BELUSLAN, 1243, 819, 260, "Kidorun", "Kidoruns Lager");
		addLocation(BELUSLAN, 2358, 1241, 470, "Red Mane", "Rotmähnenhöhle");
		addLocation(BELUSLAN, 1942, 513, 412, "Kistenian");
		addLocation(BELUSLAN, 2431, 2063, 579, "Hoarfrost", "Raureif");

		/*
		 * Balaurea
		 */
		addLocation(INGGISON, 1335, 276, 590, "Inggison");
		addLocation(INGGISON, 382, 951, 460, "Ufob", "Undirborg");
		addLocation(INGGISON, 2713, 1477, 382, "Soteria");
		addLocation(INGGISON, 1892, 1748, 327, "Hanarkand");
		addLocation(GELKMAROS, 1763, 2911, 554, "Gelkmaros");
		addLocation(GELKMAROS, 2503, 2147, 464, "Subterranea");
		addLocation(GELKMAROS, 845, 1737, 354, "Rhonnam");
		addLocation(GELKMAROS, 1295, 1558, 306, "Späherposten");
		addLocation(SILENTERA_CANYON, 583, 767, 300, "Silentera");

		/*
		 * Abyss
		 */
		addLocation(RESHANTA, 951, 936, 1667, "Reshanta");
		addLocation(RESHANTA, 2867, 1034, 1528, "Abyss 1");
		addLocation(RESHANTA, 1078, 2839, 1636, "Abyss 2");
		addLocation(RESHANTA, 1596, 2952, 2943, "Abyss 3");
		addLocation(RESHANTA, 2054, 660, 2843, "Abyss 4");
		addLocation(RESHANTA, 1979, 2114, 2291, "Eye of Reshanta");
		addLocation(RESHANTA, 2130, 1925, 2322, "Divine Fortress");

		/*
		 * Instances
		 */
		addLocation(HARAMEL, 176, 21, 144, "Haramel");
		addLocation(NOCHSANA_TRAINING_CAMP, 513, 668, 331, "Nochsana Training Camp", "NTC");
		addLocation(ARKANIS_TEMPLE, 177, 229, 536, "Sky Temple of Arkanis", "Himmelstempel von Arkanis");
		addLocation(FIRE_TEMPLE, 144, 312, 123, "Fire Temple", "FT", "Feuertempel");
		addLocation(KROMEDES_TRIAL, 248, 244, 189, "Kromedes Trial", "Kromedes Prozess");
		addLocation(STEEL_RAKE, 237, 506, 948, "Steel Rake", "SR", "Stahlharke");
		addLocation(STEEL_RAKE, 283, 453, 903, "Steel Rake Lower", "SR Low", "Stahlharke Unten");
		addLocation(STEEL_RAKE, 283, 453, 953, "Steel Rake Middle", "SR Mid", "Stahlharke Mitte");
		addLocation(INDRATU_FORTRESS, 562, 335, 1015, "Indratu Fortress", "Indratu Festung");
		addLocation(AZOTURAN_FORTRESS, 458, 428, 1039, "Azoturan Fortress", "Azoturan Festung");
		addLocation(AETHEROGENETICS_LAB, 225, 244, 133, "Aetherogenetics Lab", "Lepharisten Geheimlabor");
		addLocation(ADMA_STRONGHOLD, 450, 200, 168, "Adma", "Adma Stronghold", "Adma Festung");
		addLocation(ALQUIMIA_RESEARCH_CENTER, 603, 527, 200, "Alquimia Research Center", "Alquimia Labor");
		addLocation(DRAUPNIR_CAVE, 491, 373, 622, "Draupnir Cave", "Draupnir Höhle");
		addLocation(THEOBOMOS_LAB, 477, 201, 170, "Theobomos Lab", "Theobomos Research Lab", "TheoLab", "Theobomos Geheimlabor");
		addLocation(DARK_POETA, 1214, 412, 140, "Dark Poeta", "DP", "Poeta der Finsternis");
		addLocation(SHUGO_IMPERIAL_TOMB, 178, 234, 537, "Shugo Imperial Tomb", "Imperial", "Tomb");
		// Lower Abyss
		addLocation(SULFUR_TREE_NEST, 462, 345, 163, "Sulfur Tree Nest", "Schwefelbaum Nest");
		addLocation(RIGHT_WING_CHAMBER, 263, 386, 103, "Right Wing Chamber", "Rechter Flügel", "Kammer Im Rechten Flügel");
		addLocation(LEFT_WING_CHAMBER, 672, 606, 321, "Left Wing Chamber", "Linker Flügel", "Kammer Im Linken Flügel");
		// Upper Abyss
		addLocation(ASTERIA_CHAMBER, 469, 568, 202, "Asteria Chamber", "Abyss von Asteria");
		addLocation(MIREN_CHAMBER, 527, 120, 176, "Miren Chamber", "Miren Kammer");
		addLocation(LEGIONS_MIREN_BARRACKS, 528, 121, 176, "Miren Legion Barracks", "Miren Legionsfestung");
		addLocation(MIREN_BARRACKS, 528, 121, 176, "Miren Barracks", "Miren Kriegsfestung");
		addLocation(KYSIS_CHAMBER, 528, 121, 176, "Kysis Chamber", "Kysis Kammer");
		addLocation(LEGIONS_KYSIS_BARRACKS, 528, 121, 176, "Kysis Legion Barracks", "Kysis Legionsfestung");
		addLocation(KYSIS_BARRACKS, 528, 121, 176, "Kysis Barracks", "Kysis Kriegsfestung");
		addLocation(KROTAN_CHAMBER, 528, 109, 176, "Krotan Chamber", "Krotan Kammer");
		addLocation(LEGIONS_KROTAN_BARRACKS, 528, 121, 176, "Krotan Legion Barracks", "Krotan Legionsfestung");
		addLocation(KROTAN_BARRACKS, 528, 121, 176, "Krotan Barracks", "Krotan Kriegsfestung");
		addLocation(CHAMBER_OF_ROAH, 504, 396, 94, "Roah Chamber", "Kaverne von Roah");
		// Divine
		addLocation(ABYSSAL_SPLINTER, 704, 153, 453, "Abyssal Splinter", "AS", "Abyss Splitter");
		addLocation(UNSTABLE_SPLINTER, 704, 153, 453, "Unstable Abyssal Splinter", "UAS", "Zerbrochener Abyss Splitter");
		addLocation(DREDGION, 414, 193, 431, "Dredgion");
		addLocation(DREDGION_OF_CHANTRA, 414, 193, 431, "Chantra Dredgion");
		addLocation(TERATH_DREDGION, 414, 193, 431, "Terath Dredgion", "Sadha Dredgion");
		addLocation(TALOCS_HOLLOW, 200, 214, 1099, "Taloc's Hollow", "Talocs Höhle");
		addLocation(UDAS_TEMPLE, 637, 657, 134, "Udas", "Udas Temple", "Udas Tempel");
		addLocation(UDAS_TEMPLE_LOWER, 1146, 277, 116, "Udas Lower Temple", "Udas Tempelgruft");
		addLocation(BESHMUNDIR_TEMPLE, 1477, 237, 243, "Beshmundir Temple", "BT");
		addLocation(PADMARASHKA_CAVE, 385, 506, 66, "Padmarashka Cave");
		// 3.0 Instances
		addLocation(ATURAM_SKY_FORTRESS, 691.459f, 456.8719f, 655.7797f, "ASF", "Aturam Sky Fortress", "Aturam Himmelsfestung");
		// 4.0 Instances
		addLocation(SAURO_SUPPLY_BASE, 640.7884f, 174.29156f, 195.625f, "Sauro Supply Base");
		addLocation(ETERNAL_BASTION, 745.86206f, 291.18323f, 233.7940f, "Eternal Bastion", "EB", "Stahlmauerbastion");
		addLocation(SEALED_DANUAR_MYSTICARIUM, 184.35f, 121.3f, 231.3f, "Danuar Mysticarium", "DM", "Versiegelte Halle des Wissens", "VHDW");
		addLocation(OPHIDAN_BRIDGE, 755.41864f, 560.617f, 572.9637f, "Ophidan Bridge", "OB", "Jormungand Brücke");
		addLocation(DANUAR_RELIQUARY, 256.60f, 257.99f, 241.78f, "Danuar Reliquary", "DR", "Ruhnadium");
		addLocation(DANUAR_SANCTUARY, 388.66f, 1185.07f, 55.31f, "Danuar Sanctuary", "DS", "Zuflucht der Ruhn", "ZDR");
		addLocation(SEIZED_DANUAR_SANCTUARY, 388.66f, 1185.07f, 55.31f, "Seized Danuar Sanctuary", "SDS", "Verlorene Zukunft", "VZ");
		addLocation(THE_NIGHTMARE_CIRCUS, 467.64f, 568.34f, 201.67f, "Nightmare Circus", "NC", "Rukibuki Zirkus");
		addLocation(KAMAR_BATTLEFIELD, 1329, 1501, 593, "Kamar Battlefield", "KB", "Schlachtfeld von Kamar");
		// 4.3 Instance
		addLocation(HEXWAY, 672.8f, 606.2f, 321.2f, "The Hexway", "Korridor des Verrats", "KDV");
		// 4.5 instances
		addLocation(ENGULFED_OPHIDAN_BRIDGE, 753, 532, 577, "Engulfed Ophidian Bridge", "EOB", "Jormungand Marschroute");
		addLocation(IRON_WALL_WARFRONT, 550, 477, 213, "Iron Wall Warfront", "IWW", "Schlachtfeld der Stahlmauerbastion");
		addLocation(LUCKY_OPHIDAN_BRIDGE, 750, 554, 574, "Lucky Ophidian Bridge", "LOB", "Lucky Ophidan", "Jormungand Bonus");
		addLocation(LUCKY_DANUAR_RELIQUARY, 750, 554, 574, "Lucky Danuar Reliquary", "LDR", "Lucky Danuar");
		addLocation(ILLUMINARY_OBELISK, 322.38f, 324.47f, 405.49997f, "Illuminary Obelisk", "IO", "Schutzturm");

		/*
		 * Quest Instance Maps
		 */
		addLocation(KARAMATIS, 221, 250, 206, "Karamatis 1");
		addLocation(KARAMATIS_B, 312, 274, 206, "Karamatis 2");
		addLocation(IDAB_PRO_L3, 221, 250, 206, "Karamatis 3");
		addLocation(AERDINA, 275, 168, 205, "Aerdina");
		addLocation(GERANAIA, 275, 168, 205, "Geranaia");
		// Stigma quest
		addLocation(IDLF1B_STIGMA, 247, 249, 1392, "Sliver of Darkness", "Fragment der Finsternis");
		addLocation(SPACE_OF_DESTINY, 246, 246, 125, "Space of Destiny", "Raum des Schicksals");
		addLocation(ATAXIAR, 221, 250, 206, "Ataxiar 1");
		addLocation(ATAXIAR_B, 221, 250, 206, "Ataxiar 2");
		addLocation(BREGIRUN, 275, 168, 205, "Bregirun");
		addLocation(NIDALBER, 275, 168, 205, "Nidalber");

		/*
		 * Arenas
		 */
		addLocation(SANCTUM_UNDERGROUND_ARENA, 275, 242, 159, "Sanctum Arena", "Unterirdische Arena von Sanctum");
		addLocation(TRINIEL_UNDERGROUND_ARENA, 275, 239, 159, "Triniel Arena", "Triniels unterirdische Arena");
		// Empyrean Crucible
		addLocation(EMPYREAN_CRUCIBLE, 380, 350, 95, "Crucible 1-0");
		addLocation(EMPYREAN_CRUCIBLE, 346, 350, 96, "Crucible 1-1");
		addLocation(EMPYREAN_CRUCIBLE, 1265, 821, 359, "Crucible 5-0");
		addLocation(EMPYREAN_CRUCIBLE, 1256, 797, 359, "Crucible 5-1");
		addLocation(EMPYREAN_CRUCIBLE, 1596, 150, 129, "Crucible 6-0");
		addLocation(EMPYREAN_CRUCIBLE, 1628, 155, 126, "Crucible 6-1");
		addLocation(EMPYREAN_CRUCIBLE, 1813, 797, 470, "Crucible 7-0");
		addLocation(EMPYREAN_CRUCIBLE, 1785, 797, 470, "Crucible 7-1");
		addLocation(EMPYREAN_CRUCIBLE, 1776, 1728, 304, "Crucible 8-0");
		addLocation(EMPYREAN_CRUCIBLE, 1776, 1760, 304, "Crucible 8-1");
		addLocation(EMPYREAN_CRUCIBLE, 1357, 1748, 320, "Crucible 9-0");
		addLocation(EMPYREAN_CRUCIBLE, 1334, 1741, 316, "Crucible 9-1");
		addLocation(EMPYREAN_CRUCIBLE, 1750, 1255, 395, "Crucible 10-0");
		addLocation(EMPYREAN_CRUCIBLE, 1761, 1280, 395, "Crucible 10-1");
		// Arena Of Chaos
		addLocation(ARENA_OF_CHAOS, 1332, 1078, 340, "Arena of Chaos 1");
		addLocation(ARENA_OF_CHAOS, 599, 1854, 227, "Arena of Chaos 2");
		addLocation(ARENA_OF_CHAOS, 663, 265, 512, "Arena of Chaos 3");
		addLocation(ARENA_OF_CHAOS, 1840, 1730, 302, "Arena of Chaos 4");
		addLocation(ARENA_OF_CHAOS, 1932, 1228, 270, "Arena of Chaos 5");
		addLocation(ARENA_OF_CHAOS, 1949, 946, 224, "Arena of Chaos 6");

		/*
		 * Miscellaneous
		 */
		// Prison
		addLocation(LF_PRISON, 256, 256, 49, "Prison LF", "Prison Elyos");
		addLocation(DF_PRISON, 256, 256, 49, "Prison DF", "Prison Asmos");
		// Test
		addLocation(ID_TEST_DUNGEON, 104, 66, 25, "Test Dungeon");
		addLocation(TEST_BASIC, 144, 136, 20, "Test Basic");
		addLocation(TEST_SERVER, 228, 171, 49, "Test Server");
		addLocation(TEST_GIANTMONSTER, 196, 187, 20, "Test Giantmonster");
		// Unknown
		addLocation(NO_ZONE_NAME, 270, 200, 206, "IDAbPro");
		// GM zone
		addLocation(HOUSING_BARRACK, 2594.29f, 85.48f, 121f, (byte) 20, "GM");

		/*
		 * 2.5 Maps
		 */
		addLocation(KAISINEL_ACADEMY, 459, 251, 128, "Kaisinel Academy", "Kaisinels Akademie");
		addLocation(MARCHUTAN_PRIORY, 577, 250, 94, "Marchutan Priory", "Marchutans Konzil");
		addLocation(ESOTERRACE, 333, 437, 326, "Esoterrace", "Esoterrasse");

		/*
		 * 3.0 Maps
		 */
		addLocation(PERNON, 1069, 1539, 98, "Pernon");
		addLocation(ORIEL, 1261, 1845, 98, "Oriel", "Elian");
		addLocation(RENTUS_BASE, 557, 593, 154, "Rentus", "Rentus Base", "Rentus Basis");

		/*
		 * 3.5
		 */
		addLocation(TIAMAT_STRONGHOLD, 1581, 1068, 492, "Tiamat Stronghold", "Tiamats Festung", "TF");
		addLocation(DRAGON_LORDS_REFUGE, 506, 516, 242, "Dragon Lords Refuge", "Tiamats Unterschlupf", "TU");
		addLocation(DRAGON_LORDS_REFUGE, 495, 528, 417, "Throne of Blood", "Tiamat", "Blutthron");

		/*
		 * 4.0 Instances
		 */
		addLocation(INFINITY_SHARD, 109, 131, 125, "Infinity Shard", "Katalamize");

		/*
		 * 4.7 Instances
		 */
		addLocation(IDGEL_DOME, 254, 179, 83, "Idgel Dome", "Ruhnatorium");
		addLocation(LINKGATE_FOUNDRY, 362, 260, 312, "Linkgate Foundry", "Baruna Forschungslabor");
		addLocation(INFERNAL_ILLUMINARY_OBELISK, 322.38f, 324.47f, 405.49997f, "Infernal Illuminary Obelisk", "IIO", "Schutzturm Heroisch");
		addLocation(THE_SHUGO_EMPERORS_VAULT, 542.9366f, 299.9885f, 401f, (byte) 22, "Shugo Emperors Vault");

		// New map 4.7
		addLocation(KALDOR, 397, 1380, 163, "Kaldor");
		addLocation(LEVINSHOR, 207, 183, 374, "Levinshor", "Akaron");
		addLocation(BELUS, 1238, 1232, 1518, "Belus");
		addLocation(TRANSIDIUM_ANNEX, 509, 513, 675, "Transidium Annex", "Antriksha", "Ahserion");
		addLocation(ASPIDA, 1238, 1232, 1518, "Aspida");
		addLocation(ATANATOS, 1238, 1232, 1518, "Atanatos", "Athanos");
		addLocation(DISILLON, 1238, 1232, 1518, "Disillon", "Deylon");

		// New instances 4.8
		addLocation(OCCUPIED_RENTUS_BASE, 557, 593, 154, "Occupied Rentus Base", "Occupied Rentus", "Verlorene Rentus Basis");
		addLocation(ANGUISHED_DRAGON_LORDS_REFUGE, 506, 516, 242, "Anguished Dragon Lords Refuge", "Tiamats Hideout HM", "TU HM");
		addLocation(DRAKENSPIRE_DEPHTS, 322, 183, 1688, "Drakenspire Depths", "Makarna");
		addLocation(RAKSANG_RUINS, 830, 942, 1207, (byte) 73, "Raksang Ruins", "Mantor");
		addLocation(INFERNAL_DANUAR_RELIQUARY, 256.60f, 257.99f, 241.78f, "Infernal Danuar Reliquary", "IDR", "Ruhnadium Heroisch");
		addLocation(STONESPEAR_REACH, 231.14f, 264.399f, 98, "Stonespear Reach", "Plaza of Challenge", "Platz der Herausforderung");

		// New maps 4.8
		addLocation(CYGNEA, 2905, 803, 570, "Cygnea", "Signia");
		addLocation(CYGNEA, 1360, 613, 583, "Kenoa");
		addLocation(CYGNEA, 1186, 1610, 468, "Deluan");
		addLocation(CYGNEA, 2368, 1507, 440, "Attika");
		addLocation(CYGNEA, 2134, 2912, 329, "Außenposten der Legion des Neubeginns", "Außenposten Ldn");
		addLocation(CYGNEA, 518, 1900, 469, "Angriffsposten der Legion des Neubeginns", "Angriffsposten Ldn");
		addLocation(ENSHAR, 454, 2262, 220, "Enshar", "Vengar");
		addLocation(ENSHAR, 1768, 2555, 300, "Mura");
		addLocation(ENSHAR, 1475, 1746, 330, "Satyr");
		addLocation(ENSHAR, 759, 1274, 253, "Velias");
		addLocation(ENSHAR, 1578, 143, 188, "Oasentempel");
		addLocation(ENSHAR, 2685, 1407, 341, "Tempel des Sonnenuntergangs");
		addLocation(GRIFFOEN, 263, 127, 501, "Griffoen", "Ellegef");
		addLocation(HABROK, 253, 107, 505, "Habrok", "Lagnatun");
		addLocation(IDIAN_DEPTHS_LIGHT, 684, 654, 515, "Idian Depths Elyos", "Untergrund von Katalam Elyos", "UGE");
		addLocation(IDIAN_DEPTHS_DARK, 684, 654, 515, "Idian Depths Asmo", "Untergrund von Katalam Asmo", "UGA");
	}

	private void addLocation(WorldMapType mapType, float x, float y, float z, String... identifiers) {
		addLocation(mapType, x, y, z, (byte) 0, identifiers);
	}

	private void addLocation(WorldMapType mapType, float x, float y, float z, byte h, String... identifiers) {
		Location location = new Location(mapType, x, y, z, h, Arrays.asList(identifiers));
		for (String identifier : identifiers) {
			if (locations.put(identifier.toLowerCase(), location) != null)
				throw new IllegalArgumentException("Duplicate location identifier: " + identifier);
		}
	}

	private static class Location {

		private final WorldMapType mapType;
		private final float x, y, z;
		private final byte h;
		private final List<String> identifiers;

		public Location(WorldMapType mapType, float x, float y, float z, byte h, List<String> identifiers) {
			this.mapType = mapType;
			this.x = x;
			this.y = y;
			this.z = z;
			this.h = h;
			this.identifiers = identifiers;
		}
	}
}
