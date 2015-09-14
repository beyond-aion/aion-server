package instance;

import com.aionemu.gameserver.instance.handlers.InstanceID;

/**
 * @author Cheatkiller
 */
@InstanceID(301330000)
public class DanuarReliquaryInstance_L extends DanuarReliquaryInstance {

	@Override
	protected void finalSpawn() {
		spawn(730843, 255.66669f, 263.78525f, 241.7986f, (byte) 86); // Spawn exit portal
		spawn(802183, 256.65f, 258.09f, 241.78f, (byte) 100);
		// TODO
	}
}
