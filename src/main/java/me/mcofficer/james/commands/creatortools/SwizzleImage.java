package me.mcofficer.james.commands.creatortools;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.tools.ImageSwizzler;
import net.dv8tion.jda.core.entities.Message;

import java.io.IOException;
import java.util.List;

public class SwizzleImage extends Command {

    private final ImageSwizzler swizzler = new ImageSwizzler();

    public SwizzleImage() {
        this.name = "swizzleimage";
        this.help = "Applies the Swizzle X to the uploaded image[s]. If X is not defined, applies swizzles 1-6.";
        this.arguments = "[X] <images as attachment>";
    }

    @Override
    protected void execute(CommandEvent event) {
        List<Message.Attachment> attachments = event.getMessage().getAttachments();
        if (attachments.isEmpty())
            event.reply("Please attach one or more images.");
        else
            for (Message.Attachment a : attachments) {
                if (a.getWidth() > 1000 || a.getHeight() > 1000) {
                    event.reply(a.getFileName() + "is larger than 1000px.");
                    continue;
                }
                try {
                    event.getTextChannel().sendFile(swizzler.swizzle(a.getInputStream(), event.getArgs()), "swizzled.png").queue();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }

}
