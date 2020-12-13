package ai.instance.sauroBase;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl, Yeats
 */
@AIName("brigade_general_sheba")
public class BrigadeGeneralShebaAI extends AggressiveNpcAI {

	public BrigadeGeneralShebaAI(Npc owner) {
		super(owner);
	}

	private float multiplier = 1f;
	private boolean spawnCenter = true;

	@Override
	public float modifyOwnerDamage(float damage, Creature effected, Effect effect) {
		return damage * multiplier;
	}

	@Override
	public void onStartUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 21188:
				multiplier = 0.55f;
				PacketSendUtility.broadcastMessage(getOwner(),1500775);
				break;
			case 21183:
				PacketSendUtility.broadcastMessage(getOwner(), 1500779);
			case 21187:
			case 21410:
			case 21425:
			case 21450:
				multiplier = 0.55f;
				break;
			case 21186:
				PacketSendUtility.broadcastMessage(getOwner(), 1500780);
				multiplier = 0.55f;
				break;
			case 21189:
				PacketSendUtility.broadcastMessage(getOwner(),1500774);
				break;
		}
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		multiplier = 1f;
		switch (skillTemplate.getSkillId()) {
			case 21189: // danuar henchman
				spawn(284435, 891.17f, 880.87f, 411.54f, (byte) 14);
				spawn(284435,890.8f, 898.97f, 411.46f, (byte) 105);
				spawn(284435, 904.95f, 880.1f, 411.34f, (byte) 40);
				spawn(284435, 905.95f, 900.22f, 411.34f, (byte) 80);
				break;
			case 21184: // danuar channeling
				if (spawnCenter) {
					spawn(284436, 900.1f, 889.84f, 411.54f, (byte) 20);
					spawnCenter = false;
				} else {
					spawn(284436, 887.63f, 902.31f, 411.46f, (byte) 105);
					spawn(284436, 882.2f, 889.77f, 411.46f, (byte) 120);
					spawn(284436, 887.59f, 877.32f, 411.46f, (byte) 15);
					spawn(284436, 900f, 872.28f, 411.46f, (byte) 30);
					spawn(284436, 912.39f, 877.44f, 411.46f, (byte) 45);
					spawn(284436, 917.2f, 889.89f, 411.46f, (byte) 60);
					spawn(284436, 912.33f, 902f, 411.46f, (byte) 75);
					spawn(284436, 900f, 907.16f, 411.46f, (byte) 90);
					spawnCenter = true;
				}
				break;
			case 21186:
				PacketSendUtility.broadcastMessage(getOwner(), 1500781);
				break;
		}
	}

	@Override
	protected void handleBackHome() {
		removeAdds();
		super.handleBackHome();
	}

	@Override
	protected void handleDied() {
		removeAdds();
		super.handleDied();
	}

	private void removeAdds() {
		for (VisibleObject obj : getKnownList().getKnownObjects().values()) {
			if (obj instanceof Npc) {
				if (((Npc) obj).getNpcId() == 284435 || ((Npc) obj).getNpcId() == 284436) {
					((Npc) obj).getController().delete();
				}
			}
		}
	}
}
