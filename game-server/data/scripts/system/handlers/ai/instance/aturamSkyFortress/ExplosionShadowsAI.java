package ai.instance.aturamSkyFortress;

import java.util.function.Consumer;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("explosion_shadows")
public class ExplosionShadowsAI extends AggressiveNpcAI {

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		getPosition().getWorldMapInstance().getDoors().get(17).setOpen(true);
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		getPosition().getWorldMapInstance().getDoors().get(17).setOpen(true);
		getPosition().getWorldMapInstance().getDoors().get(2).setOpen(true);
	}

	@Override
	public void onEndUseSkill(NpcSkillEntry usedSkill) {
		super.onEndUseSkill(usedSkill);
		if (usedSkill.getSkillId() == 19425) // Self Destruct
			ThreadPoolManager.getInstance().schedule(() -> check(), 1500);
	}

	private void check() {
		if (!isDead()) {
			getKnownList().forEachPlayer(new Consumer<Player>() {

				@Override
				public void accept(Player player) {
					if (player.getEffectController().hasAbnormalEffect(19502)) {
						Npc npc = (Npc) spawn(799657, player.getX(), player.getY(), player.getZ(), player.getHeading());
						PacketSendUtility.sendPacket(player, new SM_EMOTION(npc, EmotionType.DIE));
						player.getEffectController().removeEffect(19502);
					}
				}
			});
			AIActions.deleteOwner(this);
		}
	}
}
