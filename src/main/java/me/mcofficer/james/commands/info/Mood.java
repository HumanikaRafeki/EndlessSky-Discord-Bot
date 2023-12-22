package me.mcofficer.james.commands.misc;

import me.mcofficer.james.James;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.util.Random;

public class Mood extends Command {

    protected static final int MAX_REPITITIONS = 20;

    public Mood() {
        name = "mood";
        help = "How is James feeling right now?";
        arguments = "[<count>]";
        category = James.info;
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().trim().split("\s+");
        int count = 1;
        System.out.println(event.getArgs().trim());
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
            builder.append(James.mood.generate());
	}
        event.reply(builder.toString());
    }
};
