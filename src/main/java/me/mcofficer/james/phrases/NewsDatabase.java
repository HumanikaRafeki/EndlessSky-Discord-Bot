package me.mcofficer.james.phrases;

import me.mcofficer.esparser.DataNode;

import java.util.ArrayList;
import java.util.HashMap;

public class NewsDatabase {
    HashMap<String, News> news;
    public NewsDatabase() {
        news = new HashMap<String, News>();
    }
    public void addNews(ArrayList<DataNode> data) {
        for(DataNode node : data)
            if(node.size() > 1 && node.token(0).equals("news"))
                news.put(node.token(1), new News(node));
    }

    public String getNews(String name, PhraseDatabase phrases, PhraseLimits limits) {
        News item = news.getOrDefault(name, null);
        if(item == null)
            return null;
        return item.toString(phrases, limits);
    }
};
