/* お題スポット撮影正否判定 (出力0:不正解 1:正解)*/

/* OpenCVを利用 */
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/opencv.hpp>
#include <opencv2/features2d/features2d.hpp>

#include<iostream>
#include<vector>
#include<sstream>
#include<iterator>
#include<algorithm>
#include<fstream>
#include<string>

using namespace cv;
using namespace std;

//400x400にリサイズする
static const pair<double, double> SIZE = make_pair(400, 400);

//照合する2枚の画像のファイルパスを受け取り特徴点マッチングで正否判定
void featuresMatching(const string filePath1,const string filePath2);

//ヒストグラム平均化による明度の均一化
Mat myEqualizeHist(const Mat& src);

/* メイン関数（コマンドライン引数でお題スポットIDを受け取る） */
int main(int argc,char *argv[])
{
        //特徴点マッチングによる正否判定実行（第一引数：撮影写真のパス、第二引数：お題スポットIDから生成されたお題スポット写真ファイルパス）
	featuresMatching("takenImage.jpg","../open_data/photos/theme/"+string(argv[1])+".jpg");

	//終了
	return 0;
}

/* 照合する2枚の画像のファイルパスを受け取り特徴点マッチングで正否判定 */
void featuresMatching(const string filePath1,const string filePath2)
{
	//画像読み込み
	Mat img1 = imread(filePath1);
	Mat img2 = imread(filePath2);
	Mat out;

        //ファイルが存在しなければ"不正解"終了
	if (!img1.data || !img2.data)
	{
		cout<<"0"<<endl;
		return;
	}

	//400x400にリサイズ
	resize(img1, img1, cv::Size(), SIZE.first / img1.cols, SIZE.second / img1.rows);
	resize(img2, img2, cv::Size(), SIZE.first / img2.cols, SIZE.second / img2.rows);

	//ヒストグラム平均化
	img1 = myEqualizeHist(img1);
	img2 = myEqualizeHist(img2);

	//RGBからグレイスケールに変換
	cvtColor(img1, img1, CV_BGR2GRAY);
	cvtColor(img2, img2, CV_BGR2GRAY);

	//特徴点リストと特徴量変数宣言
	vector<KeyPoint> keyPoints1,keyPoints2;
	Mat descriptor1, descriptor2;

	//ORB特徴点検出
	auto orb = ORB::create();
	orb->detectAndCompute(img1, Mat(), keyPoints1, descriptor1);
	orb->detectAndCompute(img2, Mat(), keyPoints2, descriptor2);

	//特徴点マッチング（特徴点毎に上位2組）
	auto matcher = DescriptorMatcher::create("BruteForce-Hamming");
	vector<vector<DMatch>> matches;
	matcher->knnMatch(descriptor1, descriptor2, matches, 2);

        double thresholdRatio = 0.6;
	vector<DMatch> validMatches;

	//各上位2組について
	for (auto& match : matches)
	{
		//1位の特徴量が2位の0.6倍より小さければ（小さいほど良い）採用(特徴量が際立っているものを探す)
		if (match[0].distance < thresholdRatio*match[1].distance)
		{
			validMatches.push_back(match[0]);
		}
	}

	//採用された特徴点の組が10個以上なら正解、それ未満なら不正解
	if (validMatches.size() >= 10)
	{
		cout << "1" << endl;
	}
	else
	{
		cout << "0" << endl;
	}
}

/* ヒストグラム平均化による明度の均一化 */
Mat myEqualizeHist(const Mat& src)
{
	Mat hsv, out;
	vector<Mat> channels(3);

	//RGBからHSVに変換
	cvtColor(src, hsv, CV_BGR2HSV);
    
        //H,S,Vの3チャンネル分解
	split(hsv, channels);

        //Vについてヒストグラム平均化(明度の均一化)
	equalizeHist(channels[2], channels[2]);

	//HSV統合
	merge(channels, hsv);

	//HSVからRGBに変換
	cvtColor(hsv, out, CV_HSV2BGR);

	//出力画像を返す
	return out;
}
