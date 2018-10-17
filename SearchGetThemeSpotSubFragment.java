package jp.androidbook.photostohint;

/* 探索モードお題取得フラグメント */

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;

public class SearchGetThemeSpotSubFragment extends Fragment {

    //Bundleキー
    private static final String IS_COMPLETED_KEY="IS_COMPLETED";

    //全てのお題スポットを探索済みか
    private boolean isCompleted;

    /* コンストラクタ */
    public SearchGetThemeSpotSubFragment() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static SearchGetThemeSpotSubFragment newInstance(boolean isCompleted) {
        //フラグメントインスタンス生成
        SearchGetThemeSpotSubFragment fragment = new SearchGetThemeSpotSubFragment();

        //バンドル生成
        Bundle args = new Bundle();

        //バンドルにisCompleted追加
        args.putBoolean(IS_COMPLETED_KEY,isCompleted);

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
           isCompleted=getArguments().getBoolean(IS_COMPLETED_KEY);
        }
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_search_get_theme_spot_sub, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);

        //お題コンプリート画面表示取得
        final ConstraintLayout completionLayout=view.findViewById(R.id.search_get_theme_spot_sub_fragment_completion_layout);

        //お題取得画面表示取得
        final ConstraintLayout getThemeLayout=view.findViewById(R.id.search_get_theme_spot_sub_fragment_get_theme_layout);

        //お題取得ボタン取得
        final Button downloadButton=view.findViewById(R.id.search_get_theme_spot_sub_fragment_button);

        //お題をコンプリートしている場合
        if(isCompleted)
        {
            //お題コンプリート画面表示ON
            completionLayout.setVisibility(View.VISIBLE);

            //お題取得画面表示OFF
            getThemeLayout.setVisibility(View.GONE);
        }

        //お題をコンプリートしていない場合
        else
        {
            //お題コンプリート画面表示OFF
            completionLayout.setVisibility(View.GONE);

            //お題取得画面表示ON
            getThemeLayout.setVisibility(View.VISIBLE);
        }

        //お題取得ボタン押下時処理設定
        downloadButton.setOnClickListener(new View.OnClickListener() {
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
                ((CallBack)getParentFragment()).onSearchGetThemeSpotSubFragmentInteraction();
            }
        });
    }

    /* フラグメント遷移コールバック */
    public interface CallBack {
        void onSearchGetThemeSpotSubFragmentInteraction();
    }
}
