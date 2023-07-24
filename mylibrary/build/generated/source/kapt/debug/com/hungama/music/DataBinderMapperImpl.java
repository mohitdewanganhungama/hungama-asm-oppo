package com.hungama.music;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import androidx.databinding.DataBinderMapper;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import com.hungama.music.databinding.ActLoginMainBindingImpl;
import com.hungama.music.databinding.ActivitySplashBindingImpl;
import com.hungama.music.databinding.FrAddSongPlaylistBindingImpl;
import com.hungama.music.databinding.FrBlankBindingImpl;
import com.hungama.music.databinding.FrDashboardBindingImpl;
import com.hungama.music.databinding.FrDownloadinProgressBindingImpl;
import com.hungama.music.databinding.FrGeneralSettingBindingImpl;
import com.hungama.music.databinding.FrLibraryBlankBindingImpl;
import com.hungama.music.databinding.FrMainBindingImpl;
import com.hungama.music.databinding.FrMainLibraryBindingImpl;
import com.hungama.music.databinding.FrQueueBindingImpl;
import com.hungama.music.databinding.FrQueueLatestBindingImpl;
import com.hungama.music.databinding.FragmentMoreBucketListBindingImpl;
import com.hungama.music.databinding.FragmentMoreVideosBindingImpl;
import com.hungama.music.databinding.LayoutBottomSheetBindingImpl;
import com.hungama.music.databinding.LayoutEmptyViewBindingImpl;
import com.hungama.music.databinding.LayoutProgressBindingImpl;
import com.hungama.music.databinding.RowAddPodcastBindingImpl;
import com.hungama.music.databinding.RowAlbumDetailV1BindingImpl;
import com.hungama.music.databinding.RowArtistMerchandiseBindingImpl;
import com.hungama.music.databinding.RowArtistMusicVideoBindingImpl;
import com.hungama.music.databinding.RowArtistNewReleaseBindingImpl;
import com.hungama.music.databinding.RowArtistUpcomingBindingImpl;
import com.hungama.music.databinding.RowBucketBindingImpl;
import com.hungama.music.databinding.RowBucketMovieBindingImpl;
import com.hungama.music.databinding.RowBucketMovieTrailerBindingImpl;
import com.hungama.music.databinding.RowChartDetailV1BindingImpl;
import com.hungama.music.databinding.RowChartDetailV2BindingImpl;
import com.hungama.music.databinding.RowChartDetailV3BindingImpl;
import com.hungama.music.databinding.RowChartDetailV4BindingImpl;
import com.hungama.music.databinding.RowCountryBindingImpl;
import com.hungama.music.databinding.RowDownloadDetailBindingImpl;
import com.hungama.music.databinding.RowDownloadInProgressBindingImpl;
import com.hungama.music.databinding.RowFavoritedSongsDetailBindingImpl;
import com.hungama.music.databinding.RowItype1001BindingImpl;
import com.hungama.music.databinding.RowItype10BindingImpl;
import com.hungama.music.databinding.RowItype10DynamicBindingImpl;
import com.hungama.music.databinding.RowItype10MoreBindingImpl;
import com.hungama.music.databinding.RowItype11BindingImpl;
import com.hungama.music.databinding.RowItype11DynamicBindingImpl;
import com.hungama.music.databinding.RowItype11MoreBindingImpl;
import com.hungama.music.databinding.RowItype12BindingImpl;
import com.hungama.music.databinding.RowItype12DynamicBindingImpl;
import com.hungama.music.databinding.RowItype12MoreBindingImpl;
import com.hungama.music.databinding.RowItype13BindingImpl;
import com.hungama.music.databinding.RowItype13DynamicBindingImpl;
import com.hungama.music.databinding.RowItype13MoreBindingImpl;
import com.hungama.music.databinding.RowItype13OldBindingImpl;
import com.hungama.music.databinding.RowItype14BindingImpl;
import com.hungama.music.databinding.RowItype14DynamicBindingImpl;
import com.hungama.music.databinding.RowItype15BindingImpl;
import com.hungama.music.databinding.RowItype15DynamicBindingImpl;
import com.hungama.music.databinding.RowItype15MoreBindingImpl;
import com.hungama.music.databinding.RowItype16BindingImpl;
import com.hungama.music.databinding.RowItype16DynamicBindingImpl;
import com.hungama.music.databinding.RowItype16MoreBindingImpl;
import com.hungama.music.databinding.RowItype18BindingImpl;
import com.hungama.music.databinding.RowItype18DynamicBindingImpl;
import com.hungama.music.databinding.RowItype19BindingImpl;
import com.hungama.music.databinding.RowItype19DynamicBindingImpl;
import com.hungama.music.databinding.RowItype1BindingImpl;
import com.hungama.music.databinding.RowItype1DynamicBindingImpl;
import com.hungama.music.databinding.RowItype1MoreBindingImpl;
import com.hungama.music.databinding.RowItype20BindingImpl;
import com.hungama.music.databinding.RowItype20DynamicBindingImpl;
import com.hungama.music.databinding.RowItype20OldBindingImpl;
import com.hungama.music.databinding.RowItype21BindingImpl;
import com.hungama.music.databinding.RowItype21DynamicBindingImpl;
import com.hungama.music.databinding.RowItype21MoreBindingImpl;
import com.hungama.music.databinding.RowItype22BindingImpl;
import com.hungama.music.databinding.RowItype22DynamicBindingImpl;
import com.hungama.music.databinding.RowItype22MoreBindingImpl;
import com.hungama.music.databinding.RowItype23BindingImpl;
import com.hungama.music.databinding.RowItype23DynamicBindingImpl;
import com.hungama.music.databinding.RowItype2BindingImpl;
import com.hungama.music.databinding.RowItype2DynamicBindingImpl;
import com.hungama.music.databinding.RowItype2MoreBindingImpl;
import com.hungama.music.databinding.RowItype31BindingImpl;
import com.hungama.music.databinding.RowItype3BindingImpl;
import com.hungama.music.databinding.RowItype3DynamicBindingImpl;
import com.hungama.music.databinding.RowItype3OldBindingImpl;
import com.hungama.music.databinding.RowItype41BindingImpl;
import com.hungama.music.databinding.RowItype41DynamicBindingImpl;
import com.hungama.music.databinding.RowItype41MoreBindingImpl;
import com.hungama.music.databinding.RowItype42BindingImpl;
import com.hungama.music.databinding.RowItype42DynamicBindingImpl;
import com.hungama.music.databinding.RowItype42MoreBindingImpl;
import com.hungama.music.databinding.RowItype43BindingImpl;
import com.hungama.music.databinding.RowItype43DynamicBindingImpl;
import com.hungama.music.databinding.RowItype43MoreBindingImpl;
import com.hungama.music.databinding.RowItype45DynamicBindingImpl;
import com.hungama.music.databinding.RowItype46DynamicBindingImpl;
import com.hungama.music.databinding.RowItype47OriginalBindingImpl;
import com.hungama.music.databinding.RowItype48BindingImpl;
import com.hungama.music.databinding.RowItype4BindingImpl;
import com.hungama.music.databinding.RowItype4DynamicBindingImpl;
import com.hungama.music.databinding.RowItype50BindingImpl;
import com.hungama.music.databinding.RowItype51BindingImpl;
import com.hungama.music.databinding.RowItype51EpisodesViewBindingImpl;
import com.hungama.music.databinding.RowItype52BindingImpl;
import com.hungama.music.databinding.RowItype5BindingImpl;
import com.hungama.music.databinding.RowItype5DynamicBindingImpl;
import com.hungama.music.databinding.RowItype5MoreBindingImpl;
import com.hungama.music.databinding.RowItype6BindingImpl;
import com.hungama.music.databinding.RowItype6DynamicBindingImpl;
import com.hungama.music.databinding.RowItype6MoreBindingImpl;
import com.hungama.music.databinding.RowItype7BindingImpl;
import com.hungama.music.databinding.RowItype7DynamicBindingImpl;
import com.hungama.music.databinding.RowItype8BindingImpl;
import com.hungama.music.databinding.RowItype8DynamicBindingImpl;
import com.hungama.music.databinding.RowItype8MoreBindingImpl;
import com.hungama.music.databinding.RowItype9BindingImpl;
import com.hungama.music.databinding.RowItype9DynamicBindingImpl;
import com.hungama.music.databinding.RowItype9MoreBindingImpl;
import com.hungama.music.databinding.RowItypeInapp1002BindingImpl;
import com.hungama.music.databinding.RowItypeInapp101BindingImpl;
import com.hungama.music.databinding.RowItypeInapp102BindingImpl;
import com.hungama.music.databinding.RowItypeInapp103BindingImpl;
import com.hungama.music.databinding.RowItypeInapp104BindingImpl;
import com.hungama.music.databinding.RowItypeOrignalBindingImpl;
import com.hungama.music.databinding.RowLanguageBindingImpl;
import com.hungama.music.databinding.RowLocalDeviceSongsLayoutBindingImpl;
import com.hungama.music.databinding.RowNewShortFlimBindingImpl;
import com.hungama.music.databinding.RowNewsBindingImpl;
import com.hungama.music.databinding.RowNewsPodcastBindingImpl;
import com.hungama.music.databinding.RowNewsPodcastNewBindingImpl;
import com.hungama.music.databinding.RowNowPlayingBindingImpl;
import com.hungama.music.databinding.RowPickSongBindingImpl;
import com.hungama.music.databinding.RowPlaylistAddSongBindingImpl;
import com.hungama.music.databinding.RowPlaylistBindingImpl;
import com.hungama.music.databinding.RowPlaylistDetailV1BindingImpl;
import com.hungama.music.databinding.RowPopularVideoBindingImpl;
import com.hungama.music.databinding.RowQueueBindingImpl;
import com.hungama.music.databinding.RowQueueHistoryBindingImpl;
import com.hungama.music.databinding.RowRecentHistoryBindingImpl;
import com.hungama.music.databinding.RowRecommandedVideoBindingImpl;
import com.hungama.music.databinding.RowSelectLangPopupBindingImpl;
import com.hungama.music.databinding.RowSelectMoodPopupBindingImpl;
import com.hungama.music.databinding.RowSelectMusicPlaybackQualityBindingImpl;
import com.hungama.music.databinding.RowSelectTempoBindingImpl;
import com.hungama.music.databinding.RowSongBindingImpl;
import com.hungama.music.databinding.RowTrenMovieBindingImpl;
import com.hungama.music.databinding.RowTrendBollywoodBindingImpl;
import com.hungama.music.databinding.RowTvShowDetailBindingImpl;
import com.hungama.music.databinding.RowUpcomingLivePerformanceBindingImpl;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.RuntimeException;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataBinderMapperImpl extends DataBinderMapper {
  private static final int LAYOUT_ACTLOGINMAIN = 1;

  private static final int LAYOUT_ACTIVITYSPLASH = 2;

  private static final int LAYOUT_FRADDSONGPLAYLIST = 3;

  private static final int LAYOUT_FRBLANK = 4;

  private static final int LAYOUT_FRDASHBOARD = 5;

  private static final int LAYOUT_FRDOWNLOADINPROGRESS = 6;

  private static final int LAYOUT_FRGENERALSETTING = 7;

  private static final int LAYOUT_FRLIBRARYBLANK = 8;

  private static final int LAYOUT_FRMAIN = 9;

  private static final int LAYOUT_FRMAINLIBRARY = 10;

  private static final int LAYOUT_FRQUEUE = 11;

  private static final int LAYOUT_FRQUEUELATEST = 12;

  private static final int LAYOUT_FRAGMENTMOREBUCKETLIST = 13;

  private static final int LAYOUT_FRAGMENTMOREVIDEOS = 14;

  private static final int LAYOUT_LAYOUTBOTTOMSHEET = 15;

  private static final int LAYOUT_LAYOUTEMPTYVIEW = 16;

  private static final int LAYOUT_LAYOUTPROGRESS = 17;

  private static final int LAYOUT_ROWADDPODCAST = 18;

  private static final int LAYOUT_ROWALBUMDETAILV1 = 19;

  private static final int LAYOUT_ROWARTISTMERCHANDISE = 20;

  private static final int LAYOUT_ROWARTISTMUSICVIDEO = 21;

  private static final int LAYOUT_ROWARTISTNEWRELEASE = 22;

  private static final int LAYOUT_ROWARTISTUPCOMING = 23;

  private static final int LAYOUT_ROWBUCKET = 24;

  private static final int LAYOUT_ROWBUCKETMOVIE = 25;

  private static final int LAYOUT_ROWBUCKETMOVIETRAILER = 26;

  private static final int LAYOUT_ROWCHARTDETAILV1 = 27;

  private static final int LAYOUT_ROWCHARTDETAILV2 = 28;

  private static final int LAYOUT_ROWCHARTDETAILV3 = 29;

  private static final int LAYOUT_ROWCHARTDETAILV4 = 30;

  private static final int LAYOUT_ROWCOUNTRY = 31;

  private static final int LAYOUT_ROWDOWNLOADDETAIL = 32;

  private static final int LAYOUT_ROWDOWNLOADINPROGRESS = 33;

  private static final int LAYOUT_ROWFAVORITEDSONGSDETAIL = 34;

  private static final int LAYOUT_ROWITYPE1 = 35;

  private static final int LAYOUT_ROWITYPE10 = 36;

  private static final int LAYOUT_ROWITYPE1001 = 37;

  private static final int LAYOUT_ROWITYPE10DYNAMIC = 38;

  private static final int LAYOUT_ROWITYPE10MORE = 39;

  private static final int LAYOUT_ROWITYPE11 = 40;

  private static final int LAYOUT_ROWITYPE11DYNAMIC = 41;

  private static final int LAYOUT_ROWITYPE11MORE = 42;

  private static final int LAYOUT_ROWITYPE12 = 43;

  private static final int LAYOUT_ROWITYPE12DYNAMIC = 44;

  private static final int LAYOUT_ROWITYPE12MORE = 45;

  private static final int LAYOUT_ROWITYPE13 = 46;

  private static final int LAYOUT_ROWITYPE13DYNAMIC = 47;

  private static final int LAYOUT_ROWITYPE13MORE = 48;

  private static final int LAYOUT_ROWITYPE13OLD = 49;

  private static final int LAYOUT_ROWITYPE14 = 50;

  private static final int LAYOUT_ROWITYPE14DYNAMIC = 51;

  private static final int LAYOUT_ROWITYPE15 = 52;

  private static final int LAYOUT_ROWITYPE15DYNAMIC = 53;

  private static final int LAYOUT_ROWITYPE15MORE = 54;

  private static final int LAYOUT_ROWITYPE16 = 55;

  private static final int LAYOUT_ROWITYPE16DYNAMIC = 56;

  private static final int LAYOUT_ROWITYPE16MORE = 57;

  private static final int LAYOUT_ROWITYPE18 = 58;

  private static final int LAYOUT_ROWITYPE18DYNAMIC = 59;

  private static final int LAYOUT_ROWITYPE19 = 60;

  private static final int LAYOUT_ROWITYPE19DYNAMIC = 61;

  private static final int LAYOUT_ROWITYPE1DYNAMIC = 62;

  private static final int LAYOUT_ROWITYPE1MORE = 63;

  private static final int LAYOUT_ROWITYPE2 = 64;

  private static final int LAYOUT_ROWITYPE20 = 65;

  private static final int LAYOUT_ROWITYPE20DYNAMIC = 66;

  private static final int LAYOUT_ROWITYPE20OLD = 67;

  private static final int LAYOUT_ROWITYPE21 = 68;

  private static final int LAYOUT_ROWITYPE21DYNAMIC = 69;

  private static final int LAYOUT_ROWITYPE21MORE = 70;

  private static final int LAYOUT_ROWITYPE22 = 71;

  private static final int LAYOUT_ROWITYPE22DYNAMIC = 72;

  private static final int LAYOUT_ROWITYPE22MORE = 73;

  private static final int LAYOUT_ROWITYPE23 = 74;

  private static final int LAYOUT_ROWITYPE23DYNAMIC = 75;

  private static final int LAYOUT_ROWITYPE2DYNAMIC = 76;

  private static final int LAYOUT_ROWITYPE2MORE = 77;

  private static final int LAYOUT_ROWITYPE3 = 78;

  private static final int LAYOUT_ROWITYPE31 = 79;

  private static final int LAYOUT_ROWITYPE3DYNAMIC = 80;

  private static final int LAYOUT_ROWITYPE3OLD = 81;

  private static final int LAYOUT_ROWITYPE4 = 82;

  private static final int LAYOUT_ROWITYPE41 = 83;

  private static final int LAYOUT_ROWITYPE41DYNAMIC = 84;

  private static final int LAYOUT_ROWITYPE41MORE = 85;

  private static final int LAYOUT_ROWITYPE42 = 86;

  private static final int LAYOUT_ROWITYPE42DYNAMIC = 87;

  private static final int LAYOUT_ROWITYPE42MORE = 88;

  private static final int LAYOUT_ROWITYPE43 = 89;

  private static final int LAYOUT_ROWITYPE43DYNAMIC = 90;

  private static final int LAYOUT_ROWITYPE43MORE = 91;

  private static final int LAYOUT_ROWITYPE45DYNAMIC = 92;

  private static final int LAYOUT_ROWITYPE46DYNAMIC = 93;

  private static final int LAYOUT_ROWITYPE47ORIGINAL = 94;

  private static final int LAYOUT_ROWITYPE48 = 95;

  private static final int LAYOUT_ROWITYPE4DYNAMIC = 96;

  private static final int LAYOUT_ROWITYPE5 = 97;

  private static final int LAYOUT_ROWITYPE50 = 98;

  private static final int LAYOUT_ROWITYPE51EPISODESVIEW = 99;

  private static final int LAYOUT_ROWITYPE51 = 100;

  private static final int LAYOUT_ROWITYPE52 = 101;

  private static final int LAYOUT_ROWITYPE5DYNAMIC = 102;

  private static final int LAYOUT_ROWITYPE5MORE = 103;

  private static final int LAYOUT_ROWITYPE6 = 104;

  private static final int LAYOUT_ROWITYPE6DYNAMIC = 105;

  private static final int LAYOUT_ROWITYPE6MORE = 106;

  private static final int LAYOUT_ROWITYPE7 = 107;

  private static final int LAYOUT_ROWITYPE7DYNAMIC = 108;

  private static final int LAYOUT_ROWITYPE8 = 109;

  private static final int LAYOUT_ROWITYPE8DYNAMIC = 110;

  private static final int LAYOUT_ROWITYPE8MORE = 111;

  private static final int LAYOUT_ROWITYPE9 = 112;

  private static final int LAYOUT_ROWITYPE9DYNAMIC = 113;

  private static final int LAYOUT_ROWITYPE9MORE = 114;

  private static final int LAYOUT_ROWITYPEINAPP1002 = 115;

  private static final int LAYOUT_ROWITYPEINAPP101 = 116;

  private static final int LAYOUT_ROWITYPEINAPP102 = 117;

  private static final int LAYOUT_ROWITYPEINAPP103 = 118;

  private static final int LAYOUT_ROWITYPEINAPP104 = 119;

  private static final int LAYOUT_ROWITYPEORIGNAL = 120;

  private static final int LAYOUT_ROWLANGUAGE = 121;

  private static final int LAYOUT_ROWLOCALDEVICESONGSLAYOUT = 122;

  private static final int LAYOUT_ROWNEWSHORTFLIM = 123;

  private static final int LAYOUT_ROWNEWS = 124;

  private static final int LAYOUT_ROWNEWSPODCAST = 125;

  private static final int LAYOUT_ROWNEWSPODCASTNEW = 126;

  private static final int LAYOUT_ROWNOWPLAYING = 127;

  private static final int LAYOUT_ROWPICKSONG = 128;

  private static final int LAYOUT_ROWPLAYLIST = 129;

  private static final int LAYOUT_ROWPLAYLISTADDSONG = 130;

  private static final int LAYOUT_ROWPLAYLISTDETAILV1 = 131;

  private static final int LAYOUT_ROWPOPULARVIDEO = 132;

  private static final int LAYOUT_ROWQUEUE = 133;

  private static final int LAYOUT_ROWQUEUEHISTORY = 134;

  private static final int LAYOUT_ROWRECENTHISTORY = 135;

  private static final int LAYOUT_ROWRECOMMANDEDVIDEO = 136;

  private static final int LAYOUT_ROWSELECTLANGPOPUP = 137;

  private static final int LAYOUT_ROWSELECTMOODPOPUP = 138;

  private static final int LAYOUT_ROWSELECTMUSICPLAYBACKQUALITY = 139;

  private static final int LAYOUT_ROWSELECTTEMPO = 140;

  private static final int LAYOUT_ROWSONG = 141;

  private static final int LAYOUT_ROWTRENMOVIE = 142;

  private static final int LAYOUT_ROWTRENDBOLLYWOOD = 143;

  private static final int LAYOUT_ROWTVSHOWDETAIL = 144;

  private static final int LAYOUT_ROWUPCOMINGLIVEPERFORMANCE = 145;

  private static final SparseIntArray INTERNAL_LAYOUT_ID_LOOKUP = new SparseIntArray(145);

  static {
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.act_login_main, LAYOUT_ACTLOGINMAIN);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.activity_splash, LAYOUT_ACTIVITYSPLASH);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.fr_add_song_playlist, LAYOUT_FRADDSONGPLAYLIST);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.fr_blank, LAYOUT_FRBLANK);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.fr_dashboard, LAYOUT_FRDASHBOARD);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.fr_downloadin_progress, LAYOUT_FRDOWNLOADINPROGRESS);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.fr_general_setting, LAYOUT_FRGENERALSETTING);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.fr_library_blank, LAYOUT_FRLIBRARYBLANK);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.fr_main, LAYOUT_FRMAIN);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.fr_main_library, LAYOUT_FRMAINLIBRARY);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.fr_queue, LAYOUT_FRQUEUE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.fr_queue_latest, LAYOUT_FRQUEUELATEST);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.fragment_more_bucket_list, LAYOUT_FRAGMENTMOREBUCKETLIST);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.fragment_more_videos, LAYOUT_FRAGMENTMOREVIDEOS);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.layout_bottom_sheet, LAYOUT_LAYOUTBOTTOMSHEET);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.layout_empty_view, LAYOUT_LAYOUTEMPTYVIEW);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.layout_progress, LAYOUT_LAYOUTPROGRESS);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_add_podcast, LAYOUT_ROWADDPODCAST);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_album_detail_v1, LAYOUT_ROWALBUMDETAILV1);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_artist_merchandise, LAYOUT_ROWARTISTMERCHANDISE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_artist_music_video, LAYOUT_ROWARTISTMUSICVIDEO);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_artist_new_release, LAYOUT_ROWARTISTNEWRELEASE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_artist_upcoming, LAYOUT_ROWARTISTUPCOMING);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_bucket, LAYOUT_ROWBUCKET);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_bucket_movie, LAYOUT_ROWBUCKETMOVIE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_bucket_movie_trailer, LAYOUT_ROWBUCKETMOVIETRAILER);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_chart_detail_v1, LAYOUT_ROWCHARTDETAILV1);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_chart_detail_v2, LAYOUT_ROWCHARTDETAILV2);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_chart_detail_v3, LAYOUT_ROWCHARTDETAILV3);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_chart_detail_v4, LAYOUT_ROWCHARTDETAILV4);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_country, LAYOUT_ROWCOUNTRY);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_download_detail, LAYOUT_ROWDOWNLOADDETAIL);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_download_in_progress, LAYOUT_ROWDOWNLOADINPROGRESS);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_favorited_songs_detail, LAYOUT_ROWFAVORITEDSONGSDETAIL);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_1, LAYOUT_ROWITYPE1);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_10, LAYOUT_ROWITYPE10);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_1001, LAYOUT_ROWITYPE1001);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_10_dynamic, LAYOUT_ROWITYPE10DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_10_more, LAYOUT_ROWITYPE10MORE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_11, LAYOUT_ROWITYPE11);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_11_dynamic, LAYOUT_ROWITYPE11DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_11_more, LAYOUT_ROWITYPE11MORE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_12, LAYOUT_ROWITYPE12);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_12_dynamic, LAYOUT_ROWITYPE12DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_12_more, LAYOUT_ROWITYPE12MORE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_13, LAYOUT_ROWITYPE13);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_13_dynamic, LAYOUT_ROWITYPE13DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_13_more, LAYOUT_ROWITYPE13MORE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_13_old, LAYOUT_ROWITYPE13OLD);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_14, LAYOUT_ROWITYPE14);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_14_dynamic, LAYOUT_ROWITYPE14DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_15, LAYOUT_ROWITYPE15);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_15_dynamic, LAYOUT_ROWITYPE15DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_15_more, LAYOUT_ROWITYPE15MORE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_16, LAYOUT_ROWITYPE16);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_16_dynamic, LAYOUT_ROWITYPE16DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_16_more, LAYOUT_ROWITYPE16MORE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_18, LAYOUT_ROWITYPE18);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_18_dynamic, LAYOUT_ROWITYPE18DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_19, LAYOUT_ROWITYPE19);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_19_dynamic, LAYOUT_ROWITYPE19DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_1_dynamic, LAYOUT_ROWITYPE1DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_1_more, LAYOUT_ROWITYPE1MORE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_2, LAYOUT_ROWITYPE2);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_20, LAYOUT_ROWITYPE20);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_20_dynamic, LAYOUT_ROWITYPE20DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_20_old, LAYOUT_ROWITYPE20OLD);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_21, LAYOUT_ROWITYPE21);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_21_dynamic, LAYOUT_ROWITYPE21DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_21_more, LAYOUT_ROWITYPE21MORE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_22, LAYOUT_ROWITYPE22);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_22_dynamic, LAYOUT_ROWITYPE22DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_22_more, LAYOUT_ROWITYPE22MORE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_23, LAYOUT_ROWITYPE23);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_23_dynamic, LAYOUT_ROWITYPE23DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_2_dynamic, LAYOUT_ROWITYPE2DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_2_more, LAYOUT_ROWITYPE2MORE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_3, LAYOUT_ROWITYPE3);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_3_1, LAYOUT_ROWITYPE31);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_3_dynamic, LAYOUT_ROWITYPE3DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_3_old, LAYOUT_ROWITYPE3OLD);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_4, LAYOUT_ROWITYPE4);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_41, LAYOUT_ROWITYPE41);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_41_dynamic, LAYOUT_ROWITYPE41DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_41_more, LAYOUT_ROWITYPE41MORE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_42, LAYOUT_ROWITYPE42);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_42_dynamic, LAYOUT_ROWITYPE42DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_42_more, LAYOUT_ROWITYPE42MORE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_43, LAYOUT_ROWITYPE43);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_43_dynamic, LAYOUT_ROWITYPE43DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_43_more, LAYOUT_ROWITYPE43MORE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_45_dynamic, LAYOUT_ROWITYPE45DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_46_dynamic, LAYOUT_ROWITYPE46DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_47_original, LAYOUT_ROWITYPE47ORIGINAL);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_48, LAYOUT_ROWITYPE48);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_4_dynamic, LAYOUT_ROWITYPE4DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_5, LAYOUT_ROWITYPE5);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_50, LAYOUT_ROWITYPE50);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_51_episodes_view, LAYOUT_ROWITYPE51EPISODESVIEW);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_5_1, LAYOUT_ROWITYPE51);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_5_2, LAYOUT_ROWITYPE52);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_5_dynamic, LAYOUT_ROWITYPE5DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_5_more, LAYOUT_ROWITYPE5MORE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_6, LAYOUT_ROWITYPE6);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_6_dynamic, LAYOUT_ROWITYPE6DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_6_more, LAYOUT_ROWITYPE6MORE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_7, LAYOUT_ROWITYPE7);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_7_dynamic, LAYOUT_ROWITYPE7DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_8, LAYOUT_ROWITYPE8);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_8_dynamic, LAYOUT_ROWITYPE8DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_8_more, LAYOUT_ROWITYPE8MORE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_9, LAYOUT_ROWITYPE9);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_9_dynamic, LAYOUT_ROWITYPE9DYNAMIC);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_9_more, LAYOUT_ROWITYPE9MORE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_inapp_1002, LAYOUT_ROWITYPEINAPP1002);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_inapp_101, LAYOUT_ROWITYPEINAPP101);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_inapp_102, LAYOUT_ROWITYPEINAPP102);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_inapp_103, LAYOUT_ROWITYPEINAPP103);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_inapp_104, LAYOUT_ROWITYPEINAPP104);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_itype_orignal, LAYOUT_ROWITYPEORIGNAL);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_language, LAYOUT_ROWLANGUAGE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_local_device_songs_layout, LAYOUT_ROWLOCALDEVICESONGSLAYOUT);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_new_short_flim, LAYOUT_ROWNEWSHORTFLIM);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_news, LAYOUT_ROWNEWS);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_news_podcast, LAYOUT_ROWNEWSPODCAST);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_news_podcast_new, LAYOUT_ROWNEWSPODCASTNEW);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_now_playing, LAYOUT_ROWNOWPLAYING);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_pick_song, LAYOUT_ROWPICKSONG);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_playlist, LAYOUT_ROWPLAYLIST);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_playlist_add_song, LAYOUT_ROWPLAYLISTADDSONG);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_playlist_detail_v1, LAYOUT_ROWPLAYLISTDETAILV1);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_popular_video, LAYOUT_ROWPOPULARVIDEO);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_queue, LAYOUT_ROWQUEUE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_queue_history, LAYOUT_ROWQUEUEHISTORY);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_recent_history, LAYOUT_ROWRECENTHISTORY);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_recommanded_video, LAYOUT_ROWRECOMMANDEDVIDEO);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_select_lang_popup, LAYOUT_ROWSELECTLANGPOPUP);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_select_mood_popup, LAYOUT_ROWSELECTMOODPOPUP);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_select_music_playback_quality, LAYOUT_ROWSELECTMUSICPLAYBACKQUALITY);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_select_tempo, LAYOUT_ROWSELECTTEMPO);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_song, LAYOUT_ROWSONG);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_tren_movie, LAYOUT_ROWTRENMOVIE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_trend_bollywood, LAYOUT_ROWTRENDBOLLYWOOD);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_tv_show_detail, LAYOUT_ROWTVSHOWDETAIL);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.hungama.music.R.layout.row_upcoming_live_performance, LAYOUT_ROWUPCOMINGLIVEPERFORMANCE);
  }

  private final ViewDataBinding internalGetViewDataBinding0(DataBindingComponent component,
      View view, int internalId, Object tag) {
    switch(internalId) {
      case  LAYOUT_ACTLOGINMAIN: {
        if ("layout/act_login_main_0".equals(tag)) {
          return new ActLoginMainBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for act_login_main is invalid. Received: " + tag);
      }
      case  LAYOUT_ACTIVITYSPLASH: {
        if ("layout/activity_splash_0".equals(tag)) {
          return new ActivitySplashBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for activity_splash is invalid. Received: " + tag);
      }
      case  LAYOUT_FRADDSONGPLAYLIST: {
        if ("layout/fr_add_song_playlist_0".equals(tag)) {
          return new FrAddSongPlaylistBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for fr_add_song_playlist is invalid. Received: " + tag);
      }
      case  LAYOUT_FRBLANK: {
        if ("layout/fr_blank_0".equals(tag)) {
          return new FrBlankBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for fr_blank is invalid. Received: " + tag);
      }
      case  LAYOUT_FRDASHBOARD: {
        if ("layout/fr_dashboard_0".equals(tag)) {
          return new FrDashboardBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for fr_dashboard is invalid. Received: " + tag);
      }
      case  LAYOUT_FRDOWNLOADINPROGRESS: {
        if ("layout/fr_downloadin_progress_0".equals(tag)) {
          return new FrDownloadinProgressBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for fr_downloadin_progress is invalid. Received: " + tag);
      }
      case  LAYOUT_FRGENERALSETTING: {
        if ("layout/fr_general_setting_0".equals(tag)) {
          return new FrGeneralSettingBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for fr_general_setting is invalid. Received: " + tag);
      }
      case  LAYOUT_FRLIBRARYBLANK: {
        if ("layout/fr_library_blank_0".equals(tag)) {
          return new FrLibraryBlankBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for fr_library_blank is invalid. Received: " + tag);
      }
      case  LAYOUT_FRMAIN: {
        if ("layout/fr_main_0".equals(tag)) {
          return new FrMainBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for fr_main is invalid. Received: " + tag);
      }
      case  LAYOUT_FRMAINLIBRARY: {
        if ("layout/fr_main_library_0".equals(tag)) {
          return new FrMainLibraryBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for fr_main_library is invalid. Received: " + tag);
      }
      case  LAYOUT_FRQUEUE: {
        if ("layout/fr_queue_0".equals(tag)) {
          return new FrQueueBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for fr_queue is invalid. Received: " + tag);
      }
      case  LAYOUT_FRQUEUELATEST: {
        if ("layout/fr_queue_latest_0".equals(tag)) {
          return new FrQueueLatestBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for fr_queue_latest is invalid. Received: " + tag);
      }
      case  LAYOUT_FRAGMENTMOREBUCKETLIST: {
        if ("layout/fragment_more_bucket_list_0".equals(tag)) {
          return new FragmentMoreBucketListBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for fragment_more_bucket_list is invalid. Received: " + tag);
      }
      case  LAYOUT_FRAGMENTMOREVIDEOS: {
        if ("layout/fragment_more_videos_0".equals(tag)) {
          return new FragmentMoreVideosBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for fragment_more_videos is invalid. Received: " + tag);
      }
      case  LAYOUT_LAYOUTBOTTOMSHEET: {
        if ("layout/layout_bottom_sheet_0".equals(tag)) {
          return new LayoutBottomSheetBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for layout_bottom_sheet is invalid. Received: " + tag);
      }
      case  LAYOUT_LAYOUTEMPTYVIEW: {
        if ("layout/layout_empty_view_0".equals(tag)) {
          return new LayoutEmptyViewBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for layout_empty_view is invalid. Received: " + tag);
      }
      case  LAYOUT_LAYOUTPROGRESS: {
        if ("layout/layout_progress_0".equals(tag)) {
          return new LayoutProgressBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for layout_progress is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWADDPODCAST: {
        if ("layout/row_add_podcast_0".equals(tag)) {
          return new RowAddPodcastBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_add_podcast is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWALBUMDETAILV1: {
        if ("layout/row_album_detail_v1_0".equals(tag)) {
          return new RowAlbumDetailV1BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_album_detail_v1 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWARTISTMERCHANDISE: {
        if ("layout/row_artist_merchandise_0".equals(tag)) {
          return new RowArtistMerchandiseBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_artist_merchandise is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWARTISTMUSICVIDEO: {
        if ("layout/row_artist_music_video_0".equals(tag)) {
          return new RowArtistMusicVideoBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_artist_music_video is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWARTISTNEWRELEASE: {
        if ("layout/row_artist_new_release_0".equals(tag)) {
          return new RowArtistNewReleaseBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_artist_new_release is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWARTISTUPCOMING: {
        if ("layout/row_artist_upcoming_0".equals(tag)) {
          return new RowArtistUpcomingBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_artist_upcoming is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWBUCKET: {
        if ("layout/row_bucket_0".equals(tag)) {
          return new RowBucketBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_bucket is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWBUCKETMOVIE: {
        if ("layout/row_bucket_movie_0".equals(tag)) {
          return new RowBucketMovieBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_bucket_movie is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWBUCKETMOVIETRAILER: {
        if ("layout/row_bucket_movie_trailer_0".equals(tag)) {
          return new RowBucketMovieTrailerBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_bucket_movie_trailer is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWCHARTDETAILV1: {
        if ("layout/row_chart_detail_v1_0".equals(tag)) {
          return new RowChartDetailV1BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_chart_detail_v1 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWCHARTDETAILV2: {
        if ("layout/row_chart_detail_v2_0".equals(tag)) {
          return new RowChartDetailV2BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_chart_detail_v2 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWCHARTDETAILV3: {
        if ("layout/row_chart_detail_v3_0".equals(tag)) {
          return new RowChartDetailV3BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_chart_detail_v3 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWCHARTDETAILV4: {
        if ("layout/row_chart_detail_v4_0".equals(tag)) {
          return new RowChartDetailV4BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_chart_detail_v4 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWCOUNTRY: {
        if ("layout/row_country_0".equals(tag)) {
          return new RowCountryBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_country is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWDOWNLOADDETAIL: {
        if ("layout/row_download_detail_0".equals(tag)) {
          return new RowDownloadDetailBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_download_detail is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWDOWNLOADINPROGRESS: {
        if ("layout/row_download_in_progress_0".equals(tag)) {
          return new RowDownloadInProgressBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_download_in_progress is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWFAVORITEDSONGSDETAIL: {
        if ("layout/row_favorited_songs_detail_0".equals(tag)) {
          return new RowFavoritedSongsDetailBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_favorited_songs_detail is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE1: {
        if ("layout/row_itype_1_0".equals(tag)) {
          return new RowItype1BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_1 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE10: {
        if ("layout/row_itype_10_0".equals(tag)) {
          return new RowItype10BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_10 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE1001: {
        if ("layout/row_itype_1001_0".equals(tag)) {
          return new RowItype1001BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_1001 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE10DYNAMIC: {
        if ("layout/row_itype_10_dynamic_0".equals(tag)) {
          return new RowItype10DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_10_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE10MORE: {
        if ("layout/row_itype_10_more_0".equals(tag)) {
          return new RowItype10MoreBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_10_more is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE11: {
        if ("layout/row_itype_11_0".equals(tag)) {
          return new RowItype11BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_11 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE11DYNAMIC: {
        if ("layout/row_itype_11_dynamic_0".equals(tag)) {
          return new RowItype11DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_11_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE11MORE: {
        if ("layout/row_itype_11_more_0".equals(tag)) {
          return new RowItype11MoreBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_11_more is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE12: {
        if ("layout/row_itype_12_0".equals(tag)) {
          return new RowItype12BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_12 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE12DYNAMIC: {
        if ("layout/row_itype_12_dynamic_0".equals(tag)) {
          return new RowItype12DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_12_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE12MORE: {
        if ("layout/row_itype_12_more_0".equals(tag)) {
          return new RowItype12MoreBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_12_more is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE13: {
        if ("layout/row_itype_13_0".equals(tag)) {
          return new RowItype13BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_13 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE13DYNAMIC: {
        if ("layout/row_itype_13_dynamic_0".equals(tag)) {
          return new RowItype13DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_13_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE13MORE: {
        if ("layout/row_itype_13_more_0".equals(tag)) {
          return new RowItype13MoreBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_13_more is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE13OLD: {
        if ("layout/row_itype_13_old_0".equals(tag)) {
          return new RowItype13OldBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_13_old is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE14: {
        if ("layout/row_itype_14_0".equals(tag)) {
          return new RowItype14BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_14 is invalid. Received: " + tag);
      }
    }
    return null;
  }

  private final ViewDataBinding internalGetViewDataBinding1(DataBindingComponent component,
      View view, int internalId, Object tag) {
    switch(internalId) {
      case  LAYOUT_ROWITYPE14DYNAMIC: {
        if ("layout/row_itype_14_dynamic_0".equals(tag)) {
          return new RowItype14DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_14_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE15: {
        if ("layout/row_itype_15_0".equals(tag)) {
          return new RowItype15BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_15 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE15DYNAMIC: {
        if ("layout/row_itype_15_dynamic_0".equals(tag)) {
          return new RowItype15DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_15_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE15MORE: {
        if ("layout/row_itype_15_more_0".equals(tag)) {
          return new RowItype15MoreBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_15_more is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE16: {
        if ("layout/row_itype_16_0".equals(tag)) {
          return new RowItype16BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_16 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE16DYNAMIC: {
        if ("layout/row_itype_16_dynamic_0".equals(tag)) {
          return new RowItype16DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_16_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE16MORE: {
        if ("layout/row_itype_16_more_0".equals(tag)) {
          return new RowItype16MoreBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_16_more is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE18: {
        if ("layout/row_itype_18_0".equals(tag)) {
          return new RowItype18BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_18 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE18DYNAMIC: {
        if ("layout/row_itype_18_dynamic_0".equals(tag)) {
          return new RowItype18DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_18_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE19: {
        if ("layout/row_itype_19_0".equals(tag)) {
          return new RowItype19BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_19 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE19DYNAMIC: {
        if ("layout/row_itype_19_dynamic_0".equals(tag)) {
          return new RowItype19DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_19_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE1DYNAMIC: {
        if ("layout/row_itype_1_dynamic_0".equals(tag)) {
          return new RowItype1DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_1_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE1MORE: {
        if ("layout/row_itype_1_more_0".equals(tag)) {
          return new RowItype1MoreBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_1_more is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE2: {
        if ("layout/row_itype_2_0".equals(tag)) {
          return new RowItype2BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_2 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE20: {
        if ("layout/row_itype_20_0".equals(tag)) {
          return new RowItype20BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_20 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE20DYNAMIC: {
        if ("layout/row_itype_20_dynamic_0".equals(tag)) {
          return new RowItype20DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_20_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE20OLD: {
        if ("layout/row_itype_20_old_0".equals(tag)) {
          return new RowItype20OldBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_20_old is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE21: {
        if ("layout/row_itype_21_0".equals(tag)) {
          return new RowItype21BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_21 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE21DYNAMIC: {
        if ("layout/row_itype_21_dynamic_0".equals(tag)) {
          return new RowItype21DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_21_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE21MORE: {
        if ("layout/row_itype_21_more_0".equals(tag)) {
          return new RowItype21MoreBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_21_more is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE22: {
        if ("layout/row_itype_22_0".equals(tag)) {
          return new RowItype22BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_22 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE22DYNAMIC: {
        if ("layout/row_itype_22_dynamic_0".equals(tag)) {
          return new RowItype22DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_22_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE22MORE: {
        if ("layout/row_itype_22_more_0".equals(tag)) {
          return new RowItype22MoreBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_22_more is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE23: {
        if ("layout/row_itype_23_0".equals(tag)) {
          return new RowItype23BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_23 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE23DYNAMIC: {
        if ("layout/row_itype_23_dynamic_0".equals(tag)) {
          return new RowItype23DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_23_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE2DYNAMIC: {
        if ("layout/row_itype_2_dynamic_0".equals(tag)) {
          return new RowItype2DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_2_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE2MORE: {
        if ("layout/row_itype_2_more_0".equals(tag)) {
          return new RowItype2MoreBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_2_more is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE3: {
        if ("layout/row_itype_3_0".equals(tag)) {
          return new RowItype3BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_3 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE31: {
        if ("layout/row_itype_3_1_0".equals(tag)) {
          return new RowItype31BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_3_1 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE3DYNAMIC: {
        if ("layout/row_itype_3_dynamic_0".equals(tag)) {
          return new RowItype3DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_3_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE3OLD: {
        if ("layout/row_itype_3_old_0".equals(tag)) {
          return new RowItype3OldBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_3_old is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE4: {
        if ("layout/row_itype_4_0".equals(tag)) {
          return new RowItype4BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_4 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE41: {
        if ("layout/row_itype_41_0".equals(tag)) {
          return new RowItype41BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_41 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE41DYNAMIC: {
        if ("layout/row_itype_41_dynamic_0".equals(tag)) {
          return new RowItype41DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_41_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE41MORE: {
        if ("layout/row_itype_41_more_0".equals(tag)) {
          return new RowItype41MoreBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_41_more is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE42: {
        if ("layout/row_itype_42_0".equals(tag)) {
          return new RowItype42BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_42 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE42DYNAMIC: {
        if ("layout/row_itype_42_dynamic_0".equals(tag)) {
          return new RowItype42DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_42_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE42MORE: {
        if ("layout/row_itype_42_more_0".equals(tag)) {
          return new RowItype42MoreBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_42_more is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE43: {
        if ("layout/row_itype_43_0".equals(tag)) {
          return new RowItype43BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_43 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE43DYNAMIC: {
        if ("layout/row_itype_43_dynamic_0".equals(tag)) {
          return new RowItype43DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_43_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE43MORE: {
        if ("layout/row_itype_43_more_0".equals(tag)) {
          return new RowItype43MoreBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_43_more is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE45DYNAMIC: {
        if ("layout/row_itype_45_dynamic_0".equals(tag)) {
          return new RowItype45DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_45_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE46DYNAMIC: {
        if ("layout/row_itype_46_dynamic_0".equals(tag)) {
          return new RowItype46DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_46_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE47ORIGINAL: {
        if ("layout/row_itype_47_original_0".equals(tag)) {
          return new RowItype47OriginalBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_47_original is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE48: {
        if ("layout/row_itype_48_0".equals(tag)) {
          return new RowItype48BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_48 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE4DYNAMIC: {
        if ("layout/row_itype_4_dynamic_0".equals(tag)) {
          return new RowItype4DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_4_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE5: {
        if ("layout/row_itype_5_0".equals(tag)) {
          return new RowItype5BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_5 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE50: {
        if ("layout/row_itype_50_0".equals(tag)) {
          return new RowItype50BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_50 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE51EPISODESVIEW: {
        if ("layout/row_itype_51_episodes_view_0".equals(tag)) {
          return new RowItype51EpisodesViewBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_51_episodes_view is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE51: {
        if ("layout/row_itype_5_1_0".equals(tag)) {
          return new RowItype51BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_5_1 is invalid. Received: " + tag);
      }
    }
    return null;
  }

  private final ViewDataBinding internalGetViewDataBinding2(DataBindingComponent component,
      View view, int internalId, Object tag) {
    switch(internalId) {
      case  LAYOUT_ROWITYPE52: {
        if ("layout/row_itype_5_2_0".equals(tag)) {
          return new RowItype52BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_5_2 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE5DYNAMIC: {
        if ("layout/row_itype_5_dynamic_0".equals(tag)) {
          return new RowItype5DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_5_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE5MORE: {
        if ("layout/row_itype_5_more_0".equals(tag)) {
          return new RowItype5MoreBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_5_more is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE6: {
        if ("layout/row_itype_6_0".equals(tag)) {
          return new RowItype6BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_6 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE6DYNAMIC: {
        if ("layout/row_itype_6_dynamic_0".equals(tag)) {
          return new RowItype6DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_6_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE6MORE: {
        if ("layout/row_itype_6_more_0".equals(tag)) {
          return new RowItype6MoreBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_6_more is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE7: {
        if ("layout/row_itype_7_0".equals(tag)) {
          return new RowItype7BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_7 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE7DYNAMIC: {
        if ("layout/row_itype_7_dynamic_0".equals(tag)) {
          return new RowItype7DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_7_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE8: {
        if ("layout/row_itype_8_0".equals(tag)) {
          return new RowItype8BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_8 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE8DYNAMIC: {
        if ("layout/row_itype_8_dynamic_0".equals(tag)) {
          return new RowItype8DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_8_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE8MORE: {
        if ("layout/row_itype_8_more_0".equals(tag)) {
          return new RowItype8MoreBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_8_more is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE9: {
        if ("layout/row_itype_9_0".equals(tag)) {
          return new RowItype9BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_9 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE9DYNAMIC: {
        if ("layout/row_itype_9_dynamic_0".equals(tag)) {
          return new RowItype9DynamicBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_9_dynamic is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPE9MORE: {
        if ("layout/row_itype_9_more_0".equals(tag)) {
          return new RowItype9MoreBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_9_more is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPEINAPP1002: {
        if ("layout/row_itype_inapp_1002_0".equals(tag)) {
          return new RowItypeInapp1002BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_inapp_1002 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPEINAPP101: {
        if ("layout/row_itype_inapp_101_0".equals(tag)) {
          return new RowItypeInapp101BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_inapp_101 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPEINAPP102: {
        if ("layout/row_itype_inapp_102_0".equals(tag)) {
          return new RowItypeInapp102BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_inapp_102 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPEINAPP103: {
        if ("layout/row_itype_inapp_103_0".equals(tag)) {
          return new RowItypeInapp103BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_inapp_103 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPEINAPP104: {
        if ("layout/row_itype_inapp_104_0".equals(tag)) {
          return new RowItypeInapp104BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_inapp_104 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWITYPEORIGNAL: {
        if ("layout/row_itype_orignal_0".equals(tag)) {
          return new RowItypeOrignalBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_itype_orignal is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWLANGUAGE: {
        if ("layout/row_language_0".equals(tag)) {
          return new RowLanguageBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_language is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWLOCALDEVICESONGSLAYOUT: {
        if ("layout/row_local_device_songs_layout_0".equals(tag)) {
          return new RowLocalDeviceSongsLayoutBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_local_device_songs_layout is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWNEWSHORTFLIM: {
        if ("layout/row_new_short_flim_0".equals(tag)) {
          return new RowNewShortFlimBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_new_short_flim is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWNEWS: {
        if ("layout/row_news_0".equals(tag)) {
          return new RowNewsBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_news is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWNEWSPODCAST: {
        if ("layout/row_news_podcast_0".equals(tag)) {
          return new RowNewsPodcastBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_news_podcast is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWNEWSPODCASTNEW: {
        if ("layout/row_news_podcast_new_0".equals(tag)) {
          return new RowNewsPodcastNewBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_news_podcast_new is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWNOWPLAYING: {
        if ("layout/row_now_playing_0".equals(tag)) {
          return new RowNowPlayingBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_now_playing is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWPICKSONG: {
        if ("layout/row_pick_song_0".equals(tag)) {
          return new RowPickSongBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_pick_song is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWPLAYLIST: {
        if ("layout/row_playlist_0".equals(tag)) {
          return new RowPlaylistBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_playlist is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWPLAYLISTADDSONG: {
        if ("layout/row_playlist_add_song_0".equals(tag)) {
          return new RowPlaylistAddSongBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_playlist_add_song is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWPLAYLISTDETAILV1: {
        if ("layout/row_playlist_detail_v1_0".equals(tag)) {
          return new RowPlaylistDetailV1BindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_playlist_detail_v1 is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWPOPULARVIDEO: {
        if ("layout/row_popular_video_0".equals(tag)) {
          return new RowPopularVideoBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_popular_video is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWQUEUE: {
        if ("layout/row_queue_0".equals(tag)) {
          return new RowQueueBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_queue is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWQUEUEHISTORY: {
        if ("layout/row_queue_history_0".equals(tag)) {
          return new RowQueueHistoryBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_queue_history is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWRECENTHISTORY: {
        if ("layout/row_recent_history_0".equals(tag)) {
          return new RowRecentHistoryBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_recent_history is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWRECOMMANDEDVIDEO: {
        if ("layout/row_recommanded_video_0".equals(tag)) {
          return new RowRecommandedVideoBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_recommanded_video is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWSELECTLANGPOPUP: {
        if ("layout/row_select_lang_popup_0".equals(tag)) {
          return new RowSelectLangPopupBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_select_lang_popup is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWSELECTMOODPOPUP: {
        if ("layout/row_select_mood_popup_0".equals(tag)) {
          return new RowSelectMoodPopupBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_select_mood_popup is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWSELECTMUSICPLAYBACKQUALITY: {
        if ("layout/row_select_music_playback_quality_0".equals(tag)) {
          return new RowSelectMusicPlaybackQualityBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_select_music_playback_quality is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWSELECTTEMPO: {
        if ("layout/row_select_tempo_0".equals(tag)) {
          return new RowSelectTempoBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_select_tempo is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWSONG: {
        if ("layout/row_song_0".equals(tag)) {
          return new RowSongBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_song is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWTRENMOVIE: {
        if ("layout/row_tren_movie_0".equals(tag)) {
          return new RowTrenMovieBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_tren_movie is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWTRENDBOLLYWOOD: {
        if ("layout/row_trend_bollywood_0".equals(tag)) {
          return new RowTrendBollywoodBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_trend_bollywood is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWTVSHOWDETAIL: {
        if ("layout/row_tv_show_detail_0".equals(tag)) {
          return new RowTvShowDetailBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_tv_show_detail is invalid. Received: " + tag);
      }
      case  LAYOUT_ROWUPCOMINGLIVEPERFORMANCE: {
        if ("layout/row_upcoming_live_performance_0".equals(tag)) {
          return new RowUpcomingLivePerformanceBindingImpl(component, view);
        }
        throw new IllegalArgumentException("The tag for row_upcoming_live_performance is invalid. Received: " + tag);
      }
    }
    return null;
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View view, int layoutId) {
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = view.getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      // find which method will have it. -1 is necessary becausefirst id starts with 1;
      int methodIndex = (localizedLayoutId - 1) / 50;
      switch(methodIndex) {
        case 0: {
          return internalGetViewDataBinding0(component, view, localizedLayoutId, tag);
        }
        case 1: {
          return internalGetViewDataBinding1(component, view, localizedLayoutId, tag);
        }
        case 2: {
          return internalGetViewDataBinding2(component, view, localizedLayoutId, tag);
        }
      }
    }
    return null;
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View[] views, int layoutId) {
    if(views == null || views.length == 0) {
      return null;
    }
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = views[0].getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
      }
    }
    return null;
  }

  @Override
  public int getLayoutId(String tag) {
    if (tag == null) {
      return 0;
    }
    Integer tmpVal = InnerLayoutIdLookup.sKeys.get(tag);
    return tmpVal == null ? 0 : tmpVal;
  }

  @Override
  public String convertBrIdToString(int localId) {
    String tmpVal = InnerBrLookup.sKeys.get(localId);
    return tmpVal;
  }

  @Override
  public List<DataBinderMapper> collectDependencies() {
    ArrayList<DataBinderMapper> result = new ArrayList<DataBinderMapper>(1);
    result.add(new androidx.databinding.library.baseAdapters.DataBinderMapperImpl());
    return result;
  }

  private static class InnerBrLookup {
    static final SparseArray<String> sKeys = new SparseArray<String>(1);

    static {
      sKeys.put(0, "_all");
    }
  }

  private static class InnerLayoutIdLookup {
    static final HashMap<String, Integer> sKeys = new HashMap<String, Integer>(145);

    static {
      sKeys.put("layout/act_login_main_0", com.hungama.music.R.layout.act_login_main);
      sKeys.put("layout/activity_splash_0", com.hungama.music.R.layout.activity_splash);
      sKeys.put("layout/fr_add_song_playlist_0", com.hungama.music.R.layout.fr_add_song_playlist);
      sKeys.put("layout/fr_blank_0", com.hungama.music.R.layout.fr_blank);
      sKeys.put("layout/fr_dashboard_0", com.hungama.music.R.layout.fr_dashboard);
      sKeys.put("layout/fr_downloadin_progress_0", com.hungama.music.R.layout.fr_downloadin_progress);
      sKeys.put("layout/fr_general_setting_0", com.hungama.music.R.layout.fr_general_setting);
      sKeys.put("layout/fr_library_blank_0", com.hungama.music.R.layout.fr_library_blank);
      sKeys.put("layout/fr_main_0", com.hungama.music.R.layout.fr_main);
      sKeys.put("layout/fr_main_library_0", com.hungama.music.R.layout.fr_main_library);
      sKeys.put("layout/fr_queue_0", com.hungama.music.R.layout.fr_queue);
      sKeys.put("layout/fr_queue_latest_0", com.hungama.music.R.layout.fr_queue_latest);
      sKeys.put("layout/fragment_more_bucket_list_0", com.hungama.music.R.layout.fragment_more_bucket_list);
      sKeys.put("layout/fragment_more_videos_0", com.hungama.music.R.layout.fragment_more_videos);
      sKeys.put("layout/layout_bottom_sheet_0", com.hungama.music.R.layout.layout_bottom_sheet);
      sKeys.put("layout/layout_empty_view_0", com.hungama.music.R.layout.layout_empty_view);
      sKeys.put("layout/layout_progress_0", com.hungama.music.R.layout.layout_progress);
      sKeys.put("layout/row_add_podcast_0", com.hungama.music.R.layout.row_add_podcast);
      sKeys.put("layout/row_album_detail_v1_0", com.hungama.music.R.layout.row_album_detail_v1);
      sKeys.put("layout/row_artist_merchandise_0", com.hungama.music.R.layout.row_artist_merchandise);
      sKeys.put("layout/row_artist_music_video_0", com.hungama.music.R.layout.row_artist_music_video);
      sKeys.put("layout/row_artist_new_release_0", com.hungama.music.R.layout.row_artist_new_release);
      sKeys.put("layout/row_artist_upcoming_0", com.hungama.music.R.layout.row_artist_upcoming);
      sKeys.put("layout/row_bucket_0", com.hungama.music.R.layout.row_bucket);
      sKeys.put("layout/row_bucket_movie_0", com.hungama.music.R.layout.row_bucket_movie);
      sKeys.put("layout/row_bucket_movie_trailer_0", com.hungama.music.R.layout.row_bucket_movie_trailer);
      sKeys.put("layout/row_chart_detail_v1_0", com.hungama.music.R.layout.row_chart_detail_v1);
      sKeys.put("layout/row_chart_detail_v2_0", com.hungama.music.R.layout.row_chart_detail_v2);
      sKeys.put("layout/row_chart_detail_v3_0", com.hungama.music.R.layout.row_chart_detail_v3);
      sKeys.put("layout/row_chart_detail_v4_0", com.hungama.music.R.layout.row_chart_detail_v4);
      sKeys.put("layout/row_country_0", com.hungama.music.R.layout.row_country);
      sKeys.put("layout/row_download_detail_0", com.hungama.music.R.layout.row_download_detail);
      sKeys.put("layout/row_download_in_progress_0", com.hungama.music.R.layout.row_download_in_progress);
      sKeys.put("layout/row_favorited_songs_detail_0", com.hungama.music.R.layout.row_favorited_songs_detail);
      sKeys.put("layout/row_itype_1_0", com.hungama.music.R.layout.row_itype_1);
      sKeys.put("layout/row_itype_10_0", com.hungama.music.R.layout.row_itype_10);
      sKeys.put("layout/row_itype_1001_0", com.hungama.music.R.layout.row_itype_1001);
      sKeys.put("layout/row_itype_10_dynamic_0", com.hungama.music.R.layout.row_itype_10_dynamic);
      sKeys.put("layout/row_itype_10_more_0", com.hungama.music.R.layout.row_itype_10_more);
      sKeys.put("layout/row_itype_11_0", com.hungama.music.R.layout.row_itype_11);
      sKeys.put("layout/row_itype_11_dynamic_0", com.hungama.music.R.layout.row_itype_11_dynamic);
      sKeys.put("layout/row_itype_11_more_0", com.hungama.music.R.layout.row_itype_11_more);
      sKeys.put("layout/row_itype_12_0", com.hungama.music.R.layout.row_itype_12);
      sKeys.put("layout/row_itype_12_dynamic_0", com.hungama.music.R.layout.row_itype_12_dynamic);
      sKeys.put("layout/row_itype_12_more_0", com.hungama.music.R.layout.row_itype_12_more);
      sKeys.put("layout/row_itype_13_0", com.hungama.music.R.layout.row_itype_13);
      sKeys.put("layout/row_itype_13_dynamic_0", com.hungama.music.R.layout.row_itype_13_dynamic);
      sKeys.put("layout/row_itype_13_more_0", com.hungama.music.R.layout.row_itype_13_more);
      sKeys.put("layout/row_itype_13_old_0", com.hungama.music.R.layout.row_itype_13_old);
      sKeys.put("layout/row_itype_14_0", com.hungama.music.R.layout.row_itype_14);
      sKeys.put("layout/row_itype_14_dynamic_0", com.hungama.music.R.layout.row_itype_14_dynamic);
      sKeys.put("layout/row_itype_15_0", com.hungama.music.R.layout.row_itype_15);
      sKeys.put("layout/row_itype_15_dynamic_0", com.hungama.music.R.layout.row_itype_15_dynamic);
      sKeys.put("layout/row_itype_15_more_0", com.hungama.music.R.layout.row_itype_15_more);
      sKeys.put("layout/row_itype_16_0", com.hungama.music.R.layout.row_itype_16);
      sKeys.put("layout/row_itype_16_dynamic_0", com.hungama.music.R.layout.row_itype_16_dynamic);
      sKeys.put("layout/row_itype_16_more_0", com.hungama.music.R.layout.row_itype_16_more);
      sKeys.put("layout/row_itype_18_0", com.hungama.music.R.layout.row_itype_18);
      sKeys.put("layout/row_itype_18_dynamic_0", com.hungama.music.R.layout.row_itype_18_dynamic);
      sKeys.put("layout/row_itype_19_0", com.hungama.music.R.layout.row_itype_19);
      sKeys.put("layout/row_itype_19_dynamic_0", com.hungama.music.R.layout.row_itype_19_dynamic);
      sKeys.put("layout/row_itype_1_dynamic_0", com.hungama.music.R.layout.row_itype_1_dynamic);
      sKeys.put("layout/row_itype_1_more_0", com.hungama.music.R.layout.row_itype_1_more);
      sKeys.put("layout/row_itype_2_0", com.hungama.music.R.layout.row_itype_2);
      sKeys.put("layout/row_itype_20_0", com.hungama.music.R.layout.row_itype_20);
      sKeys.put("layout/row_itype_20_dynamic_0", com.hungama.music.R.layout.row_itype_20_dynamic);
      sKeys.put("layout/row_itype_20_old_0", com.hungama.music.R.layout.row_itype_20_old);
      sKeys.put("layout/row_itype_21_0", com.hungama.music.R.layout.row_itype_21);
      sKeys.put("layout/row_itype_21_dynamic_0", com.hungama.music.R.layout.row_itype_21_dynamic);
      sKeys.put("layout/row_itype_21_more_0", com.hungama.music.R.layout.row_itype_21_more);
      sKeys.put("layout/row_itype_22_0", com.hungama.music.R.layout.row_itype_22);
      sKeys.put("layout/row_itype_22_dynamic_0", com.hungama.music.R.layout.row_itype_22_dynamic);
      sKeys.put("layout/row_itype_22_more_0", com.hungama.music.R.layout.row_itype_22_more);
      sKeys.put("layout/row_itype_23_0", com.hungama.music.R.layout.row_itype_23);
      sKeys.put("layout/row_itype_23_dynamic_0", com.hungama.music.R.layout.row_itype_23_dynamic);
      sKeys.put("layout/row_itype_2_dynamic_0", com.hungama.music.R.layout.row_itype_2_dynamic);
      sKeys.put("layout/row_itype_2_more_0", com.hungama.music.R.layout.row_itype_2_more);
      sKeys.put("layout/row_itype_3_0", com.hungama.music.R.layout.row_itype_3);
      sKeys.put("layout/row_itype_3_1_0", com.hungama.music.R.layout.row_itype_3_1);
      sKeys.put("layout/row_itype_3_dynamic_0", com.hungama.music.R.layout.row_itype_3_dynamic);
      sKeys.put("layout/row_itype_3_old_0", com.hungama.music.R.layout.row_itype_3_old);
      sKeys.put("layout/row_itype_4_0", com.hungama.music.R.layout.row_itype_4);
      sKeys.put("layout/row_itype_41_0", com.hungama.music.R.layout.row_itype_41);
      sKeys.put("layout/row_itype_41_dynamic_0", com.hungama.music.R.layout.row_itype_41_dynamic);
      sKeys.put("layout/row_itype_41_more_0", com.hungama.music.R.layout.row_itype_41_more);
      sKeys.put("layout/row_itype_42_0", com.hungama.music.R.layout.row_itype_42);
      sKeys.put("layout/row_itype_42_dynamic_0", com.hungama.music.R.layout.row_itype_42_dynamic);
      sKeys.put("layout/row_itype_42_more_0", com.hungama.music.R.layout.row_itype_42_more);
      sKeys.put("layout/row_itype_43_0", com.hungama.music.R.layout.row_itype_43);
      sKeys.put("layout/row_itype_43_dynamic_0", com.hungama.music.R.layout.row_itype_43_dynamic);
      sKeys.put("layout/row_itype_43_more_0", com.hungama.music.R.layout.row_itype_43_more);
      sKeys.put("layout/row_itype_45_dynamic_0", com.hungama.music.R.layout.row_itype_45_dynamic);
      sKeys.put("layout/row_itype_46_dynamic_0", com.hungama.music.R.layout.row_itype_46_dynamic);
      sKeys.put("layout/row_itype_47_original_0", com.hungama.music.R.layout.row_itype_47_original);
      sKeys.put("layout/row_itype_48_0", com.hungama.music.R.layout.row_itype_48);
      sKeys.put("layout/row_itype_4_dynamic_0", com.hungama.music.R.layout.row_itype_4_dynamic);
      sKeys.put("layout/row_itype_5_0", com.hungama.music.R.layout.row_itype_5);
      sKeys.put("layout/row_itype_50_0", com.hungama.music.R.layout.row_itype_50);
      sKeys.put("layout/row_itype_51_episodes_view_0", com.hungama.music.R.layout.row_itype_51_episodes_view);
      sKeys.put("layout/row_itype_5_1_0", com.hungama.music.R.layout.row_itype_5_1);
      sKeys.put("layout/row_itype_5_2_0", com.hungama.music.R.layout.row_itype_5_2);
      sKeys.put("layout/row_itype_5_dynamic_0", com.hungama.music.R.layout.row_itype_5_dynamic);
      sKeys.put("layout/row_itype_5_more_0", com.hungama.music.R.layout.row_itype_5_more);
      sKeys.put("layout/row_itype_6_0", com.hungama.music.R.layout.row_itype_6);
      sKeys.put("layout/row_itype_6_dynamic_0", com.hungama.music.R.layout.row_itype_6_dynamic);
      sKeys.put("layout/row_itype_6_more_0", com.hungama.music.R.layout.row_itype_6_more);
      sKeys.put("layout/row_itype_7_0", com.hungama.music.R.layout.row_itype_7);
      sKeys.put("layout/row_itype_7_dynamic_0", com.hungama.music.R.layout.row_itype_7_dynamic);
      sKeys.put("layout/row_itype_8_0", com.hungama.music.R.layout.row_itype_8);
      sKeys.put("layout/row_itype_8_dynamic_0", com.hungama.music.R.layout.row_itype_8_dynamic);
      sKeys.put("layout/row_itype_8_more_0", com.hungama.music.R.layout.row_itype_8_more);
      sKeys.put("layout/row_itype_9_0", com.hungama.music.R.layout.row_itype_9);
      sKeys.put("layout/row_itype_9_dynamic_0", com.hungama.music.R.layout.row_itype_9_dynamic);
      sKeys.put("layout/row_itype_9_more_0", com.hungama.music.R.layout.row_itype_9_more);
      sKeys.put("layout/row_itype_inapp_1002_0", com.hungama.music.R.layout.row_itype_inapp_1002);
      sKeys.put("layout/row_itype_inapp_101_0", com.hungama.music.R.layout.row_itype_inapp_101);
      sKeys.put("layout/row_itype_inapp_102_0", com.hungama.music.R.layout.row_itype_inapp_102);
      sKeys.put("layout/row_itype_inapp_103_0", com.hungama.music.R.layout.row_itype_inapp_103);
      sKeys.put("layout/row_itype_inapp_104_0", com.hungama.music.R.layout.row_itype_inapp_104);
      sKeys.put("layout/row_itype_orignal_0", com.hungama.music.R.layout.row_itype_orignal);
      sKeys.put("layout/row_language_0", com.hungama.music.R.layout.row_language);
      sKeys.put("layout/row_local_device_songs_layout_0", com.hungama.music.R.layout.row_local_device_songs_layout);
      sKeys.put("layout/row_new_short_flim_0", com.hungama.music.R.layout.row_new_short_flim);
      sKeys.put("layout/row_news_0", com.hungama.music.R.layout.row_news);
      sKeys.put("layout/row_news_podcast_0", com.hungama.music.R.layout.row_news_podcast);
      sKeys.put("layout/row_news_podcast_new_0", com.hungama.music.R.layout.row_news_podcast_new);
      sKeys.put("layout/row_now_playing_0", com.hungama.music.R.layout.row_now_playing);
      sKeys.put("layout/row_pick_song_0", com.hungama.music.R.layout.row_pick_song);
      sKeys.put("layout/row_playlist_0", com.hungama.music.R.layout.row_playlist);
      sKeys.put("layout/row_playlist_add_song_0", com.hungama.music.R.layout.row_playlist_add_song);
      sKeys.put("layout/row_playlist_detail_v1_0", com.hungama.music.R.layout.row_playlist_detail_v1);
      sKeys.put("layout/row_popular_video_0", com.hungama.music.R.layout.row_popular_video);
      sKeys.put("layout/row_queue_0", com.hungama.music.R.layout.row_queue);
      sKeys.put("layout/row_queue_history_0", com.hungama.music.R.layout.row_queue_history);
      sKeys.put("layout/row_recent_history_0", com.hungama.music.R.layout.row_recent_history);
      sKeys.put("layout/row_recommanded_video_0", com.hungama.music.R.layout.row_recommanded_video);
      sKeys.put("layout/row_select_lang_popup_0", com.hungama.music.R.layout.row_select_lang_popup);
      sKeys.put("layout/row_select_mood_popup_0", com.hungama.music.R.layout.row_select_mood_popup);
      sKeys.put("layout/row_select_music_playback_quality_0", com.hungama.music.R.layout.row_select_music_playback_quality);
      sKeys.put("layout/row_select_tempo_0", com.hungama.music.R.layout.row_select_tempo);
      sKeys.put("layout/row_song_0", com.hungama.music.R.layout.row_song);
      sKeys.put("layout/row_tren_movie_0", com.hungama.music.R.layout.row_tren_movie);
      sKeys.put("layout/row_trend_bollywood_0", com.hungama.music.R.layout.row_trend_bollywood);
      sKeys.put("layout/row_tv_show_detail_0", com.hungama.music.R.layout.row_tv_show_detail);
      sKeys.put("layout/row_upcoming_live_performance_0", com.hungama.music.R.layout.row_upcoming_live_performance);
    }
  }
}
