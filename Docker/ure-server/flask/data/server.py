#---------------------------------------------------------------------------------------------#
# dockerで走ってる ure-server-flask 内部で動かすAPI
# 
# 機能:
# http://ure-server-flask/
# : サーバー起動チェック
# http://ure-server-flask/register/<name>
# : <name>をユーザー名として登録，idを返す
# http://ure-server-flask/search_road/<radius>/<now_location> 
# : 現在地を中心に半径<radius>の円が内接する四角形の範囲で道路を検索してjsonを返す
# http://ure-server-flask/upload_photo/<user_id>/<user_name>
# : ユーザーからアップロードされた写真を受け取って保存
# http://ure-server-flask/do_sql/<db>/<sql>
# : データベース<db>にログインし，<sql> を実行．実行した結果を返す
#
#---------------------------------------------------------------------------------------------#

from flask import Flask, jsonify, request, send_from_directory
from werkzeug.utils import secure_filename
import psycopg2 as pg
import math
import os

ALLOWED_EXTENSIONS = set(['png', 'jpg', 'gif'])

app = Flask(__name__)
app.config['JSON_AS_ASCII'] = False #日本語文字化け対策
app.config["JSON_SORT_KEYS"] = False #ソートをそのまま
app.config['UPOLOAD_FOLDER'] =  './photos' #アップロード先ディレクトリ

def allwed_file(filename):
    # .があるかどうかのチェックと、拡張子の確認
    # OKなら１、だめなら0
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

def getConnect(username, dbname, password):
    settings = "host=ure-server-postgres port=5432" + " dbname=" + str(dbname) + " user=" + str(username) + " password=" + str(password)
    try:
        connect = pg.connect(settings)
    except:
        return "connecting failed. / " + settings
    return connect

@app.route('/')
def index():
    return "Server Ready"

@app.route('/register/<name>')
def register(name):
    try:
        connect = getConnect("ure", "ure_data", "procon30")
        cursor = connect.cursor()
    except:
        return "Error Occured at connect to server / " + connect
    try:
        sql = "SELECT COUNT(*) FROM user_list"
        cursor.execute(sql)
        result = cursor.fetchall()
        sql = "INSERT INTO user_list VALUES( "+ str(result[0][0]) +", '" + str(name) + "')"
        cursor.execute(sql)
        connect.commit()
    except:
        return "Error Occurred at execute sql / " + sql
    cursor.close()
    connect.close()
    return str(result[0][0])
    
@app.route('/search_road/<radius>/<now_location>')
def search_route(radius, now_location):
    try:
        connect = getConnect("ure", "osm_data", "procon30")
        cursor = connect.cursor()
    except:
        return "Error Occured at connect to server / " + connect
    y_diff = (360*float(radius)/(2*math.pi*6356752.314))
    x_diff = (360*float(radius)/(math.cos(float(now_location.split(",")[0])*2*math.pi*6356752.314))
    point1x = float(now_location.split(",")[0]) - x_diff
    point2x = float(now_location.split(",")[0]) + x_diff
    point1y = float(now_location.split(",")[1]) - y_diff
    point2y = float(now_location.split(",")[1]) + y_diff
    sql = "SELECT name, ST_Astext(ST_Transform(way, 4326)) FROM planet_osm_line WHERE way && ST_Transform(ST_GeomFromText('LINESTRING(" + str(point1x) + " " + str(point1y) + " , " + str(point2x) + " " + str(point2y) + ")', 4326), 900913);"
    try:
        cursor.execute(sql)
        result = cursor.fetchall()
    except:
        return "Error Occurred at execute sql / " + sql
    cursor.close()
    connect.close()
    temp = result
    result = []
    for i in range(len(temp)):
        result.append(dict(name=temp[i][0], way=temp[i][1]))
    return jsonify({
        'result':result
    })

@app.route('/upload_photo/<user_id>/<user_name>', methods=['GET', 'POST'])
def upload_photo(user_id, user_name):
    try:
        connect = getConnect("ure", "ure_data", "procon30")
        cursor = connect.cursor()
    except:
        return "Error Occured at connect to server / " + connect
    sql = "SELECT user_name FROM user_list WHERE user_id=" + str(user_id)
    try:
        cursor.execute(sql)
        result = cursor.fetchall()
        if user_name != result[0][0]:
            raise ValueError()
    except:
        return "Error Occurred at Authentication / " + sql
    if request.method == 'POST':
        if 'file' not in request.files:
            return "Error Occurred / file don't sended."
        file = request.files['file']
        if file.filename == '':
            return "Error Occurred / file don't sended."
        if file and allwed_file(file.filename):
            filename = secure_filename(file.filename)
            file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
    return "upload Successful"
    

@app.route('/do_sql/<db>/<sql>')
def do_sql(db, sql):
    try:
        connect = getConnect("postgres", db, "postgres")
        cursor = connect.cursor()
    except:
        return "Error Occured at connect to server / " + connect
    try:
        cursor.execute(sql)
        result = cursor.fetchall()
    except:
        return "Error Occurred at execute sql / " + sql
    cursor.close()
    connect.close()
    return str(result)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)