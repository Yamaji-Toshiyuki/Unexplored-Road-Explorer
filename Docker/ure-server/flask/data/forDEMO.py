from flask import Flask, jsonify, request, send_from_directory
from werkzeug.datastructures import ImmutableMultiDict
from werkzeug.utils import secure_filename
from PIL.ExifTags import TAGS
from PIL import Image
import psycopg2 as pg
import routing
import datetime
import math
import os

ALLOWED_EXTENSIONS = set(['png', 'jpg', 'gif', 'bmp'])

app = Flask(__name__)
config = app.config
config["JSON_AS_ASCII"] = False #日本語文字化け対策
config["JSON_SORT_KEYS"] = False #ソートをそのまま
config["UPLOAD_FOLDER"] = '/home/photos' #アップロード先ディレクトリ

def getConnect(username, dbname, password):
    settings = "host=ure-server-postgres port=5432" + " dbname=" + str(dbname) + " user=" + str(username) + " password=" + str(password)
    try:
        connect = pg.connect(settings)
    except:
        return "connecting failed. / " + settings
    return connect

def makedata():
    connect_osm = getConnect("postgres", "osm_data", "postgres")
    cursor_osm = connect_osm.cursor()
    connect_ure = getConnect("postgres", "ure_data", "postgres")
    cursor_ure = connect_ure.cursor()
    sql = "SELECT osm_id, name, ST_AsText(ST_Transform(way, 4326)) FROM planet_osm_line WHERE st_intersects(way, st_transform(st_makepolygon(st_geomfromtext('LINESTRING(133.634403 34.236432,133.638297 34.236432,133.638297 34.233368,133.634403 34.233368,133.634403 34.236432)', 4326)), 3857))"
    cursor_osm.execute(sql)
    result = cursor_osm.fetchall()
    for items in result:
        if items[0] != 196213113 and items[0] != 508307550:
            print(str(items) + "\n")
            way = items[2]
            way = way.lstrip("LINESTRING(").rstrip(")").split(",")
            for i in range(len(way)):
                way[i] = way[i].split(" ")
            detail = "LINESTRING("
            for i in range(len(way)):
                times = round((float(way[i][0]) - float(way[i+1][0]))/0.00001)
                temp = round((float(way[i][1]) - float(way[i+1][1]))/0.00001)
                print(times)
                print(temp)
                if times > 0:
                    y_dim = 1
                else:
                    y_dim = -1
                if temp > 0:
                    x_dim = 1
                else:
                    x_dim = -1
                if items[0] == 196212271:
                    y_dim = -1
                    x_dim = 1
                elif items[0] == 196210190:
                    y_dim = -1
                    x_dim = 1
                elif items[0] == 196211176:
                    y_dim = -1
                    x_dim = 1
                print(abs(times))
                y_diff = y_dim * abs(float(way[i][0]) - float(way[i+1][0])) / times
                x_diff = x_dim * abs(float(way[i][1]) - float(way[i+1][1])) / times
                y = []
                x = []
                print(str(y_dim) + " " + str(x_dim))
                points = []
                for j in range(abs(times)):
                    y.append(float(way[i][0]) + y_diff * j)
                    x.append(float(way[i][1]) + x_diff * j)
                    points.append(str(y[j]) + " " + str(x[j]))
                    print(points[j])
                for j in range(len(points)):
                    detail += points[j] + ","
                if i+2 >= len(way):
                    break
            detail = detail.rstrip(",") + ")"
            print(detail)
            sql = "INSERT INTO forDEMO VALUES(" + str(items[0]) + ", '" + str(items[1]) + "', ST_Transform(ST_GeomFromText('" + detail + "', 4326), 3857))"
            cursor_ure.execute(sql)
    connect_ure.commit()

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)