package com.aionemu.gameserver.model.items;

import com.aionemu.gameserver.model.stats.calc.functions.StatFunction;
import com.aionemu.gameserver.model.templates.stats.ModifiersTemplate;

/**
 * @author Rolandas
 */
public class RandomBonusResult {

	private final ModifiersTemplate template;
	private final int templateNumber;

	public RandomBonusResult(ModifiersTemplate template, int number) {
		this.template = template;
		for (StatFunction function : template.getModifiers())
			function.setRandomNumber(number);
		this.templateNumber = number;
	}

	public ModifiersTemplate getTemplate() {
		return template;
	}

	public int getTemplateNumber() {
		return templateNumber;
	}

}
