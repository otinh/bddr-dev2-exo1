package crawler;

import com.google.gson.GsonBuilder;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;

abstract class MonsterParser
{
	Document document;

	MonsterParser(Document document)
	{
		this.document = document;
	}

	String getSpells(Elements elements)
	{
		var spellList = new ArrayList<String>();

		elements.stream()
				.map(line -> line
						.getElementsByAttribute("href")
						.append(";")
						.text())
				.filter(spell -> !spell.isEmpty())
				.map(spell -> spell.split(";"))
				.forEach(parsedSpells -> Arrays.stream(parsedSpells)
						.map(String::trim)
						.forEach(spellList::add));

		return new GsonBuilder()
				.create()
				.toJson(spellList);
	}
}
