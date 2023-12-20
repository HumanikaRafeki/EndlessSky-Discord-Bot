package me.mcofficer.james.phrases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import me.mcofficer.esparser.DataNode;

public class WordList implements PhraseExpander {

    protected static Pattern phrasePattern = Pattern.compile("\\$\\{[^}]*\\}");

    protected ArrayList<Choice> choices;
    private double accumulatedWeight = 1;

    public WordList(DataNode node) {
        this(node, true);
    }

    public WordList(DataNode node, boolean allowPhraseReferences) {
        choices = new ArrayList<Choice>();
        for(DataNode child : node.getChildren()) {
            addWord(child, allowPhraseReferences);
        }
        accumulateWeights();
    }

    @Override
    public void expand(StringBuilder result, PhraseProvider phrases, Set<String> touched) {
        if(choices.size() < 1)
            return;

        Choice chosen = randomChoice();

        if(chosen == null)
            return;
        else if(chosen.expander != null)
            chosen.expander.expand(result, phrases, touched);
        else if(chosen.word != null)
            result.append(chosen.word);
    }

    protected Choice randomChoice() {
        if(choices.size() == 0)
            return null;
        else if(choices.size() == 1)
            return choices.get(0);
        double accum = Math.random() * accumulatedWeight;
        int first = 0, last = choices.size() - 1;
        Choice chosen = choices.get(last);
        if(accum >= chosen.accum)
            return chosen;
        if(accum <= 0)
            return choices.get(0);
        while(last > first) {
            int middle = (first + last + 1) / 2;
            chosen = choices.get(middle);
            double middleAccum = chosen.accum;
            if(middleAccum > accum)
                last = middle - 1;
            else
                first = middle;
        }
        if(last == first)
            return chosen;
        if(first > choices.size())
            return choices.get(choices.size() - 1);
        return choices.get(0);
    }

    protected void accumulateWeights() {
        double accum = 0;
        for(Choice choice : choices) {
            choice.accum = accum;
            // Replace bad weights with 1
            if(!(choice.weight > 1e-5) || !(choice.weight < 1e+12))
                choice.weight = 1;
            accum += choice.weight;
        }
        accumulatedWeight = accum;
    }

    protected void addWord(DataNode node, boolean allowPhraseReferences) {
        ArrayList<String> tokens = node.getTokens();
        if(tokens.size() == 0)
            return;

        double weight = 1;
        if(node.size() > 1 && node.isNumberAt(1))
            Double.valueOf(tokens.get(1));

        choices.add(asChoice(weight, tokens.get(0), allowPhraseReferences));
    }

    private Choice asChoice(double weight, String token, boolean allowPhraseReferences) {
        // if(allowPhraseReferences && phraseMatcher.matcher(token).matches())
        //     return new Choice(weight, null, new WordExpander(token));
        // else
            return new Choice(weight, token, null);
    }
}
