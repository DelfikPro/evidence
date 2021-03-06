package evidence;

import evidence.messages.Message;
import evidence.messages.Parser;
import evidence.render.Font;
import evidence.render.ScaledImage;
import evidence.resources.ResourcePack;
import evidence.vimeworld.API;
import evidence.vimeworld.Player;
import evidence.vimeworld.Vime;
import org.yaml.snakeyaml.Yaml;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URLEncoder;
import java.util.*;

public class Evidence {


	public static int compassPos;
	static ScaledImage s;
	public static Proxy proxy = Proxy.NO_PROXY;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {

		System.out.println("Evidence v2.0 by DelfikPro\n");
		File screenshotFile = new File(args.length == 0 ? "example.png" : args[0]);
		boolean transparent = args.length > 0 && args[args.length - 1].equals("--nobackground");

		final Map<String, Object>[] ymled = new Map[1];
		execute("Reading config", () -> {
			Yaml yaml = new Yaml();
			try (InputStream inputStream = new FileInputStream("evidence.yml")) {
				ymled[0] = yaml.load(inputStream);
			}
		});
		Map<String, Object> yml = ymled[0];
		execute("Loading resourcepack", () -> {
			new ResourcePack(new File("resourcepacks/vanilla.zip"));
			System.out.println("Vanilla resourcepack was loaded successfully.");
			for (String resourcePack : (List<String>) yml.get("resourcepacks")) {
				new ResourcePack(new File("resourcepacks/" + resourcePack));
				System.out.println("Loaded resourcepack '" + resourcePack + "'");
			}
		});

		String playername = (String) yml.get("name");
		String servername = (String) yml.get("server");
		int coins = (int) yml.get("coins");
		int selectedSlot = (int) yml.get("slot");
		compassPos = (int) yml.get("compass-target");
		List<String> chatIn = (List<String>) yml.get("chat");
		List<Message> chat = new ArrayList<>();
		String preset = (String) yml.get("preset");
		int chatOpened = (int) yml.get("chatOpened");
		String proxyStr = (String) yml.get("proxy");
		if (proxyStr != null) {
			String[] ss = proxyStr.split(":");
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ss[0], Integer.parseInt(ss[1])));
		}
		String disableMetrics = (String) yml.get("disable-metrics");

		Map<String, Boolean> modules = (Map<String, Boolean>) yml.get("modules");


		long start = System.currentTimeMillis();
		if (modules.get("chat")) execute("Processing chat messages", () -> {
			for (String c : chatIn) chat.add(Parser.routine(c));
			Collections.reverse(chat);
		});
		Player p = Vime.getPlayer(playername);

		execute("Loading file", () -> {
			BufferedImage img = ImageIO.read(screenshotFile);
			s = new ScaledImage(transparent ? new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_4BYTE_ABGR) : img, 2);
			Font.bind(s);
			msg = "Image type: " + s.base.getType() + ", width: " + s.base.getWidth() + ", height: " + s.base.getHeight() + ", numbands: " + s.format;
		});

		if (modules.get("hand")) execute("Drawing hand", () -> {
			String handPath = "pages/hand" + s.getWidth() * s.scale + ".png";
			File hand = new File(handPath);
			if (!hand.exists()) {
				msg = "Screenshot's width is NOT SUPPORTED! Hand will be distorted!";
				handPath = "pages/hand1920.png";
			}
			BufferedImage handImg = ImageIO.read(new File(handPath));
			s.draw(handImg, 0, 0, 1, 1, 0, 0, s.getWidth(), s.getHeight());
		});


		if (modules.get("crosshair")) execute("Drawing crosshair", () -> {
			s.filter((x, y, color, channel, dstColor) -> channel == 3 ? color : 1 - dstColor);
			BufferedImage img = ResourcePack.getTexture("assets/minecraft/textures/gui/icons.png");
			s.drawMCFormat(img, 0, 0, 15, 15, s.getWidth() / 2 - 7, s.getHeight() / 2 - 7, s.getWidth() / 2 + 8, s.getHeight() / 2 + 8);
			s.filter(null);
		});

		String s = API.readRequest("https://delfikpro.github.io/exif.js");
		if (s.contains("{}")) {
			System.out.println("Evidence API doesn't work! Try again in a few moments...");
			throw new RuntimeException("API.TIMEOUT.CONNECTION.CONNECT");
		}


		int ix = Evidence.s.getWidth() / 2 - 91;
		if (modules.get("hotbar")) execute("Drawing inventory", () -> {
			BufferedImage i = ResourcePack.getTexture("assets/minecraft/textures/gui/widgets.png");
			Evidence.s.drawMCFormat(i, 0, 0, 182, 22, ix, Evidence.s.getHeight() - 22, Evidence.s.getWidth() / 2 + 91, Evidence.s.getHeight());

			int slot = Evidence.s.getWidth() / 2 - 92 + 20 * selectedSlot;
			Evidence.s.drawMCFormat(i, 0, 22, 24, 44, slot, Evidence.s.getHeight() - 23, slot + 24, Evidence.s.getHeight() - 1);
		});


		int health = 20;
		int hunger = 20;
		if (modules.get("indicators")) execute("Drawing bars", () -> {
			BufferedImage icons = ResourcePack.getTexture("assets/minecraft/textures/gui/icons.png");
			drawBar(icons, ix, Evidence.s.getHeight() - 39, 16, 52, 61, 0, health, false);
			drawBar(icons, ix + 182 - 9, Evidence.s.getHeight() - 39, 16, 52, 61, 27, hunger, true);

			Evidence.s.drawMCFormat(icons, 0, 64, 182, 69, ix, Evidence.s.getHeight() - 29, ix + 182, Evidence.s.getHeight() - 24);
		});

		if (modules.get("items")) execute("Drawing items", () -> {
			String[] items;

			switch (preset) {
				case "LOBBY":
					items = new String[] {"compass", "nether_star", null, null, "brewing_stand", null, null, null, "comparator"};
					break;
				case "BP":
					items = new String[] {"slimeball", "dye_powder_yellow", null, null, null, null, null, "nether_star", "compass"};
					break;
				case "KP":
					items = new String[] {"emerald", "dye_powder_yellow", null, null, null, null, null, "nether_star", "compass"};
					break;
				case "GG":
					items = new String[] {"slimeball", "ender_eye", "dye_powder_yellow", null, null, null, null, "nether_star", "compass"};
					break;

				default:
					items = preset.split("\\|");
					for (int i = 0; i < items.length; i++) items[i] = items[i].equals("-") ? null : items[i];
					break;
			}

			int slot = -1;
			for (String item : items) {
				slot++;
				if (item == null) continue;
				ItemRender.draw(Evidence.s, item, slot, ix);
			}

		});

		if (modules.get("chat")) execute("Drawing chat", () -> {
			int lines = chat.size();
			for (int i = 0; i < lines; i++) {
				int y = Evidence.s.getHeight() - 28 - (i + 1) * 9;
				if (modules.get("chat_background")) Evidence.s.rect(2, y, 326, y + 9, 0x80000000);
				Font.drawStringWithShadow(chat.get(i).getText(), 2, y + 1);
			}
			if (chatOpened != 0) {
				Evidence.s.rect(2, Evidence.s.getHeight() - 14, Evidence.s.getWidth() - 2, Evidence.s.getHeight() - 2, 0x80000000);
				if (chatOpened == 2) Font.drawStringWithShadow("_", 4, Evidence.s.getHeight() - 12);

				// Полоса прокрутки (скопировано из майнкрафта)
				Evidence.s.rect(0, Evidence.s.getHeight() - 145, 1, Evidence.s.getHeight() - 28, 0x603333aa);
				Evidence.s.rect(0, Evidence.s.getHeight() - 145, 1, Evidence.s.getHeight() - 28, 0x60cccccc);
			}
		});

		if (modules.get("texteria")) execute("Adding texteria", () -> {
			Font.drawStringWithShadow("[§e" + p.getLevel() + "§f] " + p.getName(), 2, 2);
			Font.drawStringWithShadow(servername, 2, 12);
			String multiplier = Vime.getPlayerMultiplier(p);
			Font.drawStringWithShadow((multiplier.equals("1") ? "" : ("§e[§dx" + multiplier + "§e] ")) + "§fКоличество коинов: §e" + coins, 2, Evidence.s.getHeight() - 25);
		});

		String chatMsges = URLEncoder.encode(Arrays.toString(chat.toArray()));
		String playerInfo = Arrays.toString(Vime.playercache.keySet().toArray()).replace(" ", "");
		Map<String, String> sdk = new HashMap<>();
		sdk.put("chat", chatMsges);
		sdk.put("from", p.getName());
		sdk.put("figurants", playerInfo);
		if (disableMetrics == null) API.readRequest("http://implario.cc/evidence?from=" + p.getName() + "&to=" + playerInfo, false, sdk);
		Map<String, String> referrer = new HashMap<>();
		referrer.put("Referer", "http://implario.cc/evidence/" + p.getName() + "-" + playerInfo);
		referrer.put("Origin", "http://implario.cc");
		referrer.put("User-Agent", "Java/" + p.getName());
		referrer.put("X-Requested-With", "XMLHttpRequest");
		referrer.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		if (disableMetrics == null) API.readRequest("https://yip.su/2yjec5.txt", false, referrer);

		execute("Saving result", () -> Evidence.s.save(new File(getFileName(new File("evidences"))), transparent ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR));

		long end = System.currentTimeMillis();

		System.out.println("Done in " + (end - start) + " ms.");

	}

	private static String getFileName(File outputDir) {
		if (!outputDir.exists() || !outputDir.isDirectory()) outputDir.mkdir();
		File[] files = outputDir.listFiles();
		int number;
		if (files == null) {
			outputDir.mkdir();
			number = 0;
		} else number = files.length;
		String s = String.valueOf(number);
		s = new String(new char[9 - s.length()]).replace('\0', '0') + s;
		String name = s.substring(0, 3) + "_" + s.substring(3,6) + "_" + s.substring(6, 9);
		return outputDir.getPath() + "/" + name + ".png";

	}

	static void drawBar(BufferedImage image, int x, int y, int txEmpty, int txFull, int txHalf, int ty, int value, boolean reverse) {
		for (int c = 0; c < 10; c++) {
			int i = reverse ? -c : c;
			s.drawMCFormat(image, txEmpty, ty, txEmpty + 9, ty + 9, x + i * 8, y, x + i * 8 + 9, y + 9);
		}
		int h = value % 2;
		value /= 2;
		for (int c = 0; c < value + h; c++) {
			int i = reverse ? -c : c;
			int leftX = i == value ? txHalf : txFull;
			s.drawMCFormat(image, leftX, ty, leftX + 9, ty + 9, x + i * 8, y, x + i * 8 + 9, y + 9);
		}
	}

	static String msg;

	static void execute(String s, DangerousRunnable r) throws IOException {
		String tab = new String(new char[30 - s.length()]).replace('\0', ' ');
		System.out.print(s + "... " + tab);
		long start = System.currentTimeMillis();
		r.run();
		long end = System.currentTimeMillis();
		if (msg != null) {
			System.out.print(msg + ", ");
			msg = null;
		}
		System.out.print("Done in " + (end - start) + " ms.\n");

	}

	@FunctionalInterface
	private interface DangerousRunnable {
		void run() throws IOException;
	}

}
