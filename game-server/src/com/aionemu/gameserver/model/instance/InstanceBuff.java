package com.aionemu.gameserver.model.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatAddFunction;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.instance_bonusatrr.InstanceBonusAttr;
import com.aionemu.gameserver.model.templates.instance_bonusatrr.InstancePenaltyAttr;
import com.aionemu.gameserver.skillengine.change.Func;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 *
 * @author xTz
 */
public class InstanceBuff implements StatOwner {

	private Future<?> task;
	private List<IStatFunction> functions = new ArrayList<IStatFunction>();
	private InstanceBonusAttr instanceBonusAttr;
	private long startTime;

	public InstanceBuff(int buffId) {
		instanceBonusAttr = DataManager.INSTANCE_BUFF_DATA.getInstanceBonusattr(buffId);
	}

	public void applyEffect(Player player, int time) {

		if (hasInstanceBuff() || instanceBonusAttr == null) {
			return;
		}
		if (time != 0) {
			task = ThreadPoolManager.getInstance().schedule(new InstanceBuffTask(player), time);
		}
		startTime = System.currentTimeMillis();
		for (InstancePenaltyAttr instancePenaltyAttr : instanceBonusAttr.getPenaltyAttr()) {
			StatEnum stat = instancePenaltyAttr.getStat();
			int statToModified = player.getGameStats().getStat(stat, 0).getBase();
			int value = instancePenaltyAttr.getValue();
			int valueModified = instancePenaltyAttr.getFunc().equals(Func.PERCENT) ? (statToModified * value / 100) : (value);
			functions.add(new StatAddFunction(stat, valueModified, true));
		}
		player.getGameStats().addEffect(this, functions);
	}

	public void endEffect(Player player) {
		functions.clear();
		if (hasInstanceBuff()) {
			task.cancel(true);
		}
		player.getGameStats().endEffect(this);
	}
	
	public int getRemaningTime() {
		return (int) ((System.currentTimeMillis() - startTime) / 1000);
	}

	private class InstanceBuffTask implements Runnable {

		private Player player;

		public InstanceBuffTask(Player player) {
			this.player = player;
		}

		@Override
		public void run() {
			endEffect(player);
		}

	}

	public boolean hasInstanceBuff() {
		return task != null && !task.isDone();
	}

}