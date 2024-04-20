package com.aionemu.gameserver.dataholders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropNpc;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropNpcName;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropNpcs;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalRule;
import com.aionemu.gameserver.model.templates.globaldrops.StringFunction;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;

/**
 * @author AionCool, Bobobear, Neon
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "global_rules")
public class GlobalDropData {

	@XmlElement(name = "gd_rule")
	private List<GlobalRule> globalDropRules;

	public void processRules(Collection<NpcTemplate> npcs) {
		List<NpcTemplate> npcList = new ArrayList<>(npcs);
		for (GlobalRule gr : globalDropRules) {
			if (gr.getGlobalRuleNpcNames() != null) {
				List<GlobalDropNpc> allowedNpcs = getAllowedNpcs(gr, npcList);
				if (!allowedNpcs.isEmpty()) {
					gr.setNpcs(new GlobalDropNpcs());
					gr.getGlobalRuleNpcs().addNpcs(allowedNpcs);
					gr.getGlobalRuleNpcNames().getGlobalDropNpcNames().clear();
				}
			}
		}
	}

	private List<GlobalDropNpc> getAllowedNpcs(GlobalRule rule, List<NpcTemplate> npcs) {
		List<GlobalDropNpc> allowedNpcs = new ArrayList<>();
		if (rule.getGlobalRuleNpcs() != null) {
			allowedNpcs = rule.getGlobalRuleNpcs().getGlobalDropNpcs();
		}
		if (rule.getGlobalRuleNpcNames() != null) {
			for (GlobalDropNpcName gdNpcName : rule.getGlobalRuleNpcNames().getGlobalDropNpcNames()) {
				List<NpcTemplate> matchedNpcs = new ArrayList<>();
				if (gdNpcName.getFunction().equals(StringFunction.CONTAINS))
					matchedNpcs = npcs.stream().filter(npc -> npc.getName().contains(gdNpcName.getValue().toLowerCase())).collect(Collectors.toList());
				else if (gdNpcName.getFunction().equals(StringFunction.END_WITH))
					matchedNpcs = npcs.stream().filter(npc -> npc.getName().endsWith(gdNpcName.getValue().toLowerCase())).collect(Collectors.toList());
				else if (gdNpcName.getFunction().equals(StringFunction.START_WITH))
					matchedNpcs = npcs.stream().filter(npc -> npc.getName().startsWith(gdNpcName.getValue().toLowerCase())).collect(Collectors.toList());
				else if (gdNpcName.getFunction().equals(StringFunction.EQUALS)) {
					matchedNpcs = npcs.stream().filter(npc -> npc.getName().equalsIgnoreCase(gdNpcName.getValue())).collect(Collectors.toList());
				}
				for (NpcTemplate npc : matchedNpcs) {
					GlobalDropNpc gdNpc = new GlobalDropNpc();
					gdNpc.setNpcId(npc.getTemplateId());
					if (!allowedNpcs.contains(gdNpc)) {
						allowedNpcs.add(gdNpc);
					}
				}
			}
		}
		return allowedNpcs;
	}

	/**
	 * Gets the value of the globalDrop property.
	 */
	public List<GlobalRule> getAllRules() {
		return globalDropRules;
	}

	public int size() {
		return globalDropRules.size();
	}
}
