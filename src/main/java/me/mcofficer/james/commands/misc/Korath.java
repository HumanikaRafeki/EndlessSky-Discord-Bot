package me.mcofficer.james.commands.misc;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.tools.KorathTranslator;
import net.dv8tion.jda.api.EmbedBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

public class Korath extends Command {

    protected final KorathTranslator translator;

    public Korath(KorathTranslator translator) {
        this.translator = translator;
        name = "korath";
        help = "Applies the Korath encoding on <english>. Translate <english> from English to Indonesian, reverses letters in words, and passes it through the cipher.";
        arguments = "<english>";
        category = James.misc;
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            runTranslator(event.getArgs(), embedBuilder);
            event.reply(embedBuilder.build());
        }
        catch (IOException e) {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            event.reply("An error occured:\n```" + stringWriter.toString() + "```");
        }
    }

    protected void runTranslator(String query, EmbedBuilder builder) throws IOException {
        builder.setTitle("Korath Encoding");
        translator.korath(query, builder);
    }
}
