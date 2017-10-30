package ai.worlds.panesterra.ahserionsflight;

import java.util.HashMap;
import java.util.Map;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionRaid;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraFaction;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraTeam;

import ai.AggressiveNpcAI;

/**
 * @author Yeats
 * @modified Estrayl October 29th, 2017.
 */
@AIName("ahserion")
public class Ahserion extends AggressiveNpcAI {

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
						Integer dmg = panesterraDamage.get(faction);
						if (dmg != null)
							panesterraDamage.put(faction, dmg + ai.getDamage());
						else
							panesterraDamage.put(faction, ai.getDamage());
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
}
