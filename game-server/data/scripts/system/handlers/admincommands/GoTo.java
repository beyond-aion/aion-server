package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMap;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * Goto command
 * 
 * @author Dwarfpicker
 * @rework Imaginary
 */
public class GoTo extends AdminCommand {

	public GoTo() {
		super("goto");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params == null || params.length < 1) {
			PacketSendUtility.sendMessage(player, "syntax //goto <location>");
			return;
		}

		StringBuilder sbDestination = new StringBuilder();
		for (String p : params)
			sbDestination.append(p + " ");

		String destination = sbDestination.toString().trim();

		/**
		 * Elysea
		 */
		// Sanctum
		if (destination.equalsIgnoreCase("Sanctum"))
			goTo(player, WorldMapType.SANCTUM.getId(), 1322, 1511, 568);
		// Kaisinel
		else if (destination.equalsIgnoreCase("Kaisinel") || destination.equalsIgnoreCase("kaisinels kloster"))
			goTo(player, WorldMapType.KAISINEL.getId(), 2155, 1567, 1205);
		// Wisplight Abbey
		else if (destination.equalsIgnoreCase("Wisplight") || destination.equalsIgnoreCase("Wisplight Abbey")
						|| destination.equalsIgnoreCase("zuflucht ely") || destination.equalsIgnoreCase("zuflucht der rueckkehrer ely"))
			goTo(player, WorldMapType.WISPLIGHT_ABBEY.getId(), 247, 228, 130);
		// Poeta
		else if (destination.equalsIgnoreCase("Poeta"))
			goTo(player, WorldMapType.POETA.getId(), 806, 1242, 119);
		else if (destination.equalsIgnoreCase("Melponeh") || destination.equalsIgnoreCase("melponehs lager"))
			goTo(player, WorldMapType.POETA.getId(), 426, 1740, 119);
		// Verteron
		else if (destination.equalsIgnoreCase("Verteron"))
			goTo(player, WorldMapType.VERTERON.getId(), 1643, 1500, 119);
		else if (destination.equalsIgnoreCase("Cantas") || destination.equalsIgnoreCase("Cantas Coast")
						|| destination.equalsIgnoreCase("kantas") || destination.equalsIgnoreCase("kantas kueste"))
			goTo(player, WorldMapType.VERTERON.getId(), 2384, 788, 102);
		else if (destination.equalsIgnoreCase("Ardus") || destination.equalsIgnoreCase("Ardus Shrine")
						|| destination.equalsIgnoreCase("ardus schrein"))
			goTo(player, WorldMapType.VERTERON.getId(), 2333, 1817, 193);
		else if (destination.equalsIgnoreCase("Pilgrims") || destination.equalsIgnoreCase("Pilgrims Respite")
						|| destination.equalsIgnoreCase("pilgers") || destination.equalsIgnoreCase("pilgers rast"))
			goTo(player, WorldMapType.VERTERON.getId(), 2063, 2412, 274);
		else if (destination.equalsIgnoreCase("Tolbas") || destination.equalsIgnoreCase("Tolbas Village"))
			goTo(player, WorldMapType.VERTERON.getId(), 1291, 2206, 142);
		// Eltnen
		else if (destination.equalsIgnoreCase("Eltnen"))
			goTo(player, WorldMapType.ELTNEN.getId(), 343, 2724, 264);
		else if (destination.equalsIgnoreCase("Golden") || destination.equalsIgnoreCase("Golden Bough Garrison"))
			goTo(player, WorldMapType.ELTNEN.getId(), 688, 431, 332);
		else if (destination.equalsIgnoreCase("Eltnen Observatory") || destination.equalsIgnoreCase("Observatorium von Eltnen")
						|| destination.equalsIgnoreCase("garnison der goldbogenlegion"))
			goTo(player, WorldMapType.ELTNEN.getId(), 1779, 883, 422);
		else if (destination.equalsIgnoreCase("Novan") || destination.equalsIgnoreCase("novans kreuzung"))
			goTo(player, WorldMapType.ELTNEN.getId(), 947, 2215, 252);
		else if (destination.equalsIgnoreCase("Agairon"))
			goTo(player, WorldMapType.ELTNEN.getId(), 1921, 2045, 361);
		else if (destination.equalsIgnoreCase("Kuriullu"))
			goTo(player, WorldMapType.ELTNEN.getId(), 2411, 2724, 361);
		// Theobomos
		else if (destination.equalsIgnoreCase("Theobomos"))
			goTo(player, WorldMapType.THEOBOMOS.getId(), 1398, 1557, 31);
		else if (destination.equalsIgnoreCase("Jamanok") || destination.equalsIgnoreCase("Jamanok Inn")
						|| destination.equalsIgnoreCase("jamanoks gasthaus"))
			goTo(player, WorldMapType.THEOBOMOS.getId(), 458, 1257, 127);
		else if (destination.equalsIgnoreCase("Meniherk"))
			goTo(player, WorldMapType.THEOBOMOS.getId(), 1396, 1560, 31);
		else if (destination.equalsIgnoreCase("obsvillage") || destination.equalsIgnoreCase("observatoriumsdorf"))
			goTo(player, WorldMapType.THEOBOMOS.getId(), 2234, 2284, 50);
		else if (destination.equalsIgnoreCase("Josnack") || destination.equalsIgnoreCase("josnacks zelt"))
			goTo(player, WorldMapType.THEOBOMOS.getId(), 901, 2774, 62);
		else if (destination.equalsIgnoreCase("Anangke"))
			goTo(player, WorldMapType.THEOBOMOS.getId(), 2681, 847, 138);
		// Heiron
		else if (destination.equalsIgnoreCase("Heiron") || destination.equalsIgnoreCase("Heiron Observatorium"))
			goTo(player, WorldMapType.HEIRON.getId(), 2540, 343, 411);
		else if (destination.equalsIgnoreCase("Heiron Observatory"))
			goTo(player, WorldMapType.HEIRON.getId(), 1423, 1334, 175);
		else if (destination.equalsIgnoreCase("Senemonea") || destination.equalsIgnoreCase("seneas lager"))
			goTo(player, WorldMapType.HEIRON.getId(), 971, 686, 135);
		else if (destination.equalsIgnoreCase("Jeiaparan"))
			goTo(player, WorldMapType.HEIRON.getId(), 1635, 2693, 115);
		else if (destination.equalsIgnoreCase("Changarnerk") || destination.equalsIgnoreCase("changarnerks lager"))
			goTo(player, WorldMapType.HEIRON.getId(), 916, 2256, 157);
		else if (destination.equalsIgnoreCase("Kishar"))
			goTo(player, WorldMapType.HEIRON.getId(), 1999, 1391, 118);
		else if (destination.equalsIgnoreCase("Arbolu") || destination.equalsIgnoreCase("Arbolus Oase"))
			goTo(player, WorldMapType.HEIRON.getId(), 170, 1662, 120);

