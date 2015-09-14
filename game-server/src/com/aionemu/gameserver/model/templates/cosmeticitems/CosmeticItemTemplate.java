package com.aionemu.gameserver.model.templates.cosmeticitems;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Race;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CosmeticItemTemplate")
public class CosmeticItemTemplate {

	@XmlAttribute(name = "type")
	private String type;
	@XmlAttribute(name = "cosmetic_name")
	private String cosmeticName;
	@XmlAttribute(name = "id")
	private int id;
	@XmlAttribute(name = "race")
	private Race race;
	@XmlAttribute(name = "gender_permitted")
	private String genderPermitted;
	@XmlElement(name = "preset")
	private Preset preset;

	public String getType() {
		return type;
	}

	public String getCosmeticName() {
		return cosmeticName;
	}

	public int getId() {
		return id;
	}

	public Race getRace() {
		return race;
	}

	public String getGenderPermitted() {
		return genderPermitted;
	}

	public Preset getPreset() {
		return preset;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "Preset")
	public static class Preset {

		@XmlElement(name = "scale")
		private float scale;
		@XmlElement(name = "hair_type")
		private int hairType;
		@XmlElement(name = "face_type")
		private int faceType;
		@XmlElement(name = "hair_color")
		private int hairColor;
		@XmlElement(name = "lip_color")
		private int lipColor;
		@XmlElement(name = "eye_color")
		private int eyeColor;
		@XmlElement(name = "skin_color")
		private int skinColor;

		public float getScale() {
			return scale;
		}

		public int getHairType() {
			return hairType;
		}

		public int getFaceType() {
			return faceType;
		}

		public int getHairColor() {
			return hairColor;
		}

		public int getLipColor() {
			return lipColor;
		}

		public int getEyeColor() {
			return eyeColor;
		}

		public int getSkinColor() {
			return skinColor;
		}
	}
}
