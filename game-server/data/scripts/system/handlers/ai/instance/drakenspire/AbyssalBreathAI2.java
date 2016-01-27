package ai.instance.drakenspire;

import ai.GeneralNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Estrayl
 */
@AIName("abyssal_breath")
public class AbyssalBreathAI2 extends GeneralNpcAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			
			@Override
			public void run() {
				SkillEngine.getInstance().getSkill(getOwner(), 21620, 1, getOwner()).useSkill();
				ThreadPoolManager.getInstance().schedule(new Runnable() {
					
					@Override
					public void run() {
						getOwner().getKnownList().doOnAllPlayers(new Visitor<Player>() {
							
							@Override
							public void visit(Player player) {
								if (isInRange(player, 11))
									SkillEngine.getInstance().getSkill(getOwner(), 21874, 1, player).useSkill();
							}
						});
					}
				}, 4250);
			}
		}, 4000);
	}
}
