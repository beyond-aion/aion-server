import com.aionemu.commons.scripting.metadata.OnClassLoad;
import com.aionemu.commons.scripting.metadata.OnClassUnload;
import com.aionemu.commons.scripting.scriptmanager.ScriptManagerTest;

public class TestOnClassLoadAndUnload {

	@OnClassLoad
	public static void onClassLoad(){
		System.getProperties().setProperty(ScriptManagerTest.SYSTEM_PROPERTY_KEY_CLASS_LOADED, "true");
	}

	@OnClassUnload
	public static void onClassUnload(){
		System.getProperties().setProperty(ScriptManagerTest.SYSTEM_PROPERTY_KEY_CLASS_UNLOADED, "true");
	}
}
