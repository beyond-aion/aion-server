package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.spawnengine.VisibleObjectSpawner;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ginho1
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummonFunctionalNpcEffect")
public class SummonFunctionalNpcEffect extends SummonEffect {

	@XmlAttribute(name = "owner")
	private SummonOwner owner;

	@Override
	public void applyEffect(Effect effect) {
		Player effected = (Player) effect.getEffected();
		final Npc functionalNpc = VisibleObjectSpawner.spawnFunctionalNpc(effected, npcId, owner);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (functionalNpc != null && functionalNpc.isSpawned())
					functionalNpc.getController().delete();
			}
		}, 300000);
	}
}
