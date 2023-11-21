package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.panels.SkillPanel;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "polymorph_panels")
public class PanelSkillsData {

	@XmlElement(name = "panel")
	protected List<SkillPanel> templates;

	@XmlTransient
	private final Map<Integer, SkillPanel> skillPanels = new HashMap<>();

	void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		for (SkillPanel panel : templates) {
			skillPanels.put(panel.getPanelId(), panel);
		}
		templates = null;
	}

	public SkillPanel getSkillPanel(int id) {
		return skillPanels.get(id);
	}

	public int size() {
		return skillPanels.size();
	}
}
