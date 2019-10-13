import psycopg2 as pg
import csv
import os

def getConnect(username, dbname, password):
    settings = "host=ure-server-postgres port=5432" + " dbname=" + str(dbname) + " user=" + str(username) + " password=" + str(password)
    try:
        connect = pg.connect(settings)
    except:
        return "connecting failed. / " + settings
    return connect

print(os.listdir())

with open('id_7_log.csv') as f:
    data = f.read().split("\n")[1:]
    connect_ure = getConnect("ure", "ure_data", "procon30")
    cursor_ure = connect_ure.cursor()
    for i in range(len(data)):
        sql = "INSERT INTO example_log VALUES(0X" + str(data[i]) + ")" 
        print(sql)
        cursor_ure.execute(sql)
    connect_ure.commit()
    cursor_ure.close()
    connect_ure.close()