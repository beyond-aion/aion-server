package ai.worlds.brusthonin;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author Cheatkiller
 */

@AIName("unfaithfulntuamu")
public class UnfaithfulNtuamuAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private void checkPercentage(int hpPercentage) {
		if (hpPercentage <= 50) {
			spawn(214583, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) getOwner().getHeading());
			AI2Actions.deleteOwner(this);
		}
	}
}