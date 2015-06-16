package ai.worlds.tiamaranta.ativasCristalline;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author Ritsu
 */

@AIName("ativascristalline")
public class AtivasCristallineAI2 extends AggressiveNpcAI2
{

	private AtomicBoolean isStart90Event = new AtomicBoolean(false);
	private AtomicBoolean isStart60Event = new AtomicBoolean(false);
	private AtomicBoolean isStart30Event = new AtomicBoolean(false);
	private AtomicBoolean isStart10Event = new AtomicBoolean(false);

	@Override
	protected void handleAttack(Creature creature)
	{
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	@Override
	protected void handleBackHome() 
	{
		isStart90Event.set(false);
		isStart60Event.set(false);
		isStart30Event.set(false);
		isStart10Event.set(false);
		super.handleBackHome();
	}

	private void checkPercentage(int hpPercentage)
	{
		if (hpPercentage <= 90)
		{
			if (isStart90Event.compareAndSet(false, true)) {
				topazKomad();
			}
		}
		else if (hpPercentage <= 60)
		{
			if (isStart60Event.compareAndSet(false, true)) {
				garnetKomad();
			}
		}
		else if (hpPercentage <= 30)
		{
			if (isStart30Event.compareAndSet(false, true)) {
				topazKomad();
			}
		}
		else if (hpPercentage <= 10)
		{
			if (isStart10Event.compareAndSet(false, true)) {
				garnetKomad();
			}
		}
	}

	private void garnetKomad() {
		if (getPosition().isSpawned() && !isAlreadyDead())
		{
			for (int i = 0; i < 1; i++) {
				int distance = Rnd.get(3, 5);
				int nrNpc = Rnd.get(1, 0);
				switch (nrNpc) {
					case 1:
						nrNpc = 282708; //Garnet Komad.
						break;
				}
				rndSpawnInRange(nrNpc, distance);
			}
		}
	}

	private void topazKomad() {
		if (getPosition().isSpawned() && !isAlreadyDead())
		{
			for (int i = 0; i < 1; i++) {
				int distance = Rnd.get(3, 5);
				int nrNpc = Rnd.get(1, 0);
				switch (nrNpc) {
					case 1:
						nrNpc = 282709; //Topaz Komad.
						break;
				}
				rndSpawnInRange(nrNpc, distance);
			}
		}
	}

	private void rndSpawnInRange(int npcId, float distance) {
		float direction = Rnd.get(0, 199) / 100f;
		float x1 = (float) (Math.cos(Math.PI * direction) * distance);
		float y1 = (float) (Math.sin(Math.PI * direction) * distance);
		spawn(npcId, getPosition().getX() + x1, getPosition().getY() + y1, getPosition().getZ(), (byte) 0);
	}
}