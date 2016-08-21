package instance;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Ritsu
 * @modified Estrayl
 */

@InstanceID(301370000)
public class InfernalIlluminaryObelisk extends IlluminaryObeliskInstance {
	
	@Override
	protected int getBossId() {
		return 284858;
	}
	
	@Override
	protected void scheduleWipeTask() {
		wipeTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (!isInstanceDestroyed) {
					switch (wipeProgress) {
						case 0: //30min announce
						case 5: //25min announce
						case 10://20min announce
						case 15://15min announce
						case 20://10min announce
						case 25:// 5min announce
							sendMsg(wipeMsgProgress++);
							if (wipeProgress != 0) //no sense for assault at start
								scheduleAdditionalAttack(Rnd.get(1, 4));
							break;
						case 29:// 1min announce
							sendMsg(1402235);
							break;
						case 30:// wipe
							sendMsg(1402236);
							wipe();
							break;
						case 2:
						case 4:
						case 6:
						case 8:
						case 12:
						case 14:
						case 16:
						case 18:
						case 24:
						case 26:
						case 28:
							assault(Rnd.get(1, 4));
							break;
					}
					wipeProgress++;
				}
			}
		}, 0, 60 * 1000); //Repeat every minute
	}
	
	private void assault(int locId) {
		switch (locId) {
			case 1: //North
				sp(233723, 181.01f, 257.40f, 291.83f, (byte) 119, 0, "4_left_301230000");
				sp(233724, 180.83f, 252.54f, 291.83f, (byte) 119, 0, "4_right_301230000");
				sp(233725, 183.05f, 254.72f, 291.83f, (byte) 119, 0, "4_center_301230000");
				sp(233726, 181.01f, 257.40f, 291.83f, (byte) 119, 2000, "4_left_301230000");
				sp(233727, 180.83f, 252.54f, 291.83f, (byte) 119, 2000, "4_right_301230000");
				break;
			case 2: //South
				sp(233723, 329.78f, 251.68f, 291.83f, (byte) 60, 0, "3_left_301230000");
				sp(233724, 329.84f, 256.80f, 291.83f, (byte) 60, 0, "3_right_301230000");
				sp(233725, 328.09f, 254.24f, 291.83f, (byte) 60, 0, "3_center_301230000");
				sp(233726, 329.78f, 251.68f, 291.83f, (byte) 60, 2000, "3_left_301230000");
				sp(233727, 329.84f, 256.80f, 291.83f, (byte) 60, 2000, "3_right_301230000");
				break;
			case 3: //West
				sp(233723, 253.31f, 180.35f, 325.00f, (byte) 30, 0, "2_left_301230000");
				sp(233724, 257.56f, 180.41f, 325.00f, (byte) 30, 0, "2_right_301230000");
				sp(233725, 255.39f, 182.25f, 325.00f, (byte) 30, 0, "2_center_301230000");
				sp(233726, 253.31f, 180.35f, 325.00f, (byte) 30, 2000, "2_left_301230000");
				sp(233727, 257.56f, 180.41f, 325.00f, (byte) 30, 2000, "2_right_301230000");
				break;
			case 4: //East
				sp(233720, 257.31f, 328.03f, 325.00f, (byte) 91, 0, "1_left_301230000");
				sp(233721, 253.57f, 328.10f, 325.00f, (byte) 91, 0, "1_right_301230000");
				sp(233722, 255.40f, 326.54f, 325.00f, (byte) 91, 0, "1_center_301230000");
				sp(233723, 257.31f, 328.03f, 325.00f, (byte) 91, 2000, "1_left_301230000");
				sp(233724, 253.57f, 328.10f, 325.00f, (byte) 91, 2000, "1_right_301230000");
				break;
		}
	}
}
