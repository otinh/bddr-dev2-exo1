package crawler;

import com.google.gson.JsonObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

class ComplexMonsterParser extends MonsterParser
{
	private ArrayList<JsonObject> monsters;
	private JsonObject monster;

	private ArrayList<Element> elementsBetween;

	private String[] monsterNames;
	private int monsterIndex;

	ComplexMonsterParser(Document document)
	{
		super(document);
		initParser();
	}

	private void initParser()
	{
		monsters = new ArrayList<>();
		monster = new JsonObject();

		monsterNames = getNames();
		monsterIndex = 0;

		elementsBetween = new ArrayList<>();
	}

	ArrayList<JsonObject> parseToJson()
	{
		var body = document.getElementsByClass("stat-block-title");
		var siblings = body.first().siblingElements();

		var containsSpells = new AtomicBoolean(false);
		var spellsElementBetween = new ArrayList<Element>();

		siblings.forEach(line -> {

			// monster spell

			if (isOffenseBlock(line))
			{
				containsSpells.set(true);
				spellsElementBetween.clear();
			}

			if (isStatsBlock(line))
			{
				containsSpells.set(false);
			}

			if (containsSpells.get())
			{
				spellsElementBetween.add(line);
			}

			// monster name

			if (isMonsterName(line) && spellsElementBetween.size() != 0)
			{
				if (isValidMonster()) parseMonster(spellsElementBetween);
				elementsBetween.clear();
			} else
			{
				elementsBetween.add(line);
			}
		});

		parseMonster(spellsElementBetween);

		return monsters;
	}

	/*
	 * Récupère toutes les informations du monstre et l'ajoute à la liste des monstres
	 * déjà analysés.
	 * */
	private void parseMonster(ArrayList<Element> elements)
	{
		var name = getName();

		monster = new JsonObject();
		monster.addProperty("name", name);
		monster.addProperty("spell", getSpellsByElements(elements));
		monsters.add(monster);
	}

	/*
	 * Le bloc suivant contient les sorts
	 * */
	private boolean isOffenseBlock(Element line)
	{
		return line.hasClass("stat-block-breaker") && line.text().equals("Offense");
	}

	/*
	 * Le bloc précédent contient les sorts
	 * */
	private boolean isStatsBlock(Element line)
	{
		return line.hasClass("stat-block-breaker") && line.text().equals("Statistics");
	}

	/*
	 * Est-ce que la ligne HTML courante appartient à la liste des noms de monstres de la page ?
	 * */
	private boolean isMonsterName(Element line)
	{
		return line.select("p").hasClass("stat-block-title") &&
				!line.select("span").text().isEmpty();
	}

	/*
	 * Un monstre est valide s'il contient certaines classes HTML.
	 * */
	private boolean isValidMonster()
	{
		for (var htmlLine : elementsBetween)
		{
			var statBlock1 = htmlLine.getElementsByClass("stat-block-1");
			var statBlock2 = htmlLine.getElementsByClass("stat-block-2");
			if (!statBlock1.text().isEmpty() || !statBlock2.text().isEmpty())
				return true;
		}
		return false;
	}

	/*
	 * Retourne le nom du monstre pointé et incrémente ce pointeur.
	 * */
	private String getName()
	{
		return monsterNames[monsterIndex++].split(" <")[0];
	}

	/*
	 * Récupère la liste des noms des monstres dans la page courante.
	 * */
	private String[] getNames()
	{
		var monsterNames = document
				.getElementsByClass("stat-block-title")
				.select("b:has(span)");

		return monsterNames.html().split("\n");
	}

	/*
	 * Récupère tous les sorts dans l'intervalle de lignes HTML donné.
	 * */
	private String getSpellsByElements(ArrayList<Element> elementsBetween)
	{
		var spellsHtml = new Element("div");
		for (var htmlLine : elementsBetween)
		{
			var line = htmlLine.getElementsByClass("stat-block-2");
			if (!line.isEmpty())
				spellsHtml.append(htmlLine.html());
		}
		return getSpells(spellsHtml.getElementsByTag("a"));
	}

}
