package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.operations;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * Plays a cutscene or, with {@code movie="true"}, a movie. The quest is notified by {@code on_movie_end_event} when it finished playing.
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
