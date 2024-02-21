package ai.worlds.panesterra.ahserionsflight;

import java.util.*;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionRaid;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraFaction;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraTeam;

import ai.AggressiveNpcAI;

/**
 * @author Yeats, Estrayl
 */
@AIName("ahserion")
public class Ahserion extends AggressiveNpcAI {

	private final List<Integer> hpEvents = new ArrayList<>();

	public Ahserion(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		initHpEvents();
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private synchronized void checkPercentage(int hpPercentage) {
		for (Integer hpEvent : hpEvents) {
			if (hpPercentage <= hpEvent) {
				hpEvents.remove(hpEvent);
				switch (hpEvent) {
					case 75, 50, 25, 10 -> getOwner().getQueuedSkills()
						.offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21571, 1, 100, 0, 3000, NpcSkillTargetAttribute.ME)));
				}
				break;
			}
		}
	}

	@Override
	protected void handleDied() {
		if (getOwner().getWorldId() == 400030000 && AhserionRaid.getInstance().isStarted()) {
			Map<PanesterraFaction, Integer> panesterraDamage = new HashMap<>();

			// Only players can attack Ahserion on this map.
			for (AggroInfo ai : getOwner().getAggroList().getFinalDamageList(false)) {
				if (ai.getAttacker() instanceof Player) {
					PanesterraTeam team = AhserionRaid.getInstance().getPanesterraFactionTeam((Player) ai.getAttacker());
					if (team != null && !team.isEliminated()) {
						PanesterraFaction faction = team.getFaction();
						panesterraDamage.merge(faction, ai.getDamage(), Integer::sum);
					}
				}
			}
			PanesterraFaction winner = findWinnerTeam(panesterraDamage);
			if (winner != null)
				AhserionRaid.getInstance().handleBossKilled(getOwner(), winner);
		}
		super.handleDied();
	}

	private PanesterraFaction findWinnerTeam(Map<PanesterraFaction, Integer> panesterraDamage) {
		PanesterraFaction winner = null;
		int maxDmg = 0;
		for (PanesterraFaction faction : PanesterraFaction.values()) {
			Integer dmg = panesterraDamage.get(faction);
			if (dmg != null && !AhserionRaid.getInstance().getFactionTeam(faction).isEliminated()) {
				if (dmg > maxDmg) {
					maxDmg = dmg;
					winner = faction;
				}
			}
		}
		return winner;
	}

	private void initHpEvents() {
		hpEvents.clear();
		Collections.addAll(hpEvents, 75, 50, 25, 10);
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		initHpEvents();
	}
}
