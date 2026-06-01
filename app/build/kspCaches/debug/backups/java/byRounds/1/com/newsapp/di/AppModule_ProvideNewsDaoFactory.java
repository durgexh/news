package com.newsapp.di;

import com.newsapp.data.local.NewsDao;
import com.newsapp.data.local.NewsDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideNewsDaoFactory implements Factory<NewsDao> {
  private final Provider<NewsDatabase> databaseProvider;

  public AppModule_ProvideNewsDaoFactory(Provider<NewsDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public NewsDao get() {
    return provideNewsDao(databaseProvider.get());
  }

  public static AppModule_ProvideNewsDaoFactory create(Provider<NewsDatabase> databaseProvider) {
    return new AppModule_ProvideNewsDaoFactory(databaseProvider);
  }

  public static NewsDao provideNewsDao(NewsDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideNewsDao(database));
  }
}
