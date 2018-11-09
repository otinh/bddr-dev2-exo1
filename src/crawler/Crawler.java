package crawler;

import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import java.io.IOException;
import java.util.ArrayList;

public final class Crawler
{
	private static final String BASE_URL = "http://legacy.aonprd.com/bestiary";

	private static String[] bestiaries = new String[5];

	private Crawler()
	{
		setBestiaries();
	}

	public static void setBestiaries()
	{
		bestiaries[0] = ("http://legacy.aonprd.com/bestiary/monsterIndex.html");
		bestiaries[1] = ("http://legacy.aonprd.com/bestiary2/additionalMonsterIndex.html");
		bestiaries[2] = ("http://legacy.aonprd.com/bestiary3/monsterIndex.html");
		bestiaries[3] = ("http://legacy.aonprd.com/bestiary4/monsterIndex.html");
		bestiaries[4] = ("http://legacy.aonprd.com/bestiary5/index.html");
	}

	static String getBaseUrl()
	{
		return BASE_URL;
	}

	public static ArrayList<JsonObject> getMonsterInfos()
	{
		var monstersInfos = new ArrayList<JsonObject>();

		try
		{
			for (var i = 0; i < 1; i++) {
				System.out.println("\n[Acquiring infos on bestiary #" + (i + 1) + "...]");
				var connection = Jsoup.connect(bestiaries[i]);
				monstersInfos.addAll(new BestiaryParser(connection.get(), i).parseToArrayList());
			}
		} catch (IOException e)
		{
			System.err.println("Error in HTTP request: " + e);
		}

		return monstersInfos;
	}
}