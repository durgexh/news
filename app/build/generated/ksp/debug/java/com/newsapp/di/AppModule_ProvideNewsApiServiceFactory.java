package com.newsapp.di;

import com.newsapp.data.NewsApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class AppModule_ProvideNewsApiServiceFactory implements Factory<NewsApiService> {
  @Override
  public NewsApiService get() {
    return provideNewsApiService();
  }

  public static AppModule_ProvideNewsApiServiceFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NewsApiService provideNewsApiService() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideNewsApiService());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideNewsApiServiceFactory INSTANCE = new AppModule_ProvideNewsApiServiceFactory();
  }
}
