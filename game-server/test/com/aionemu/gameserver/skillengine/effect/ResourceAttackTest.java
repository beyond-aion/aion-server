package com.aionemu.gameserver.skillengine.effect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.EffectReserved;
import com.aionemu.gameserver.skillengine.model.EffectReserved.ResourceType;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

class ResourceAttackTest {

	@Test
	void instantMpDamageIncludesTheLevelCoefficient() {
		MpAttackInstantEffect attack = new MpAttackInstantEffect();
		attack.position = 1;
		attack.noResist = true;
		attack.value = 10;
		attack.delta = 20;
		Creature target = new Creature(0, null, null, new NpcTemplate(), null, false) {

			@Override
			public byte getLevel() {
				return 1;
			}
		};
		Effect effect = new Effect(null, target, new SkillTemplate(), 3);

		attack.calculate(effect);

		assertEquals(70, effect.getReserveds(1).getValue());
		assertEquals(ResourceType.MP, effect.getReservedEffectsToSend().iterator().next().getType());
	}

	@Test
	void laterResistSuppressesEarlierResourceDamageInCastResult() {
		MpAttackInstantEffect resistedAttack = new MpAttackInstantEffect() {

			@Override
			protected boolean isDodgedOrResisted(Effect effect, StatEnum statEnum) {
				return true;
			}
		};
		resistedAttack.position = 2;

		for (ResourceType resource : List.of(ResourceType.FP, ResourceType.MP)) {
			Effect effect = new Effect(null, null, new SkillTemplate(), 1);
			EffectTemplate firstAttack = resource == ResourceType.FP ? new FpAttackInstantEffect() : new MpAttackInstantEffect();
			firstAttack.position = 1;
			effect.addSuccessEffect(firstAttack);
			effect.setReserveds(new EffectReserved(1, 40, resource, true), false);

			resistedAttack.calculate(effect);

			EffectReserved packetEntry = effect.getReservedEffectsToSend().iterator().next();
			assertEquals(0, packetEntry.getValue());
		}
	}
}
