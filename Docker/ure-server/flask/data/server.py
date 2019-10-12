#---------------------------------------------------------------------------------------------#
# dockerで走ってる ure-server-flask 内部で動かすAPI
# 
# 機能:
# http://ure-server-flask/
# : サーバー起動チェック
# http://ure-server-flask/register/<name>
# : <name>をユーザー名として登録，idを返す
# http://ure-server-flask/search_road/<user_id>/<user_name>/<radius>/<now_location> 
# : 現在地を中心に半径<radius>の円が内接する四角形の範囲で道路を検索してjsonを返す
# http://ure-server-flask/upload_photo/<user_id>/<user_name>
# : ユーザーからアップロードされた写真を受け取って保存
# http://ure-server-flask/do_sql/<db>/<sql>
# : データベース<db>にログインし，<sql> を実行．実行した結果を返す
# http:///logging_switch/<user_id>/<user_name>/<state>
# : ロギングの開始・停止．<state>には ON もしくは OFF を指定．
# http://logging/<user_id>/<user_name>/<now_location>
# : <now_location>をロギングする．
#---------------------------------------------------------------------------------------------#

from flask import Flask, jsonify, request, send_from_directory
from werkzeug.utils import secure_filename
import psycopg2 as pg
import datetime
import math
import os

ALLOWED_EXTENSIONS = set(['png', 'jpg', 'gif'])

app = Flask(__name__)
app.config["JSON_AS_ASCII"] = False #日本語文字化け対策
app.config["JSON_SORT_KEYS"] = False #ソートをそのまま
app.config["UPOLOAD_FOLDER"] =  '~data/photos' #アップロード先ディレクトリ

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

def user_auth(user_id, user_name):
    try:
        connect_osm = getConnect("ure", "osm_data", "procon30")
        cursor_osm = connect_osm.cursor()
        connect_ure = getConnect("ure", "ure_data", "procon30")
        cursor_ure = connect_ure.cursor()
    except:
        return jsonify({
            'status':"Failure",
            'message':"Error Occured at connect to server"
        })
    sql = "SELECT user_name FROM user_list WHERE user_id=" + str(user_id)
    try:
        cursor_ure.execute(sql)
        result = cursor_ure.fetchall()
        if user_name != result[0][0]:
            raise ValueError()
    except ValueError:
        return "Error Occurred at Authentication / " + sql
    except:
        return "Error Occurred at execute sql / " + sql
    cursor_osm.close()
    cursor_ure.close()
    connect_osm.close()
    connect_ure.close()
    return "Successful"

def culc_metre(radius, now_location):
    temp = now_location
    now_location = {}
    now_location["x"] = float(temp.split(",")[0])
    now_location["y"] = float(temp.split(",")[1])
    y_diff = (360*float(radius)/(2*math.pi*6356752.314))
    x_diff = y_diff / math.cos(math.pi * now_location["y"] / 180)
    result = []
    result.append(str(now_location["x"] - x_diff) + " " + str(now_location["y"] - y_diff))
    result.append(str(now_location["x"] - x_diff) + " " + str(now_location["y"] + y_diff))
    result.append(str(now_location["x"] + x_diff) + " " + str(now_location["y"] + y_diff))
    result.append(str(now_location["x"] + x_diff) + " " + str(now_location["y"] - y_diff))
    return result

def make_square(points):
    result = "'LINESTRING("
    for i in range(len(points)):
        result += str(points[i-len(points)]) + ","
    result += str(points[0]) + ")'"
    return result