		/**
		 * Asmodae
		 */
		// Pandaemonium
		else if (destination.equalsIgnoreCase("Pandaemonium") || destination.equalsIgnoreCase("pandae")
						|| destination.equalsIgnoreCase("pandaemonium"))
			goTo(player, WorldMapType.PANDAEMONIUM.getId(), 1679, 1400, 195);
		// Marchutran
		else if (destination.equalsIgnoreCase("Marchutan") || destination.equalsIgnoreCase("marchutans konvent"))
			goTo(player, WorldMapType.MARCHUTAN.getId(), 1557, 1429, 266);
		else if (destination.equalsIgnoreCase("Fatebound") || destination.equalsIgnoreCase("Fatebound Abbey")
					|| destination.equalsIgnoreCase("zuflucht der rueckkehrer asmo") || destination.equalsIgnoreCase("zuflucht asmo"))
			goTo(player, WorldMapType.FATEBOUND_ABBEY.getId(), 281, 266, 97);
		// Ishalgen
		else if (destination.equalsIgnoreCase("Ishalgen"))
			goTo(player, WorldMapType.ISHALGEN.getId(), 529, 2449, 281);
		else if (destination.equalsIgnoreCase("Anturoon") || destination.equalsIgnoreCase("anturoon wachposten"))
			goTo(player, WorldMapType.ISHALGEN.getId(), 940, 1707, 259);
		// Altgard
		else if (destination.equalsIgnoreCase("Altgard"))
			goTo(player, WorldMapType.ALTGARD.getId(), 1748, 1807, 254);
		else if (destination.equalsIgnoreCase("Basfelt"))
			goTo(player, WorldMapType.ALTGARD.getId(), 1903, 696, 260);
		else if (destination.equalsIgnoreCase("Trader") || destination.equalsIgnoreCase("handelshafen"))
			goTo(player, WorldMapType.ALTGARD.getId(), 2680, 1024, 311);
		else if (destination.equalsIgnoreCase("Impetusiom") || destination.equalsIgnoreCase("herz des impetusiums")
						|| destination.equalsIgnoreCase("impetusium"))
			goTo(player, WorldMapType.ALTGARD.getId(), 2643, 1658, 324);
		else if (destination.equalsIgnoreCase("Altgard Observatory") || destination.equalsIgnoreCase("Observatorium von Altgard"))
			goTo(player, WorldMapType.ALTGARD.getId(), 1468, 2560, 299);
		// Morheim
		else if (destination.equalsIgnoreCase("Morheim"))
			goTo(player, WorldMapType.MORHEIM.getId(), 308, 2274, 449);
		else if (destination.equalsIgnoreCase("Desert") || destination.equalsIgnoreCase("wuestengarnison"))
			goTo(player, WorldMapType.MORHEIM.getId(), 634, 900, 360);
		else if (destination.equalsIgnoreCase("Slag") || destination.equalsIgnoreCase("schlackenbollwerk"))
			goTo(player, WorldMapType.MORHEIM.getId(), 1772, 1662, 197);
		else if (destination.equalsIgnoreCase("Kellan") || destination.equalsIgnoreCase("kellans huette"))
			goTo(player, WorldMapType.MORHEIM.getId(), 1070, 2486, 239);
		else if (destination.equalsIgnoreCase("Alsig"))
			goTo(player, WorldMapType.MORHEIM.getId(), 2387, 1742, 102);
		else if (destination.equalsIgnoreCase("Morheim Observatory") || destination.equalsIgnoreCase("Observatorium von Morheim"))
			goTo(player, WorldMapType.MORHEIM.getId(), 2794, 1122, 171);
		else if (destination.equalsIgnoreCase("Halabana"))
			goTo(player, WorldMapType.MORHEIM.getId(), 2346, 2219, 127);
		// Brusthonin
		else if (destination.equalsIgnoreCase("Brusthonin"))
			goTo(player, WorldMapType.BRUSTHONIN.getId(), 2917, 2421, 15);
		else if (destination.equalsIgnoreCase("Baltasar"))
			goTo(player, WorldMapType.BRUSTHONIN.getId(), 1413, 2013, 51);
		else if (destination.equalsIgnoreCase("Bollu") || destination.equalsIgnoreCase("iollu"))
			goTo(player, WorldMapType.BRUSTHONIN.getId(), 840, 2016, 307);
		else if (destination.equalsIgnoreCase("Edge") || destination.equalsIgnoreCase("saum der qualen"))
			goTo(player, WorldMapType.BRUSTHONIN.getId(), 1523, 374, 231);
		else if (destination.equalsIgnoreCase("Bubu") || destination.equalsIgnoreCase("bubu dorf"))
			goTo(player, WorldMapType.BRUSTHONIN.getId(), 526, 848, 76);
		else if (destination.equalsIgnoreCase("Settlers") || destination.equalsIgnoreCase("siedlerlager"))
			goTo(player, WorldMapType.BRUSTHONIN.getId(), 2917, 2417, 15);
		// Beluslan
		else if (destination.equalsIgnoreCase("Beluslan"))
			goTo(player, WorldMapType.BELUSLAN.getId(), 398, 400, 222);
		else if (destination.equalsIgnoreCase("Besfer"))
			goTo(player, WorldMapType.BELUSLAN.getId(), 533, 1866, 262);
		else if (destination.equalsIgnoreCase("Kidorun") || destination.equalsIgnoreCase("kidoruns lager"))
			goTo(player, WorldMapType.BELUSLAN.getId(), 1243, 819, 260);
		else if (destination.equalsIgnoreCase("Red Mane") || destination.equalsIgnoreCase("rotmaehnenhoehle"))
			goTo(player, WorldMapType.BELUSLAN.getId(), 2358, 1241, 470);
		else if (destination.equalsIgnoreCase("Kistenian"))
			goTo(player, WorldMapType.BELUSLAN.getId(), 1942, 513, 412);
		else if (destination.equalsIgnoreCase("Hoarfrost") || destination.equalsIgnoreCase("raureif"))
			goTo(player, WorldMapType.BELUSLAN.getId(), 2431, 2063, 579);

