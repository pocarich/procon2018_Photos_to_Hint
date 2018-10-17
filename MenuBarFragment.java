package jp.androidbook.photostohint;

/* モード選択メニューバーフラグメント */

import android.content.Context;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import java.util.ArrayList;

public class MenuBarFragment extends Fragment {

    //遷移ID
    public static final int TRANSITION_ID_TOURISM = 0;
    public static final int TRANSITION_ID_SEARCH = 1;
    public static final int TRANSITION_ID_ALBUM=2;
    public static final int TRANSITION_ID_SETTINGS=3;

    //モード選択コールバック
    private CallBack callBack;

    //モード選択ボタンリスト
    private ArrayList<ImageButton> buttonList=new ArrayList<ImageButton>();

    /* コンストラクタ */
    public MenuBarFragment() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static MenuBarFragment newInstance() {
        //フラグメントインスタンス生成
        MenuBarFragment fragment = new MenuBarFragment();

        //フラグメントインスタンスを返す
        return fragment;
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_menu_bar, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);

        //ボタンリストに各モード選択ボタンを追加
        buttonList.add((ImageButton)view.findViewById(R.id.menu_bar_button0));
        buttonList.add((ImageButton)view.findViewById(R.id.menu_bar_button1));
        buttonList.add((ImageButton)view.findViewById(R.id.menu_bar_button2));
        buttonList.add((ImageButton)view.findViewById(R.id.menu_bar_button3));

        //観光モード選択ボタンを押下表示する
        buttonList.get(0).setColorFilter(new LightingColorFilter(Color.GRAY, 0));
        buttonList.get(0).setPadding(0,(int)(2*getResources().getDisplayMetrics().density+0.5f),0,0);

        //各ボタンに対して押下時処理設定
        for(int i=0;i<buttonList.size();i++)
        {
            //i番目のボタンが存在するなら
            if(buttonList.get(i)!=null)
            {
                //iの定数宣言
                final int temp_i=i;

                //i番目のボタン押下時処理設定
                buttonList.get(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //各ボタンについて押下表示の更新
                        for(int j=0;j<buttonList.size();j++)
                        {
                            //自分自身なら
                            if(j==temp_i)
                            {
                                /* 押下表示にする */
                                buttonList.get(j).setColorFilter(new LightingColorFilter(Color.GRAY, 0));
                                buttonList.get(j).setPadding(0,(int)(2*getResources().getDisplayMetrics().density+0.5f),0,0);
                            }

                            //自分以外なら
                            else
                            {
                                /* 押下表示をOFFにする */
                                buttonList.get(j).clearColorFilter();
                                buttonList.get(j).setPadding(0,0,0,0);
                            }
                        }

                        //モード選択コールバック実行
                        switch(temp_i) {
                            case 0:
                                callBack.onMenuBarFragmentInteraction(TRANSITION_ID_TOURISM);
                                break;
                            case 1:
                                callBack.onMenuBarFragmentInteraction(TRANSITION_ID_SEARCH);
                                break;
                            case 2:
                                callBack.onMenuBarFragmentInteraction(TRANSITION_ID_ALBUM);
                                break;
                            case 3:
                                callBack.onMenuBarFragmentInteraction(TRANSITION_ID_SETTINGS);
                                break;
                        }
                    }
                });
            }
        }
    }

    /* フラグメント生成時 */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //アクティヴィティがコールバックを実装しているなら
        if (context instanceof CallBack) {
            //コールバックセット
            callBack = (CallBack) context;
        }

        //アクティヴィティがコールバックを実装していないなら
        else {
            //例外を投げる
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    /* フラグメント破棄時 */
    @Override
    public void onDetach() {
        super.onDetach();

        //コールバックをOFFにする
        callBack = null;
    }

    /* モード選択コールバック */
    public interface CallBack {
        void onMenuBarFragmentInteraction(int transitionID);
    }
}
