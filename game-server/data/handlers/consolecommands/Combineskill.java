package consolecommands;

import java.io.File;
import java.util.List;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;
import com.aionemu.gameserver.utils.xml.JAXBUtil;

/**
 * @author ginho1
 */
public class Combineskill extends ConsoleCommand {

	public Combineskill() {
		super("combineskill", "Adds a skill.", """
			<skill name> <level> - Adds the skill with the level to your target (or yourself).
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 2) {
			sendInfo(admin);
			return;
		}

		final Player player = admin.getTarget() instanceof Player target ? target : admin;

		String skillName = params[0];
		int skillId = 0;
		int skillLvl = Integer.parseInt(params[1]);
		if (skillLvl < 1) {
			sendInfo(admin, "The skill level must be at least 1.");
			return;
		}

		File xml = new File("./data/handlers/consolecommands/data/skills.xml");
		SkillData data = JAXBUtil.deserialize(xml, SkillData.class);
		SkillTemplate skillTemplate = data.getSkillTemplate(skillName);

		if (skillTemplate != null)
			skillId = skillTemplate.getTemplateId();

		if (skillId <= 0) {
			sendInfo(admin, "There is no skill named " + skillName + ".");
			return;
		}
		if (player.getSkillList().addSkill(player, skillId, skillLvl))
			sendInfo(admin, "The skill was added successfully.");
		else
			sendInfo(admin, "The player already has this skill at the same or a higher level.");
	}

	@XmlAccessorType(XmlAccessType.NONE)
	@XmlType(namespace = "", name = "SkillTemplate")
	private static class SkillTemplate {

		@XmlAttribute(name = "id", required = true)
		@XmlID
		private String id;

		@XmlAttribute(name = "name")
		private String name;

		public String getName() {
			return name;
		}

		public int getTemplateId() {
			return skillId;
		}

		private int skillId;

		public void setSkillId(int skillId) {
			this.skillId = skillId;
		}

		@SuppressWarnings("unused")
		void afterUnmarshal(Unmarshaller u, Object parent) {
			setSkillId(Integer.parseInt(id));
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
