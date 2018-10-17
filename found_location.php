<?php
/* お題発見時処理 */

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
if(!isset($params["user_id"])||!isset($params["region_name"])||!isset($params["gain_hint_point"])||!isset($params["gain_search_point"])||!isset($params["found_state"])||!isset($params["target_location_name"])||!isset($params["comment"]))
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

//ヒントポイント加算、探索ポイント加算、探索パラメータリセット
$sql="UPDATE search_region_info";
$sql.=" SET hint_point=hint_point+'{$params["gain_hint_point"]}',search_point=search_point+'{$params["gain_search_point"]}',target_location_name=NULL,hint_level=0";
$sql.=" WHERE user_id='{$params["user_id"]}' AND region_name='{$params["region_name"]}'";
$sql_result=$db->query($sql);
$sql_result=json_decode($sql_result,true);

//エラー発生時エラー内容をJSON形式で送信して終了
if($sql_result['status']===FALSE)
{
	$rtn=array(
		'status' => FALSE,
                'result' => -1,
		'error' => $sql_result['error']
	);
	exit(json_encode($rtn));
}

//発見したお題についての探索状況を発見済みにする
$sql="UPDATE search_location_info";
$sql.=" SET state='{$params["found_state"]}',latitude0=0,latitude1=0,latitude2=0,longitude0=0,longitude1=0,longitude2=0";
$sql.=" WHERE user_id='{$params["user_id"]}' AND region_name='{$params["region_name"]}' AND location_name='{$params["target_location_name"]}'";
$sql_result=$db->query($sql);
$sql_result=json_decode($sql_result,true);

//エラー発生時エラー内容をJSON形式で送信して終了
if($sql_result['status']===FALSE)
{
	$rtn=array(
		'status' => FALSE,
                'result' => -1,
		'error' => $sql_result['error']
	);
	exit(json_encode($rtn));
}

//発見したお題スポットについての口コミを登録
$date=date('Y-m-d H:i:s');
$sql="INSERT INTO theme_spot_review(region_name,location_name,comment,registered_time) VALUES('{$params["region_name"]}','{$params["target_location_name"]}','{$params["comment"]}','{$date}')";
$sql_result=$db->query($sql);
$sql_result=json_decode($sql_result,true);

//エラー発生時エラー内容をJSON形式で送信して終了
if($sql_result['status']===FALSE)
{
	$rtn=array(
		'status' => FALSE,
                'result' => -1,
		'error' => $sql_result['error']
	);
	exit(json_encode($rtn));
}

//発見したお題スポットをアルバムに登録
$sql="INSERT INTO album_comment(user_id,region_name,location_name,comment) VALUES('{$params["user_id"]}','{$params["region_name"]}','{$params["target_location_name"]}','{$params["comment"]}')";
$sql_result=$db->query($sql);
$sql_result=json_decode($sql_result,true);

//エラー発生時エラー内容をJSON形式で送信して終了
if($sql_result['status']===FALSE)
{
	$rtn=array(
		'status' => FALSE,
                'result' => -1,
		'error' => $sql_result['error']
	);
	exit(json_encode($rtn));
}

//正常終了
$rtn=array(
	'status' => TRUE,
        'result' => 0,
	'error' => ""
);
echo json_encode($rtn);