package ai.instance.abyss;

import java.util.concurrent.Future;

import ai.NoActionAI2;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * Created on June 24th, 2016
 *
 * @author Estrayl
 * @since AION 4.8
 */
@AIName("illusion_gate")
public class IllusionGateAI2 extends NoActionAI2 {

	private Future<?> spawnTask;

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		spawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			for (int i = 0; i < 2; i++) {
				Point3D p = getRndPosition();
				spawn(getNpcId() + Rnd.get(1, 3), p.getX(), p.getY(), p.getZ(), (byte) 0);
			}
		}, 10000, 30000);
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		cancelTask();
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelTask();
	}

	private void cancelTask() {
		if (spawnTask != null && !spawnTask.isCancelled())
			spawnTask.cancel(true);
	}

	private Point3D getRndPosition() {
		float direction = Rnd.get(0, 199) / 100f;
		float distance = Rnd.get();
		float x1 = (float) (Math.cos(Math.PI * direction) * distance);
		float y1 = (float) (Math.sin(Math.PI * direction) * distance);
		WorldPosition p = getPosition();
		return new Point3D(p.getX() + x1, p.getY() + y1, p.getZ());
	}

}
