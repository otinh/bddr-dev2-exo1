package crawler;

import com.google.gson.JsonObject;
import org.jsoup.nodes.Document;

class SimpleMonsterParser extends MonsterParser
{
	SimpleMonsterParser(Document document)
	{
		super(document);
	}

	JsonObject parseToJson()
	{
		var monster = new JsonObject();

		monster.addProperty("name", getName());
		monster.addProperty("spells", getSpells());

		return monster;
	}

	private String getName()
	{
		return document.getElementsByClass("monster-header").text();
	}

	private String getSpells()
	{
		var spellLines = document.getElementsByClass("stat-block-2");
		return getSpells(spellLines);
	}
}
