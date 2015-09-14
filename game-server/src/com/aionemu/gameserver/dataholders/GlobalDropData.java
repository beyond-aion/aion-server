package com.aionemu.gameserver.dataholders;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.select;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hamcrest.Matchers;

import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropNpc;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropNpcName;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropNpcs;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalRule;
import com.aionemu.gameserver.model.templates.globaldrops.StringFunction;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;

/**
 * @author AionCool, modified Bobobear
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "global_rules")
public class GlobalDropData {

	@XmlElement(name = "gd_rule")
	protected List<GlobalRule> globalDropRules;

	@XmlTransient
	private List<GlobalRule> gdRules = new ArrayList<GlobalRule>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (GlobalRule gr : globalDropRules) {
			gdRules.add(gr);
		}
		globalDropRules.clear();
		globalDropRules = null;
	}

	public void processRules(TIntObjectHashMap<NpcTemplate> npcs) {
		List<NpcTemplate> npcList = new ArrayList<NpcTemplate>();
		npcList.addAll(npcs.valueCollection());
		for (GlobalRule gr : gdRules) {
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
		List<GlobalDropNpc> allowedNpcs = new ArrayList<GlobalDropNpc>();
		if (rule.getGlobalRuleNpcs() != null) {
			allowedNpcs = rule.getGlobalRuleNpcs().getGlobalDropNpcs();
		}
		if (rule.getGlobalRuleNpcNames() != null) {
			for (GlobalDropNpcName gdNpcName : rule.getGlobalRuleNpcNames().getGlobalDropNpcNames()) {
				List<NpcTemplate> matchesNpcs = new ArrayList<NpcTemplate>();
				if (gdNpcName.getFunction().equals(StringFunction.CONTAINS))
					matchesNpcs = select(npcs, having(on(NpcTemplate.class).getName(), Matchers.containsString(gdNpcName.getValue().toLowerCase())));
				else if (gdNpcName.getFunction().equals(StringFunction.END_WITH))
					matchesNpcs = select(npcs, having(on(NpcTemplate.class).getName(), Matchers.endsWith(gdNpcName.getValue().toLowerCase())));
				else if (gdNpcName.getFunction().equals(StringFunction.START_WITH))
					matchesNpcs = select(npcs, having(on(NpcTemplate.class).getName(), Matchers.startsWith(gdNpcName.getValue().toLowerCase())));
				else if (gdNpcName.getFunction().equals(StringFunction.EQUALS)) {
					matchesNpcs = select(npcs, having(on(NpcTemplate.class).getName(), Matchers.equalToIgnoringCase(gdNpcName.getValue().toLowerCase())));
				}
				for (NpcTemplate npc : matchesNpcs) {
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
		return gdRules;
	}

	public int size() {
		return gdRules.size();
	}
}
