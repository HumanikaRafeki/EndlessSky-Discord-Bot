package me.mcofficer.james.commands.misc;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.mcofficer.james.James;
import me.mcofficer.james.phrases.PhraseDatabase;
import me.mcofficer.james.phrases.Phrase;
import me.mcofficer.esparser.DataFile;
import me.mcofficer.esparser.DataNode;
import me.mcofficer.esparser.DataNodeStringLogger;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;

public class Parse extends Command {

    private static long FILE_TIMEOUT = 30;
    private static int MAX_INDIVIDUAL_FILE_SIZE = 300*1024;
    private static int MAX_TOTAL_FILE_SIZE = 300*1024;
    private static Pattern REPITITION = Pattern.compile("\\A(\\d+)\\s+");
    private static int MAX_STRING_LENGTH = 1000;
    private static int MAX_REPITITIONS = 20;

    private static PhraseDatabase gameDataPhrases = null;

    public Parse(PhraseDatabase gameDataPhrases) {
        name = "parse";
        help = "Parses attached game data files.";
        arguments = "<attached txt files>";
        category = James.misc;
        this.gameDataPhrases = gameDataPhrases;
    }

    @Override
    protected void execute(CommandEvent event) {
        String phrase = event.getArgs().trim().replaceAll("[@#<>|*_ \t]+"," ");
        int count = 1;
        Matcher matcher = REPITITION.matcher(phrase);
        if(matcher.find() && matcher.end() < phrase.length()) {
            count = Integer.parseInt(matcher.group(1), 10);
            if(count < 1)
                count = 1;
            if(count > MAX_REPITITIONS)
                count = MAX_REPITITIONS;
            phrase = phrase.substring(matcher.end());
        }

        EmbedBuilder embed = new EmbedBuilder();
	int status = 0;
        PhraseDatabase phrases = new PhraseDatabase(gameDataPhrases);
        String expanded = null;
        List<Message.Attachment> attachments = event.getMessage().getAttachments();

        if(attachments.size() > 0) {
            String attachmentErrors = validateAttachmentList(attachments);
            if(attachmentErrors.length() > 0) {
                embed.addField(null, attachmentErrors, false).setTitle("Invalid Attachments");
                event.reply(embed.build());
                return;
            }
            status = readPhrasesFromAttachments(attachments, embed, phrases);
            if(status < 0) {
                // Thread was interrupted and should exit as soon as possible.
                System.out.println("thread interrupted");
                return;
            }
        }

        if(status == 0) {
            expanded = expandPhrases(count, phrases, phrase);
            if(expanded == null)
                embed.addField(phrase, "*Phrase not found!*", false);
            else {
                String title = phrase;
                if(count > 1)
                    title += " x " + count;
                embed.addField(title, expanded, false);
            }
        }
        event.reply(embed.build());
    }

    private String expandPhrases(int count, PhraseDatabase phrases, String phrase) {
        StringBuilder builder = new StringBuilder();

        Phrase gotten = phrases.get(phrase);
        if(gotten == null)
            return null;

        for(int repeat = 0; repeat < count && builder.length() < MAX_STRING_LENGTH; repeat++)
            builder.append(gotten.expand(phrases)).append('\n');

        // "very long string" becomes "very long s..."
        if(builder.length() > MAX_STRING_LENGTH) {
            builder.delete(MAX_STRING_LENGTH - 3, builder.length());
            builder.append("...");
        }
        return builder.toString().replaceAll("[@#<>|*_ \t]+"," ");
    }

