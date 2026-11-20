package com.aionemu.gameserver.controllers.effect;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

class NoFlySourceTest {

	@Test
	void noFlyLastsUntilTheLastSourceEnds() {
		Creature owner = new Creature(0, null, null, new NpcTemplate(), null, false) {

			@Override
			public byte getLevel() {
				return 1;
			}
		};
		EffectController controller = new EffectController(owner);
		Effect direct = new Effect(null, owner, new SkillTemplate(), 1);
		Effect redirected = new Effect(null, null, new SkillTemplate(), 1);

		controller.addNoFlySource(direct);
		controller.addNoFlySource(redirected);
		controller.addNoFlySource(redirected);
		assertTrue(controller.isAbnormalSet(AbnormalState.NOFLY));

		assertTrue(controller.removeNoFlySource(direct));
		assertTrue(controller.isAbnormalSet(AbnormalState.NOFLY));
		assertFalse(controller.removeNoFlySource(direct));
		assertTrue(controller.isAbnormalSet(AbnormalState.NOFLY));

		assertTrue(controller.removeNoFlySource(redirected));
		assertFalse(controller.isAbnormalSet(AbnormalState.NOFLY));
	}
}
