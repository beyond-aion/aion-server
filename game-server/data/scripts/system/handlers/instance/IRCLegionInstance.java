package instance;

import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;


/**
 * @author Cheatkiller
 *
 */
@InstanceID(301170000)
public class IRCLegionInstance extends IdgelResearchCenterInstance {
	
	@Override
	public void onDie(Npc npc) {
		super.onDie(npc);
		//TODO
	}
	
	@Override
	protected int checkRank(int totalPoints) {
		//TODO
		return 0;
	}
}
