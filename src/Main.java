import crawler.Crawler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Main
{
	public static void main(String args[]) throws IOException
	{
		var out = new PrintWriter(new FileWriter(System.getProperty("user.dir") + "\\monsters.json"));
		out.write(String.valueOf(Crawler.getMonsterInfos()));
		out.close();
	}

	// TODO: fucking cockroach and aeon bestiary 2
}