package com.newsapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newsapp.data.NewsItem
import com.newsapp.data.RssParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.newsapp.data.NewsApiService
import java.net.URL

// 100+ Public RSS Feeds for categories, grouped regionally
val countryFeeds = mapOf(
    "Global \uD83C\uDF10" to mapOf(
        "All News" to listOf(
            "http://feeds.bbci.co.uk/news/world/rss.xml" to "BBC",
            "https://rss.nytimes.com/services/xml/rss/nyt/World.xml" to "New York Times",
            "http://rss.cnn.com/rss/edition_world.rss" to "CNN",
            "https://www.aljazeera.com/xml/rss/all.xml" to "Al Jazeera",
            "https://feeds.npr.org/1004/rss.xml" to "NPR",
            "https://feeds.washingtonpost.com/rss/world" to "Washington Post",
            "http://feeds.foxnews.com/foxnews/world" to "Fox News",
            "https://www.theguardian.com/world/rss" to "The Guardian",
            "https://abcnews.go.com/abcnews/internationalheadlines" to "ABC",
            "https://www.cbsnews.com/latest/rss/world" to "CBS",
            "https://time.com/feed/" to "Time",
            "https://feeds.skynews.com/feeds/rss/world.xml" to "Sky News",
            "https://feeds.a.dj.com/rss/RSSWorldNews.xml" to "Wall Street Journal",
            "https://www.independent.co.uk/news/world/rss" to "The Independent",
            "https://www.dailymail.co.uk/news/worldnews/index.rss" to "Daily Mail",
            "https://moxie.foxnews.com/google-publisher/world.xml" to "Fox World",
            "https://www.buzzfeednews.com/news.xml" to "BuzzFeed",
            "https://www.latimes.com/world/rss2.0.xml" to "LA Times",
            "https://nypost.com/world/feed/" to "New York Post",
            "https://www.scmp.com/rss/91/feed" to "SCMP",
            "https://globalnews.ca/world/feed/" to "Global News CA",
            "https://www.cbc.ca/cmlink/rss-world" to "CBC",
            "https://rss.dw.com/rdf/rss-en-all" to "DW",
            "https://www.france24.com/en/rss" to "France 24",
            "https://www.rt.com/rss/" to "RT",
            "https://themoscowtimes.com/rss/news" to "Moscow Times",
            "https://timesofindia.indiatimes.com/rssfeedstopstories.cms" to "Times of India",
            "https://www.thehindu.com/news/international/feeder/default.rss" to "The Hindu"
        ),
        "Geopolitics" to listOf(
            "https://www.aljazeera.com/xml/rss/all.xml" to "Al Jazeera",
            "https://www.foreignaffairs.com/rss.xml" to "Foreign Affairs",
            "https://foreignpolicy.com/feed/" to "Foreign Policy",
            "https://www.theguardian.com/world/politics/rss" to "The Guardian",
            "https://www.politico.com/rss/politicopicks.xml" to "Politico",
            "https://www.cfr.org/rss/articles" to "Council on Foreign Relations",
            "https://www.csis.org/rss/articles" to "CSIS",
            "https://warontherocks.com/feed/" to "War on the Rocks",
            "https://nationalinterest.org/rss" to "National Interest",
            "https://www.atlanticcouncil.org/feed/" to "Atlantic Council",
            "https://www.chathamhouse.org/rss/news" to "Chatham House",
            "https://www.brookings.edu/feed/" to "Brookings",
            "https://carnegieendowment.org/rss/articles" to "Carnegie",
            "https://www.rand.org/news/press.xml" to "RAND",
            "https://theintercept.com/feed/?lang=en" to "The Intercept"
        ),
        "eSports & Gaming" to listOf(
            "https://feeds.ign.com/ign/games-all" to "IGN",
            "https://www.polygon.com/rss/index.xml" to "Polygon",
            "https://www.pcgamer.com/rss/" to "PC Gamer",
            "https://www.gamesradar.com/rss/" to "GamesRadar",
            "https://esportsinsider.com/feed" to "Esports Insider",
            "https://dotesports.com/feed" to "Dot Esports",
            "https://www.rockpapershotgun.com/feed" to "Rock Paper Shotgun",
            "https://kotaku.com/rss" to "Kotaku",
            "https://www.nintendolife.com/feeds/latest" to "Nintendo Life",
            "https://www.pushsquare.com/feeds/latest" to "Push Square",
            "https://www.eurogamer.net/feed/news" to "Eurogamer",
            "https://www.gematsu.com/feed" to "Gematsu",
            "https://www.siliconera.com/feed/" to "Siliconera",
            "https://www.gamesindustry.biz/feed" to "GamesIndustry",
            "https://www.shacknews.com/rss" to "Shacknews"
        ),
        "Technology" to listOf(
            "https://techcrunch.com/feed/" to "TechCrunch",
            "https://www.theverge.com/rss/index.xml" to "The Verge",
            "https://www.wired.com/feed/rss" to "Wired",
            "https://www.cnet.com/rss/news/" to "CNET",
            "https://feeds.arstechnica.com/arstechnica/index" to "Ars Technica",
            "https://www.engadget.com/rss.xml" to "Engadget",
            "https://gizmodo.com/rss" to "Gizmodo",
            "https://mashable.com/feeds/rss/all" to "Mashable",
            "https://www.zdnet.com/news/rss.xml" to "ZDNet",
            "https://www.techradar.com/rss" to "TechRadar",
            "https://www.tomsguide.com/feeds/all" to "Tom's Guide",
            "https://www.androidpolice.com/feed/" to "Android Police",
            "https://www.macrumors.com/macrumors.xml" to "MacRumors",
            "https://venturebeat.com/feed/" to "VentureBeat",
            "https://readwrite.com/feed/" to "ReadWrite",
            "https://www.xda-developers.com/feed/" to "XDA"
        ),
        "Business & Finance" to listOf(
            "https://feeds.bloomberg.com/markets/news.xml" to "Bloomberg",
            "https://www.cnbc.com/id/10000846/device/rss/rss.html" to "CNBC",
            "https://www.forbes.com/business/feed/" to "Forbes",
            "https://www.ft.com/?format=rss" to "Financial Times",
            "https://feeds.businessinsider.com/custom/all" to "Business Insider",
            "https://www.economist.com/finance-and-economics/rss.xml" to "The Economist",
            "https://www.marketwatch.com/rss/topstories" to "MarketWatch",
            "https://www.wsj.com/xml/rss/3_7014.xml" to "WSJ",
            "https://www.fool.com/a/feeds/fool-all-rss" to "Motley Fool",
            "https://www.kiplinger.com/rss" to "Kiplinger",
            "https://fortune.com/feed/" to "Fortune",
            "https://hbr.org/feed" to "Harvard Business Review",
            "https://seekingalpha.com/market_currents.xml" to "Seeking Alpha",
            "https://www.investopedia.com/feed/" to "Investopedia"
        ),
        "Science" to listOf(
            "https://www.sciencedaily.com/rss/all.xml" to "Science Daily",
            "https://www.space.com/home/feed/site.xml" to "Space.com",
            "https://www.nature.com/nature.rss" to "Nature",
            "https://www.newscientist.com/feed/home/" to "New Scientist",
            "https://www.scientificamerican.com/page/rss/" to "Scientific American",
            "https://phys.org/rss-feed/" to "Phys.org",
            "https://www.sciencemag.org/rss/news_current.xml" to "Science Mag",
            "https://www.livescience.com/home/feed/site.xml" to "Live Science",
            "https://www.popsci.com/feed/" to "PopSci",
            "https://www.smithsonianmag.com/rss/science-nature/" to "Smithsonian",
            "https://www.sciencenews.org/feed" to "Science News",
            "https://earthsky.org/feed/" to "EarthSky"
        ),
        "Health" to listOf(
            "https://www.who.int/rss-feeds/news-english.xml" to "WHO",
            "https://khn.org/feed/" to "KFF Health News",
            "https://www.statnews.com/feed/" to "STAT",
            "https://tools.cdc.gov/api/v2/resources/media/403372.rss" to "CDC",
            "https://www.medscape.com/cx/rssfeeds/2700.xml" to "Medscape",
            "https://www.news-medical.net/tag/feed/Medical-Research.aspx" to "Medical News",
            "https://publichealth.jmir.org/feed/atom" to "JMIR",
            "https://www.nih.gov/news-events/news-releases/rss.xml" to "NIH"
        ),
        "Sports" to listOf(
            "https://www.espn.com/espn/rss/news" to "ESPN",
            "https://api.foxsports.com/v1/rss?setupId=sports" to "Fox Sports",
            "https://sports.yahoo.com/rss/" to "Yahoo",
            "https://www.cbssports.com/rss/headlines/" to "CBS",
            "https://www.skysports.com/rss/12040" to "Sky Sports",
            "https://www.si.com/.rss/full/" to "Sports Illustrated",
            "https://bleacherreport.com/articles/feed" to "Bleacher Report",
            "https://theathletic.com/feed/" to "The Athletic",
            "https://www.sbnation.com/rss/index.xml" to "SB Nation",
            "https://nypost.com/sports/feed/" to "NY Post Sports",
            "https://www.tsn.ca/rss/top%20stories.xml" to "TSN",
            "https://deadspin.com/rss" to "Deadspin"
        ),
        "Entertainment" to listOf(
            "https://variety.com/feed/" to "Variety",
            "https://www.hollywoodreporter.com/feed/" to "Hollywood Reporter",
            "https://www.tmz.com/rss.xml" to "TMZ",
            "https://www.eonline.com/syndication/feeds/rssfeeds/topstories.xml" to "E! News",
            "https://deadline.com/feed/" to "Deadline",
            "https://people.com/feed/" to "People",
            "https://entertainmentweekly.com/feed/" to "EW",
            "https://www.usmagazine.com/feed/" to "Us Weekly",
            "https://pitchfork.com/rss/news/" to "Pitchfork",
            "https://consequence.net/feed/" to "Consequence",
            "https://www.rollingstone.com/feed/" to "Rolling Stone",
            "https://www.billboard.com/feed/" to "Billboard",
            "https://screenrant.com/feed/" to "Screen Rant",
            "https://collider.com/feed/" to "Collider"
        ),
        "Education" to listOf(
            "http://feeds.bbci.co.uk/news/education/rss.xml" to "BBC Education",
            "https://www.edweek.org/feed/" to "Education Week",
            "https://www.insidehighered.com/rss/feed/news" to "Inside Higher Ed"
        ),
        "Weather" to listOf(
            "https://feeds.foxnews.com/foxnews/weather" to "Fox Weather",
            "https://rss.nytimes.com/services/xml/rss/nyt/Climate.xml" to "NYT Climate",
            "https://www.cbsnews.com/latest/rss/weather" to "CBS Weather"
        ),
        "Automobile" to listOf(
            "https://www.autoblog.com/rss.xml" to "Autoblog",
            "https://www.motortrend.com/feed/" to "MotorTrend",
            "https://www.caranddriver.com/rss/all.xml/" to "Car and Driver"
        ),
        "Crime & Law" to listOf(
            "https://lawandcrime.com/feed/" to "Law & Crime",
            "https://www.courthousenews.com/feed/" to "Courthouse News",
            "https://www.fbi.gov/feeds/fbi-in-the-news/rss.xml" to "FBI News"
        ),
        "Editorial" to listOf(
            "https://rss.nytimes.com/services/xml/rss/nyt/Opinion.xml" to "NYT Opinion",
            "https://feeds.washingtonpost.com/rss/opinions" to "Washington Post Opinion",
            "https://www.theguardian.com/uk/commentisfree/rss" to "Guardian Opinion"
        ),
        "Trending" to listOf(
            "https://www.buzzfeednews.com/news.xml" to "BuzzFeed Trending",
            "https://moxie.foxnews.com/google-publisher/latest.xml" to "Fox News Latest",
            "https://www.huffpost.com/section/front-page/feed" to "HuffPost"
        ),
        "National" to listOf(
            "https://feeds.npr.org/1003/rss.xml" to "NPR National",
            "https://abcnews.go.com/abcnews/usheadlines" to "ABC National",
            "https://www.cbsnews.com/latest/rss/us" to "CBS National"
        )
    ),
    "United States \uD83C\uDDFA\uD83C\uDDF8" to mapOf(
        "Top Stories" to listOf(
            "http://rss.cnn.com/rss/cnn_topstories.rss" to "CNN",
            "https://moxie.foxnews.com/google-publisher/latest.xml" to "Fox News",
            "https://rss.nytimes.com/services/xml/rss/nyt/HomePage.xml" to "New York Times",
            "https://feeds.npr.org/1001/rss.xml" to "NPR",
            "https://www.cbsnews.com/latest/rss/main" to "CBS News",
            "https://abcnews.go.com/abcnews/topstories" to "ABC News",
            "https://feeds.washingtonpost.com/rss/national" to "Washington Post",
            "https://nypost.com/feed/" to "New York Post",
            "https://www.latimes.com/local/rss2.0.xml" to "LA Times",
            "https://www.chicagotribune.com/arcio/rss/category/news/" to "Chicago Tribune"
        ),
        "Business" to listOf(
            "https://feeds.bloomberg.com/markets/news.xml" to "Bloomberg",
            "https://www.cnbc.com/id/10000115/device/rss/rss.html" to "CNBC",
            "https://www.wsj.com/xml/rss/3_7014.xml" to "WSJ",
            "https://fortune.com/feed/" to "Fortune",
            "https://www.marketwatch.com/rss/topstories" to "MarketWatch"
        ),
        "Sports" to listOf(
            "https://www.espn.com/espn/rss/news" to "ESPN",
            "https://api.foxsports.com/v1/rss?setupId=sports" to "Fox Sports",
            "https://www.cbssports.com/rss/headlines/" to "CBS Sports",
            "https://nypost.com/sports/feed/" to "NY Post Sports",
            "https://www.si.com/.rss/full/" to "Sports Illustrated"
        )
    ),
    "United Kingdom \uD83C\uDDEC\uD83C\uDDE7" to mapOf(
        "Top Stories" to listOf(
            "http://feeds.bbci.co.uk/news/rss.xml" to "BBC News",
            "https://feeds.skynews.com/feeds/rss/home.xml" to "Sky News",
            "https://www.theguardian.com/uk/rss" to "The Guardian",
            "https://www.telegraph.co.uk/news/rss.xml" to "The Telegraph",
            "https://www.independent.co.uk/news/uk/rss" to "The Independent",
            "https://www.dailymail.co.uk/news/index.rss" to "Daily Mail",
            "https://www.mirror.co.uk/news/?service=rss" to "The Mirror",
            "https://www.standard.co.uk/news/rss" to "Evening Standard"
        ),
        "Business" to listOf(
            "https://www.ft.com/?format=rss" to "Financial Times",
            "http://feeds.bbci.co.uk/news/business/rss.xml" to "BBC Business",
            "https://www.telegraph.co.uk/business/rss.xml" to "Telegraph Business",
            "https://feeds.skynews.com/feeds/rss/business.xml" to "Sky Business"
        ),
        "Sports" to listOf(
            "https://www.skysports.com/rss/12040" to "Sky Sports",
            "http://feeds.bbci.co.uk/sport/rss.xml" to "BBC Sport",
            "https://www.theguardian.com/uk/sport/rss" to "Guardian Sport",
            "https://www.telegraph.co.uk/sport/rss.xml" to "Telegraph Sport"
        )
    ),
    "India \uD83C\uDDEE\uD83C\uDDF3" to mapOf(
        "Top Stories" to listOf(
            "https://timesofindia.indiatimes.com/rssfeedstopstories.cms" to "Times of India",
            "https://feeds.feedburner.com/ndtvnews-top-stories" to "NDTV",
            "https://www.thehindu.com/news/national/feeder/default.rss" to "The Hindu",
            "https://www.hindustantimes.com/feeds/rss/topnews/rssfeed.xml" to "Hindustan Times",
            "https://indianexpress.com/feed/" to "Indian Express",
            "https://www.news18.com/rss/india.xml" to "News18",
            "https://zeenews.india.com/rss/india-national-news.xml" to "Zee News",
            "https://www.business-standard.com/rss/home_page_top_stories.rss" to "Business Standard"
        ),
        "Business" to listOf(
            "https://economictimes.indiatimes.com/rssfeedstopstories.cms" to "Economic Times",
            "https://www.livemint.com/rss/news" to "Mint",
            "https://www.financialexpress.com/feed/" to "Financial Express",
            "https://www.moneycontrol.com/rss/business.xml" to "Moneycontrol"
        ),
        "Sports" to listOf(
            "https://timesofindia.indiatimes.com/rssfeeds/4719148.cms" to "TOI Sports",
            "https://feeds.feedburner.com/ndtvsports-latest" to "NDTV Sports",
            "https://www.thehindu.com/sport/feeder/default.rss" to "Hindu Sports",
            "https://indianexpress.com/section/sports/feed/" to "Express Sports"
        )
    ),
    "Canada \uD83C\uDDE8\uD83C\uDDE6" to mapOf(
        "Top Stories" to listOf(
            "https://www.cbc.ca/cmlink/rss-topstories" to "CBC News",
            "https://globalnews.ca/feed/" to "Global News",
            "https://www.thestar.com/search/?f=rss&t=article&c=news&l=50&s=start_time&sd=desc" to "Toronto Star",
            "https://www.ctvnews.ca/rss/ctvnews-ca-top-stories-public-rss-1.822009" to "CTV News",
            "https://nationalpost.com/feed/" to "National Post",
            "https://vancouversun.com/feed/" to "Vancouver Sun",
            "https://calgaryherald.com/feed/" to "Calgary Herald"
        ),
        "Business" to listOf(
            "https://www.cbc.ca/cmlink/rss-business" to "CBC Business",
            "https://financialpost.com/feed/" to "Financial Post",
            "https://www.theglobeandmail.com/business/?service=rss" to "Globe and Mail",
            "https://www.thestar.com/search/?f=rss&t=article&c=business&l=50&s=start_time&sd=desc" to "Toronto Star Business"
        ),
        "Sports" to listOf(
            "https://www.tsn.ca/rss/top%20stories.xml" to "TSN",
            "https://www.sportsnet.ca/feed/" to "Sportsnet",
            "https://www.cbc.ca/cmlink/rss-sports" to "CBC Sports",
            "https://www.thestar.com/search/?f=rss&t=article&c=sports&l=50&s=start_time&sd=desc" to "Toronto Star Sports"
        )
    ),
    "Australia \uD83C\uDDE6\uD83C\uDDFA" to mapOf(
        "Top Stories" to listOf(
            "https://www.abc.net.au/news/feed/51120/rss.xml" to "ABC News",
            "https://www.smh.com.au/rss/feed.xml" to "Sydney Morning Herald",
            "https://www.theage.com.au/rss/feed.xml" to "The Age",
            "https://www.news.com.au/feed" to "News.com.au",
            "https://www.theguardian.com/australia-news/rss" to "The Guardian AU",
            "https://www.brisbanetimes.com.au/rss/feed.xml" to "Brisbane Times",
            "https://www.perthnow.com.au/rss" to "PerthNow"
        ),
        "Business" to listOf(
            "https://www.abc.net.au/news/feed/51892/rss.xml" to "ABC Business",
            "https://www.smh.com.au/business/rss/feed.xml" to "SMH Business",
            "https://www.afr.com/rss/feed.xml" to "Financial Review",
            "https://www.theage.com.au/business/rss/feed.xml" to "The Age Business"
        ),
        "Sports" to listOf(
            "https://www.abc.net.au/news/feed/45924/rss.xml" to "ABC Sport",
            "https://www.foxsports.com.au/content-feeds/all-sports/" to "Fox Sports AU",
            "https://www.smh.com.au/sport/rss/feed.xml" to "SMH Sport",
            "https://www.theage.com.au/sport/rss/feed.xml" to "The Age Sport"
        )
    ),
    "Local \uD83D\uDCCD" to mapOf(
        "Top Stories" to emptyList()
    )
)