		/**
		 * Balaurea
		 */
		// Inggison
		else if (destination.equalsIgnoreCase("Inggison"))
			goTo(player, WorldMapType.INGGISON.getId(), 1335, 276, 590);
		else if (destination.equalsIgnoreCase("Ufob") || destination.equalsIgnoreCase("undirborg"))
			goTo(player, WorldMapType.INGGISON.getId(), 382, 951, 460);
		else if (destination.equalsIgnoreCase("Soteria"))
			goTo(player, WorldMapType.INGGISON.getId(), 2713, 1477, 382);
		else if (destination.equalsIgnoreCase("Hanarkand"))
			goTo(player, WorldMapType.INGGISON.getId(), 1892, 1748, 327);
		// Gelkmaros
		else if (destination.equalsIgnoreCase("Gelkmaros"))
			goTo(player, WorldMapType.GELKMAROS.getId(), 1763, 2911, 554);
		else if (destination.equalsIgnoreCase("Subterranea"))
			goTo(player, WorldMapType.GELKMAROS.getId(), 2503, 2147, 464);
		else if (destination.equalsIgnoreCase("Rhonnam"))
			goTo(player, WorldMapType.GELKMAROS.getId(), 845, 1737, 354);
		else if (destination.equalsIgnoreCase("spaeherposten"))
			goTo(player, WorldMapType.GELKMAROS.getId(), 1295, 1558, 306);
		// Silentera
		else if (destination.equalsIgnoreCase("Silentera"))
			goTo(player, 600010000, 583, 767, 300);

		/**
		 * Abyss
		 */
		else if (destination.equalsIgnoreCase("Reshanta"))
			goTo(player, WorldMapType.RESHANTA.getId(), 951, 936, 1667);
		else if (destination.equalsIgnoreCase("Abyss 1"))
			goTo(player, WorldMapType.RESHANTA.getId(), 2867, 1034, 1528);
		else if (destination.equalsIgnoreCase("Abyss 2"))
			goTo(player, WorldMapType.RESHANTA.getId(), 1078, 2839, 1636);
		else if (destination.equalsIgnoreCase("Abyss 3"))
			goTo(player, WorldMapType.RESHANTA.getId(), 1596, 2952, 2943);
		else if (destination.equalsIgnoreCase("Abyss 4"))
			goTo(player, WorldMapType.RESHANTA.getId(), 2054, 660, 2843);
		else if (destination.equalsIgnoreCase("Eye of Reshanta") || destination.equalsIgnoreCase("Eye"))
			goTo(player, WorldMapType.RESHANTA.getId(), 1979, 2114, 2291);
		else if (destination.equalsIgnoreCase("Divine Fortress") || destination.equalsIgnoreCase("Divine"))
			goTo(player, WorldMapType.RESHANTA.getId(), 2130, 1925, 2322);

