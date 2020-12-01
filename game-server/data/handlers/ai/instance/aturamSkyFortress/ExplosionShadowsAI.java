package ai.instance.aturamSkyFortress;

import java.util.function.Consumer;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("explosion_shadows")
public class ExplosionShadowsAI extends AggressiveNpcAI {

	public ExplosionShadowsAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		getPosition().getWorldMapInstance().setDoorState(2, false); // this actually opens it on client side (wtf)
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		getPosition().getWorldMapInstance().setDoorState(17, false); // this actually opens it on client side (wtf)
		getPosition().getWorldMapInstance().setDoorState(2, false); // this actually opens it on client side (wtf)
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		super.onEndUseSkill(skillTemplate, skillLevel);
		if (skillTemplate.getSkillId() == 19425) // Self Destruct
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
