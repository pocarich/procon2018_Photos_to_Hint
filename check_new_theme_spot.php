<?php
/* ログイン時お題スポットの追加チェック */

//データベース操作クラス
class MyDB
{
     //mysqli使用
     public $mysqli;

     /* コンストラクタ */
     function __construct($host_or_ip,$user_id,$password,$db_name)
     {
          //データベースアクセス
          $this->mysqli = new mysqli($host_or_ip,$user_id,$password,$db_name);

          //コネクションエラー発生時
          if($this->mysqli->connect_error)
          {
               //処理中断
               echo $this->mysqli->connect_error;
               exit;
          }
          else
          {
               //文字コード"UTF-8"使用
               $this->mysqli->set_charset("utf8");
          }
     }

     /* デストラクタ */
     function __destruct()
     {
          //データベースアクセス終了
          $this->mysqli->close();
     }

     /* クエリ実行 */
     function query($sql)
     {
          //クエリを実行して結果を受け取る
          $result = $this->mysqli->query($sql);

          //エラー発生時
          if($result == FALSE)
          {
               //エラーをJSON形式で返す
               $error = $this->mysqli->errno.": ".$this->mysqli->error;
               $rtn = array(
                    'status' => FALSE,
                    'count' => 0,
                    'result' => "",
                    'error' => $error
               );
               return json_encode($rtn);
          }

          //ブーリアン結果(SELECT文以外)の場合
          if($result === TRUE)
          {
               //変更を受けたレコード数をJSON形式で返す
               $rtn = array(
                    'status' => TRUE,
                    'count' => $this->mysqli->affected_rows,
                    'result' => "",
                    'error' => ""
               );
               return json_encode($rtn);
          }

          //ブーリアン結果でない場合
          else
          {
               //取得データをJSON形式で返す
               $data = array();
               while($row = $result->fetch_assoc())
               {
                    $data[]=$row;
               }
               $result->close();

               $rtn = array(
                    'status' => TRUE,
                    'count' => $this->mysqli->affected_rows,
                    'result' => $data,
                    'error' =>""
               );
               return json_encode($rtn);
          }

          //文字列のエスケープ
          function escape($str)
          {
               return $this->mysqli->real_escape_string($str);
          }
     }   
}

//ヘッダ：JSON形式、文字コード"UTF-8"
header('Content-Type: application/json; charset=utf-8');

//クライアントからのデータ受信
$params=json_decode(file_get_contents('php://input'),true);

//必要なパラメータが受信データに含まれていない場合エラー
if(!isset($params["user_id"])||!isset($params["hint_point"])||!isset($params["search_point"]))
{
	$rtn=array(
		'status' => FALSE,
                'result' => -1,
		'error' => 'some parameters are not set.'
	);
	exit(json_encode($rtn));
}

//データベース接続
$db = new MyDB("*****","*****","*****","*****");

//お題スポットから地名取得
$region_name_list=$db->query("SELECT DISTINCT region_name FROM map_theme_spot");
$region_name_list=json_decode($region_name_list,true);

//エラー発生時エラー内容をJSON形式で送信して終了
if($region_name_list['status']===FALSE)
{
	$rtn=array(
		'status' => FALSE,
                'result' => -1,
		'error' => $region_name_list['error']
	);
	exit(json_encode($rtn));
}

//お題スポット取得
$location_name_list=$db->query("SELECT region_name,location_name FROM map_theme_spot");
$location_name_list=json_decode($location_name_list,true);

//エラー発生時エラー内容をJSON形式で送信して終了
if($location_name_list['status']===FALSE)
{
	$rtn=array(
		'status' => FALSE,
                'result' => -1,
		'error' => $location_name_list['error']
	);
	exit(json_encode($rtn));
}

//各地域について
foreach($region_name_list['result'] as $region_name_value)
{
        //未登録ならその地域についての探索状況管理レコード追加
	$search_region_insert_result=$db->query("INSERT INTO search_region_info(user_id,region_name,hint_point,search_point) SELECT * FROM (SELECT '{$params["user_id"]}','{$region_name_value["region_name"]}',{$params['hint_point']},{$params['search_point']}) AS TMP WHERE NOT EXISTS(SELECT * FROM search_region_info WHERE region_name='{$region_name_value["region_name"]}')");
	$search_region_insert_result=json_decode($search_region_insert_result,true);
	if($search_region_insert_result['status']===FALSE)
	{
		$rtn=array(
			'status' => FALSE,
             	 	'result' => -1,
			'error' =>$search_region_insert_result['error']
		);
		exit(json_encode($rtn));
	}
}

//各お題スポットについて
foreach($location_name_list['result'] as $location_name_value)
{
        //未登録ならそのお題スポットについての探索状況管理レコード追加
	$search_location_insert_result=$db->query("INSERT INTO search_location_info(user_id,region_name,location_name) SELECT * FROM (SELECT '{$params["user_id"]}','{$location_name_value["region_name"]}','{$location_name_value["location_name"]}') AS TMP WHERE NOT EXISTS(SELECT * FROM search_location_info WHERE region_name='{$location_name_value["region_name"]}' AND location_name='{$location_name_value["location_name"]}')");
	$search_location_insert_result=json_decode($search_location_insert_result,true);
	if($search_location_insert_result['status']===FALSE)
	{
		$rtn=array(
			'status' => FALSE,
             	 	'result' => -1,
			'error' =>$search_location_insert_result['error']
		);
		exit(json_encode($rtn));
	}
}

//正常終了
$rtn=array(
	'status' => TRUE,
        'result' => 0,
	'error' =>""
);
echo json_encode($rtn);	