		/**
		 * Instances
		 */
		else if (destination.equalsIgnoreCase("Haramel"))
			goTo(player, 300200000, 176, 21, 144);
		else if (destination.equalsIgnoreCase("Nochsana") || destination.equalsIgnoreCase("NTC"))
			goTo(player, 300030000, 513, 668, 331);
		else if (destination.equalsIgnoreCase("Arcanis") || destination.equalsIgnoreCase("Sky Temple of Arcanis")
						|| destination.equalsIgnoreCase("arkanis") || destination.equalsIgnoreCase("himmelstempel von arkanis"))
			goTo(player, 320050000, 177, 229, 536);
		else if (destination.equalsIgnoreCase("Fire Temple") || destination.equalsIgnoreCase("FT")
						|| destination.equalsIgnoreCase("feuertempel"))
			goTo(player, 320100000, 144, 312, 123);
		else if (destination.equalsIgnoreCase("Kromede") || destination.equalsIgnoreCase("Kromede Trial")
						|| destination.equalsIgnoreCase("kromedes prozess"))
			goTo(player, 300230000, 248, 244, 189);
		// Steel Rake
		else if (destination.equalsIgnoreCase("Steel Rake") || destination.equalsIgnoreCase("SR")
						|| destination.equalsIgnoreCase("stahlharke"))
			goTo(player, 300100000, 237, 506, 948);
		else if (destination.equalsIgnoreCase("Steel Rake Lower") || destination.equalsIgnoreCase("SR Low")
						|| destination.equalsIgnoreCase("stahlharke unten"))
			goTo(player, 300100000, 283, 453, 903);
		else if (destination.equalsIgnoreCase("Steel Rake Middle") || destination.equalsIgnoreCase("SR Mid")
						|| destination.equalsIgnoreCase("stahlharke mitte"))
			goTo(player, 300100000, 283, 453, 953);
		else if (destination.equalsIgnoreCase("Indratu") || destination.equalsIgnoreCase("Indratu Fortress")
						|| destination.equalsIgnoreCase("indratu festung"))
			goTo(player, 310090000, 562, 335, 1015);
		else if (destination.equalsIgnoreCase("Azoturan") || destination.equalsIgnoreCase("Azoturan Fortress")
						|| destination.equalsIgnoreCase("azoturan festung"))
			goTo(player, 310100000, 458, 428, 1039);
		else if (destination.equalsIgnoreCase("Bio Lab") || destination.equalsIgnoreCase("Aetherogenetics Lab")
						|| destination.equalsIgnoreCase("lepharisten geheimlabor"))
			goTo(player, 310050000, 225, 244, 133);
		else if (destination.equalsIgnoreCase("Adma") || destination.equalsIgnoreCase("Adma Stronghold")
						|| destination.equalsIgnoreCase("adma festung"))
			goTo(player, 320130000, 450, 200, 168);
		else if (destination.equalsIgnoreCase("Alquimia") || destination.equalsIgnoreCase("Alquimia Research Center")
						|| destination.equalsIgnoreCase("alquimia labor"))
			goTo(player, 320110000, 603, 527, 200);
		else if (destination.equalsIgnoreCase("Draupnir") || destination.equalsIgnoreCase("Draupnir Cave")
						|| destination.equalsIgnoreCase("Draupnir Hoehle"))
			goTo(player, 320080000, 491, 373, 622);
		else if (destination.equalsIgnoreCase("Theobomos Lab") || destination.equalsIgnoreCase("Theobomos Research Lab")
						|| destination.equalsIgnoreCase("Theobomos Geheimlabor"))
			goTo(player, 310110000, 477, 201, 170);
		else if (destination.equalsIgnoreCase("Dark Poeta") || destination.equalsIgnoreCase("DP")
						|| destination.equalsIgnoreCase("Poeta der Finsternis"))
			goTo(player, 300040000, 1214, 412, 140);
		// Lower Abyss
		else if (destination.equalsIgnoreCase("Sulfur") || destination.equalsIgnoreCase("Sulfur Tree Nest")
						|| destination.equalsIgnoreCase("Schwefelbaum") || destination.equalsIgnoreCase("Schwefelbaum Nest"))
			goTo(player, 300060000, 462, 345, 163);
		else if (destination.equalsIgnoreCase("Right Wing") || destination.equalsIgnoreCase("Right Wing Chamber")
						|| destination.equalsIgnoreCase("Rechter Fluegel") || destination.equalsIgnoreCase("Kammer im rechten Fluegel"))
			goTo(player, 300090000, 263, 386, 103);
		else if (destination.equalsIgnoreCase("Left Wing") || destination.equalsIgnoreCase("Left Wing Chamber")
						|| destination.equalsIgnoreCase("Linker Fluegel") || destination.equalsIgnoreCase("Kammer im linken Fluegel"))
			goTo(player, 300080000, 672, 606, 321);
		// Upper Abyss
		else if (destination.equalsIgnoreCase("Asteria Chamber") || destination.equalsIgnoreCase("Abyss von Asteria"))
			goTo(player, 300050000, 469, 568, 202);
		else if (destination.equalsIgnoreCase("Miren Chamber") || destination.equalsIgnoreCase("Miren Kammer"))
			goTo(player, 300130000, 527, 120, 176);
		else if (destination.equalsIgnoreCase("Miren Legion Barracks") || destination.equalsIgnoreCase("Miren Legionsfestung"))
			goTo(player, 301250000, 528, 121, 176);
		else if (destination.equalsIgnoreCase("Miren Barracks") || destination.equalsIgnoreCase("Miren kriegsfestung"))
			goTo(player, 301290000, 528, 121, 176);
		else if (destination.equalsIgnoreCase("Kysis Chamber") || destination.equalsIgnoreCase("Kysis Kammer"))
			goTo(player, 300120000, 528, 121, 176);
		else if (destination.equalsIgnoreCase("Kysis Legion Barracks") || destination.equalsIgnoreCase("Kysis Legionsfestung"))
			goTo(player, 301240000, 528, 121, 176);
		else if (destination.equalsIgnoreCase("Kysis Barracks") || destination.equalsIgnoreCase("Kysis kriegsfestung"))
			goTo(player, 301280000, 528, 121, 176);
		else if (destination.equalsIgnoreCase("Krotan Chamber") || destination.equalsIgnoreCase("Krotan kammer"))
			goTo(player, 300140000, 528, 109, 176);
		else if (destination.equalsIgnoreCase("Krotan Legion Barracks") || destination.equalsIgnoreCase("Krotan Legionsfestung"))
			goTo(player, 301260000, 528, 121, 176);
		else if (destination.equalsIgnoreCase("Krotan Barracks") || destination.equalsIgnoreCase("Krotan kriegsfestung"))
			goTo(player, 301300000, 528, 121, 176);
		else if (destination.equalsIgnoreCase("Roah Chamber") || destination.equalsIgnoreCase("Kaverne von Roah"))
			goTo(player, 300070000, 504, 396, 94);
		// Divine
		else if (destination.equalsIgnoreCase("Abyssal Splinter") || destination.equalsIgnoreCase("AS")
						|| destination.equalsIgnoreCase("Abyss Splitter"))
			goTo(player, 300220000, 704, 153, 453);
		else if (destination.equalsIgnoreCase("Unstable Abyssal Splinter") || destination.equalsIgnoreCase("UAS")
						|| destination.equalsIgnoreCase("Zerbrochener Abyss Splitter"))
			goTo(player, 300600000, 704, 153, 453);
		else if (destination.equalsIgnoreCase("Dredgion"))
			goTo(player, 300110000, 414, 193, 431);
		else if (destination.equalsIgnoreCase("Chantra") || destination.equalsIgnoreCase("Chantra Dredgion"))
			goTo(player, 300210000, 414, 193, 431);
		else if (destination.equalsIgnoreCase("Terath") || destination.equalsIgnoreCase("Terath Dredgion")
						|| destination.equalsIgnoreCase("Sadha") || destination.equalsIgnoreCase("Sadha Dredgion"))
			goTo(player, 300440000, 414, 193, 431);
		else if (destination.equalsIgnoreCase("Taloc") || destination.equalsIgnoreCase("Taloc's Hollow")
						|| destination.equalsIgnoreCase("Talocs Hoehle"))
			goTo(player, 300190000, 200, 214, 1099);
		// Udas
		else if (destination.equalsIgnoreCase("Udas") || destination.equalsIgnoreCase("Udas Temple")
						|| destination.equalsIgnoreCase("udas tempel"))
			goTo(player, 300150000, 637, 657, 134);
		else if (destination.equalsIgnoreCase("Udas Lower") || destination.equalsIgnoreCase("Udas Lower Temple")
						|| destination.equalsIgnoreCase("Udas Tempelgruft"))
			goTo(player, 300160000, 1146, 277, 116);
		else if (destination.equalsIgnoreCase("Beshmundir") || destination.equalsIgnoreCase("BT") || destination.equalsIgnoreCase("Beshmundir Temple"))
			goTo(player, 300170000, 1477, 237, 243);
		// Padmaraska Cave
		else if (destination.equalsIgnoreCase("Padmarashka Cave") || destination.equalsIgnoreCase("padmarashka"))
			goTo(player, 320150000, 385, 506, 66);
		// 4.0 Instances
		else if (destination.equalsIgnoreCase("Sauro") || destination.equalsIgnoreCase("Sauro Supply Base"))
			goTo(player, 301130000, 640.7884f, 174.29156f, 195.625f);
		else if (destination.equalsIgnoreCase("Eternal Bastion") || destination.equalsIgnoreCase("EB")
						|| destination.equalsIgnoreCase("stahlmauerbastion"))
			goTo(player, 300540000, 745.86206f, 291.18323f, 233.7940f);
		else if (destination.equalsIgnoreCase("Danuar Mysticarium") || destination.equalsIgnoreCase("DM")
						|| destination.equalsIgnoreCase("Versiegelte Halle des Wissens") || destination.equalsIgnoreCase("VHDW"))
			goTo(player, 300480000, 184.35f, 121.3f, 231.3f);
		else if (destination.equalsIgnoreCase("Ophidan Bridge") || destination.equalsIgnoreCase("OB")
						|| destination.equalsIgnoreCase("jormungand") || destination.equalsIgnoreCase("jormungand bruecke"))
			goTo(player, 300590000, 755.41864f, 560.617f, 572.9637f);
		else if (destination.equalsIgnoreCase("Danuar Reliquary") || destination.equalsIgnoreCase("DR")
						|| destination.equalsIgnoreCase("Ruhnadium"))
			goTo(player, 301110000, 256.60f, 257.99f, 241.78f);
		else if (destination.equalsIgnoreCase("Danuar Sanctuary") || destination.equalsIgnoreCase("DS")
						|| destination.equalsIgnoreCase("Zuflucht der Ruhn") || destination.equalsIgnoreCase("ZDR"))
			goTo(player, 301380000, 388.66f, 1185.07f, 55.31f);
		else if (destination.equalsIgnoreCase("Seized Danuar Sanctuary") || destination.equalsIgnoreCase("SDS")
						|| destination.equalsIgnoreCase("Verlorene Zukunft") || destination.equalsIgnoreCase("VZ"))
			goTo(player, 301140000, 388.66f, 1185.07f, 55.31f);
		else if (destination.equalsIgnoreCase("Nightmare Circus") || destination.equalsIgnoreCase("NC")
						|| destination.equalsIgnoreCase("Rukibuki Zirkus"))
			goTo(player, 301160000, 467.64f, 568.34f, 201.67f);
		else if (destination.equalsIgnoreCase("Kamar Battlefield") || destination.equalsIgnoreCase("KB")
						|| destination.equalsIgnoreCase("Schlachtfeld von Kamar"))
			goTo(player, 301120000, 1329, 1501, 593);
		// 4.5 instances
		else if (destination.equalsIgnoreCase("Engulfed Ophidian Bridge") || destination.equalsIgnoreCase("EOB")
						|| destination.equalsIgnoreCase("jormungand marschroute"))
			goTo(player, 301210000, 753, 532, 577);
		else if (destination.equalsIgnoreCase("Iron Wall Warfront") || destination.equalsIgnoreCase("IWW") || destination.equalsIgnoreCase("Iron Wall")
						|| destination.equalsIgnoreCase("Schlachtfeld der Stahlmauerbastion"))
			goTo(player, 301220000, 550, 477, 213);
		else if (destination.equalsIgnoreCase("Lucky Ophidian Bridge") || destination.equalsIgnoreCase("LOB")
						|| destination.equalsIgnoreCase("Lucky Ophidian"))
			goTo(player, 301320000, 750, 554, 574);
		else if (destination.equalsIgnoreCase("Lucky Danuar Reliquary") || destination.equalsIgnoreCase("LDR")
						|| destination.equalsIgnoreCase("Lucky Danuar") || destination.equalsIgnoreCase("jormungand bonus"))
			goTo(player, 301320000, 750, 554, 574);
		else if (destination.equalsIgnoreCase("Illuminary Obelisk") || destination.equalsIgnoreCase("IB")
						|| destination.equalsIgnoreCase("Schutzturm"))
			goTo(player, 301230000, 322.38f, 324.47f, 405.49997f);

