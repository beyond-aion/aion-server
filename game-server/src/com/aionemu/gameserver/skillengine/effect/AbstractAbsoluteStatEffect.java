package com.aionemu.gameserver.skillengine.effect;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.templates.stats.ModifiersTemplate;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractAbsoluteStatEffect")
public abstract class AbstractAbsoluteStatEffect extends BufEffect {

	@XmlAttribute(name = "statsetid")
	private int statSetId;

	/**
	 * @param effect
	 * @return
	 */
	@Override
	protected List<IStatFunction> getModifiers(Effect effect) {
		List<IStatFunction> modifiers = new ArrayList<>();
		modifiers.addAll(getModifiersSet().getModifiers());

		return modifiers;
	}

	/**
	 * @return the statSetId
	 */
	public ModifiersTemplate getModifiersSet() {
		return DataManager.ABSOLUTE_STATS_DATA.getTemplate(statSetId);
	}

}
