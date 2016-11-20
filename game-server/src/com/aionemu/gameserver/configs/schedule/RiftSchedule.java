package com.aionemu.gameserver.configs.schedule;

import java.io.File;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.io.FileUtils;

import com.aionemu.commons.utils.xml.JAXBUtil;
import com.aionemu.gameserver.model.templates.rift.OpenRift;

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
		RiftSchedule rs;
		try {
			String xml = FileUtils.readFileToString(new File("./config/schedule/rift_schedule.xml"));
			rs = JAXBUtil.deserialize(xml, RiftSchedule.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize rifts", e);
		}
		return rs;
	}

}
