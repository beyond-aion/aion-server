package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.model.templates.challenge.ChallengeQuestTemplate;
import com.aionemu.gameserver.model.templates.challenge.ChallengeTaskTemplate;

/**
 * @author ViAl
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "challenge_tasks")
public class ChallengeData {

	@XmlElement(name = "task")
	protected List<ChallengeTaskTemplate> task;

	@XmlTransient
	protected Map<Integer, ChallengeTaskTemplate> tasksById = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (ChallengeTaskTemplate t : task) {
			tasksById.put(t.getId(), t);
		}
	}

	public Map<Integer, ChallengeTaskTemplate> getTasks() {
		return tasksById;
	}

	public ChallengeTaskTemplate getTaskByTaskId(int taskId) {
		return tasksById.get(taskId);
	}

	public ChallengeTaskTemplate getTaskByQuestId(int questId) {
		for (ChallengeTaskTemplate ct : tasksById.values()) {
			for (ChallengeQuestTemplate cq : ct.getQuests())
				if (cq.getId() == questId)
					return ct;
		}
		return null;
	}

	public ChallengeQuestTemplate getQuestByQuestId(int questId) {
		for (ChallengeTaskTemplate ct : tasksById.values()) {
			for (ChallengeQuestTemplate cq : ct.getQuests())
				if (cq.getId() == questId)
					return cq;
		}
		return null;
	}

	public int size() {
		return tasksById.size();
	}
}
