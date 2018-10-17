package jp.androidbook.photostohint;

/* 観光モードインフォメーションフラグメント */

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TourismInformationSubFragment extends Fragment {

    //遷移ID
    public static final int TRANSITION_ID_BACK = 0;
    public static final int TRANSITION_ID_NEXT = 1;

    //Bundleキー
    private static final String REGION_NAME_KEY="REGION_NAME";

    //通信画面表示
    private FrameLayout connectionLayout;

    //通信失敗画面表示
    private ConstraintLayout connectionFailureLayout;

    //メインコンテンツ画面表示
    private FrameLayout mainLayout;

    //インフォメーションリスト表示
    private LinearLayout infoLayout;

    //戻るボタン
    private ImageButton backButton;

    //更新ボタン
    private ImageButton updateButton;

    //地名
    private String regionName;

    /* コンストラクタ */
    public TourismInformationSubFragment() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static TourismInformationSubFragment newInstance(String regionName) {
        //フラグメントインスタンス生成
        TourismInformationSubFragment fragment = new TourismInformationSubFragment();

        //バンドル生成
        Bundle args = new Bundle();

        //バンドルに地名追加
        args.putString(REGION_NAME_KEY,regionName);

        //フラグメントインスタンスにバンドルセット
        fragment.setArguments(args);

        //フラグメントインスタンスを返す
        return fragment;
    }

    /* フラグメント生成時処理 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //newInstanceで設定したパラメータを取得
        if (getArguments() != null) {
            regionName=getArguments().getString(REGION_NAME_KEY);
        }
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_tourism_information_sub, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);

        //インフォメーションリスト表示取得
        infoLayout=view.findViewById(R.id.tourism_open_data_sub_fragment_linearLayout);

        //通信画面表示取得
        connectionLayout=view.findViewById(R.id.tourism_open_data_sub_fragment_connection_layout);

        //通信失敗画面表示取得
        connectionFailureLayout=view.findViewById(R.id.tourism_open_data_sub_fragment_connection_failure_layout);

        //メインコンテンツ画面表示取得
        mainLayout=view.findViewById(R.id.tourism_open_data_sub_fragment_main_layout);

        //戻るボタン取得
        backButton=view.findViewById(R.id.tourism_open_data_sub_fragment_backButton);

        //更新ボタン取得
        updateButton=view.findViewById(R.id.tourism_open_data_sub_fragment_updateButton);

        //通信再試行ボタン取得
        final Button retryConnectionButton=view.findViewById(R.id.tourism_open_data_sub_fragment_connection_failure_button);

        //戻るボタン押下時処理設定
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //フラグメント遷移コールバック実行
                ((CallBack)getParentFragment()).onTourismInformationSubFragmentInteraction(TRANSITION_ID_BACK,null,null);
            }
        });

        //更新ボタン押下時処理設定
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //インフォメーション取得HTTP通信
                connection();
            }
        });

        //通信再試行ボタン押下時処理設定
        retryConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(50);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                //UIサイズアニメーション開始
                v.startAnimation(scaleAnimation);

                //インフォメーション取得HTTP通信
                connection();
            }
        });

        //インフォメーション取得HTTP通信
        connection();
    }

    /* インフォメーション取得HTTP通信 */
    private void connection() {
        //インフォメーションリストクリア
        infoLayout.removeAllViews();

        //通信画面表示ON
        connectionLayout.setVisibility(View.VISIBLE);

        //通信失敗画面表示OFF
        connectionFailureLayout.setVisibility(View.GONE);

        //メインコンテンツ画面表示OFF
        mainLayout.setVisibility(View.GONE);

        //戻るボタン表示OFF
        backButton.setVisibility(View.GONE);

        //更新ボタン表示OFF
        updateButton.setVisibility(View.GONE);

        try {
            //JSON生成
            JSONObject json = new JSONObject();

            //JSONに地名追加
            json.put("region_name", regionName);

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

                        //戻るボタン表示ON
                        backButton.setVisibility(View.VISIBLE);

                        //更新ボタン表示ON
                        updateButton.setVisibility(View.VISIBLE);

                        //コールバック処理中断
                        return;
                    }
                    try {
                        //受信データからJSON生成
                        JSONObject json = new JSONObject(data);

                        //"status"パラメータ取得
                        Boolean status = json.getBoolean("status");

                        //"status"パラメータがtrueなら（通信成功時）
                        if(status)
                        {
                            //"result"パラメータ(インフォメーション)取得
                            JSONArray dataArray = json.getJSONArray("result");

                            //各インフォメーションについて
                            for (int i = 0; i < dataArray.length(); i++) {
                                //各種パラメータ取得
                                JSONObject result = dataArray.getJSONObject(i);
                                final String title = result.getString("title");
                                final String description = result.getString("description");

                                //インフォメーションボタン生成
                                Button newButton = new Button(getActivity());
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                layoutParams.setMargins((int) getResources().getDimension(R.dimen.dp5), (int) getResources().getDimension(R.dimen.dp5), (int) getResources().getDimension(R.dimen.dp5), 0);
                                newButton.setPadding((int) getResources().getDimension(R.dimen.dp15), 0, (int) getResources().getDimension(R.dimen.dp15), 0);
                                layoutParams.height = (int) getResources().getDimension(R.dimen.dp50);
                                newButton.setLayoutParams(layoutParams);
                                newButton.setBackgroundResource(R.drawable.ui_message_box0);
                                newButton.setEllipsize(TextUtils.TruncateAt.END);
                                newButton.setHorizontallyScrolling(true);
                                newButton.setSingleLine(true);
                                newButton.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                                newButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.sp18));
                                newButton.setText(title);

                                //ボタン押下時処理設定
                                newButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //UIサイズアニメーション生成(100msでsize 1.0->0.7->1.0)
                                        ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                        scaleAnimation.setDuration(50);
                                        scaleAnimation.setRepeatCount(1);
                                        scaleAnimation.setRepeatMode(Animation.REVERSE);

                                        //UIサイズアニメーション開始
                                        v.startAnimation(scaleAnimation);

                                        //フラグメント遷移コールバック実行
                                        ((CallBack) getParentFragment()).onTourismInformationSubFragmentInteraction(TRANSITION_ID_NEXT, title, description);
                                    }
                                });

                                //インフォメーションリストに追加
                                infoLayout.addView(newButton);
                            }

                            //通信画面表示OFF
                            connectionLayout.setVisibility(View.GONE);

                            //メインコンテンツ画面表示ON
                            mainLayout.setVisibility(View.VISIBLE);

                            //戻るボタン表示ON
                            backButton.setVisibility(View.VISIBLE);

                            //更新ボタン表示ON
                            updateButton.setVisibility(View.VISIBLE);
                        }

                        //"status"パラメータがfalseなら（webサーバーエラー発生時）
                        else
                        {
                            //通信画面表示OFF
                            connectionLayout.setVisibility(View.GONE);

                            //通信失敗画面表示ON
                            connectionFailureLayout.setVisibility(View.VISIBLE);

                            //戻るボタン表示ON
                            backButton.setVisibility(View.VISIBLE);

                            //更新ボタン表示ON
                            updateButton.setVisibility(View.VISIBLE);
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

                        //戻るボタン表示ON
                        backButton.setVisibility(View.VISIBLE);

                        //更新ボタン表示ON
                        updateButton.setVisibility(View.VISIBLE);
                    }
                }
            });

            //HTTP通信実行
            httpResponsAsync.execute("https://" + Constants.Http.SERVER_IP + "/open_data/official_information.php", json.toString());
        }

        //JSON例外発生時
        catch (JSONException e) {
            //ログに例外メッセージ出力
            e.printStackTrace();

            //通信画面表示OFF
            connectionLayout.setVisibility(View.GONE);

            //通信失敗画面表示ON
            connectionFailureLayout.setVisibility(View.VISIBLE);

            //戻るボタン表示ON
            backButton.setVisibility(View.VISIBLE);

            //更新ボタン表示ON
            updateButton.setVisibility(View.VISIBLE);
        }
    }

    /* フラグメント遷移コールバック */
    public interface CallBack {
        void onTourismInformationSubFragmentInteraction(int transitionID,String title,String sentence);
    }
}
