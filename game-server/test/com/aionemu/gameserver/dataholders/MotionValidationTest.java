package com.aionemu.gameserver.dataholders;

import static org.junit.jupiter.api.Assertions.*;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.TransformModel;
import com.aionemu.gameserver.model.stats.container.NpcGameStats;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class MotionValidationTest {
	private MotionData parse(String xml) throws Exception {
		return (MotionData) JAXBContext.newInstance(MotionData.class).createUnmarshaller().unmarshal(new StringReader(xml));
	}

	private Path skillsDirectory() {
		Path path = Path.of("data/static_data/skills");
		return Files.isDirectory(path) ? path : Path.of("game-server").resolve(path);
	}

	@Test
	void recoveredDataLoadsAndRealGapsRemainVisible() throws Exception {
		Path directory = skillsDirectory();
		SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
			.newSchema(directory.resolve("motion_times.xsd").toFile()).newValidator()
			.validate(new StreamSource(directory.resolve("motion_times.xml").toFile()));
		MotionData motions = parse(Files.readString(directory.resolve("motion_times.xml")));
		SkillData skills = (SkillData) JAXBContext.newInstance(SkillData.class).createUnmarshaller()
			.unmarshal(directory.resolve("skill_templates.xml").toFile());
		var missing = new java.util.TreeSet<String>();
		for (var skill : skills.getSkillTemplates()) {
			var motion = skill.getMotion();
			if (motion != null && motion.getName() != null && !motion.isInstantSkill() && motions.getMotionTime(motion.getName()) == null)
				missing.add(motion.getName());
		}
		assertEquals(java.util.Set.of("areafireod", "normalfiremo", "open", "pointfirerh"), missing);
		assertNotNull(motions.getMotionTime("sumrobot"));
		assertNotNull(motions.getMotionTime("herb"));
		var giant = motions.getMotionTime("areaatklf").getTimesForNpc(286163, 1);
		assertEquals(3.06666666f, giant.getMinTime(), 0.00001f);
		assertEquals(5.5f, giant.getAnimationLength());
		assertTrue(giant.hasHitpoints());
		assertNull(motions.getMotionTime("areaatklf").getTimesForNpc(219362, 1));
		var breath = motions.getMotionTime("breathl").getTimesForNpc(219362, 1);
		assertEquals(1.419f, breath.getMinTime());
		assertFalse(breath.isCastAnimation());
		var cast = motions.getMotionTime("breathl4s").getTimesForNpc(219362, 1);
		assertEquals(4f, cast.getAnimationLength());
		assertTrue(cast.isCastAnimation());
		assertFalse(cast.hasHitpoints());
	}

	@Test
	void missingNonInstantNamesStillWarnOnce() throws Exception {
		MotionData previous = DataManager.MOTION_DATA;
		Logger logger = (Logger) LoggerFactory.getLogger(SkillData.class);
		Level previousLevel = logger.getLevel();
		ListAppender<ILoggingEvent> appender = new ListAppender<>();
		appender.start();
		logger.addAppender(appender);
		logger.setLevel(Level.WARN);
		try {
			DataManager.MOTION_DATA = parse("<motion_times><motion_time name=\"timed\"/></motion_times>");
			SkillData skills = (SkillData) JAXBContext.newInstance(SkillData.class).createUnmarshaller().unmarshal(new StringReader("""
				<skill_data>
				  <skill_template skill_id="1"><motion name="timed"/></skill_template>
				  <skill_template skill_id="2"><motion name="native_instant" instant_skill="true"/></skill_template>
				  <skill_template skill_id="3"><motion name="missing"/></skill_template>
				  <skill_template skill_id="4"><motion name="missing"/></skill_template>
				</skill_data>
				"""));
			skills.validateMotions();
			assertEquals(1, appender.list.size());
			assertEquals("Missing motion times for these motion names: \"missing\" (skill id 3)", appender.list.getFirst().getFormattedMessage());
		} finally {
			DataManager.MOTION_DATA = previous;
			logger.detachAppender(appender);
			logger.setLevel(previousLevel);
			appender.stop();
		}
	}

	@Test
	void npcEffectsUseModelHitMarkersAndSpeedWithoutDelayingInstantSkills() throws Exception {
		MotionData previous = DataManager.MOTION_DATA;
		try {
			DataManager.MOTION_DATA = parse(Files.readString(skillsDirectory().resolve("motion_times.xml")));
			NpcTemplate template = (NpcTemplate) JAXBContext.newInstance(NpcTemplate.class).createUnmarshaller()
				.unmarshal(new StringReader("<npc_template npc_id=\"286163\" level=\"1\"/>"));
			Creature giant = new Creature(1, null, null, template, null, false) {
				@Override public byte getLevel() { return 1; }
				@Override public NpcGameStats getGameStats() {
					return new NpcGameStats(null) {
						@Override public float getAttackSpeedRate() { return 0.5f; }
					};
				}
			};
			var skillContext = JAXBContext.newInstance(SkillTemplate.class);
			class ProbeSkill extends Skill {
				ProbeSkill(String motion) throws Exception {
					this(motion, giant);
				}
				ProbeSkill(String motion, Creature actor) throws Exception {
					super(skillContext.createUnmarshaller().unmarshal(new StreamSource(new StringReader(
						"<skill_template skill_id=\"1\"><motion " + motion + "/></skill_template>")), SkillTemplate.class).getValue(), actor, 1, null, null);
				}
				void update(boolean check) { updateHitTime(check); }
			}
			var attack = new ProbeSkill("name=\"areaatklf\"");
			attack.update(true);
			assertEquals(1533, attack.getHitTime());
			var instant = new ProbeSkill("name=\"areaatklf\" instant_skill=\"true\"");
			instant.update(true);
			assertEquals(0, instant.getHitTime());
			var unchecked = new ProbeSkill("name=\"areaatklf\"");
			unchecked.update(false);
			assertEquals(0, unchecked.getHitTime());
			var unmapped = new ProbeSkill("name=\"breathl\"");
			unmapped.update(true);
			assertEquals(0, unmapped.getHitTime());
			Creature transformed = new Creature(2, null, null, template, null, false) {
				@Override public byte getLevel() { return 1; }
				@Override public TransformModel getTransformModel() {
					return new TransformModel(this) {
						@Override public int getModelId() { return 219362; }
					};
				}
				@Override public NpcGameStats getGameStats() {
					return new NpcGameStats(null) {
						@Override public float getAttackSpeedRate() { return 1f; }
					};
				}
			};
			var transformedAttack = new ProbeSkill("name=\"breathl\"", transformed);
			transformedAttack.update(true);
			assertEquals(1419, transformedAttack.getHitTime());
			var castOnly = new ProbeSkill("name=\"breathl4s\"", transformed);
			castOnly.update(true);
			assertEquals(0, castOnly.getHitTime()); // cast clip must not become another four-second fire delay
		} finally {
			DataManager.MOTION_DATA = previous;
		}
	}
}
