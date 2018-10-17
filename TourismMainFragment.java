package jp.androidbook.photostohint;

/* 観光モード管理フラグメント */

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.model.LatLng;

public class TourismMainFragment extends Fragment implements TourismMapSubFragment.CallBack,TourismInformationSubFragment.CallBack,TourismAddReviewSubFragment.CallBack,TourismAddReviewSubFragment2.CallBack,TourismAddReviewSubFragment3.CallBack,TourismAddReviewSubFragment4.CallBack,TourismInformationSubFragment2.CallBack{

    //Bundleキー
    private static final String USER_ID_KEY="USER_ID";
    private static final String REGION_NAME_KEY="REGION_NAME";

    //ユーザID
    private String userID;

    //地名
    private String regionName;

    //ユーザ位置
    private LatLng userLocation;

    //撮影写真
    private Bitmap takenImage;

    /* コンストラクタ */
    public TourismMainFragment() {
        //空のコンストラクタ
    }

    /* インスタンス生成メソッド */
    public static TourismMainFragment newInstance(String userID,String regionName) {
        //フラグメントインスタンス生成
        TourismMainFragment fragment = new TourismMainFragment();

        //バンドル生成
        Bundle args = new Bundle();

        //バンドルに各種パラメータ追加
        args.putString(USER_ID_KEY,userID);
        args.putString(REGION_NAME_KEY,regionName);

        //フラグメントインスタンスにバンドルをセット
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
            userID=getArguments().getString(USER_ID_KEY);
            regionName=getArguments().getString(REGION_NAME_KEY);
        }
    }

    /* ビュー生成 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //フラグメントビュー生成
        return inflater.inflate(R.layout.fragment_tourism, container, false);
    }

    /* ビュー生成時処理 */
    @Override
    public void onViewCreated(View view,Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);

        //初めての生成時
        if(savedInstanceState==null) {
            /* TourismMapSubFragment生成 */

            //TourismMapSubFragmentインスタンス生成
            TourismMapSubFragment tourismMapSubFragment = TourismMapSubFragment.newInstance(userID, regionName);

            //トランザクション生成
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

            //フラグメント遷移時アニメーション設定
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

            //FrameLayoutに組み込む
            fragmentTransaction.replace(R.id.tourismFragmentFrameLayout, tourismMapSubFragment);

            //遷移実行
            fragmentTransaction.commit();
        }
    }

    /* TourismMapSubFragmentコールバック */
    @Override
    public void onTourismMapSubFragmentInteraction(int transitionID,LatLng userLocation)
    {
        //ユーザ位置保持
        if(userLocation!=null) {
            this.userLocation = new LatLng(userLocation.latitude, userLocation.longitude);
        }

        switch(transitionID)
        {
            case TourismMapSubFragment.TRANSITION_ID_ADD_REVIEW: {
                /* TourismAddReviewSubFragmentに遷移 */

                //トランザクション生成
                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

                //TourismAddReviewSubFragmentインスタンス生成
                TourismAddReviewSubFragment tourismAddReviewSubFragment = TourismAddReviewSubFragment.newInstance();

                //バックスタックに追加
                fragmentTransaction.addToBackStack(Constants.FragmentManager.TOURISM_ADD_REVIEW_SUB_FRAGMENT_TAG);

                //フラグメント遷移時アニメーション設定
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

                //FrameLayoutに組み込む
                fragmentTransaction.replace(R.id.tourismFragmentFrameLayout, tourismAddReviewSubFragment);

                //遷移実行
                fragmentTransaction.commit();
            }
                break;
            case TourismMapSubFragment.TRANSITION_ID_INFORMATION: {
                /* TourismInformationSubFragmentに遷移 */

                //トランザクション生成
                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

                //TourismInformationSubFragmentインスタンス生成
                TourismInformationSubFragment tourismInformationSubFragment = TourismInformationSubFragment.newInstance(regionName);

                //バックスタックに追加
                fragmentTransaction.addToBackStack(null);

                //フラグメント遷移時アニメーション設定
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

                //FrameLayoutに組み込む
                fragmentTransaction.replace(R.id.tourismFragmentFrameLayout, tourismInformationSubFragment);

                //遷移実行
                fragmentTransaction.commit();
            }
                break;
        }
    }

    /* TourismAddReviewSubFragmentコールバック */
    @Override
    public void onTourismAddReviewSubFragmentInteraction(int transitionID,Bitmap takenImage)
    {
        //遷移IDに応じてフラグメント遷移
        switch(transitionID) {
            case TourismAddReviewSubFragment.TRANSITION_ID_BACK:
                //1つ前のフラグメントに戻る
                getChildFragmentManager().popBackStack();
                break;
            case TourismAddReviewSubFragment.TRANSITION_ID_NEXT:
                /* TourismAddReviewSubFragment2に遷移 */

                //トランザクション生成
                FragmentTransaction fragmentTransaction =getChildFragmentManager().beginTransaction();

                //TourismAddReviewSubFragment2インスタンス生成
                TourismAddReviewSubFragment2 tourismAddReviewSubFragment2=TourismAddReviewSubFragment2.newInstance(takenImage);

                //バックスタックに追加
                fragmentTransaction.addToBackStack(null);

                //フラグメント遷移時アニメーション設定
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right);

                //FrameLayoutに組み込む
                fragmentTransaction.replace(R.id.tourismFragmentFrameLayout,tourismAddReviewSubFragment2);

                //遷移事項
                fragmentTransaction.commit();
                break;
        }
    }

    /* TourismAddReviewSubFragment2コールバック */
    @Override
    public void onTourismAddReviewSubFragment2Interaction(int transitionID,Bitmap takenImage)
    {
        //遷移IDに応じてフラグメント遷移
        switch(transitionID) {
            case TourismAddReviewSubFragment2.TRANSITION_ID_BACK:
                //1つ前のフラグメントに戻る
                getChildFragmentManager().popBackStack();
                break;
            case TourismAddReviewSubFragment2.TRANSITION_ID_NEXT:
                //撮影写真保持
                this.takenImage=takenImage;

                /* TourismAddReviewSubFragment3に遷移 */

                //トランザクション生成
                FragmentTransaction fragmentTransaction =getChildFragmentManager().beginTransaction();

                //TourismAddReviewSubFragment3インスタンス生成
                TourismAddReviewSubFragment3 tourismAddReviewSubFragment3=TourismAddReviewSubFragment3.newInstance(takenImage);

                //バックスタックに追加
                fragmentTransaction.addToBackStack(null);

                //フラグメント遷移時アニメーション設定
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right);

                //FrameLayoutに追加
                fragmentTransaction.replace(R.id.tourismFragmentFrameLayout,tourismAddReviewSubFragment3);

                //遷移実行
                fragmentTransaction.commit();
                break;
        }
    }

    /* TourismAddReviewSubFragment3コールバック */
    @Override
    public void onTourismAddReviewSubFragment3Interaction(int transitionID,String message)
    {
        //遷移IDに応じてフラグメント遷移
        switch(transitionID)
        {
            case TourismAddReviewSubFragment3.TRANSITION_ID_BACK:
                //1つ前のフラグメントに戻る
                getChildFragmentManager().popBackStack();
                break;
            case TourismAddReviewSubFragment3.TRANSITION_ID_NEXT:
                /* TourismAddReviewSubFragment4に遷移 */

                //トランザクション生成
                FragmentTransaction fragmentTransaction =getChildFragmentManager().beginTransaction();

                //TourismAddReviewSubFragment4インスタンス生成
                TourismAddReviewSubFragment4 tourismAddReviewSubFragment4=TourismAddReviewSubFragment4.newInstance(regionName,userLocation,message,takenImage);

                //バックスタックに追加
                fragmentTransaction.addToBackStack(null);

                //フラグメント遷移時アニメーション設定
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right);

                //FrameLayoutに追加
                fragmentTransaction.replace(R.id.tourismFragmentFrameLayout,tourismAddReviewSubFragment4);

                //遷移実行
                fragmentTransaction.commit();
                break;
        }
    }

    /* TourismAddReviewSubFragment4コールバック */
    @Override
    public void onTourismAddReviewSubFragment4Interaction()
    {
        //TourismAddReviewSubFragmentフラグメントまで戻る
        getChildFragmentManager().popBackStack(Constants.FragmentManager.TOURISM_ADD_REVIEW_SUB_FRAGMENT_TAG,0);
    }

    /* TourismInformationSubFragmentコールバック */
    @Override
    public void onTourismInformationSubFragmentInteraction(int transitionID,String title,String sentence)
    {
        //遷移IDに応じてフラグメント遷移
        switch(transitionID)
        {
            case TourismInformationSubFragment.TRANSITION_ID_BACK: {
                //1つ前のフラグメントに戻る
                getChildFragmentManager().popBackStack();
            }
                break;
            case TourismInformationSubFragment.TRANSITION_ID_NEXT: {
                /* TourismInformationSubFragment2に遷移 */

                //トランザクション生成
                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

                //TourismInformationSubFragment2インスタンス生成
                TourismInformationSubFragment2 tourismInformationSubFragment2 = TourismInformationSubFragment2.newInstance(title, sentence);

                //バックスタックに追加
                fragmentTransaction.addToBackStack(null);

                //フラグメント遷移時アニメーション設定
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

                //FrameLayoutに組み込む
                fragmentTransaction.replace(R.id.tourismFragmentFrameLayout, tourismInformationSubFragment2);

                //遷移実行
                fragmentTransaction.commit();
                break;
            }
        }
    }

    /* TourismInformationSubFragment2コールバック */
    @Override
    public void onTourismInformationSubFragment2Interaction()
    {
        //1つ前のフラグメントに戻る
        getChildFragmentManager().popBackStack();
    }
}