		/**
		 * Quest Instance Maps
		 */
		// TODO : Changer id maps
		else if (destination.equalsIgnoreCase("Karamatis 0"))
			goTo(player, 310010000, 221, 250, 206);
		else if (destination.equalsIgnoreCase("Karamatis 1"))
			goTo(player, 310020000, 312, 274, 206);
		else if (destination.equalsIgnoreCase("Karamatis 2"))
			goTo(player, 310120000, 221, 250, 206);
		else if (destination.equalsIgnoreCase("Aerdina"))
			goTo(player, 310030000, 275, 168, 205);
		else if (destination.equalsIgnoreCase("Geranaia"))
			goTo(player, 310040000, 275, 168, 205);
		// Stigma quest
		else if (destination.equalsIgnoreCase("Sliver") || destination.equalsIgnoreCase("Sliver of Darkness")
						|| destination.equalsIgnoreCase("fragment der finsternis"))
			goTo(player, 310070000, 247, 249, 1392);
		else if (destination.equalsIgnoreCase("Space") || destination.equalsIgnoreCase("Space of Destiny")
						|| destination.equalsIgnoreCase("raum des schicksals"))
			goTo(player, 320070000, 246, 246, 125);
		else if (destination.equalsIgnoreCase("Ataxiar 1"))
			goTo(player, 320010000, 221, 250, 206);
		else if (destination.equalsIgnoreCase("Ataxiar 2"))
			goTo(player, 320020000, 221, 250, 206);
		else if (destination.equalsIgnoreCase("Bregirun"))
			goTo(player, 320030000, 275, 168, 205);
		else if (destination.equalsIgnoreCase("Nidalber"))
			goTo(player, 320040000, 275, 168, 205);