import com.newsapp.data.NewsRepository
import com.newsapp.ui.NewsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val uiState: StateFlow<NewsUiState> = _uiState

    val supportedCountries = countryFeeds.keys.toList()

    private val _selectedCountry = MutableStateFlow("Global \uD83C\uDF10")
    val selectedCountry: StateFlow<String> = _selectedCountry

    private val _categories = MutableStateFlow(countryFeeds["Global \uD83C\uDF10"]?.keys?.toList() ?: emptyList())
    val categories: StateFlow<List<String>> = _categories

    private val _selectedCategory = MutableStateFlow("All News")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _localCity = MutableStateFlow<String?>(null)
    val localCity: StateFlow<String?> = _localCity

    init {
        fetchNews("All News", "Global \uD83C\uDF10")
    }

    fun selectCountry(country: String) {
        if (_selectedCountry.value != country) {
            _selectedCountry.value = country
            val newCategories = countryFeeds[country]?.keys?.toList() ?: emptyList()
            _categories.value = newCategories
            val defaultCategory = newCategories.firstOrNull() ?: ""
            _selectedCategory.value = defaultCategory
            
            if (defaultCategory.isNotEmpty()) {
                fetchNews(defaultCategory, country)
            } else {
                _uiState.value = NewsUiState.Success(emptyList())
            }
        }
    }

    fun selectCategory(category: String) {
        if (_selectedCategory.value != category) {
            _selectedCategory.value = category
            fetchNews(category, _selectedCountry.value)
        }
    }

    fun setLocalCity(city: String) {
        _localCity.value = city
        if (_selectedCountry.value == "Local \uD83D\uDCCD") {
            fetchNews(_selectedCategory.value, "Local \uD83D\uDCCD")
        }
    }

    fun refreshNews() {
        fetchNews(_selectedCategory.value, _selectedCountry.value)
    }

    private var observeJob: kotlinx.coroutines.Job? = null

    private fun fetchNews(category: String, country: String) {
        observeJob?.cancel()

        _uiState.value = NewsUiState.Loading

        observeJob = viewModelScope.launch {
            if (country == "Local \uD83D\uDCCD") {
                val city = _localCity.value
                if (!city.isNullOrBlank()) {
                    launch {
                        repository.observeLocalNews(city).collect { cachedNews ->
                            if (cachedNews.isNotEmpty()) {
                                _uiState.value = NewsUiState.Success(cachedNews)
                            }
                        }
                    }
                    try {
                        repository.refreshLocalNews(city)
                    } catch (e: Exception) {
                        if (_uiState.value !is NewsUiState.Success) {
                            _uiState.value = NewsUiState.Error(e.message ?: "Failed to fetch local news.")
                        }
                    }
                } else {
                    _uiState.value = NewsUiState.Success(emptyList())
                }
            } else {
                launch {
                    repository.observeNews(category, country).collect { cachedNews ->
                        if (cachedNews.isNotEmpty()) {
                            _uiState.value = NewsUiState.Success(cachedNews)
                        }
                    }
                }
                try {
                    val sources = countryFeeds[country]?.get(category) ?: emptyList()
                    if (sources.isNotEmpty()) {
                        repository.refreshNews(sources, category, country)
                    } else {
                        _uiState.value = NewsUiState.Success(emptyList())
                    }
                } catch (e: Exception) {
                    if (_uiState.value !is NewsUiState.Success) {
                        _uiState.value = NewsUiState.Error(e.message ?: "Failed to load news stream.")
                    }
                }
            }
        }
    }
}
