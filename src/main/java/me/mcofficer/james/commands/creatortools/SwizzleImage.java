package me.mcofficer.james.commands.creatortools;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.tools.ImageSwizzler;
import net.dv8tion.jda.api.entities.Message;

import java.io.IOException;
import java.util.List;

public class SwizzleImage extends Command {

    private final ImageSwizzler swizzler = new ImageSwizzler();

    public SwizzleImage() {
        name = "swizzleimage";
        help = "Applies the Swizzle X to the uploaded image[s]. If X is not defined, applies swizzles 1-6.";
        arguments = "[X] <attached images>";
        category = James.creatorTools;
    }

    @Override
    protected void execute(CommandEvent event) {
        List<Message.Attachment> attachments = event.getMessage().getAttachments();
        if (attachments.isEmpty())
            event.reply("Please attach one or more images.");
        else
            for (Message.Attachment a : attachments) {
                if (a.getWidth() < 1 || a.getHeight() < 1) {
                    event.reply(a.getFileName() + " is not an image.");
                    continue;
                } else if (a.getWidth() > 1080 || a.getHeight() > 1080) {
                    event.reply(a.getFileName() + " is larger than 1080px.");
                    continue;
                }

                a.retrieveInputStream().thenAccept(inputStream -> {
                    try {
                        event.getTextChannel().sendFile(swizzler.swizzle(inputStream, event.getArgs()), "swizzled.png").queue();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    } catch(Exception ee) {
                        ee.printStackTrace();
                        throw ee;
                    }
                });
            }
    }

}
