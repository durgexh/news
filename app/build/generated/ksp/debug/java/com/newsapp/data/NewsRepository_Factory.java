package com.newsapp.data;

import com.newsapp.data.local.NewsDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class NewsRepository_Factory implements Factory<NewsRepository> {
  private final Provider<RssParser> parserProvider;

  private final Provider<NewsApiService> apiServiceProvider;

  private final Provider<NewsDao> newsDaoProvider;

  public NewsRepository_Factory(Provider<RssParser> parserProvider,
      Provider<NewsApiService> apiServiceProvider, Provider<NewsDao> newsDaoProvider) {
    this.parserProvider = parserProvider;
    this.apiServiceProvider = apiServiceProvider;
    this.newsDaoProvider = newsDaoProvider;
  }

  @Override
  public NewsRepository get() {
    return newInstance(parserProvider.get(), apiServiceProvider.get(), newsDaoProvider.get());
  }

  public static NewsRepository_Factory create(Provider<RssParser> parserProvider,
      Provider<NewsApiService> apiServiceProvider, Provider<NewsDao> newsDaoProvider) {
    return new NewsRepository_Factory(parserProvider, apiServiceProvider, newsDaoProvider);
  }

  public static NewsRepository newInstance(RssParser parser, NewsApiService apiService,
      NewsDao newsDao) {
    return new NewsRepository(parser, apiService, newsDao);
  }
}
