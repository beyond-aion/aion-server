package com.aionemu.gameserver.model.gameobjects.player;

/**
 * @author SoulKeeper, srx47, alexa026
 */
public class PlayerAppearance {

	/**
	 * Player's face
	 */
	private int face;
	private int hair;
	private int deco;
	private int tattoo;
	private int faceContour; // 2.5
	private int expression; // 2.5
	private int jawLine; // 2.5
	private int skinRGB;
	private int hairRGB;
	private int lipRGB;
	private int eyeRGB;
	private int faceShape;
	private int forehead;
	private int eyeHeight;
	private int eyeSpace;
	private int eyeWidth;
	private int eyeSize;
	private int eyeShape;
	private int eyeAngle;
	private int browHeight;
	private int browAngle;
	private int browShape;
	private int nose;
	private int noseBridge;
	private int noseWidth;
	private int noseTip;
	private int cheek;
	private int lipHeight;
	private int mouthSize;
	private int lipSize;
	private int smile;
	private int lipShape;
	private int jawHeigh;
	private int chinJut;
	private int earShape;
	private int headSize;
	private int neck;
	private int neckLength;
	private int shoulders;
	private int shoulderSize;
	private int torso;
	private int chest;
	private int waist;
	private int hips;
	private int armThickness;
	private int armLength;
	private int handSize;
	private int legThickness;
	private int legLength;
	private int footSize;
	private int facialRate;
	private int voice;
	private float height;

	/**
	 * Returns character face
	 * 
	 * @return character face
	 */
	public int getFace() {
		return face;
	}

	/**
	 * Sets character's face
	 * 
	 * @param face
	 *          characters face
	 */
	public void setFace(int face) {
		this.face = face;
	}

	/**
	 * Returns character's hair
	 * 
	 * @return characters hair
	 */
	public int getHair() {
		return hair;
	}

	/**
	 * Sets charaxcters hair
	 * 
	 * @param hair
	 *          characters hair
	 */
	public void setHair(int hair) {
		this.hair = hair;
	}

	/**
	 * Returns dunno what is this
	 * 
	 * @return some crap, ask Neme what it is
	 */
	public int getDeco() {
		return deco;
	}

	/**
	 * Sets some crap, ask Neme what it is
	 * 
	 * @param deco
	 *          crap
	 */
	public void setDeco(int deco) {
		this.deco = deco;
	}

	/**
	 * Returns sexy tattoo
	 * 
	 * @return sexy tattoo
	 */
	public int getTattoo() {
		return tattoo;
	}

	/**
	 * Set's sexy tattoo.<br>
	 * Not sexy will throw NotSexyTattooException. Just kidding ;)
	 * 
	 * @param tattoo
	 *          some tattoo
	 */
	public void setTattoo(int tattoo) {
		this.tattoo = tattoo;
	}

	/**
	 * @return the faceContour
	 */
	public int getFaceContour() {
		return faceContour;
	}

	/**
	 * @param faceContour
	 *          the faceContour to set
	 */
	public void setFaceContour(int faceContour) {
		this.faceContour = faceContour;
	}

	/**
	 * @return the expression
	 */
	public int getExpression() {
		return expression;
	}

	/**
	 * @param expression
	 *          the expression to set
	 */
	public void setExpression(int expression) {
		this.expression = expression;
	}

	/**
	 * @return the jawLine
	 */
	public int getJawLine() {
		return jawLine;
	}

	/**
	 * @param jawLine
	 *          the jawLine to set
	 */
	public void setJawLine(int jawLine) {
		this.jawLine = jawLine;
	}

	/**
	 * Skin color, let's create pink lesbians :D
	 * 
	 * @return skin color
	 */
	public int getSkinRGB() {
		return skinRGB;
	}

	/**
	 * Here is the valid place to make lesbians skin pink
	 * 
	 * @param skinRGB
	 *          skin color
	 */
	public void setSkinRGB(int skinRGB) {
		this.skinRGB = skinRGB;
	}

	/**
	 * Hair color, personally i prefer brunettes
	 * 
	 * @return har color
	 */
	public int getHairRGB() {
		return hairRGB;
	}

	/**
	 * Sets hair colors. Blonds must pass IQ test ;)
	 * 
	 * @param hairRGB
	 *          Hair color
	 */
	public void setHairRGB(int hairRGB) {
		this.hairRGB = hairRGB;
	}

	/**
	 * Eye colour
	 */
	public void setEyeRGB(int eyeRGB) {
		this.eyeRGB = eyeRGB;
	}

	/**
	 * Sets eye colour
	 */
	public int getEyeRGB() {
		return eyeRGB;
	}

	/**
	 * Lips color.
	 * 
	 * @return lips color
	 */
	public int getLipRGB() {
		return lipRGB;
	}