def map_matching(linestring):
    try:
        connect_osm = getConnect("ure", "osm_data", "procon30")
        cursor_osm = connect_osm.cursor()
        connect_ure = getConnect("ure", "ure_data", "procon30")
        cursor_ure = connect_ure.cursor()
    except:
        return jsonify({
            'status':"Failure",
            'message':"Error Occured at connect to server"
        })
    print("Processing Map_Matching...")
    linestring = linestring.lstrip("LINESTRING(").rstrip(")")
    linestring = linestring.split(",")
    print("log = " + str(linestring))
    match_ids = []
    for i in range(len(linestring)):
        print(str(len(linestring)) + " " + str(i))
        search_area = culc_metre(100, linestring[i].replace(" ", ","))
        sql = "SELECT osm_id FROM planet_osm_line WHERE ST_Intersects(way, ST_Transform(ST_MakePolygon(ST_GeomFromText(" + make_square(search_area) + ", 4326)), 3857)) AND route != 'ferry' AND route != 'rail';"
        cursor_osm.execute(sql)
        id_list = cursor_osm.fetchall()
        # print("id_lists = " + str(id_list))
        dist = 0
        index = 0
        for j in range(len(id_list)):
            sql = "SELECT ST_Distance((SELECT way FROM planet_osm_line WHERE osm_id = " + str(id_list[j][0]) + "), ST_Transform(ST_GeomFromText('POINT(" + linestring[i] + ")', 4326), 3857));"
            cursor_osm.execute(sql)
            temp = cursor_osm.fetchall()
            if temp[0][0] < dist or j == 0:
                dist = temp
                index = j
        try:
            match_ids.append(id_list[index][0])
        except IndexError:
            linestring.pop(i)
            i -= 1
        except:
            print("Error Occured at search nearest road from\" " + str(linestring[i]) + "\".")
        print("match_ids = " + str(match_ids))
        if len(linestring)-1 <= i+1:
            break
    points = []
    for i in range(len(match_ids)):
        sql = "SELECT ST_AsText(ST_Transform(ST_ClosestPoint((SELECT way FROM planet_osm_line WHERE osm_id = " + str(match_ids[i]) + "), ST_Transform(ST_GeomFromText('POINT(" + str(linestring[i]) + ")', 4326),3857)), 4326));"
        cursor_osm.execute(sql)
        temp = cursor_osm.fetchall()
        points.append(str(i) + "/" + str(temp[0][0]))
    for i in range(len(points)):
        points[i] = points[i].split("/")
        print("points[" + str(i) + "] " + str(points[i]))
    result = []
    temp = 0
    for i in range(len(match_ids)):
        if match_ids[i] != temp:
            temp = match_ids[i]
            for j in range(i, len(match_ids)):
                if temp == match_ids[j]:
                    i = min(i+1 , len(match_ids)-1)
            way = "LINESTRING("
            for j in range(len(points)):
                way += points[j][1].lstrip("POINT(").replace(")", ",")
            way = way.rstrip(",") + ")"
            result.append([match_ids[i], way])
    print(str(result))
    cursor_osm.close()
    cursor_ure.close()
    connect_osm.close()
    connect_ure.close()
    return result

@app.route('/')
def index():
    return "Server Ready"

@app.route('/favicon.ico')
def favicon():
    return app.send_static_file("images/fav.ico")

@app.route('/user_auth/<user_id>/<user_name>')
def user_auth_alt(user_id, user_name):
    try:
        connect_osm = getConnect("ure", "osm_data", "procon30")
        cursor_osm = connect_osm.cursor()
        connect_ure = getConnect("ure", "ure_data", "procon30")
        cursor_ure = connect_ure.cursor()
    except:
        return jsonify({
            'status':"Failure",
            'message':"Error Occured at connect to server"
        })
    sql = "SELECT user_name FROM user_list WHERE user_id=" + str(user_id)
    try:
        cursor_ure.execute(sql)
        result = cursor_ure.fetchall()
        if user_name != result[0][0]:
            raise ValueError()
    except ValueError:
        cursor_osm.close()
        cursor_ure.close()
        connect_osm.close()
        connect_ure.close()
        return jsonify({
            'status':"failure",
            'message':"Error Occurred at Authentication / " + sql
        })
    except:
        cursor_osm.close()
        cursor_ure.close()
        connect_osm.close()
        connect_ure.close()
        return jsonify({
            'status':"failure",
            'message':"Error Occurred at execute sql / " + sql
        })
    cursor_osm.close()
    cursor_ure.close()
    connect_osm.close()
    connect_ure.close()
    return jsonify({
        'status':"success"
    })

@app.route('/register/<name>')
def register(name):
    try:
        connect = getConnect("ure", "ure_data", "procon30")
        cursor = connect.cursor()
    except:
        return jsonify({
            'status':"Failure",
            'message':"Error Occured at connect to server / " + connect
        })
    try:
        sql = "SELECT COUNT(*) FROM user_list"
        cursor.execute(sql)
        result = cursor.fetchall()
        sql = "INSERT INTO user_list VALUES( "+ str(result[0][0]) +", '" + str(name) + "')"
        cursor.execute(sql)
        #sql = "CREATE TABLE id_" + str(result[0][0]) + "_explored (osm_id bigint, ure_id int, way geometry(LineString, 3857), First_time date, latest_date date)"
        sql = "CREATE TABLE id_0_explored (osm_id bigint, ure_id int, way geometry(LineString, 3857), First_time date, latest_date date)"
        cursor.execute(sql)
        connect.commit()
    except:
        return jsonify({
            'status':"failure",
            'message':"Error Occurred at execute sql / " + sql
        })
    cursor.close()
    connect.close()
    return jsonify({
        'status':"success",
        'id':str(result[0][0])
    })
    
