package ca.jarcode.consoles;

import ca.jarcode.consoles.api.Position2D;
import ca.jarcode.consoles.computer.ComputerHandler;
import ca.jarcode.consoles.computer.GeneralListener;
import ca.jarcode.consoles.computer.MapDataStore;
import ca.jarcode.consoles.computer.NativeLoader;
import ca.jarcode.consoles.computer.command.CommandComputer;
import ca.jarcode.consoles.computer.interpreter.Lua;
import ca.jarcode.consoles.computer.interpreter.luaj.LuaJEngine;
import ca.jarcode.consoles.internal.ConsoleHandler;
import jni.NLoader;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.function.Supplier;

public class Computers extends JavaPlugin {

	public static HashMap<String, Runnable> ENGINES = new HashMap<>();

	static {
		ENGINES.put("luaj", LuaJEngine::install);
	}

	private static Computers INSTANCE = null;

	// flag we use in case the plugin is reloaded, the JVM complains if we try to re-load the same native
	private static boolean LOADED_NATIVES = false;

	// allow the crafting of computers
	public static boolean allowCrafting = true;
	// whether to enable the basic frame rendering for computers
	public static boolean frameRenderingEnabled = true;
	// command prefix
	public static String commandPrefix;
	// self-explanatory
	public static int maxComputers = 3;
	// hide save messages
	public static boolean hideSaveMessages = false;
	// maximum time that a program is allowed to run before being interrupted
	public static int maxTimeWithoutInterrupt = 7000;
	// chunk size (in kilobytes) that wget downloads every 300ms
	public static int wgetChunkSize = 2;
	// max heap size for scripts (in kilobytes)
	public static int scriptHeapSize = 64 * 1024;
	// script interpreter to use
	public static String scriptEngine = "luaj";

	public static File jarFile;

	public static Computers getInstance() {
		return INSTANCE;
	}

	{
		INSTANCE = this;
	}

	public void onEnable() {

		jarFile = getFile();

		Lua.killAll = false; // if this plugin was reloaded
		saveDefaultConfig();

		frameRenderingEnabled = getConfig().getBoolean("frame-rendering", frameRenderingEnabled);
		allowCrafting = getConfig().getBoolean("allow-computer-crafting", allowCrafting);
		commandPrefix = getConfig().getString("command-prefix", commandPrefix).trim();
		maxComputers = getConfig().getInt("computer-limit", maxComputers);
		hideSaveMessages = getConfig().getBoolean("hide-save-messages", hideSaveMessages);
		maxTimeWithoutInterrupt = getConfig().getInt("max-time-without-interrupt", maxTimeWithoutInterrupt);
		wgetChunkSize = getConfig().getInt("wget-chunk-size", wgetChunkSize);
		scriptHeapSize = getConfig().getInt("script-heap-size", scriptHeapSize);
		scriptEngine = getConfig().getString("script-engine", scriptEngine);

		if (!LOADED_NATIVES) {
			new NativeLoader("computerimpl").loadAsJNILibrary(this);
			NativeLoader.linkLoader(new NLoader());
		}

		LOADED_NATIVES = true;

		if (!ENGINES.containsKey(scriptEngine)) {
			scriptEngine = "luaj";
		}

		getLogger().info("using script engine: " + scriptEngine);

		ENGINES.get(scriptEngine).run();

		MapDataStore.init(this);

		register(ComputerHandler::new, GeneralListener::new);

		try {
			Consoles consoles = Consoles.getInstance();
			consoles.getCommandHandler().addCommand(CommandComputer.class);
			ConsoleHandler.getInstance().interactionHooks.add((x, y, player, console) -> {
				ComputerHandler handler = ComputerHandler.getInstance();
				if (handler != null) {
					handler.interact(new Position2D(x, y), player, console);
				}
			});
		}
		catch (NoClassDefFoundError e) {
			getLogger().warning("This shouldn't happen!");
			e.printStackTrace();
		}
		catch (IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
		}

	}
	public void onDisable() {
		Lua.killAll = true;
	}

	private void register(Supplier... suppliers) {
		for (Supplier supplier : suppliers) {
			Object obj = supplier.get();
			if (obj instanceof Listener)
				this.getServer().getPluginManager().registerEvents((Listener) obj, this);
		}
	}
}
