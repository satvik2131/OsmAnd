package net.osmand.plus.wikivoyage.data;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class PopularArticles {

	public static final int ARTICLES_PER_PAGE = 30;

	private final List<TravelArticle> articles;

	public PopularArticles() {
		this.articles = new ArrayList<>();
	}

	public PopularArticles(@NonNull PopularArticles articles) {
		this.articles = articles.articles;
	}

	public void clear() {
		articles.clear();
	}

	@NonNull
	public List<TravelArticle> getArticles() {
		return articles;
	}

	public boolean add(@NonNull TravelArticle article) {
		articles.add(article);
		return articles.size() % ARTICLES_PER_PAGE != 0;
	}

	public boolean contains(@NonNull TravelArticle article) {
		return articles.contains(article);
	}

	public boolean containsByRouteId(@NonNull String routeId) {
		for (TravelArticle article : articles) {
			if (article.getRouteId().equals(routeId)) {
				return true;
			}
		}
		return false;
	}
}