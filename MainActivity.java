package jp.androidbook.photostohint;

/* メインアクティヴィティ */

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends FragmentActivity implements SensorEventListener,MenuBarFragment.CallBack,TitleFragment.CallBack,SearchMainFragment.CallBack,LoginMainFragment.CallBack,SettingsMainFragment.CallBack{

    //ユーザID
    private String userID;

    //地名
    private String regionName;

    //タイトルフラグメントインスタンス
    private TitleFragment titleFragment;

    //ログインフラグメントインスタンス
    private LoginMainFragment loginMainFragment;

    //メニューバーフラグメントインスタンス
    private MenuBarFragment menuBarFragment;

    //設定モードフラグメントインスタンス
    private SettingsMainFragment settingsMainFragment;

    //通信画面表示
    private FrameLayout connectionLayout;

    //通信失敗画面表示
    private ConstraintLayout connectionFailureLayout;

    //歩きスマホ警告表示
    private FrameLayout cautionLayout;

    //加速度センサ利用のためのセンサマネージャー
    private SensorManager sensorManager;

    //加速度センサ最小値最大値一時保存用変数
    double minr=100000000.0,maxr=0.0;

    //加速度センサ読み取り回数
    int counter=0;

    //歩きスマホ検知カウンター
    int walkingCounter=0;

    /* アクティヴィティ生成時処理 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //SharedPreferencesから端末に保存されているユーザIDを取得
        SharedPreferences prefs=getSharedPreferences(Constants.SharedPreferences.ACCOUNT_DATA, Context.MODE_PRIVATE);
        userID=prefs.getString(Constants.SharedPreferences.ID_KEY,null);

        //地名セット
        regionName=Constants.Common.TARGET_REGION;

        //通信画面表示取得
        connectionLayout=findViewById(R.id.main_activity_connection_layout);

        //通信失敗画面表示取得
        connectionFailureLayout=findViewById(R.id.main_activity_connection_failure_layout);

        //歩きスマホ警告表示取得
        cautionLayout=findViewById(R.id.caution_layout);

        //センサマネージャー生成
        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);

        //歩きスマホ警告表示OFF
        cautionLayout.setVisibility(View.GONE);

        //通信画面表示OFF
        connectionLayout.setVisibility(View.GONE);

        //通信失敗画面表示OFF
        connectionFailureLayout.setVisibility(View.GONE);

        //通信再試行ボタン取得
        Button retryButton=findViewById(R.id.main_activity_connection_failure_button);

        //歩きスマホ警告表示タッチ時処理
        cautionLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //タッチ入力を無効化する
                return true;
            }
        });

        //通信再試行ボタン押下時処理設定
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //データ更新チェックHTTP通信実行
                checkUpdate();
            }
        });

        //アプリ起動時なら
        if(savedInstanceState==null) {

            /* titleFragment生成 */

            //titleFragmentインスタンス生成
            titleFragment=TitleFragment.newInstance();

            //トランザクション生成
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            //FrameLayoutに組み込む
            fragmentTransaction.replace(R.id.title_frameLayout,titleFragment);

            //フラグメント表示
            fragmentTransaction.commit();

            //ユーザIDがnullでなかった場合(ユーザIDが端末に保存されていた場合)
            if(userID!=null)
            {
                //データ更新チェックHTTP通信実行
                checkUpdate();
            }

            //ユーザIDがnullだった場合（ユーザIDが端末に保存されていなかった場合）
            else
            {
                /* loginMainFragment生成 */

                //トランザクション生成
                FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();

                //loginMainFragmentインスタンス生成
                loginMainFragment=LoginMainFragment.newInstance();

                //FrameLayoutに組み込む
                fragmentTransaction2.replace(R.id.login_frameLayout, loginMainFragment);

                //フラグメント表示
                fragmentTransaction2.commit();
            }
        }
    }

    /* データ更新チェックHTTP */
    private void checkUpdate()
    {
        //通信画面表示ON
        connectionLayout.setVisibility(View.VISIBLE);

        //通信失敗画面表示OFF
        connectionFailureLayout.setVisibility(View.GONE);

        try {
            //JSON生成
            JSONObject json=new JSONObject();

            //JSONにユーザID追加
            json.put("user_id",userID);

            //JSONに初期ヒントポイント追加
            json.put("hint_point",Constants.Search.DEFAULT_HINT_POINT);

            //JSONに初期探索ポイント追加
            json.put("search_point",Constants.Search.DEFAULT_SEARCH_POINT);

            //HTTP通信インスタンス生成
            HttpResponsAsync httpResponsAsync = new HttpResponsAsync();

            //通信終了後処理設定
            httpResponsAsync.setCallBack(new HttpResponsAsync.CallBack() {
                @Override
                public void onAsyncTaskResult(String data) {
                    //受信データが空なら（通信失敗時）
                    if (data == "") {
                        //通信画面表示OFF
                        connectionLayout.setVisibility(View.GONE);

                        //通信失敗画面表示ON
                        connectionFailureLayout.setVisibility(View.VISIBLE);
                        return;
                    }

                    try {
                        //受信データからJSON生成
                        JSONObject json = new JSONObject(data);

                        //"status"パラメータ取得
                        Boolean status = json.getBoolean("status");

                        //"status"パラメータがtrueなら（通信成功時）
                        if (status) {
                            //通信画面表示OFF
                            connectionLayout.setVisibility(View.GONE);


                            /* TourismMainFragment生成 */

                            //トランザクション生成
                            FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();

                            //TourismMainFragmentインスタンス生成->FrameLayoutに組み込む
                            fragmentTransaction2.replace(R.id.mainActivityFrameLayout0, TourismMainFragment.newInstance(userID,regionName));

                            //フラグメント表示
                            fragmentTransaction2.commit();


                            /* MenuBarFragment生成 */

                            //トランザクション生成
                            FragmentTransaction fragmentTransaction3 = getSupportFragmentManager().beginTransaction();

                            //MenuBarFragmentインスタンス生成
                            menuBarFragment=MenuBarFragment.newInstance();

                            //FrameLayoutに組み込む
                            fragmentTransaction3.replace(R.id.mainActivityFrameLayout1, menuBarFragment);

                            //フラグメント表示
                            fragmentTransaction3.commit();
                        }

                        //"status"パラメータがfalseのとき（webサーバエラー発生時）
                        else {
                            //通信画面表示OFF
                            connectionLayout.setVisibility(View.GONE);

                            //通信失敗画面表示ON
                            connectionFailureLayout.setVisibility(View.VISIBLE);
                        }
                    }

                    //JSON例外発生時
                    catch (JSONException e) {
                        //ログに例外メッセージ出力
                        e.printStackTrace();

                        //通信画面表示OFF
                        connectionLayout.setVisibility(View.GONE);

                        //通信失敗画面表示ON
                        connectionFailureLayout.setVisibility(View.VISIBLE);
                    }
                }
            });

            //HTTP通信実行
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/user/check_new_theme_spot.php",json.toString());
        }

        //JSON例外発生時
        catch(JSONException e){
            //ログに例外メッセージ出力
            e.printStackTrace();

            //通信画面表示OFF
            connectionLayout.setVisibility(View.GONE);

            //通信失敗画面表示ON
            connectionFailureLayout.setVisibility(View.VISIBLE);
        }
    }

    /* MenuBarFragmentコールバック */
    @Override
    public void onMenuBarFragmentInteraction(int transitionID)
    {
        /* モード遷移 */

        //トランザクション生成
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        //遷移IDに応じてフラグメント遷移
        switch(transitionID)
        {
            case MenuBarFragment.TRANSITION_ID_TOURISM:
                //観光モード遷移
                fragmentTransaction.replace(R.id.mainActivityFrameLayout0, TourismMainFragment.newInstance(userID,regionName));
                break;
            case MenuBarFragment.TRANSITION_ID_SEARCH:
                //探索モード遷移
                fragmentTransaction.replace(R.id.mainActivityFrameLayout0, SearchMainFragment.newInstance(userID,regionName));
                break;
            case MenuBarFragment.TRANSITION_ID_ALBUM:
                //アルバムモード遷移
                fragmentTransaction.replace(R.id.mainActivityFrameLayout0, AlbumMainFragment.newInstance(userID));
                break;
            case MenuBarFragment.TRANSITION_ID_SETTINGS:
                //設定モード遷移
                settingsMainFragment=SettingsMainFragment.newInstance();
                fragmentTransaction.replace(R.id.mainActivityFrameLayout0, settingsMainFragment);
                break;
        }

        //遷移実行
        fragmentTransaction.commit();
    }

    /* LoginMainFragmentコールバック */
    @Override
    public void onLoginMainFragmentInteraction(String userID)
    {
        //ログイン時ID保持
        this.userID=userID;

        /* Login画面削除 */

        //トランザクション生成
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        //LoginMainFragment削除
        fragmentTransaction.remove(loginMainFragment);

        //削除実行
        fragmentTransaction.commit();

        //loginMainFragmentの参照を外す
        loginMainFragment=null;

        //データ更新チェックHTTP通信実行
        checkUpdate();
    }

    /* TitleFragmentコールバック */
    @Override
    public void onTitleFragmentInteraction()
    {
        /* タイトル画面削除 */

        //トランザクション生成
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        //TitleFragment削除
        fragmentTransaction.remove(titleFragment);

        //削除実行
        fragmentTransaction.commit();
    }

    /* SearchMainFragmentコールバック */
    @Override
    public void onSearchMainFragmentInteraction() {
        /* 観光モード再起動 */

        //トランザクション生成
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        //新たなSearchMainFragmentを生成してFrameLayoutに組み込む
        fragmentTransaction.replace(R.id.mainActivityFrameLayout0, SearchMainFragment.newInstance(userID, regionName));

        //遷移実行
        fragmentTransaction.commit();
    }

    /* SettingsMainFragmentコールバック */
    @Override
    public void onSettingsMainFragmentInteraction()
    {
        /* メニューバー削除 */

        //トランザクション生成
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        //MenuBarFragment削除
        fragmentTransaction.remove(menuBarFragment);
        fragmentTransaction.remove(settingsMainFragment);

        //削除実行
        fragmentTransaction.commit();

        //menuBarFragmentの参照を外す
        menuBarFragment=null;


        /* ログイン画面遷移 */

        //トランザクション生成
        FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();

        //LoginMainFragmentインスタンス生成
        loginMainFragment=LoginMainFragment.newInstance();

        //FrameLayoutに組み込む
        fragmentTransaction2.replace(R.id.login_frameLayout, loginMainFragment);

        //遷移実行
        fragmentTransaction2.commit();
    }

    /* センサー値更新時コールバック */
    @Override
    public void onSensorChanged(SensorEvent event)
    {
        //センサの種類が加速度センサなら
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER)
        {
            //センサ値(x,y,z)の大きさを取得
            double r=event.values[0]*event.values[0]+event.values[1]*event.values[1]+event.values[2]*event.values[2];

            //センサ値最小値最大値更新
            minr=Math.min(minr,r);
            maxr=Math.max(maxr,r);

            //加速度センサ読み取り回数加算
            counter++;

            //加速度センサ読み取り回数が歩きスマホ検知周期に達したとき
            if(counter%Constants.Common.WALKING_DETECTION_INTERVAL==0)
            {
                //最大値と最小値の差が歩きスマホ検知の閾値を超えていたら
                if(maxr-minr>Constants.Common.WALKING_DETECTION_THRESHOLD_SENSOR_VALUE)
                {
                    //歩きスマホ検知カウンター加算
                    walkingCounter=Math.min(Constants.Common.MAX_WALKING_COUNTER,walkingCounter+1);
                }

                //最大値と最小値の差が歩きスマホ検知の閾値を超えていなかったら
                else
                {
                    //歩きスマホ検知カウンターリセット
                    walkingCounter=Math.max(0,walkingCounter-1);
                }

                //歩きスマホ検知カウンターが歩きスマホ警告表示閾値以上なら
                if(walkingCounter>=Constants.Common.WALKING_DETECTION_THRESHOLD_WALKING_COUNTER)
                {
                    //歩きスマホ警告表示
                    cautionLayout.setVisibility(View.VISIBLE);
                }

                //歩きスマホ検知カウンターが歩きスマホ警告表示閾値未満なら
                else
                {
                    //歩きスマホ警告非表示
                    cautionLayout.setVisibility(View.GONE);
                }

                //センサ値最小値最大値リセット
                minr=100000000.0;
                maxr=0.0;
            }
        }
    }

    /* センサ精度設定変更時コールバック */
    @Override
    public void onAccuracyChanged(Sensor sensor,int accuracy)
    {

    }

    /* フラグメント停止時 */
    @Override
    protected void onPause()
    {
        super.onPause();

        //センサコールバックを無効化
        sensorManager.unregisterListener(this);
    }

    /* フラグメント再開時 */
    @Override
    protected void onResume()
    {
        super.onResume();

        //センサコールバックを有効にする
        Sensor accel=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this,accel,SensorManager.SENSOR_DELAY_FASTEST);
    }
}