	/**
	 * Sets lips color
	 * 
	 * @param lipRGB
	 *          face shape
	 */
	public void setLipRGB(int lipRGB) {
		this.lipRGB = lipRGB;
	}

	/**
	 * Returns face shape
	 * 
	 * @return face shape
	 */
	public int getFaceShape() {
		return faceShape;
	}

	/**
	 * Sets face shape
	 * 
	 * @param faceShape
	 *          face shape
	 */
	public void setFaceShape(int faceShape) {
		this.faceShape = faceShape;
	}

	/**
	 * Returns forehead
	 * 
	 * @return forehead
	 */
	public int getForehead() {
		return forehead;
	}

	/**
	 * Sets forehead
	 * 
	 * @param forehead
	 *          size
	 */
	public void setForehead(int forehead) {
		this.forehead = forehead;
	}

	/**
	 * Returns eye heigth
	 * 
	 * @return eye height
	 */
	public int getEyeHeight() {
		return eyeHeight;
	}

	/**
	 * Sets eye heigth
	 * 
	 * @param eyeHeight
	 *          eye heigth
	 */
	public void setEyeHeight(int eyeHeight) {
		this.eyeHeight = eyeHeight;
	}

	/**
	 * Eye space
	 * 
	 * @return eye space
	 */
	public int getEyeSpace() {
		return eyeSpace;
	}

	/**
	 * Eye space
	 * 
	 * @param eyeSpace
	 *          someting connected to eyes
	 */
	public void setEyeSpace(int eyeSpace) {
		this.eyeSpace = eyeSpace;
	}

	/**
	 * Returns eye width
	 * 
	 * @return eye width
	 */
	public int getEyeWidth() {
		return eyeWidth;
	}

	/**
	 * Sets eye width
	 * 
	 * @param eyeWidth
	 *          eye width
	 */
	public void setEyeWidth(int eyeWidth) {
		this.eyeWidth = eyeWidth;
	}

	/**
	 * Returns eye size. Hentai girls usually have very big eyes
	 * 
	 * @return eyes
	 */
	public int getEyeSize() {
		return eyeSize;
	}

	/**
	 * Set's eye size.<br>
	 * Can be . o O ;)
	 * 
	 * @param eyeSize
	 *          eye size,
	 */
	public void setEyeSize(int eyeSize) {
		this.eyeSize = eyeSize;
	}

	/**
	 * Return eye shape
	 * 
	 * @return eye shape
	 */
	public int getEyeShape() {
		return eyeShape;
	}

	/**
	 * Sets Eye shape.<br>
	 * Can be . _ | 0 o O etc :)
	 * 
	 * @param eyeShape
	 *          eye shape
	 */
	public void setEyeShape(int eyeShape) {
		this.eyeShape = eyeShape;
	}

	/**
	 * Return eye angle
	 * 
	 * @return eye angle
	 */
	public int getEyeAngle() {
		return eyeAngle;
	}

	/**
	 * Sets eye angle, / | \.
	 * 
	 * @param eyeAngle
	 *          eye angle
	 */
	public void setEyeAngle(int eyeAngle) {
		this.eyeAngle = eyeAngle;
	}

	/**
	 * Rerturn brow heigth
	 * 
	 * @return brow heigth
	 */
	public int getBrowHeight() {
		return browHeight;
	}

	/**
	 * Brow heigth
	 * 
	 * @param browHeight
	 *          brow heigth
	 */
	public void setBrowHeight(int browHeight) {
		this.browHeight = browHeight;
	}

	/**
	 * Returns brow angle
	 * 
	 * @return brow angle
	 */
	public int getBrowAngle() {
		return browAngle;
	}

	/**
	 * Sets brow angle
	 * 
	 * @param browAngle
	 *          brow angle
	 */
	public void setBrowAngle(int browAngle) {
		this.browAngle = browAngle;
	}

	/**
	 * Returns brow shape
	 * 
	 * @return brow shape
	 */
	public int getBrowShape() {
		return browShape;
	}

	/*******************************************************************************************************************
	 * Sets brow shape
	 * 
	 * @param browShape
	 *          brow shape
	 */
	public void setBrowShape(int browShape) {
		this.browShape = browShape;
	}

	/**
	 * Returns nose
	 * 
	 * @return nose
	 */
	public int getNose() {
		return nose;
	}

	/**
	 * Sets nose
	 * 
	 * @param nose
	 *          nose
	 */
	public void setNose(int nose) {
		this.nose = nose;
	}

	/**
	 * Returns nose bridge
	 * 
	 * @return nose bridge
	 */
	public int getNoseBridge() {
		return noseBridge;
	}

	/**
	 * Sets nose bridge
	 * 
	 * @param noseBridge
	 *          nose bridge
	 */
	public void setNoseBridge(int noseBridge) {
		this.noseBridge = noseBridge;
	}

