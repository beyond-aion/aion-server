package com.aionemu.gameserver.configs.schedule;

import java.io.File;
import java.util.List;

import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.rift.OpenRift;
import com.aionemu.gameserver.utils.xml.JAXBUtil;

/**
 * @author Source
 */
@XmlRootElement(name = "rift_schedule")
@XmlAccessorType(XmlAccessType.FIELD)
public class RiftSchedule {

	@XmlElement(name = "rift", required = true)
	private List<Rift> riftsList;

	public List<Rift> getRiftsList() {
		return riftsList;
	}

	public void setRiftsList(List<Rift> fortressList) {
		this.riftsList = fortressList;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "rift")
	public static class Rift {

		@XmlAttribute(required = true)
		private int id;
		@XmlElement(name = "open")
		private List<OpenRift> openRift;

		public int getWorldId() {
			return id;
		}

		public List<OpenRift> getRift() {
			return openRift;
		}

	}

	public static RiftSchedule load() {
		return JAXBUtil.deserialize(new File("./config/schedule/rift_schedule.xml"), RiftSchedule.class);
	}

}
