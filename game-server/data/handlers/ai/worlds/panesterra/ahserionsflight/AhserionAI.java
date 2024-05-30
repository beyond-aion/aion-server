package ai.worlds.panesterra.ahserionsflight;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionRaid;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraFaction;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraTeam;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.WorldPosition;

import ai.AggressiveNpcAI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yeats, Estrayl
 */
@AIName("ahserion")
public class AhserionAI extends AggressiveNpcAI {

	private static final Logger log = LoggerFactory.getLogger("SIEGE_LOG");

	public AhserionAI(Npc owner) {
		super(owner);
	}

	@Override
	public void modifyOwnerStat(Stat2 stat) {
		if (stat.getStat() == StatEnum.MAXHP)
			stat.setBaseRate(SiegeConfig.AHSERION_MAX_PLAYERS_PER_TEAM / 100f);
	}

	@Override
	public float modifyDamage(Creature attacker, float damage, Effect effect) {
		if (effect != null && effect.getSkillId() == 21583) // Artillery Blast
			return damage * (SiegeConfig.AHSERION_MAX_PLAYERS_PER_TEAM / 100f);
		return super.modifyDamage(attacker, damage, effect);
	}

	@Override
	public void onStartUseSkill(SkillTemplate st, int lv) {
		switch (st.getSkillId()) {
			case 21566 -> { // Thunder Crash
				if (lv == 55 && System.currentTimeMillis() - getOwner().getGameStats().getFightStartingTime() < 30000)
					PacketSendUtility.broadcastMessage(getOwner(), 1501157); // _01: Beritra thinks me unready. I shall prove myself in your slaughter!
			}
			case 21570 -> { // Death Shriek
				if (lv == 58)
					PacketSendUtility.broadcastMessage(getOwner(), 1501163); // _07: Not enough time… My Lords, come save me!
			}
			case 21571 -> { // Ereshkigal's Reign
				WorldPosition p = getPosition();
				spawn(297186, p.getX(), p.getY(), p.getZ() + 10, (byte) 0); // Ereshkigal's Voice
			}
			case 21573 -> { // Distortion Pulse
				if (lv == 58)
					PacketSendUtility.broadcastMessage(getOwner(), 1501162); // _06: Impudent pestilence! Be gone!
			}
			// Ide Destruction
			case 21574 -> PacketSendUtility.broadcastMessage(getOwner(), 1501164); // _08: I will return you to your beloved Aion's dust!
		}
	}

	@Override
	public void onEndUseSkill(SkillTemplate st, int lv) {
		switch (st.getSkillId()) {
			case 21564 -> { // Dragon Claw
				if (lv == 58)
					handleBaseAssault();
			}
			case 21567 -> { // Toxic Spew
				if (lv == 58)
					handleSupportSpawns();
			}
			case 21574 -> handleIdeDestruction(); // Ide Destruction
		}

		// Custom solution to resolve the retail add hate event (switch_target_by_attacker_indicator)
		if (lv == 57 || lv == 26) {
			addHateToRandomPlayer();
		}
	}

	@Override
	public void onEffectApplied(Effect effect) {
		if (effect.getSkillId() == 21571) // Ereshkigal's Reign
			PacketSendUtility.broadcastMessage(getOwner(), 1501159); // _03: My head… Nooooo! Get out of my head!
	}

	@Override
	public void onEffectEnd(Effect effect) {
		if (effect.getSkillId() == 21571) // Ereshkigal's Reign
			PacketSendUtility.broadcastMessage(getOwner(), 1501161); // _03: Ereshkigal…? No! Nooooooo-argh!
	}

	private void handleSupportSpawns() {
		spawn(297353, 475.530f, 536.809f, 675.989f, (byte) 75);
		spawn(297353, 532.324f, 545.949f, 675.746f, (byte) 75);
		spawn(297353, 541.742f, 491.245f, 675.462f, (byte) 75);
		spawn(297353, 487.297f, 479.381f, 678.300f, (byte) 75);
	}

	private void handleBaseAssault() {
		if (getOwner().getWorldId() == 400030000 && AhserionRaid.getInstance().isStarted()) {
			for (PanesterraFaction faction : PanesterraFaction.values()) {
				PanesterraTeam team = AhserionRaid.getInstance().getFactionTeam(faction);
				if (team != null && !team.isEliminated())
					AhserionRaid.getInstance().spawnStage(5, faction);
			}
		}
	}

	/**
	 * Retail: activate_skillarea 21575
	 */
	private void handleIdeDestruction() {
		getKnownList().getKnownPlayers().values().stream().filter(p -> !p.isDead() && PositionUtil.isInRange(p, 509.64f, 513.25f, 675.145f, 45))
			.forEach(p -> SkillEngine.getInstance().getSkill(getOwner(), 21575, 1, p).useWithoutPropSkill());
	}

	private void addHateToRandomPlayer() {
		List<AggroInfo> attackingPlayers = getAggroList().getList().stream().filter(ai -> ai.getAttacker() instanceof Player player && !player.isDead())
			.toList();
		AggroInfo aggroInfo = Rnd.get(attackingPlayers);
		if (aggroInfo != null)
			aggroInfo.addHate(100000);
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
		logMetrics();
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

	private void logMetrics() {
		long fullFightTime = (System.currentTimeMillis() - getOwner().getGameStats().getFightStartingTime()) / 1000;
		String damageDealt = getAggroList().getFinalDamageList(false).stream().sorted((Comparator.comparingInt(AggroInfo::getDamage).reversed()))
			.map(ai -> String.format("%s (ID: %d, Dmg: %d)", ai.getAttacker().getName(), ai.getAttacker().getObjectId(), ai.getDamage()))
			.collect(Collectors.joining(", "));

		log.info("[{}] {} (ID:{}) was killed in {}s. Damage List: {}", getPosition().getWorldMapInstance().getTemplate().getName(), getOwner().getName(),
			getNpcId(), fullFightTime, damageDealt);
	}
}
