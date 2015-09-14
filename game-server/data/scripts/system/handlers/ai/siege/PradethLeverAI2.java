package ai.siege;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.ActionItemNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.knownlist.Visitor;


/**
 * @author Whoop
 *
 */
@AIName("pradeth_lever")
public class PradethLeverAI2 extends ActionItemNpcAI2 {
			
	private AtomicBoolean isRewarded = new AtomicBoolean(false);

	@Override
	protected void handleUseItemStart(Player player) {
		
		FortressLocation fLoc = SiegeService.getInstance().getFortress(6021);
		
		if ((player.getRace().equals(Race.ASMODIANS) && fLoc.getRace().equals(SiegeRace.ASMODIANS)) 
				|| (player.getRace().equals(Race.ELYOS) && fLoc.getRace().equals(SiegeRace.ELYOS))) {
			return;
		}
		super.handleUseItemStart(player);
	}
	
	@Override
	protected void handleUseItemFinish(Player player) {
		if (isRewarded.compareAndSet(false, true)) {
			AI2Actions.handleUseItemFinish(this, player);			
			
			switch(getNpcId()) {
				case 701785: //NorthWest Leaver for Elyos (destroys Northwest Main Gate)
					scheduleGateDestruction(getNpc(273286), 1401690);
					break;
				case 701783: //NorthWest Leaver for Asmodians (destroys Northwest Main Gate)
					scheduleGateDestruction(getNpc(273285), 1401690);
					break;
				case 701786: //SouthWest Leaver for Elyos (destroys Southwest Rear Gate)
					scheduleGateDestruction(getNpc(273289), 1401691);
					break;
				case 701784: //SouthWest Leaver for Asmodians (destroys Southwest Rear Gate)
					scheduleGateDestruction(getNpc(273288), 1401691);
					break;
				case 701705: //Northern Leaver (damages eastern inner gate)
					scheduleGateDamage(getNpc(player.getRace().equals(Race.ASMODIANS) ? 273294 : 273295), 1401694, 1401692);
					break;
				case 701706: //Western Leaver (damages western outer gate)
					scheduleGateDamage(getNpc(player.getRace().equals(Race.ASMODIANS) ? 273297 : 273298), 1401695, 1401699);
					break;
				case 701707: //Southern Leaver (damages western inner gate)
					scheduleGateDamage(getNpc(player.getRace().equals(Race.ASMODIANS) ? 273291 : 273292), 1401696, 1401693);
					break;
				case 701708: //Eastern Lever (damages eastern outer gate)
					scheduleGateDamage(getNpc(player.getRace().equals(Race.ASMODIANS) ? 273300 : 273301), 1401697, 1401702);
					break;
			}
		}
		AI2Actions.deleteOwner(this);
	}
	
	private void scheduleGateDamage(final Npc gate, final int firstMsg, final int finalMsg) {
		if (gate == null)
			return;
		
		if (!gate.getLifeStats().isAlreadyDead()) {
			announceExplosions(firstMsg);
			ThreadPoolManager.getInstance().schedule(new Runnable() {
				@Override
				public void run() {
					if (!gate.getLifeStats().isAlreadyDead()) {
						gate.getController().onAttack(gate, (int) Math.round(gate.getLifeStats().getMaxHp() * 0.5), true);
						announceExplosions(finalMsg);
					}
				}
			}, 10000);			
		}
	}
	
	private void scheduleGateDestruction(final Npc gate, final int MsgId) {
		if (gate == null)
			return;
		if (!gate.getLifeStats().isAlreadyDead()) {
			gate.getController().onDelete();
			announceExplosions(MsgId);
		}
	}
	
	private void announceExplosions(final int msgId) {
		SiegeLocation siegeLocation = SiegeService.getInstance().getSiegeLocation(((SiegeNpc) getOwner()).getSiegeId());
		
		siegeLocation.doOnAllPlayers(new Visitor<Player>() {

			@Override
			public void visit(Player player) {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(msgId));
			}
		});
	}

	private Npc getNpc(int npcId) {
		return getPosition().getWorldMapInstance().getNpc(npcId);
	}
}
