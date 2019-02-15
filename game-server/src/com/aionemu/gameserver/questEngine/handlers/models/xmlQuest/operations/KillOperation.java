package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.operations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.questEngine.model.QuestEnv;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KillOperation")
public class KillOperation extends QuestOperation {

	@Override
	public void doOperate(QuestEnv env) {
		if (env.getVisibleObject() instanceof Npc)
			((Npc) env.getVisibleObject()).getController().die(env.getPlayer());
	}

}
