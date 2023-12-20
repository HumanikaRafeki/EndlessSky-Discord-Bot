package me.mcofficer.james.phrases;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import me.mcofficer.esparser.DataNode;

class Phrase implements PhraseExpander {

    private ArrayList<PhraseExpander> expanders;
    private String name;

    public Phrase(DataNode node) {
        expanders = new ArrayList<PhraseExpander>();
        ArrayList<String> tokens = node.getTokens();
        if(node.size() > 1)
            name = node.getTokens().get(1);
        else
            name = "NO NAME";
        // for(DataNode child : node.getChildren()) {
        //     if(child.size() < 1)
        //         continue;
        //     String nodeType = child.getTokens()[0];
        //     if(nodeType.equals("phrase"))
        //         expanders.append(new PhraseList(child));
        //     else if(nodeType.equals("word"))
        //         expanders.append(new WordList(child));
        //     else if(nodeType.equals("replace"))
        //         expanders.append(new Replacements(child));
        // }
    }

    public String getName() {
        return name;
    }

    @Override
    public void addPhrasesTo(PhraseProvider phrases) {
        if(name.length() > 0)
            phrases.setExpander(name, this);
        else
            phrases.setExpander("UNKNOWN", this);
    }

    @Override
    public void expand(StringBuilder result, PhraseProvider phrases, Set<String> touched) {
        touched.add(name);
        try {
            result.append('[').append(name).append(']');
            // for(PhraseExpander expander : expanders)
            //     expander.expand(result, phrases, touched);
        } finally {
            touched.remove(name);
        }
    }
};
