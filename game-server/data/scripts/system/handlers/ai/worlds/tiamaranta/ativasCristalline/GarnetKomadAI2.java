package ai.worlds.tiamaranta.ativasCristalline;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Ritsu
 */

@AIName("garnetKomad")
public class GarnetKomadAI2 extends AggressiveNpcAI2
{
	@Override
	protected void handleSpawned()
	{
		super.handleSpawned();
		int lifetime = (getNpcId() == 282708 ? 20000 : 10000);
		toDespawn(lifetime);
	}

	private void toDespawn(int delay)
	{
		ThreadPoolManager.getInstance().schedule(new Runnable()
		{
			@Override
			public void run()
			{
				AI2Actions.deleteOwner(GarnetKomadAI2.this);
			}
		}, delay);
	}
}