package admincommands;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AbstractAI;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * Created on September 26th, 2017.
 * 
 * @author Estrayl
 * @since Beyond AION 4.8
 */
public class ForceSkill extends AdminCommand {

	public ForceSkill() {
		super("forceskill", "Forces target (or self if nothing is selected) to use parameterized skill.");
	}

	@Override
	protected void execute(Player player, String... params) {
		if (params.length == 0) {
			sendInfo(player);
			return;
		}

		SkillTemplate template;
		try {
			template = validateSkill(params[0]);
		} catch (NumberFormatException nfe) {
			sendInfo(player, "Invalid skill id.");
			return;
		} catch (NullPointerException npe) {
			sendInfo(player, "Skill id does not exist.");
			return;
		}

		VisibleObject target = player.getTarget();
		if (target instanceof Creature && !(target instanceof Player))
			forceSkillThroughAI((Creature) target, template);
		else
			SkillEngine.getInstance().getSkill(player, template.getSkillId(), template.getLvl(), player).useSkill();
	}

	private void forceSkillThroughAI(Creature aiOwner, SkillTemplate template) {
		AbstractAI ai = (AbstractAI) aiOwner.getAi();
		AIActions.useSkill(ai, template.getSkillId());
	}

	private SkillTemplate validateSkill(String paramSkillId) throws NumberFormatException, NullPointerException {
		SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(Integer.parseInt(paramSkillId));
		if (skillTemplate == null)
			throw new NullPointerException();
		return skillTemplate;
	}

}
