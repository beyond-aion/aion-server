package admincommands;

import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.drop.Drop;
import com.aionemu.gameserver.model.drop.DropGroup;
import com.aionemu.gameserver.model.drop.DropModifiers;
import com.aionemu.gameserver.model.drop.NpcDrop;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropItem;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalRule;
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.services.event.EventService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.WorldDropType;

/**
 * @author Oliver, AionCool, Bobobear, Neon
 */
public class DropInfo extends AdminCommand {

	public DropInfo() {
		super("dropinfo", "Shows drop information of your target.");

		setSyntaxInfo("[all] - Lists drops of the selected npc (default: only drops for your level range, optional: all possible drops).");
	}

	@Override
	public void execute(Player player, String... params) {
		VisibleObject visibleObject = player.getTarget();

		if (!(visibleObject instanceof Npc npc)) {
			sendInfo(player);
			return;
		}

		boolean showAll = params.length > 0 && params[0].equals("all");
		NpcDrop npcDrop = DataManager.CUSTOM_NPC_DROP.getNpcDrop(npc.getNpcId());
		DropModifiers dropModifiers = DropRegistrationService.getInstance().createDropModifiers(npc, player, player.getLevel());
		dropModifiers.setMaxDropsPerGroup(Integer.MAX_VALUE);

		int[] counts = { 0, 0 };
		String info = "[" + npc.getObjectTemplate().getL10n() + "'s drops]";
		if (npcDrop != null) {
			for (DropGroup dropGroup : npcDrop.getDropGroup()) {
				if (dropGroup.getRace() == Race.PC_ALL || dropGroup.getRace() == dropModifiers.getDropRace()) {
					info += "\nCustom drop group: " + dropGroup.getName() + ", max drops: " + dropGroup.getMaxItems();
					counts[1]++;
					for (Drop drop : dropGroup.getDrop()) {
						float finalChance = dropModifiers.calculateDropChance(drop.getChance(), dropGroup.isUseLevelBasedChanceReduction());
						if (!showAll && finalChance <= 0)
							continue;
						info += "\n\t" + ChatUtil.item(drop.getItemId()) + "\tBase chance: " + drop.getChance() + "%, effective: " + finalChance + "%";
						counts[0]++;
					}
				}
			}
		}

		// if npc ai == quest_use_item it will be always excluded from global drops
		boolean isNpcQuest = npc.getAi().getName().equals("quest_use_item");
		if (!isNpcQuest) {
			boolean hasGlobalNpcExclusions = DropRegistrationService.getInstance().hasGlobalNpcExclusions(npc);
			boolean isAllowedDefaultGlobalDropNpc = DropRegistrationService.getInstance().isAllowedDefaultGlobalDropNpc(npc, dropModifiers.isDropNpcChest());
			// instances with WorldDropType.NONE must not have global drops (example Arenas)
			if (!hasGlobalNpcExclusions && npc.getWorldDropType() != WorldDropType.NONE) {
				info += collectDropInfo("Global", DataManager.GLOBAL_DROP_DATA.getAllRules(), npc, dropModifiers, isAllowedDefaultGlobalDropNpc, showAll,
					counts);
			}
			if (!hasGlobalNpcExclusions || dropModifiers.isDropNpcChest())
				info += collectDropInfo("Event", EventService.getInstance().getActiveEventDropRules(), npc, dropModifiers, isAllowedDefaultGlobalDropNpc,
					showAll, counts);
		}

		info += "\n" + counts[0] + " total drops available in " + counts[1] + " drop groups" + (showAll ? "." : " on your level.");
		sendInfo(player, info);
	}

	private String collectDropInfo(String dropGroupPrefix, List<GlobalRule> rules, Npc npc, DropModifiers dropModifiers,
		boolean isAllowedDefaultGlobalDropNpc, boolean showAll, int[] counts) {
		String info = "";
		for (GlobalRule rule : rules) {
			// if getGlobalRuleNpcs() != null means drops are for specified npcs (like named drops) so the default restrictions will be ignored
			if (isAllowedDefaultGlobalDropNpc || rule.getGlobalRuleNpcs() != null) {

				float chance = DropRegistrationService.getInstance().calculateEffectiveChance(rule, npc, dropModifiers);
				if (!showAll && chance <= 0)
					continue;

				List<GlobalDropItem> drops = DropRegistrationService.getInstance().collectDrops(rule, npc, dropModifiers);
				if (!drops.isEmpty()) {
					info += "\n" + dropGroupPrefix + " drop group: \"" + rule.getRuleName() + "\", max drops: " + rule.getMaxDropRule();
					if (rule.getMemberLimit() != 1)
						info += ", member limit: " + rule.getMemberLimit();
					info += "\n\tBase chance: " + rule.getChance() + "%, effective: " + chance + "%";
					counts[1]++;
					for (GlobalDropItem item : drops) {
						info += "\n\t" + ChatUtil.item(item.getId());
						if (item.getChance() != 100f)
							info += "\tSub chance: " + item.getChance() + "%";
						counts[0]++;
					}
				}
			}
		}
		return info;
	}
}
