package ai.instance.drakenspire;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * Q3 goals:
 * Easy mode: 236244
 * Some ordinary boss without dps check or special mechanics, just DPS/heal for win
 * All special skills included: black tentacle areas, Y-Shape AoE, Hide teleports/jumps, player morphs
 * Normal mode: 236245
 * Beritra starts with 3 buffs, which can be removed with bombs (caster dudes on platforms)
 * After buff removal, Beritra can be damaged and will get a new buff every 90-120s
 * Boss time should (maybe) be limited to 8-10min
 * Q4 goal:
 * New stats and damage formulas for all NPCs/skills
 * Changes to existing modes:
 * Easy mode should get a time limit too
 * Normal mode some add spawn events which occurs in hard mode
 * Hard mode: 236246+236247
 * Starts like normal mode, except after removing all 3 buffs he will transform into a dragon (players will be paralyzed)
 * Time limit starts after transforming
 * Player morphs and hide teleports/jumps will not occur anymore
 * Beritra will cast Balaur Lord's Authority after 60s. Players need to kill the spawning seal guardian and explode the seal on Beritra.
 * After the seal explosion Balaur Lord's Authority will be cast every 120s.
 * Also after the first seal explosion Minion of Darkness will spawn every 30s. (They'll explode after 20s)
 * Additional darkness minions will spawn after the second and third seal explosion shifted by 10s each
 * Future stuff:
 * challenge mode?
 * //moveto 301390000 170 530 1750
 * Adds: 855444 - 855446
 */
@AIName("beritra")
public class BeritraAI extends AggressiveNpcAI {

	public BeritraAI(Npc owner) {
		super(owner);
	}

	private void handlePulseWave() {
		Npc skill1 = (Npc) spawn(855742, 152f, 519f, 1749.6f, (byte) 0);
		Npc skill2 = (Npc) spawn(855742, 152f, 519f, 1749.6f, (byte) 0);
		Npc skill3 = (Npc) spawn(855742, 152f, 519f, 1749.6f, (byte) 0);

		Npc slave1 = (Npc) spawn(855740, 160f, 494f, 1749.9f, (byte) 0);
		Npc slave2 = (Npc) spawn(855740, 160f, 543f, 1749.9f, (byte) 0);
		Npc slave3 = (Npc) spawn(855740, 125f, 517f, 1749.9f, (byte) 0);

		ThreadPoolManager.getInstance().schedule(() -> {
			SkillEngine.getInstance().getSkill(skill1, 21828, 1, slave1).useNoAnimationSkill();
			SkillEngine.getInstance().getSkill(skill2, 21828, 1, slave2).useNoAnimationSkill();
			SkillEngine.getInstance().getSkill(skill3, 21828, 1, slave3).useNoAnimationSkill();
		}, 750);
		ThreadPoolManager.getInstance().schedule(() -> despawnSkillHelper(skill1, skill2, skill3, slave1, slave2, slave3), 8000);
	}

	private void handleSoulExtinctionFields() {
		List<Player> playersInRange = getKnownList().getKnownPlayers().values().stream()
			.filter(player -> !player.isDead() && PositionUtil.isInRange(player, getOwner(), 25)).collect(Collectors.toList());
		List<Npc> spawnedFields = new ArrayList<>();
		if (playersInRange.size() <= 3)
			playersInRange.forEach(p -> spawnedFields.add((Npc) spawn(855741, p.getX(), p.getY(), p.getZ(), (byte) 0)));
		else {
			for (int i = 0; i < 3; i++) {
				Player p = Rnd.get(playersInRange);
				if (p != null) {
					spawnedFields.add((Npc) spawn(855741, p.getX(), p.getY(), p.getZ(), (byte) 0));
					playersInRange.remove(p);
				}
			}
		}
		ThreadPoolManager.getInstance()
			.schedule(() -> spawnedFields.forEach(field -> SkillEngine.getInstance().getSkill(field, 21823, 1, field).useNoAnimationSkill()), 750);
		ThreadPoolManager.getInstance().schedule(() -> spawnedFields.forEach(field -> field.getController().delete()), 8000);
	}

	@Override
	public void onStartUseSkill(SkillTemplate st, int level) {
		// TODO: 21602 Dimensional Wave => 856300 skill npc
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		// 20842 form switch paralyze
		switch (skillTemplate.getSkillId()) {
			case 21609 -> handleSoulExtinctionFields(); // Soul Extinction Field
			case 21601 -> handlePulseWave(); // Pulse Wave
		}
	}

	private void despawnSkillHelper(Npc... npcs) {
		for (Npc npc : npcs)
			if (npc != null)
				npc.getController().delete();
	}
}
