package jp.androidbook.photostohint;

/* 定数クラス */

import com.google.android.gms.location.LocationRequest;

public final class Constants {

    /* パーミッション関連 */
    public static class Permission
    {
        //位置情報アクセスのパーミッションリクエストコード
        public static final int REQUEST_CODE_ACCESS_FINE_LOCATION=1000;

        //外部ストレージ書き込みのパーミッションリクエストコード
        public static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE=1001;

        //外部からのインスタンス生成制限
        private Permission(){}
    }

    /* ユーザアカウント関連 */
    public static class Account
    {
        //IDで使用可能な文字の種類
        public static final String CHAR_ID_CONTAIN ="半角英数字,「_」,「-」";

        //パスワードで使用可能な文字の種類
        public static final String CHAR_PASSWORD_CONTAIN ="半角英数字";

        //メールアドレスで使用可能な文字の種類
        public static final String CHAR_MAIL_CONTAIN ="半角英数字,「_」,「-」,「@」,「.」";

        //パスワードの最小長
        public static final int MIN_PASSWORD_LENGTH =8;

        //パスワードの最大長
        public static final int MAX_PASSWORD_LENGTH =30;

        //外部からのインスタンス生成制限
        private Account(){}
    }

    /* 汎用 */
    public static class Common
    {
        //デバッグモードか(0:OFF,1:ON)
        public static final int DEBUG_MODE=1;

        //地名
        public static final String TARGET_REGION ="八戸市中心街";

        //お題スポット口コミの文字列最大長
        public static final int MAX_THEME_SPOT_REVIEW_STRING_LENGTH=20;

        //歩きスマホ検知の加速度センサ変動閾値
        public static final double WALKING_DETECTION_THRESHOLD_SENSOR_VALUE=50.0;

        //歩きスマホ検知カウンターの値の上限
        public static final int MAX_WALKING_COUNTER=4;

        //歩きスマホ警告表示を出す閾値となる歩きスマホ検知カウンターの値(歩きスマホ検知カウンターの値がこの値以上になった時警告表示)
        public static final int WALKING_DETECTION_THRESHOLD_WALKING_COUNTER=3;

        //歩きスマホ検知カウンター更新の周期
        public static final int WALKING_DETECTION_INTERVAL=50;

        //写真表示用イメージビューのアスペクト比
        public static final double ASPECT_RATIO_HORIZONTAL=16.0;
        public static final double ASPECT_RATIO_VERTICAL=9.0;

        //カメラインテントリクエストコード
        public static final int REQUEST_CODE_CAMERA=1001;

        //外部からのインスタンス生成制限
        private Common(){}
    }

    /* マップ関連 */
    public static class Map
    {
        //カメラ初期位置（緯度経度）
        public static final double DEFAULT_LATITUDE=40.5133543;
        public static final double DEFAULT_LONGITUDE=141.4885217;

        //マップ上で表示するお題スポットレビューの件数
        public static final int MAX_DISPLAYED_THEME_SPOT_REVIEW_NUM=3;

        //カメラの初期ズーム率
        public static final float DEFAULT_ZOOM_RATE = 16f;

        //ヒント円の拡大縮小アニメーションのFPS
        public static final int HINT_CIRCLE_ANIMATION_FPS=30;

        //ヒント円の拡大縮小アニメーションの振幅[m]
        public static final double HINT_CIRCLE_ANIMATION_AMPLITUDE=10.0;

        //各種吹き出し表示のID
        public static final int MARKER_ID_MY_LOCATION=0;
        public static final int MARKER_ID_THEME_SPOT=1;
        public static final int MARKER_ID_EVENT_SPOT=2;
        public static final int MARKER_ID_REVIEW_SPOT=3;

        //各種吹き出し表示の名前
        public static final String MARKER_NAME_MY_LOCATION="自分の位置";
        public static final String MARKER_NAME_THEME_SPOT="発見済みお題スポット";
        public static final String MARKER_NAME_EVENT_INFO="イベント情報";
        public static final String MARKER_NAME_REVIEW="口コミ";

        //外部からのインスタンス生成制限
        private Map(){}
    }

    /* GPS関連 */
    public static class GPS
    {
        //GPSモード
        public static final int LOCATION_PRIORITY= LocationRequest.PRIORITY_HIGH_ACCURACY;

        //GPS測位周期(GPSモードハイクオリティ時)
        public static final long UPDATE_INTERVAL_HIGH=5000;

        //GPS最長測位周期(GPSモードロークオリティ時)
        public static final long UPDATE_INTERVAL_LOW=60000;

        //GPS最短測位周期
        public static final long FASTEST_UPDATE_INTERVAL=16;

        //外部からのインスタンス生成制限
        private GPS(){}
    }


    /* 探索関連 */
    public static class Search
    {
        //初期ヒントポイント
        public static final int DEFAULT_HINT_POINT=100;

        //初期探索ポイント
        public static final int DEFAULT_SEARCH_POINT=0;

        //お題探索状態：発見済み
        public static final int STATE_FOUND=100;

        //お題1つ当たりの発見時加算ヒントポイント
        public static final int GAIN_HINT_POINT=50;

        //お題1つ当たりの発見時加算探索ポイント
        public static final int GAIN_SEARCH_POINT=100;

        //ヒントレベル1->2アップ時の要求ヒントポイント
        public static final int REQUIRED_HINT_POINT_LEVEL_2=50;

        //ヒントレベル2->3アップ時の要求ヒントポイント
        public static final int REQUIRED_HINT_POINT_LEVEL_3=100;

        //各レベルヒント円半径[°(緯度経度)]
        public static final double HINT_LEVEL1_LOCATION_RADIUS=0.000789;
        public static final double HINT_LEVEL2_LOCATION_RADIUS=0.000559;
        public static final double HINT_LEVEL3_LOCATION_RADIUS=0.000204;

        //各レベルヒント円半径[m]
        public static final double HINT_LEVEL1_CIRCLE_RADIUS=100.0;
        public static final double HINT_LEVEL2_CIRCLE_RADIUS=70.0;
        public static final double HINT_LEVEL3_CIRCLE_RADIUS=30.0;

        //外部からのインスタンス生成制限
        private Search(){}
    }

    /* FragmentManager関連 */
    public static class FragmentManager
    {
        //TourismAddReviewSubFragmentバックスタックタグ
        public static final String TOURISM_ADD_REVIEW_SUB_FRAGMENT_TAG ="TOURISM_ADD_REVIEW_SUB_FRAGMENT";

        //外部からのインスタンス生成制限
        private FragmentManager(){}
    }

    /* HTTP通信関連 */
    public static class Http
    {
        //接続先
        public static final String SERVER_IP ="*****.***";

        //外部からのインスタンス生成制限
        private Http(){}
    }

    /* SharedPreferences関連 */
    public static class SharedPreferences
    {
        //アカウントデータ
        public static final String ACCOUNT_DATA ="ACCOUNT_DATA";

        //IDキー
        public static final String ID_KEY ="ID";

        //メールアドレスキー
        public static final String MAIL_ADDRESS_KEY ="MAIL_ADDRESS";

        //設定データ
        public static final String SETTINGS_DATA ="SETTINGS_DATA";

        //吹き出し表示キー
        public static final String DISPLAY_BALLOON_KEY ="DISPLAYED_BALLOON";

        //外部からのインスタンス生成制限
        private SharedPreferences(){}
    }

    //外部からのインスタンス生成制限
    private Constants(){}
}
