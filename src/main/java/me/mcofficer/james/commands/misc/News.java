package me.mcofficer.james.commands.misc;

import me.mcofficer.james.James;
import me.mcofficer.james.phrases.PhraseDatabase;
import me.mcofficer.james.phrases.NewsDatabase;
import me.mcofficer.james.phrases.NewsStory;
import me.mcofficer.james.phrases.PhraseLimits;

import net.dv8tion.jda.api.EmbedBuilder;

public class News extends Parse {

    public News(PhraseDatabase gameDataPhrases, NewsDatabase gameDataNews) {
        super(gameDataPhrases, gameDataNews, true);
        name = "news";
        help = "Generates news from game data files and attached files.";
        arguments = "[<count>] name\n    [<count>] name ...\n    [<attached txt files>]";
        category = James.misc;
    }

    @Override
    protected boolean processInput(int count, PhraseDatabase phrases, NewsDatabase news, String entry, EmbedBuilder embed, PhraseLimits limits) {
        String expanded = expandNews(count, phrases, news, entry, limits);
        if(count < 1) {
            embed.addField("News \"" + entry + '"', "*Too many inputs! Use fewer phrases or repititions.*", false);
            return false;
        }
        if(expanded == null)
            embed.addField("News \"" + entry + '"', "*No news there!*", false);
        else {
            String title = "News \"" + entry + '"';
            if(count > 1)
                title += " Repeated " + count + " Times";
            embed.addField(title, "`" + expanded.replace("`","'") + "`", false);
        }
        return true;
    }

    private String expandNews(int count, PhraseDatabase phrases, NewsDatabase news, String entry, PhraseLimits limits) {
        StringBuilder builder = new StringBuilder();

        NewsStory gotten = news.getNews(entry);
        if(gotten == null) {
            System.out.println("Got null for news \""+entry+'"');
            return null;
        }

        for(int repeat = 0; repeat < count && builder.length() < MAX_STRING_LENGTH; repeat++)
            builder.append(gotten.toString(phrases, limits)).append('\n');

        // "very long string" becomes "very long s..."
        if(builder.length() > MAX_STRING_LENGTH) {
            builder.delete(MAX_STRING_LENGTH - 3, builder.length());
            builder.append("...");
        }
        return builder.toString().replaceAll("[@#<>|*_ \t]+"," ");
    }
};
