package admincommands;

import java.awt.Color;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.enchants.EnchantEffect;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.*;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.stats.ModifiersTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * @author MrPoke
 */
public class Stat extends AdminCommand {

	public Stat() {
		super("stat", "Shows and modifies any stats.");

		// @formatter:off
		setSyntaxInfo(
			"list - Lists all stats.",
			"<stat> - Shows your target's active stat functions for the given stat.",
			"<stat> <value> - Sets your target's stat to the given value.",
			"abs <stat set ID> - Applies fixed stats of the given stats_set ID from absolute_stats.xml to your target.",
			"cancel - Cancels all active stat overrides for your target.",
			"Stat parameters accept lowercase and abbreviated formats, such as flytime or flyt instead of FLY_TIME."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		VisibleObject target = admin.getTarget() == null ? admin : admin.getTarget();
		if (!(target instanceof Creature creature)) {
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			return;
		}

		if (params.length == 1 && "list".equalsIgnoreCase(params[0])) {
			listStats(admin);
		} else if (params.length == 1 && "cancel".equalsIgnoreCase(params[0])) {
			cancelStatOverrides(admin, creature);
		} else if (params.length == 1) {
			showStatFunctions(admin, creature, params[0]);
		} else if (params.length == 2 && !"abs".equalsIgnoreCase(params[0])) {
			setStat(admin, creature, params[0], Integer.parseInt(params[1]));
		} else if (params.length == 2 && "abs".equalsIgnoreCase(params[0])) {
			ModifiersTemplate template = DataManager.ABSOLUTE_STATS_DATA.getTemplate(Integer.parseInt(params[1]));
			if (template == null) {
				sendInfo(admin, "Invalid stat set ID.");
				return;
			}
			template.getModifiers().forEach(m -> applyStatFunction(creature, m));
			sendInfo(admin, "Applied absolute stats to " + creature.getName() + ".");
		} else {
			sendInfo(admin);
		}
	}

	public void showStatFunctions(Player admin, Creature target, String searchStat) {
		StatEnum stat = findStat(admin, searchStat);
		if (stat != null)
			showActiveStatFunctions(admin, target, stat);
	}

	private StatEnum findStat(Player admin, String searchStat) {
		List<StatEnum> stats = findPossibleMatches(searchStat);
		if (stats.size() != 1) {
			String message = "There is no stat with that name.";
			if (!stats.isEmpty())
				message += " Possible matches:\n\t" + stats.stream().map(Enum::name).collect(Collectors.joining("\n\t"));
			sendInfo(admin, message);
			return null;
		}
		return stats.getFirst();
	}

	private List<StatEnum> findPossibleMatches(String searchStat) {
		if (searchStat.length() < 2)
			return List.of();
		List<StatEnum> possibleMatches = new ArrayList<>();
		searchStat = searchStat.toLowerCase();
		String searchStatShort = searchStat.replace("_", "");
		for (StatEnum stat : StatEnum.values()) {
			String statName = stat.name().toLowerCase();
			String statNameShort = statName.replace("_", "");
			if (searchStatShort.equals(statNameShort))
				return List.of(stat);
			if (statNameShort.startsWith(searchStatShort) || statName.contains(searchStat)) {
				possibleMatches.add(stat);
			}
		}
		return possibleMatches;
	}

	private void showActiveStatFunctions(Player admin, Creature target, StatEnum stat) {
		List<IStatFunction> stats = target.getGameStats().getStatsSorted(stat);
		String targetInfo = admin.equals(target) ? "You currently have " : target.getName() + " currently has ";
		String statName = ChatUtil.color(stat.name(), Color.WHITE);
		if (stats.isEmpty()) {
			sendInfo(admin, targetInfo + "no active " + statName + " functions.");
			return;
		}
		sendInfo(admin, targetInfo + stats.size() + " active " + statName + " function(s):");
		stats.stream().map(StatFunctionInfo::new)
			.collect(Collectors.groupingBy(f -> f, LinkedHashMap::new, Collectors.counting()))
			.forEach((info, count) -> sendInfo(admin, ChatUtil.leftPad(count, 3) + "x " + info));
	}

	public void listStats(Player admin) {
		String stats = Arrays.stream(StatEnum.values()).map(Enum::name).collect(Collectors.joining("\n\t"));
		sendInfo(admin, "List of stats:\n\t" + stats);
	}

	public void setStat(Player admin, Creature target, String searchStat, int value) {
		StatEnum stat = findStat(admin, searchStat);
		if (stat == null)
			return;
		applyStatFunction(target, new CommandStatFunction(stat, value));
		String targetInfo = admin.equals(target) ? "Your " : target.getName() + "'s ";
		sendInfo(admin, targetInfo + stat.name().toLowerCase() + " is now set to " + value + ".");
	}

	private void applyStatFunction(Creature creature, StatFunction statFunction) {
		StatOwner statOwner = CommandStatOwner.get(statFunction.getName());
		creature.getGameStats().endEffect(statOwner);
		creature.getGameStats().addEffect(statOwner, List.of(statFunction));
	}

	public void cancelStatOverrides(Player admin, Creature target) {
		CommandStatOwner.forEach(owner -> target.getGameStats().endEffect(owner));
		String targetInfo = admin.equals(target) ? "Your" : target.getName() + "'s";
		sendInfo(admin, targetInfo + " stat overrides have been canceled.");
	}

	static class CommandStatFunction extends StatFunction {

		public CommandStatFunction(StatEnum name, int value) {
			super(name, value, true);
		}

		@Override
		public void apply(Stat2 stat, CalculationType... calculationTypes) {
			stat.setBonusRate(1f);
			stat.setFinalRate(1f);
			stat.setBonus(getValue() - stat.getExactCurrentWithoutBonus());
		}

		@Override
		public final int getPriority() {
			return 120;
		}
	}

	record CommandStatOwner(StatEnum stat) implements StatOwner {

		static final Map<StatEnum, StatOwner> statOwnerByStat = new EnumMap<>(StatEnum.class);

		static StatOwner get(StatEnum stat) {
			return statOwnerByStat.computeIfAbsent(stat, CommandStatOwner::new);
		}

		static void forEach(Consumer<StatOwner> consumer) {
			statOwnerByStat.values().forEach(consumer);
		}
	}

	record StatFunctionInfo(int value, boolean bonus, int priority, StatOwner owner, String type) {

		StatFunctionInfo(IStatFunction f) {
			this(f.getValue(), f.isBonus(), f.getPriority(), f.getOwner(), (f instanceof StatFunctionProxy p ? p.getProxiedFunction() : f).getClass().getSimpleName());
		}

		@Override
		public String toString() {
			String info = isOverrideFunction() ? "=" + value : value >= 0 ? "+" + value : "" + value;
			if (type.equals(CommandStatFunction.class.getSimpleName())) {
				info = ChatUtil.color(info, Color.CYAN);
			} else {
				if (type.equals(StatRateFunction.class.getSimpleName()))
					info += "%";
				info = ChatUtil.color(info, value < 0 ? Color.RED : bonus ? Color.GREEN : Color.WHITE);
				if (bonus)
					info += " bonus";
			}
			info += ", priority: " + priority;
			info += ", type: " + type;
			info += ", owner: " + (owner == null ? "none" : owner.getClass().getSimpleName());
			if (owner instanceof Effect effect)
				info += " (skill ID " + effect.getSkillId() + ": " + effect.getSkillName() + ")";
			else if (owner instanceof Item item)
				info += " (" + item.getName() + ")";
			else if (owner instanceof EnchantEffect enchantEffect && enchantEffect.getItemSlot() != null)
				info += " (" + enchantEffect.getItemSlot() + ")";
			return info;
		}

		private boolean isOverrideFunction() {
			return type.equals(CommandStatFunction.class.getSimpleName()) || type.equals(StatAbsFunction.class.getSimpleName()) || type.equals(StatSetFunction.class.getSimpleName());
		}
	}
}
