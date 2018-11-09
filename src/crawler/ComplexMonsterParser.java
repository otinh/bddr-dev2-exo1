package crawler;

import com.google.gson.JsonObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.util.ArrayList;
import java.util.Arrays;

class ComplexMonsterParser extends MonsterParser
{
	private ArrayList<JsonObject> monsters;
	private JsonObject monster;

	private ArrayList<Element> elementsBetween;

	private String[] monsterNames;
	private int monsterIndex;
	private String[] monsterFilter;

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
		var header = document.getElementsByClass("stat-block-title");
		var siblings = header.first().siblingElements();

		siblings.forEach(line -> {
			if (isMonsterName(line))
			{
				if (isValidMonster()) parseMonster();
				elementsBetween.clear();
			} else
			{
				elementsBetween.add(line);
			}
		});

		parseMonster();

		return monsters;
	}

	/*
	 * Récupère toutes les informations du monstre et l'ajoute à la liste des monstres
	 * déjà analysés.
	 * */
	private void parseMonster()
	{
		var name = getName();
		if (isFiltered(name)) return;

		monster = new JsonObject();
		monster.addProperty("name", name);
		monster.addProperty("spell", getSpellsByElements(elementsBetween));
		monsters.add(monster);
	}

	/*
	 * Est-ce que ce nom fait partie de la liste des noms filtrés auparavant ?
	 * */
	private boolean isFiltered(String name)
	{
		for (var f : monsterFilter)
			if (name.equals(f)) return true;
		return false;
	}

	/*
	 * Est-ce que la ligne HTML courante appartient à la liste des noms de monstres de la page ?
	 * */
	private boolean isMonsterName(Element line)
	{
		return Arrays.asList(monsterNames).contains(line.text());
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
		return monsterNames[monsterIndex++];
	}

	/*
	 * Récupère la liste des noms des monstres dans la page courante.
	 * */
	private String[] getNames()
	{
		// On attrape les noms
		var monsterNames = document
				.getElementsByClass("stat-block-title")
				.select("b");

		// On crée un filtre pour les noms de monstre non valides (pas de "CR")
		monsterFilter = monsterNames
				.select("b:not(:has(span))")
				.html()
				.split("\n");

		// On enlève le tag <span ...> qui ne nous est pas utile
		monsterNames.select("span")
				.forEach(Node::remove);

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
