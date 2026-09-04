package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.operations;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * Plays a cutscene or, with {@code movie="true"}, a movie. Retail declares both as one action with the type in front of the id.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlayCutsceneOperation")
public class PlayCutsceneOperation extends QuestOperation {

	@XmlAttribute(required = true)
	protected int id;
	@XmlAttribute
	protected boolean movie;

	@Override
	public void doOperate(QuestEnv env) {
		int targetObjectId = env.getVisibleObject() == null ? 0 : env.getVisibleObject().getObjectId();
		PacketSendUtility.sendPacket(env.getPlayer(), new SM_PLAY_MOVIE(movie, targetObjectId, env.getQuestId(), id, true));
	}
}