		/**
		 * Arenas
		 */
		else if (destination.equalsIgnoreCase("Sanctum Arena") || destination.equalsIgnoreCase("Unterirdische Arena von Sanctum"))
			goTo(player, 310080000, 275, 242, 159);
		else if (destination.equalsIgnoreCase("Triniel Arena") || destination.equalsIgnoreCase("triniels unterirdische arena"))
			goTo(player, 320090000, 275, 239, 159);
		// Empyrean Crucible
		else if (destination.equalsIgnoreCase("Crucible 1-0"))
			goTo(player, 300300000, 380, 350, 95);
		else if (destination.equalsIgnoreCase("Crucible 1-1"))
			goTo(player, 300300000, 346, 350, 96);
		else if (destination.equalsIgnoreCase("Crucible 5-0"))
			goTo(player, 300300000, 1265, 821, 359);
		else if (destination.equalsIgnoreCase("Crucible 5-1"))
			goTo(player, 300300000, 1256, 797, 359);
		else if (destination.equalsIgnoreCase("Crucible 6-0"))
			goTo(player, 300300000, 1596, 150, 129);
		else if (destination.equalsIgnoreCase("Crucible 6-1"))
			goTo(player, 300300000, 1628, 155, 126);
		else if (destination.equalsIgnoreCase("Crucible 7-0"))
			goTo(player, 300300000, 1813, 797, 470);
		else if (destination.equalsIgnoreCase("Crucible 7-1"))
			goTo(player, 300300000, 1785, 797, 470);
		else if (destination.equalsIgnoreCase("Crucible 8-0"))
			goTo(player, 300300000, 1776, 1728, 304);
		else if (destination.equalsIgnoreCase("Crucible 8-1"))
			goTo(player, 300300000, 1776, 1760, 304);
		else if (destination.equalsIgnoreCase("Crucible 9-0"))
			goTo(player, 300300000, 1357, 1748, 320);
		else if (destination.equalsIgnoreCase("Crucible 9-1"))
			goTo(player, 300300000, 1334, 1741, 316);
		else if (destination.equalsIgnoreCase("Crucible 10-0"))
			goTo(player, 300300000, 1750, 1255, 395);
		else if (destination.equalsIgnoreCase("Crucible 10-1"))
			goTo(player, 300300000, 1761, 1280, 395);
		// Arena Of Chaos
		else if (destination.equalsIgnoreCase("Arena Of Chaos - 1"))
			goTo(player, 300350000, 1332, 1078, 340);
		else if (destination.equalsIgnoreCase("Arena Of Chaos - 2"))
			goTo(player, 300350000, 599, 1854, 227);
		else if (destination.equalsIgnoreCase("Arena Of Chaos - 3"))
			goTo(player, 300350000, 663, 265, 512);
		else if (destination.equalsIgnoreCase("Arena Of Chaos - 4"))
			goTo(player, 300350000, 1840, 1730, 302);
		else if (destination.equalsIgnoreCase("Arena Of Chaos - 5"))
			goTo(player, 300350000, 1932, 1228, 270);
		else if (destination.equalsIgnoreCase("Arena Of Chaos - 6"))
			goTo(player, 300350000, 1949, 946, 224);

