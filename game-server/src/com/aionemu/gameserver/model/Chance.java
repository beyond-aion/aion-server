package com.aionemu.gameserver.model;

import java.util.Iterator;

import com.aionemu.commons.utils.Rnd;

/**
 * Any class implementing this interface will enable calling {@link #selectElement(Iterable)} on collections of such elements.
 * 
 * @author Neon
 */
public interface Chance {

	float getChance();

	/**
	 * @return Random element selected by its chance.
	 */
	static <T extends Chance> T selectElement(Iterable<T> elements) {
		return selectElement(elements, false);
	}

	/**
	 * @return Random element selected by its chance. The item will be removed from the input elements if {@code remove} is true.
	 */
	static <T extends Chance> T selectElement(Iterable<T> elements, boolean remove) {
		float sumOfChances = 0;
		for (T element : elements)
			sumOfChances += element.getChance();

		if (sumOfChances > 0) {
			float randomChance = Rnd.nextFloat(sumOfChances);
			float luck = 0;
			for (Iterator<T> iterator = elements.iterator(); iterator.hasNext();) {
				T element = iterator.next();
				luck += element.getChance();
				if (randomChance <= luck) {
					if (remove)
						iterator.remove();
					return element;
				}
			}
		}
		return null;
	}
}