@app.route('/search_road/<user_id>/<user_name>/<radius>/<now_location>')
def search_road(user_id, user_name, radius, now_location):
    try:
        connect_osm = getConnect("ure", "osm_data", "procon30")
        cursor_osm = connect_osm.cursor()
        connect_ure = getConnect("ure", "ure_data", "procon30")
        cursor_ure = connect_ure.cursor()
    except:
        return jsonify({
            'status':"Failure",
            'message':"Error Occured at connect to server"
        })
    auth_result = user_auth(user_id, user_name)
    if auth_result == "Successful":
        search_area = culc_metre(radius, now_location)
        sql = "select osm_id, name, ST_Astext(ST_Transform(way, 4326)) from planet_osm_line where st_intersects(way, st_transform(st_makepolygon(st_geomfromtext(" + make_square(search_area) + ", 4326)), 3857))"
        print(sql)
        cursor_osm.execute(sql)
        roads = cursor_osm.fetchall()
        print(len(roads))
        sql = "SELECT osm_id, ST_Astext(ST_Transform(way, 4326)) FROM id_" + str(user_id) + "_explored"
        for i in range(len(roads)):
            if i == 0 :
                sql += " WHERE osm_id = " + str(roads[i][0])
            else:
                sql += " OR osm_id = " + str(roads[i][0])
        cursor_ure.execute(sql)
        exproads = cursor_ure.fetchall()
        cursor_osm.close()
        connect_osm.close()
        cursor_ure.close()
        connect_ure.close()
        if exproads == None:
            result = roads[:][1:2]
            return jsonify({
                'status':"success",
                'search_range':"(" + str(search_area[0]) + "," + str(search_area[2]) + ")",
                'result':result
            })
        result = []
        for i in range(len(roads)):
            flag = 0
            for j in range(len(exproads)):
                if roads[i][0] == exproads[j][0]:
                    temp = str(exproads[j][1])
                    temp = temp.lstrip("LINESTRING(").rstrip(")")
                    index = roads[i][2].find(temp)
                    if index == -1:
                        continue
                    print(index)
                    result.append([roads[i][1], "LINESTRING(" + roads[i][2][:index].rstrip(",") + ")"])
                    result.append([roads[i][1], "LINESTRING(" + roads[i][2][index+len(temp):].lstrip(",") + ")"])
                    flag = 1
            if flag == 0:
                result.append([roads[i][1], roads[i][2]])
        temp = result
        way = []
        for i in range(len(temp)):
            way.append(temp[i][1])
            way[i] = way[i].lstrip("LINESTRING(").rstrip(")")
            way[i] = way[i].split(",")
            for j in range(len(way[i])):
                way[i][j] = way[i][j].split(" ")
        x_min = float(search_area[0].split(" ")[0])
        y_min = float(search_area[0].split(" ")[1])
        x_max = float(search_area[2].split(" ")[0])
        y_max = float(search_area[2].split(" ")[1])
        collect = []
        for i in range(len(way)):
            collect.append(False)
            for j in range(len(way[i])):
                if float(way[i][j][0]) > x_min and float(way[i][j][0]) < x_max and float(way[i][j][1]) > y_min and float(way[i][j][1]) < y_max :
                    collect[i] = True
                    break
        result = []
        print(collect)
        for i in range(len(temp)):
            if collect[i] == True:
                result.append(dict(name=temp[i][0], way=temp[i][1]))
        return jsonify({
            'status':"success",
            'search_range':"(" + str(search_area[0]) + "," + str(search_area[2]) + ")",
            'result':result
        })
    else:
        return jsonify({
            'status':"failure",
            'message':auth_result
        })

