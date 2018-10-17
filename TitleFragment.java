package jp.androidbook.photostohint;

/* タイトルフラグメント */

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class TitleFragment extends Fragment {

    //フラグメント遷移コールバック
    private CallBack callBack;

    //タイトルロゴアニメーションハンドラー
    private Handler titleLogoAnimationHandler;

    //タイトルロゴアニメーションタイマー
    private long titleLogoTimer;

    //スタートボタンが押されたか
    private boolean startIconPushed;

    /* コンストラクタ */
    public TitleFragment() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static TitleFragment newInstance() {
        //フラグメントインスタンス生成
        TitleFragment fragment = new TitleFragment();

        //フラグメントインスタンスを返す
        return fragment;
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_title, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //タイトル画面表示取得
        final FrameLayout titleFrameLayout = view.findViewById(R.id.title_fragment_title_frameLayout);

        //タイトルロゴビュー取得
        final ImageView titleLogo = view.findViewById(R.id.title_fragment_title_rogo);

        //タイトル背景ビュー取得
        final ImageView titleBack = view.findViewById(R.id.title_fragment_title_back);

        //タイトルスタートボタン取得
        final ImageButton titleStartIcon = view.findViewById(R.id.title_fragment_title_start_icon);

        //署名テキストビュー取得
        final TextView signatureTextView = view.findViewById(R.id.title_fragment_signature_textView);

        //タイトル背景タッチ時
        titleFrameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //タッチイベント回収（後ろのフラグメントのタッチ判定を無効化）
                return true;
            }
        });

        //タイトルスタートボタン押下時処理設定
        titleStartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //初めてタッチするときのみ実行
                if (startIconPushed) return;
                startIconPushed = true;

                //点滅アニメーションをタイトルスタートボタンにセット
                AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
                alphaAnimation.setDuration(30);
                alphaAnimation.setRepeatCount(Animation.INFINITE);
                alphaAnimation.setRepeatMode(Animation.REVERSE);
                titleStartIcon.startAnimation(alphaAnimation);

                //3.5秒後フラグメント遷移コールバック実行
                Handler titleEndHandler = new Handler();
                titleEndHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onTitleFragmentInteraction();
                    }
                }, 3500);

                //1.5秒後アニメーション設定
                Handler titleFrameLayoutAnimationHandler = new Handler();
                titleFrameLayoutAnimationHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //各種表示にフェードアウトアニメーション設定
                        AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
                        AlphaAnimation alphaAnimation2 = new AlphaAnimation(1f, 0f);
                        AlphaAnimation alphaAnimation3 = new AlphaAnimation(1f, 0f);
                        alphaAnimation.setDuration(200);
                        alphaAnimation2.setDuration(200);
                        alphaAnimation3.setDuration(200);
                        alphaAnimation.setFillAfter(true);
                        alphaAnimation2.setFillAfter(true);
                        alphaAnimation3.setFillAfter(true);
                        titleStartIcon.startAnimation(alphaAnimation);
                        titleLogoAnimationHandler.removeCallbacksAndMessages(null);
                        titleLogo.startAnimation(alphaAnimation2);
                        signatureTextView.startAnimation(alphaAnimation3);

                        //背景にフェードアウト+拡大アニメーション設定
                        AnimationSet animationSet = new AnimationSet(true);
                        AlphaAnimation alphaAnimation4 = new AlphaAnimation(1f, 0f);
                        alphaAnimation4.setDuration(2000);
                        alphaAnimation4.setFillAfter(true);
                        ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 7f, 1, 7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        scaleAnimation.setDuration(2000);
                        scaleAnimation.setFillAfter(true);
                        animationSet.addAnimation(alphaAnimation4);
                        animationSet.addAnimation(scaleAnimation);
                        animationSet.setFillAfter(true);
                        titleBack.startAnimation(animationSet);
                    }
                }, 1500);

            }
        });

        //タイトルロゴ上下移動アニメーションセット
        titleLogoAnimationHandler = new Handler();
        titleLogoAnimationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, (int) (30 * Math.sin(2 * Math.PI / 2000 * titleLogoTimer)), (int) (30 * Math.sin(2 * Math.PI / 2000 * (titleLogoTimer + 100))));
                translateAnimation.setDuration(100);
                titleLogo.setAnimation(translateAnimation);
                titleLogoTimer += 100;
                titleLogoAnimationHandler.postDelayed(this, 100);
            }
        }, 100);

        //タイトルスタートボタン拡大縮小アニメーションセット
        ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 1.2f, 1f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(500);
        scaleAnimation.setRepeatCount(Animation.INFINITE);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        titleStartIcon.setAnimation(scaleAnimation);
    }

    /* フラグメント生成時処理 */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //アクティヴィティがコールバックを実装している場合
        if (context instanceof CallBack) {
            //コールバック設定
            callBack = (CallBack) context;
        }

        //アクティヴィティがコールバックを実装していない場合
        else {
            //実行時例外を投げる
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    /* フラグメント破棄時処理 */
    @Override
    public void onDetach() {
        super.onDetach();

        //コールバックを外す
        callBack = null;

        //タイトルロゴ上下アニメーション無効化
        if (titleLogoAnimationHandler != null) {
            titleLogoAnimationHandler.removeCallbacksAndMessages(null);
        }
    }

    /* フラグメント遷移コールバック */
    public interface CallBack {
        void onTitleFragmentInteraction();
    }
}
