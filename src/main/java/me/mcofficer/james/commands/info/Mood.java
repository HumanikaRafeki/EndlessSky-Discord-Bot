package me.mcofficer.james.commands.misc;

import me.mcofficer.james.tools.TextGenerator;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.Random;

public class Mood extends Command {

    protected static final int MAX_REPITITIONS = 20;
    private final TextGenerator generator;

    public Mood(String name, String help, Command.Category category, TextGenerator generator) {
        this.name = name;
        this.help = help;
        this.generator = generator;
        arguments = "[<count>]";
        this.category = category;
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().trim().split("\s+");
        int count = 1;
        if(args.length > 0 && args[0].length() > 0) {
            try {
                count = Integer.parseInt(args[0], 10);
            } catch(NumberFormatException e) {}
            if(count < 1)
                count = 1;
            else if(count > MAX_REPITITIONS)
                count = MAX_REPITITIONS;
        }
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < count; i++) {
            if(i > 0)
		builder.append('\n');
            builder.append(generator.generate());
	}
        event.reply(builder.toString());
    }
};