@app.route('/logging_switch/<user_id>/<user_name>/<state>')
def logging_switch(user_id, user_name, state):
    try:
        connect = getConnect("ure", "ure_data", "procon30")
        cursor = connect.cursor()
    except:
        return jsonify({
            'status':"Failure",
            'message':"Error Occured at connect to server / " + connect
        })
    auth_result = user_auth(user_id, user_name)
    if auth_result == "Successful":
        if state == "ON":
            try:
                sql = "CREATE TABLE id_" + str(user_id) + "_log ( way geometry(Point, 3857))"
                cursor.execute(sql)
            except:
                cursor.close()
                connect.close()
                return jsonify({
                    'status':"failure",
                    'message':"Error Occurred at execute sql / " + sql
                })
            connect.commit()
            cursor.close()
            connect.close()
            return "logging ready."
        elif state == "OFF":
            print("Get \"logging finish\". Processing...")
            sql = "SELECT ST_Astext(ST_Transform(way, 4326)) FROM id_" + str(user_id) + "_log"
            cursor.execute(sql)
            result = cursor.fetchall()
            log = "LINESTRING("
            for i in range(len(result)):
                log += (str(result[i]).lstrip("('POINT()')").rstrip(")',)") + ",")
            log = log.rstrip(",") + ")"
            print("log = " + log)
            expect_route = map_matching(log)
            for i in range(len(expect_route)):
                sql = "SELECT COUNT(*) FROM id_" + str(user_id) + "_explored WHERE osm_id = " + str(expect_route[i][0])
                cursor.execute(sql)
                elements = cursor.fetchall()
                sql = "INSERT INTO id_" + str(user_id) + "_explored VALUES(" + str(expect_route[i][0]) + "," + str(elements[0][0] + 1) + ", ST_Transform(ST_GeomFromText('" + str(expect_route[i][1]) + "', 4326), 3857),   null, to_date('" + str(datetime.date.today()) + "', 'YYYY-MM-DD'))"
            cursor.execute(sql)
            sql = "DROP TABLE id_" + str(user_id) + "_log"
            cursor.execute(sql)
            connect.commit()
            cursor.close()
            connect.close()
            return jsonify({
                'status':"success",
                'message':"logging finished.",
                'way':expect_route
            })
        else:
            return jsonify({
                'status':"failure",
                'message':"Error Occurred / state is invalid."
            })
    else:
        return jsonify({
            'status':"failure",
            'message':auth_result
        })

@app.route('/logging/<user_id>/<user_name>/<now_location>')
def logging(user_id, user_name, now_location):
    try:
        connect = getConnect("ure", "ure_data", "procon30")
        cursor = connect.cursor()
    except:
        return jsonify({
            'status':"Failure",
            'message':"Error Occured at connect to server / " + connect
        })
    auth_result = user_auth(user_id, user_name)
    if auth_result == "Successful":
        sql = "INSERT INTO id_" + str(user_id) + "_log VALUES(ST_Transform(ST_GeomFromText('POINT(" + str(now_location.split(",")[0]) + " " + str(now_location.split(",")[1]) + ")', 4326), 3857))"
        cursor.execute(sql)
        connect.commit()
        cursor.close()
        connect.close()
        return "Successful"
    else:
        return jsonify({
            'status':"failure",
            'message':auth_result
        })

@app.route('/upload_photo/<user_id>/<user_name>', methods=['GET', 'POST'])
def upload_photo(user_id, user_name):
    try:
        connect = getConnect("ure", "ure_data", "procon30")
        cursor = connect.cursor()
    except:
        return jsonify({
            'status':"Failure",
            'message':"Error Occured at connect to server / " + connect
        })
    result = user_auth(user_id, user_name)
    if result == "Successful":
        if request.method == 'POST':
            print(request.files)
            if 'file' not in request.files:
                return "Error Occurred / file don't sended."
            file = request.files['file']
            if file.filename == '':
                return "Error Occurred / file don't sended."
            if file and allwed_file(file.filename):
                filename = secure_filename(file.filename)
                file.save(os.path.join(app.config["UPLOAD_FOLDER"], filename))
        return "upload Successful"
    else:
        return "Error Occurred / authentication faild."

@app.route('/do_sql/<db>/<sql>')
def do_sql(db, sql):
    try:
        connect = getConnect("postgres", db, "postgres")
        cursor = connect.cursor()
    except:
        return jsonify({
            'status':"Failure",
            'message':"Error Occured at connect to server / " + connect
        })
    try:
        cursor.execute(sql)
        result = cursor.fetchall()
    except:
        return jsonify({
            'status':"failure",
            'message':"Error Occurred at execute sql / " + sql
        })
    cursor.close()
    connect.close()
    return str(result)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)