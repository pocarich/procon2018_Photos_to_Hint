package jp.androidbook.photostohint;

/* 観光モードインフォメーションフラグメント2 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.TextView;

public class TourismInformationSubFragment2 extends Fragment {

    //Bundleキー
    private static final String TITLE_KEY="TITLE";
    private static final String SENTENCE_KEY="SENTENCE";

    //見出し
    private String title;

    //説明文
    private String sentence;

    /* コンストラクタ */
    public TourismInformationSubFragment2() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static TourismInformationSubFragment2 newInstance(String title, String sentence) {
        //フラグメントインスタンス生成
        TourismInformationSubFragment2 fragment = new TourismInformationSubFragment2();

        //バンドル生成
        Bundle args = new Bundle();

        //バンドルに各種パラメータ追加
        args.putString(TITLE_KEY, title);
        args.putString(SENTENCE_KEY, sentence);

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
            title = getArguments().getString(TITLE_KEY);
            sentence = getArguments().getString(SENTENCE_KEY);
        }
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_tourism_information_sub_fragment2, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //戻るボタン取得
        final ImageButton backButton = view.findViewById(R.id.tourism_open_data_sub_fragment2_backButton);

        //見出しテキストビュー取得
        final TextView titleTextView = view.findViewById(R.id.tourism_open_data_sub_fragment2_title);

        //説明文テキストビュー取得
        final TextView sentenceTextView = view.findViewById(R.id.tourism_open_data_sub_fragment2_sentence);

        //見出しセット
        titleTextView.setText(title);

        //説明文セット
        sentenceTextView.setText(sentence);

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
                ((CallBack) getParentFragment()).onTourismInformationSubFragment2Interaction();
            }
        });
    }

    /* フラグメント遷移コールバック */
    public interface CallBack {
        void onTourismInformationSubFragment2Interaction();
    }
}