		/**
		 * Miscellaneous
		 */
		// Prison
		else if (destination.equalsIgnoreCase("Prison LF") || destination.equalsIgnoreCase("Prison Elyos"))
			goTo(player, 510010000, 256, 256, 49);
		else if (destination.equalsIgnoreCase("Prison DF") || destination.equalsIgnoreCase("Prison Asmos"))
			goTo(player, 520010000, 256, 256, 49);
		// Test
		else if (destination.equalsIgnoreCase("Test Dungeon"))
			goTo(player, 300020000, 104, 66, 25);
		else if (destination.equalsIgnoreCase("Test Basic"))
			goTo(player, 900020000, 144, 136, 20);
		else if (destination.equalsIgnoreCase("Test Server"))
			goTo(player, 900030000, 228, 171, 49);
		else if (destination.equalsIgnoreCase("Test GiantMonster"))
			goTo(player, 900100000, 196, 187, 20);
		// Unknown
		else if (destination.equalsIgnoreCase("IDAbPro"))
			goTo(player, 300010000, 270, 200, 206);
		// GM zone
		else if (destination.equalsIgnoreCase("gm"))
			TeleportService2.teleportTo(player, 900110000, 2594.29f, 85.48f, 121f, (byte) 20);

		/**
		 * 2.5 Maps
		 */
		else if (destination.equalsIgnoreCase("Kaisinel Academy") || destination.equalsIgnoreCase("Kaisinels Akademie"))
			goTo(player, 110070000, 459, 251, 128);
		else if (destination.equalsIgnoreCase("Marchutan Priory") || destination.equalsIgnoreCase("Marchutans Konzil"))
			goTo(player, 120080000, 577, 250, 94);
		else if (destination.equalsIgnoreCase("Esoterrace") || destination.equalsIgnoreCase("Esoterrasse"))
			goTo(player, 300250000, 333, 437, 326);

		/**
		 * 3.0 Maps
		 */
		else if (destination.equalsIgnoreCase("Pernon"))
			goTo(player, 710010000, 1069, 1539, 98);
		else if (destination.equalsIgnoreCase("Oriel") || destination.equalsIgnoreCase("elian"))
			goTo(player, 700010000, 1261, 1845, 98);
		else if (destination.equalsIgnoreCase("Rentus") || destination.equalsIgnoreCase("Rentus Base")
						|| destination.equalsIgnoreCase("Rentus Basis"))
			goTo(player, 300280000, 557, 593, 154);

		/**
		 * 3.5
		 */
		else if (destination.equalsIgnoreCase("Dragon Lords Refuge") || destination.equalsIgnoreCase("Tiamats Unterschlupf"))
			goTo(player, 300520000, 506, 516, 242);

		else if (destination.equalsIgnoreCase("Throne of Blood") || destination.equalsIgnoreCase("Tiamat")
						|| destination.equalsIgnoreCase("Blutthron") || destination.equalsIgnoreCase("tu"))
			goTo(player, 300520000, 495, 528, 417);

