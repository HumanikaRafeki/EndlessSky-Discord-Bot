package me.mcofficer.james.phrases;

import me.mcofficer.esparser.DataNode;

import java.util.HashSet;

public class News {
    Phrase name;
    Phrase message;

    News(DataNode node) {
        name = null;
        message = null;
        for(DataNode child : node.getChildren())
            if(child.size() < 1)
                continue;
            else if(child.token(0) == "name")
                name = new Phrase(child);
            else if(child.token(0).equals("message"))
                message = new Phrase(child);
    }

    public String toString(PhraseDatabase phrases, PhraseLimits limits) {
        HashSet<String> touched = new HashSet<String>();
        StringBuilder result = new StringBuilder();
        if(name != null)
            name.expand(result, phrases, touched, limits);
        else
            limits.appendRemaining("(no name)", result);
        limits.appendRemaining(": ", result);
        if(message != null)
            message.expand(result, phrases, touched, limits);
        else
            limits.appendRemaining("(no message)", result);
        return result.toString();
    }
};
