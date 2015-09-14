package com.aionemu.chatserver.model.channel;

import com.aionemu.chatserver.model.ChannelType;
import com.aionemu.chatserver.model.Gender;
import com.aionemu.chatserver.model.PlayerClass;
import com.aionemu.chatserver.model.Race;

/**
 * @author ATracer
 */
public class JobChannel extends RaceChannel {

	private PlayerClass playerClass;
	private Gender gender;

	/**
	 * @param playerClass
	 * @param race
	 */
	public JobChannel(Gender gender, PlayerClass playerClass, Race race, String identifier) {
		super(ChannelType.JOB, race, identifier);
		this.playerClass = playerClass;
		this.gender = gender;
	}

	/**
	 * @return the playerClass
	 */
	public PlayerClass getPlayerClass() {
		return playerClass;
	}

	public Gender getGender() {
		return gender;
	}
}
