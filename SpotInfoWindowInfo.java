package jp.androidbook.photostohint;

/* 詳細ウィンドウ情報データクラス */

import android.graphics.Bitmap;
import java.util.ArrayList;

/**
 * Created by Owner on 2018/10/14.
 */

public class SpotInfoWindowInfo
{
    //位置（緯度経度）
    private double latitude;
    private double longitude;

    //吹き出し種類
    private int type;

    //タイトル
    private String title;

    //説明
    private String description;

    //口コミリスト
    private ArrayList<String> reviewList;

    //画像
    private Bitmap image;

    /* コンストラクタ */
    public SpotInfoWindowInfo(double latitude,double longitude,int type, String title, String description, Bitmap image,ArrayList<String> reviewList)
    {
        //各種フィールドセット
        this.latitude=latitude;
        this.longitude=longitude;
        this.type=type;
        this.title=title;
        this.description=description;
        this.image=image;

        if(reviewList!=null) {
            this.reviewList = new ArrayList<>(reviewList);
        }
    }

    /* ゲッター */
    public double getLatitude(){return latitude;}
    public double getLongitude(){return longitude;}
    public int getType(){return type;}
    public String getTitle(){return title;}
    public String getDescription(){return description;}
    public Bitmap getImage(){return image;}
    public ArrayList<String> getReviewList(){return reviewList;}
}