package consolecommands;

import java.io.File;
import java.util.List;

import jakarta.xml.bind.annotation.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;
import com.aionemu.gameserver.utils.xml.JAXBUtil;

/**
 * @author ginho1
 */
public class Addskill extends ConsoleCommand {

	public Addskill() {
		super("addskill", "Adds a skill.", """
			<skill name> - Adds the skill to your target (or yourself).
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			sendInfo(admin);
			return;
		}

		final Player player = admin.getTarget() instanceof Player target ? target : admin;

		String skillName = params[0];
		int skillId = 0;

		File xml = new File("./data/handlers/consolecommands/data/skills.xml");
		SkillData data = JAXBUtil.deserialize(xml, SkillData.class);
		SkillTemplate skillTemplate = data.getSkillTemplate(skillName);

		if (skillTemplate != null)
			skillId = skillTemplate.getSkillId();

		if (skillId <= 0) {
			sendInfo(admin, "There is no skill named " + skillName + ".");
			return;
		}
		if (player.getSkillList().addSkill(player, skillId, 1))
			sendInfo(admin, "The skill was added successfully.");
		else
			sendInfo(admin, "The player already has this skill.");
	}

	@XmlRootElement(name = "skill")
	@XmlAccessorType(XmlAccessType.FIELD)
	private static class SkillTemplate {

		@XmlAttribute(name = "id", required = true)
		private int skillId;

		@XmlAttribute(name = "name")
		private String name;

		public String getName() {
			return name;
		}

		public int getSkillId() {
			return skillId;
		}

	}

	@XmlRootElement(name = "skills")
	@XmlAccessorType(XmlAccessType.FIELD)
	private static class SkillData {

		@XmlElement(name = "skill")
		private List<SkillTemplate> its;

		public SkillTemplate getSkillTemplate(String skill) {

			for (SkillTemplate it : getData()) {
				if (it.getName().toLowerCase().equals(skill.toLowerCase()))
					return it;
			}
			return null;
		}

		protected List<SkillTemplate> getData() {
			return its;
		}
	}
}
