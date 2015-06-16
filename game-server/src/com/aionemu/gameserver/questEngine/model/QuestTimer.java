package com.aionemu.gameserver.questEngine.model;

import java.util.Timer;
import java.util.TimerTask;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Hilgert
 */
public class QuestTimer {

	private Timer timer;

	private int Time = 0;

	@SuppressWarnings("unused")
	private int questId;

	private boolean isTicking = false;

	private Player player;

	/**
	 * @param questId
	 */
	public QuestTimer(int questId, int seconds, Player player) {
		this.questId = questId;
		this.Time = seconds * 1000;
		this.player = player;
	}

	/**
	 * @param seconds
	 * @param player
	 * @return
	 */
	public void Start() {
		PacketSendUtility.sendMessage(player, "Timer started");
		timer = new Timer();
		isTicking = true;
		// TODO Send Packet that timer start
		TimerTask task = new TimerTask() {

			public void run() {
				PacketSendUtility.sendMessage(player, "Timer is over");
				onEnd();
			}
		};

		timer.schedule(task, Time);
	}

	public void Stop() {
		timer.cancel();
		onEnd();
	}

	public void onEnd() {
		// TODO Send Packet that timer end
		isTicking = false;
	}

	/**
	 * @return true - if Timer started, and ticking.
	 * @return false - if Timer not started or stoped.
	 */
	public boolean isTicking() {
		return this.isTicking;
	}

	/**
	 * @return
	 */
	public int getTimeSeconds() {
		return (int) this.Time / 1000;
	}
}