		/**
		 * 4.7 Instances
		 */
		else if (destination.equalsIgnoreCase("idgel_dome") || destination.equalsIgnoreCase("idgel dome")
						|| destination.equalsIgnoreCase("ruhnatorium"))
			goTo(player, 301310000, 254, 179, 83);
		else if (destination.equalsIgnoreCase("linkgate_foundry") || destination.equalsIgnoreCase("linkgate foundry")
						|| destination.equalsIgnoreCase("baruna forschungslabor") || destination.equalsIgnoreCase("baruna"))
			goTo(player, 301270000, 362, 260, 312);

		// New map 4.7
		else if (destination.equalsIgnoreCase("kaldor"))
			goTo(player, 600090000, 397, 1380, 163);
		else if (destination.equalsIgnoreCase("levinshor") || destination.equalsIgnoreCase("akaron"))
			goTo(player, 600100000, 207, 183, 374);
		else if (destination.equalsIgnoreCase("belus"))
			goTo(player, 400020000, 1238, 1232, 1518);
		else if (destination.equalsIgnoreCase("transidium") || destination.equalsIgnoreCase("antriksha") || destination.equalsIgnoreCase("ahserion"))
			goTo(player, 400030000, 509, 513, 675);
		else if (destination.equalsIgnoreCase("aspida"))
			goTo(player, 400040000, 1238, 1232, 1518);
		else if (destination.equalsIgnoreCase("atanatos") || destination.equalsIgnoreCase("athanos"))
			goTo(player, 400050000, 1238, 1232, 1518);
		else if (destination.equalsIgnoreCase("disillon") || destination.equalsIgnoreCase("deylon"))
			goTo(player, 400060000, 1238, 1232, 1518);

		// New instances 4.8
		else if (destination.equalsIgnoreCase("occupied rentus base") || destination.equalsIgnoreCase("occupied rentus")
						|| destination.equalsIgnoreCase("verlorene rentus basis") || destination.equalsIgnoreCase("verlorene rentus"))
			goTo(player, 300620000, 557, 593, 154);

		// New maps 4.8
		else if (destination.equalsIgnoreCase("cygnea") || destination.equalsIgnoreCase("signia"))
			goTo(player, 210070000, 2905, 803, 570);
		else if (destination.equalsIgnoreCase("kenoa"))
			goTo(player, 210070000, 1360, 613, 583);
		else if (destination.equalsIgnoreCase("deluan"))
			goTo(player, 210070000, 1186, 1610, 468);
		else if (destination.equalsIgnoreCase("attika"))
			goTo(player, 210070000, 2368, 1507, 440);
		else if (destination.equalsIgnoreCase("aussenposten der legion des neubeginns") || destination.equalsIgnoreCase("aussenposten ldn"))
			goTo(player, 210070000, 2134, 2912, 329);
		else if (destination.equalsIgnoreCase("angriffsposten der legion des neubeginns") || destination.equalsIgnoreCase("angriffsposten ldn"))
			goTo(player, 210070000, 518, 1900, 469);
		else if (destination.equalsIgnoreCase("enshar") || destination.equalsIgnoreCase("vengar"))
			goTo(player, 220080000, 454, 2262, 220);
		else if (destination.equalsIgnoreCase("mura"))
			goTo(player, 220080000, 1768, 2555, 300);
		else if (destination.equalsIgnoreCase("satyr"))
			goTo(player, 220080000, 1475, 1746, 330);
		else if (destination.equalsIgnoreCase("velias"))
			goTo(player, 220080000, 759, 1274, 253);
		else if (destination.equalsIgnoreCase("oasentempel"))
			goTo(player, 220080000, 1578, 143, 188);
		else if (destination.equalsIgnoreCase("Tempel des Sonnenuntergangs"))
			goTo(player, 220080000, 2685, 1407, 341);
		else if (destination.equalsIgnoreCase("griffoen") || destination.equalsIgnoreCase("ellegef"))
			goTo(player, 210080000, 263, 127, 501);
		else if (destination.equalsIgnoreCase("habrok") || destination.equalsIgnoreCase("lagnatun"))
			goTo(player, 220090000, 253, 107, 505);
		else if (destination.equalsIgnoreCase("new idian asmo") || destination.equalsIgnoreCase("untergrund von katalam asmo")
						|| destination.equalsIgnoreCase("uga"))
			goTo(player, 220100000, 684, 654, 515);
		else if (destination.equalsIgnoreCase("new idian elyos") || destination.equalsIgnoreCase("untergrund von katalam ely")
						|| destination.equalsIgnoreCase("uge"))
			goTo(player, 210090000, 684, 654, 515);

		else
			PacketSendUtility.sendMessage(player, "Could not find the specified destination !");
	}

	private static void goTo(final Player player, int worldId, float x, float y, float z) {
		WorldMap destinationMap = World.getInstance().getWorldMap(worldId);
		if (destinationMap.isInstanceType())
			TeleportService2.teleportTo(player, worldId, getInstanceId(worldId, player), x, y, z);
		else
			TeleportService2.teleportTo(player, worldId, x, y, z);
	}

	private static int getInstanceId(int worldId, Player player) {
		if (player.getWorldId() == worldId) {
			WorldMapInstance registeredInstance = InstanceService.getRegisteredInstance(worldId, player.getObjectId());
			if (registeredInstance != null)
				return registeredInstance.getInstanceId();
		}
		WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(worldId);
		InstanceService.registerPlayerWithInstance(newInstance, player);
		return newInstance.getInstanceId();
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "Syntax : //goto <location>");
	}
}
