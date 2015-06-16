package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.challenge.ChallengeQuestTemplate;
import com.aionemu.gameserver.model.templates.challenge.ChallengeTaskTemplate;

/**
 * 
 * @author ViAl
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "task" })
@XmlRootElement(name = "challenge_tasks")
public class ChallengeData {

	protected List<ChallengeTaskTemplate> task;
	
	@XmlTransient
	protected Map<Integer, ChallengeTaskTemplate> tasksById = new HashMap<Integer, ChallengeTaskTemplate>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (ChallengeTaskTemplate t : task) {
			tasksById.put(t.getId(), t);
		}
		task.clear();
		task = null;
	}

	public Map<Integer, ChallengeTaskTemplate> getTasks() {
		return this.tasksById;
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
		return this.tasksById.size();
	}
}
