package ai.worlds.kaldor;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Ritsu
 *
 */
@AIName("berserk_anoha")
public class BerserkAnohaAI2 extends AggressiveNpcAI2 
{

	@Override
	protected void handleDied() 
	{
		for (final Player player : getOwner().getKnownList().getKnownPlayers().values()) 
		{
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402504)); // Msg Anoha has been vanquished
		}
		World.getInstance().doOnAllPlayers(new Visitor<Player>() 
			{
			@Override
			public void visit(Player player) 
			{
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402505)); // Msg Anoha died
			}
			});
		super.handleDied();
	}
}

