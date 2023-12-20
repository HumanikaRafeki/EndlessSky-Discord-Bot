package me.mcofficer.james.phrases;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import me.mcofficer.esparser.DataNode;

public class Phrase implements PhraseExpander {

    private ArrayList<PhraseExpander> expanders;
    private String name;

    public Phrase(DataNode node) {
        expanders = new ArrayList<PhraseExpander>();
        ArrayList<String> tokens = node.getTokens();
        if(node.size() > 1)
            name = node.getTokens().get(1);
        else
            name = "NO NAME";
        for(DataNode child : node.getChildren()) {
            if(child.size() < 1)
                continue;
            String nodeType = child.getTokens().get(0);
            if(nodeType.equals("word"))
                expanders.add(new WordList(child));
            else if(nodeType.equals("phrase"))
                expanders.add(new PhraseList(child));
            else if(nodeType.equals("replace"))
                expanders.add(new Replacements(child));
        }
    }

    public String getName() {
        return name;
    }

    public String expand(PhraseProvider phrases) {
        StringBuilder result = new StringBuilder();
        expand(result, phrases, new HashSet<String>());
        return result.toString();
    }

    @Override
    public void expand(StringBuilder result, PhraseProvider phrases, Set<String> touched) {
        if(touched.contains(name))
            // Avoid infinite recursion.
            return;
        touched.add(name);
        try {
            for(PhraseExpander expander : expanders)
                expander.expand(result, phrases, touched);
        } finally {
            touched.remove(name);
        }
    }
};
