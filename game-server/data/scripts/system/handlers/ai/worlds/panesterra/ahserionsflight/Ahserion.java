package ai.worlds.panesterra.ahserionsflight;

import java.util.HashMap;
import java.util.Map;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionInstance;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionInstanceStatus;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraTeamId;

import ai.AggressiveNpcAI2;

/**
 * @author Yeats
 *
 */
@AIName("ahserion")
public class Ahserion extends AggressiveNpcAI2 {

	
	@Override
	public void handleAttack(Creature creature) {
		super.handleAttack(creature);
	}

	@Override
	protected void handleDied() {
		if (getOwner().getWorldId() == 400030000) {
			if (AhserionInstance.getInstance().isStarted() && AhserionInstance.getInstance().getStatus() == AhserionInstanceStatus.INSTANCE_RUNNING) {
				Map<PanesterraTeamId, Integer> panesterraDamage = new HashMap<>();
		
				//Only players can attack Ahserion on this map.
				for (AggroInfo ai : getOwner().getAggroList().getFinalDamageList(false)) {
					if (ai.getAttacker() instanceof Player) { 
						Player attacker = (Player) ai.getAttacker();
						if (attacker.getPanesterraTeam() != null && !attacker.getPanesterraTeam().isEliminated()) {
							PanesterraTeamId teamId = attacker.getPanesterraTeam().getTeamId();
						
							if (panesterraDamage.containsKey(teamId)) {
								int curDamage = panesterraDamage.get(teamId);
								panesterraDamage.put(teamId, curDamage + ai.getDamage());
							} else {
								panesterraDamage.put(attacker.getPanesterraTeam().getTeamId(), ai.getDamage());
							}
						}
					}
				}
				getWinnerTeam(panesterraDamage);
			}
		}
		super.handleDied();
	}
	
	/**
	 * @param panesterraDamage
	 */
	private void getWinnerTeam(Map<PanesterraTeamId, Integer> panesterraDamage) {
		PanesterraTeamId winner = PanesterraTeamId.BALAUR;
		int maxDmg = 0;
		if (panesterraDamage.containsKey(PanesterraTeamId.GAB1_SUB_DEST_69) 
			&& AhserionInstance.getInstance().isTeamNotEliminated(PanesterraTeamId.GAB1_SUB_DEST_69)) {
			if (panesterraDamage.get(PanesterraTeamId.GAB1_SUB_DEST_69) > maxDmg) {
				maxDmg = panesterraDamage.get(PanesterraTeamId.GAB1_SUB_DEST_69);
				winner = PanesterraTeamId.GAB1_SUB_DEST_69;
			}
		}
		if (panesterraDamage.containsKey(PanesterraTeamId.GAB1_SUB_DEST_70) 
			&& AhserionInstance.getInstance().isTeamNotEliminated(PanesterraTeamId.GAB1_SUB_DEST_70)) {
			if (panesterraDamage.get(PanesterraTeamId.GAB1_SUB_DEST_70) > maxDmg) {
				maxDmg = panesterraDamage.get(PanesterraTeamId.GAB1_SUB_DEST_70);
				winner = PanesterraTeamId.GAB1_SUB_DEST_70;
			}
		}
		if (panesterraDamage.containsKey(PanesterraTeamId.GAB1_SUB_DEST_71) 
			&& AhserionInstance.getInstance().isTeamNotEliminated(PanesterraTeamId.GAB1_SUB_DEST_71)) {
			if (panesterraDamage.get(PanesterraTeamId.GAB1_SUB_DEST_71) > maxDmg) {
				maxDmg = panesterraDamage.get(PanesterraTeamId.GAB1_SUB_DEST_71);
				winner = PanesterraTeamId.GAB1_SUB_DEST_71;
			}
		}
		if (panesterraDamage.containsKey(PanesterraTeamId.GAB1_SUB_DEST_72) 
			&& AhserionInstance.getInstance().isTeamNotEliminated(PanesterraTeamId.GAB1_SUB_DEST_72)) {
			if (panesterraDamage.get(PanesterraTeamId.GAB1_SUB_DEST_72) > maxDmg) {
				maxDmg = panesterraDamage.get(PanesterraTeamId.GAB1_SUB_DEST_72);
				winner = PanesterraTeamId.GAB1_SUB_DEST_72;
			}
		}
		AhserionInstance.getInstance().bossKilled(getOwner(), winner);
	}
}
