package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.events;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.model.QuestEnv;

/**
 * Runs its operations when one of the listed cutscenes finished playing, which is also the moment the player skipped it.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OnMovieEndEvent")
public class OnMovieEndEvent extends QuestEvent {

	@XmlAttribute(name = "movie_ids", required = true)
	protected List<Integer> movieIds;

	public List<Integer> getMovieIds() {
		if (movieIds == null)
			movieIds = new ArrayList<>();
		return movieIds;
	}

	public boolean operate(QuestEnv env, int movieId) {
		if (movieIds == null || !movieIds.contains(movieId))
			return false;
		if (conditions != null && !conditions.checkConditionOfSet(env))
			return false;
		return operations != null && operations.operate(env);
	}
}
