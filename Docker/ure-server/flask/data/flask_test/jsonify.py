from flask import Flask, jsonify
import math
import psycopg2 as pg

app = Flask(__name__)
app.config['JSON_AS_ASCII'] = False #日本語文字化け対策
app.config["JSON_SORT_KEYS"] = False #ソートをそのまま

@app.route("/items", methods=['GET'])
def items():
    list = [
                dict(id=1, name="hoge", category=1),
                dict(id=2, name="fuga", category=2)
            ]
    response = jsonify(results=list)
    response.status_code = 200
    return response    

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)