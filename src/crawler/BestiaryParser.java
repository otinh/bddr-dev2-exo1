package crawler;

import com.google.gson.JsonObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class BestiaryParser
{
	private ArrayList<JsonObject> monsters;

	private Element monsterIndex;
	private String bestiaryIndex;
	private List<String> monsterUrls;

	BestiaryParser(Document document, int bestiaryIndex)
	{
		this.monsterIndex = document.getElementById("monster-index-wrapper");
		this.bestiaryIndex = (bestiaryIndex == 0) ? "/" : (bestiaryIndex + 1) + "/";
	}

	/*
	 * Retourne une liste de monstres avec les informations suivantes :
	 * - Le nom du monstre
	 * - Ses sorts
	 * */
	ArrayList<JsonObject> parseToArrayList() throws IOException
	{
		monsterUrls = getUrlSuffixes();
		monsters = new ArrayList<>();
		addMonsters();
		return monsters;
	}

	/*
	 * Retourne le suffixe URL de tous les monstres dans le bestiaire courant.
	 * */
	private List<String> getUrlSuffixes()
	{
		return monsterIndex
				.getElementsByAttribute("href")
				.eachAttr("href");
	}

	/*
	 * Ce code est très dégueux
	 * */
	private void addMonsters() throws IOException
	{
		var isSamePage = false;

		for (var i = 0; i < monsterUrls.size(); i++)
		{
			var isDifferentFromPreviousPage = comparePreviousPage(i);
			var isDifferentFromNextPage = compareNextPage(i);

			if (isDifferentFromNextPage)
			{
				isSamePage = false;
				if (isDifferentFromPreviousPage)
				{
					addSimpleMonster(i);
				}
			} else
			{
				if (!isSamePage)
				{
					isSamePage = true;
					addComplexMonster(i);
				}
			}
		}
		//monsters.forEach(System.out::println);
	}

	/*
	 * Ajoute un monstre provenant d'une page simple à analyser.
	 * (simple :: la page ne contient qu'un monstre)
	 * */
	private void addSimpleMonster(int i) throws IOException
	{
		var monster = new JsonObject();
		monster = new SimpleMonsterParser(connect(i).get()).parseToJson();

		if (!hasEmptyName(monster))
			monsters.add(monster);
	}

	/*
	 * Ajoute des monstres provenant d'une page complexe à analyser.
	 * (complexe :: la page contient plusieurs monstres)
	 * */
	private void addComplexMonster(int i) throws IOException
	{
		var monster2 = new ArrayList<JsonObject>();
		monster2 = new ComplexMonsterParser(connect(i).get()).parseToJson();
		monsters.addAll(monster2);
	}

	private boolean comparePreviousPage(int i)
	{
		if (i == 0) return true;
		return !getRadical(i - 1).equals(getRadical(i));
	}

	/*
	 * Si le monstre suivant dans le bestiaire pointe vers le même URL que
	 * celui qu'on analyse présentement, alors on se ne contentera que d'analyser
	 * le premier lien.
	 * @return true si la page suivante ne pointe pas vers le même URL.
	 * */
	private boolean compareNextPage(int i)
	{
		if (i == monsterUrls.size() - 1) return true;
		return i == 0 || !getRadical(i + 1).equals(getRadical(i));
	}

	/*
	 * Retourne le radical d'un URL, par exemple "angel.html".
	 * */
	private String getRadical(int i)
	{
		return monsterUrls.get(i).split("#")[0];
	}

	/*
	 * Est-ce que le nom du monstre est "" ?
	 * Apparaît lorsqu'un URL d'un bestiaire ne mène pas vers un monstre, par exemple :
	 * http://legacy.aonprd.com/bestiary/angel.html#angel
	 * */
	private boolean hasEmptyName(JsonObject monster)
	{
		return monster.get("name").toString().equals("\"\"");
	}

	/*
	 * Etablit la connexion avec l'URL créée.
	 * */
	private Connection connect(int i)
	{
		var url = Crawler.getBaseUrl() + bestiaryIndex + monsterUrls.get(i);
		System.out.println(url);
		return Jsoup.connect(url);
	}
}
