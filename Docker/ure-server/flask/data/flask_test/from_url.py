from flask import Flask, render_template

app = Flask(__name__)

@app.route('/')
def index():
    return "Hello World !!"

@app.route('/<name>')
def hello(name):
    #return name
    return str(name)

## おまじない
if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000, debug=True, threaded=True)