	/**
	 * Returns nose width
	 * 
	 * @return nose width
	 */
	public int getNoseWidth() {
		return noseWidth;
	}

	/**
	 * Sets nose width
	 * 
	 * @param noseWidth
	 *          nose width
	 */
	public void setNoseWidth(int noseWidth) {
		this.noseWidth = noseWidth;
	}

	/**
	 * Returns noce tip
	 * 
	 * @return noce tip
	 */
	public int getNoseTip() {
		return noseTip;
	}

	/**
	 * Sets noce tip
	 * 
	 * @param noseTip
	 *          noce tip
	 */
	public void setNoseTip(int noseTip) {
		this.noseTip = noseTip;
	}

	/**
	 * Returns cheeks
	 * 
	 * @return cheeks
	 */
	public int getCheek() {
		return cheek;
	}

	/**
	 * Sets cheeks
	 * 
	 * @param cheek
	 *          checks
	 */
	public void setCheek(int cheek) {
		this.cheek = cheek;
	}

	/**
	 * Returns lip heigth
	 * 
	 * @return lip heigth
	 */
	public int getLipHeight() {
		return lipHeight;
	}

	/**
	 * Sets lip heigth
	 * 
	 * @param lipHeight
	 *          lip heith
	 */
	public void setLipHeight(int lipHeight) {
		this.lipHeight = lipHeight;
	}

	/**
	 * Returns mouth size
	 * 
	 * @return mouth size
	 */
	public int getMouthSize() {
		return mouthSize;
	}

	/**
	 * Sets mouth size
	 * 
	 * @param mouthSize
	 *          mouth size
	 */
	public void setMouthSize(int mouthSize) {
		this.mouthSize = mouthSize;
	}

	/**
	 * Returns lips size
	 * 
	 * @return lips size
	 */
	public int getLipSize() {
		return lipSize;
	}

	/**
	 * Sets lips size
	 * 
	 * @param lipSize
	 *          lips size
	 */
	public void setLipSize(int lipSize) {
		this.lipSize = lipSize;
	}

	/**
	 * Returns smile
	 * 
	 * @return smile
	 */
	public int getSmile() {
		return smile;
	}

	/**
	 * Sets smile
	 * 
	 * @param smile
	 *          smile
	 */
	public void setSmile(int smile) {
		this.smile = smile;
	}

	/**
	 * Returns lips shape
	 * 
	 * @return lips shape
	 */
	public int getLipShape() {
		return lipShape;
	}

	/**
	 * Sets lips shape
	 * 
	 * @param lipShape
	 *          lips shape
	 */
	public void setLipShape(int lipShape) {
		this.lipShape = lipShape;
	}

	/**
	 * Returns jaws height
	 * 
	 * @return jaws height
	 */
	public int getJawHeigh() {
		return jawHeigh;
	}

	/**
	 * Sets jaws height
	 * 
	 * @param jawHeigh
	 *          jaws height
	 */
	public void setJawHeigh(int jawHeigh) {
		this.jawHeigh = jawHeigh;
	}

	/**
	 * Returns chin jut
	 * 
	 * @return chin jut
	 */
	public int getChinJut() {
		return chinJut;
	}

	/**
	 * Sets chin jut
	 * 
	 * @param chinJut
	 *          chin jut
	 */
	public void setChinJut(int chinJut) {
		this.chinJut = chinJut;
	}

	/**
	 * Returns ear shape
	 * 
	 * @return ear shape
	 */
	public int getEarShape() {
		return earShape;
	}

	/**
	 * Sets ear shape
	 * 
	 * @param earShape
	 *          ear shape
	 */
	public void setEarShape(int earShape) {
		this.earShape = earShape;
	}

	/**
	 * Returns head size
	 * 
	 * @return head size
	 */
	public int getHeadSize() {
		return headSize;
	}

	/**
	 * Sets head size
	 * 
	 * @param headSize
	 *          head size
	 */
	public void setHeadSize(int headSize) {
		this.headSize = headSize;
	}

	/**
	 * Returns neck
	 * 
	 * @return neck
	 */
	public int getNeck() {
		return neck;
	}

	/**
	 * Sets neck
	 * 
	 * @param neck
	 *          neck
	 */
	public void setNeck(int neck) {
		this.neck = neck;
	}

	/**
	 * Returns neck length
	 * 
	 * @return neck length
	 */
	public int getNeckLength() {
		return neckLength;
	}

	/**
	 * Sets neck length, just curious, is it possible to create a giraffe?
	 * 
	 * @param neckLength
	 *          neck length
	 */
	public void setNeckLength(int neckLength) {
		this.neckLength = neckLength;
	}

	/**
	 * Shoulders
	 * 
	 * @return shouldeers
	 */
	public int getShoulders() {
		return shoulders;
	}

