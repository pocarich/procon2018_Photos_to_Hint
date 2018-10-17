package jp.androidbook.photostohint;

/* アルバムモード管理フラグメントクラス */

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AlbumMainFragment extends Fragment implements AlbumSubFragment.CallBack,AlbumSubFragment2.CallBack,AlbumSubFragment3.CallBack
{
    //Bundleキー
    private static final String USER_ID_KEY="USER_ID";

    //ユーザID
    private String userID;

    //地域名
    private String regionName;

    /* コンストラクタ */
    public AlbumMainFragment()
    {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static AlbumMainFragment newInstance(String userID)
    {
        //インスタンス生成
        AlbumMainFragment fragment = new AlbumMainFragment();

        //バンドル生成
        Bundle args = new Bundle();

        //バンドルにユーザID設定
        args.putString(USER_ID_KEY, userID);

        //フラグメントインスタンスにバンドル設定
        fragment.setArguments(args);

        //フラグメントインスタンスを返す
        return fragment;
    }

    /* フラグメント生成時処理 */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //newInstanceで設定したパラメータを取得
        if (getArguments() != null) {
            userID = getArguments().getString(USER_ID_KEY);
        }
    }

    /* View生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_album_main, container, false);
    }

    /* View生成時処理 */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        /* AlbumSubFragment生成 */

        //トランザクション生成
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

        //フラグメント遷移時のアニメーション設定
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

        //AlbumSubFragmentインスタンス生成
        AlbumSubFragment albumSubFragment = AlbumSubFragment.newInstance();

        //バックスタックに追加
        fragmentTransaction.addToBackStack(null);

        //FrameLayoutに組み込む
        fragmentTransaction.replace(R.id.album_main_fragment_fragment_layout, albumSubFragment);

        //遷移実行
        fragmentTransaction.commit();
    }

    /* AlbumSubFragmentコールバック */
    @Override
    public void onAlbumSubFragmentInteraction(String regionName)
    {
        //AlbumSubFragmentで選択された地域名取得
        this.regionName = regionName;

        /* AlbumSubFragment2に遷移 */

        //トランザクション生成
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

        //遷移時アニメーション設定
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

        //AlbumSubFragment2インスタンス生成
        AlbumSubFragment2 albumSubFragment2 = AlbumSubFragment2.newInstance(userID, regionName);

        //バックスタックに追加
        fragmentTransaction.addToBackStack(null);

        //FrameLayoutに組み込む
        fragmentTransaction.replace(R.id.album_main_fragment_fragment_layout, albumSubFragment2);

        //遷移実行
        fragmentTransaction.commit();
    }

    /* AlbumSubFragment2コールバック */
    @Override
    public void onAlbumSubFragment2Interaction(int transitionID, String locationName, String tip, Bitmap photo)
    {
        //遷移IDに応じてフラグメント遷移
        switch (transitionID) {
            case AlbumSubFragment2.TRANSITION_ID_BACK: {
                //前のフラグメントに戻る
                getChildFragmentManager().popBackStack();
            }
            break;
            case AlbumSubFragment2.TRANSITION_ID_CONTENT_SELECTED: {
                /* AlbumSubFragment3に遷移 */

                //トランザクション生成
                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

                //遷移時アニメーション設定
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

                //AlbumSubFragment3インスタンス生成
                AlbumSubFragment3 albumSubFragment3 = AlbumSubFragment3.newInstance(userID, regionName, locationName, tip, photo);

                //バックスタックに追加
                fragmentTransaction.addToBackStack(null);

                //FrameLayoutに組み込む
                fragmentTransaction.replace(R.id.album_main_fragment_fragment_layout, albumSubFragment3);

                //遷移実行
                fragmentTransaction.commit();
            }
            break;
        }
    }

    /* AlbumSubFragment3コールバック */
    @Override
    public void onAlbumSubFragment3Interaction()
    {
        //前のフラグメントに戻る
        getChildFragmentManager().popBackStack();
    }
}
