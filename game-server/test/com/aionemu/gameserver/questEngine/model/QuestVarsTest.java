package com.aionemu.gameserver.questEngine.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class QuestVarsTest {

	@Test
	void packsVarsIntoSixBitSections() {
		QuestVars vars = new QuestVars();
		vars.setVarById(0, 5);
		vars.setVarById(4, 10);
		assertEquals(0x0A000005, vars.getQuestVars());
	}

	@Test
	void unpacksProgressIntoVars() {
		QuestVars vars = new QuestVars(0x0A000005);
		assertEquals(5, vars.getVarById(0));
		assertEquals(0, vars.getVarById(1));
		assertEquals(10, vars.getVarById(4));
		assertEquals(0, vars.getVarById(5));
	}

	@Test
	void lastVarUsesTheTwoTopBits() {
		QuestVars vars = new QuestVars(0xC0000000);
		assertEquals(3, vars.getVarById(5));
		assertEquals(0, vars.getVarById(4));
		assertEquals(0xC0000000, vars.getQuestVars());
	}

	@Test
	void keepsEveryProgressValue() {
		for (int progress : new int[] { 0, 1, 63, 64, 98, 12609, 167772167, Integer.MAX_VALUE, -1, Integer.MIN_VALUE })
			assertEquals(progress, new QuestVars(progress).getQuestVars());
	}

	@Test
	void oversizedVarSpillsIntoTheNextOne() {
		QuestVars vars = new QuestVars();
		vars.setVarById(0, 98);
		assertEquals(98, vars.getQuestVars());
		assertEquals(1, new QuestVars(vars.getQuestVars()).getVarById(1));
	}
}