	/**
	 * Shoulders
	 * 
	 * @param shoulders
	 *          shoulders
	 */
	public void setShoulders(int shoulders) {
		this.shoulders = shoulders;
	}

	/**
	 * Shoulder Size
	 * 
	 * @return shouldeerSize
	 */
	public int getShoulderSize() {
		return shoulderSize;
	}

	/**
	 * Shoulder Size
	 * 
	 * @param shoulderSize
	 *          shoulderSize
	 */
	public void setShoulderSize(int shoulderSize) {
		this.shoulderSize = shoulderSize;
	}

	/**
	 * Torso
	 * 
	 * @return torso
	 */
	public int getTorso() {
		return torso;
	}

	/**
	 * Sets torso
	 * 
	 * @param torso
	 *          torso
	 */
	public void setTorso(int torso) {
		this.torso = torso;
	}

	/**
	 * Returns tits
	 * 
	 * @return tits
	 */
	public int getChest() {
		return chest;
	}

	/**
	 * Sets tits
	 * 
	 * @param chest
	 *          tits
	 */
	public void setChest(int chest) {
		this.chest = chest;
	}

	/**
	 * Returns waist
	 * 
	 * @return waist
	 */
	public int getWaist() {
		return waist;
	}

	/**
	 * sets waist
	 * 
	 * @param waist
	 *          waist
	 */
	public void setWaist(int waist) {
		this.waist = waist;
	}

	/**
	 * Returns hips
	 * 
	 * @return hips
	 */
	public int getHips() {
		return hips;
	}

	/**
	 * Sets hips
	 * 
	 * @param hips
	 *          hips
	 */
	public void setHips(int hips) {
		this.hips = hips;
	}

	/**
	 * Returns arm thickness
	 * 
	 * @return arm thickness
	 */
	public int getArmThickness() {
		return armThickness;
	}

	/**
	 * Sets arm thickness
	 * 
	 * @param armThickness
	 *          arm thickness
	 */
	public void setArmThickness(int armThickness) {
		this.armThickness = armThickness;
	}

	/**
	 * Returns arm length
	 * 
	 * @return arm length
	 */
	public int getArmLength() {
		return armLength;
	}

	/**
	 * Sets arm length
	 * 
	 * @param armLength
	 *          arm length
	 */
	public void setArmLength(int armLength) {
		this.armLength = armLength;
	}

	/**
	 * Returns hand size
	 * 
	 * @return hand size
	 */
	public int getHandSize() {
		return handSize;
	}

	/**
	 * Sets hand size
	 * 
	 * @param handSize
	 *          hand size
	 */
	public void setHandSize(int handSize) {
		this.handSize = handSize;
	}

	/**
	 * Returns legs thickness
	 * 
	 * @return leg thickness
	 */
	public int getLegThickness() {
		return legThickness;
	}

	/**
	 * Sets leg thickness
	 * 
	 * @param legThickness
	 *          leg thickness
	 */
	public void setLegThickness(int legThickness) {
		this.legThickness = legThickness;
	}

	/**
	 * Returns legs Length
	 * 
	 * @return leg Length
	 */
	public int getLegLength() {
		return legLength;
	}

	/**
	 * Sets leg length
	 * 
	 * @param legLength
	 *          leg length
	 */
	public void setLegLength(int legLength) {
		this.legLength = legLength;
	}

	/**
	 * Returns foot size
	 * 
	 * @return foot size
	 */
	public int getFootSize() {
		return footSize;
	}

	/**
	 * Sets foot size
	 * 
	 * @param footSize
	 *          foot size
	 */
	public void setFootSize(int footSize) {
		this.footSize = footSize;
	}

	/**
	 * Retunrs facial rate
	 * 
	 * @return facial rate
	 */
	public int getFacialRate() {
		return facialRate;
	}

	/**
	 * Sets facial rate
	 * 
	 * @param facialRate
	 *          facial rate
	 */
	public void setFacialRate(int facialRate) {
		this.facialRate = facialRate;
	}

	/**
	 * Returns sexy voice
	 * 
	 * @return sexy voice
	 */
	public int getVoice() {
		return voice;
	}

	/**
	 * Sets sexy voice
	 * 
	 * @param voice
	 *          sexy voice
	 */
	public void setVoice(int voice) {
		this.voice = voice;
	}

	/**
	 * Returns height
	 * 
	 * @return height
	 */
	public float getHeight() {
		return height; // character height is odd, but client needs it ¯\_(ツ)_/¯
	}

	/**
	 * @return The approximate real height of the character.
	 */
	public float getBoundHeight() {
		return height * 1.75f;
	}

	/**
	 * Sets height
	 * 
	 * @param height
	 *          height
	 */
	public void setHeight(float height) {
		this.height = height;
	}
}