    private int readPhrasesFromAttachments(List<Message.Attachment> attachments, EmbedBuilder embed, PhraseDatabase phrases) {
        DataNodeStringLogger logger = new DataNodeStringLogger();

        for(Message.Attachment a : attachments) {
            try {
                DataFile file = readAttachment(a, logger);
                if(file == null) {
                    embed.addField(a.getFileName(), ": could not read", false);
                    continue;
                }
                phrases.addPhrases(file.getNodes());
                logger.stopLogging();
                logger.freeResources();
            } catch(IOException exc) {
                embed.addField(a.getFileName(), ": could not read: " + exc, false);
                return 1;
            } catch(ExecutionException ee) {
                embed.addField(a.getFileName(), ": could not read: " + ee, false);
                return 1;
            } catch(InterruptedException iexc) {
                // embed.addField(a.getFileName(), ": operation was interrupted", false);
                return -1;
            } catch(TimeoutException texc) {
                embed.addField(a.getFileName(), ": timed out waiting for Discord to provide the file", false);
                return 1;
            }
        }

        String parserErrors = logger.toString();
        if(parserErrors.length() > 1000)
            parserErrors = parserErrors.substring(0,1000);
        if(parserErrors.length() > 0)
            embed.addField("Parser Errors", parserErrors, false);
        return 0;
    }

    private String validateAttachmentList(List<Message.Attachment> attachments) {
        int acceptable = 0;
        StringBuilder errors = new StringBuilder();
        errors.append(String.format("Please attach one or more text files. They must be less than %.1f kiB total.\n", MAX_TOTAL_FILE_SIZE/1024.0));
        int start = errors.length();
        long size = 0;
        for(Message.Attachment a : attachments)
            if(validateAttachment(a, errors)) {
                acceptable++;
                size += a.getSize();
            }
        
        if(size > MAX_TOTAL_FILE_SIZE)
            errors.append("Total file size is too large: ")
                  .append(String.format("%.1f", size/1024.0)).append(" > ")
                  .append(String.format("%.1f", MAX_TOTAL_FILE_SIZE/1024.0)).append("kiB");
        
        if(errors.length() > start || acceptable < attachments.size())
            return errors.toString();

        return "";
    }

    private boolean validateAttachment(Message.Attachment a, StringBuilder errors) {
        if(a.isImage())
            errors.append(a.getFileName()).append(": is an image, not a text file\n");
        else if(a.isVideo())
            errors.append(a.getFileName()).append(": is a video, not a text file\n");
        else if(!a.getContentType().startsWith("text/plain"))
            errors.append(a.getFileName()).append(": is not a text file (content type \"")
                .append(a.getContentType()).append("\" expected \"text/plain charset=utf-8\"\n");
        else if(a.getContentType().indexOf("charset=utf-8") < 0)
            errors.append(a.getFileName()).append(": is not a text file (content type \"")
                .append(a.getContentType()).append("\" expected \"text/plain charset=utf-8\"\n");
        else if(a.getSize() > MAX_INDIVIDUAL_FILE_SIZE)
            errors.append(a.getFileName()).append(": is too large: ")
                .append(String.format("%.1f", a.getSize()/1024.0)).append(" > ")
                .append(String.format("%.1f", MAX_INDIVIDUAL_FILE_SIZE/1024.0)).append(" kiB\n");
        else if(a.getSize() == 0)
            errors.append(a.getFileName()).append(": is empty\n");
        else
            return true;
        return false;
    }

    private static byte[] readWholeStream(int size, InputStream stream, long timeout) throws IOException {
        long startTime = System.nanoTime();
        byte[] b = new byte[size];
        int count = 0;
        int offset = 0;
        while(count >= 0 && offset < size && System.nanoTime() - startTime < timeout) {
            count = stream.read(b, offset, size - offset);
            if(count > 0)
                offset += count;
        }
        if(offset <= 0)
            return null;
        if(offset < b.length)
            b = Arrays.copyOf(b, offset);
        return b;
    }

    private DataFile readAttachment(Message.Attachment a, DataNodeStringLogger logger) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        InputStream stream = null;
        byte[] b = null;
        int fileSize = a.getSize();
        try {
            stream = a.retrieveInputStream().get(FILE_TIMEOUT, TimeUnit.SECONDS);
            b = readWholeStream(fileSize, stream, FILE_TIMEOUT * 1000000000l);
        } finally {
            if(stream != null)
                stream.close();
        }
        if(b == null || b.length < fileSize)
            return null;

        String result = new String(b, StandardCharsets.UTF_8);
        String[] lines = result.split("(?<=\\R)");

        return new DataFile(Arrays.asList(lines), logger);
    }
}
