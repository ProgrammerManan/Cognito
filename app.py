from flask import Flask, render_template, request, redirect, session, jsonify
import pyrebase
import firebase_admin
from firebase_admin import credentials, auth as admin_auth, db

import getResponceUrl
import requests
import re
app = Flask(__name__)

app.secret_key = "secret"


"""
DATABASE
"""
cred = credentials.Certificate("/Users/manan/workspace/Projects/cognito-apcsa/adminsdk/firebase-sdk.json")
firebase_admin.initialize_app(cred, {"databaseURL" : "https://apcsa-cognito-default-rtdb.firebaseio.com/"})
ref = db.reference("/")

config = {
    'apiKey': "AIzaSyDUuoTEjiuefk9zg6W_Tpcve9a4n4DF0o8",
    'authDomain': "apcsa-cognito.firebaseapp.com",
    'databaseURL': "https://apcsa-cognito-default-rtdb.firebaseio.com",
    'projectId': "apcsa-cognito",
    'storageBucket': "apcsa-cognito.appspot.com",
    'messagingSenderId': "137736336826",
    'appId': "1:137736336826:web:7e18d0d4e540c2a11dd983",
    'databaseURL': 'https://apcsa-cognito-default-rtdb.firebaseio.com/'
}

firebase = pyrebase.initialize_app(config)
auth = firebase.auth()

@app.route('/')
def index():
    return render_template('login.html')

@app.route("/login", methods=["GET", "POST"])
def login():
    if request.method == "POST":
        email = request.form['email']
        password = request.form['password']

        # Check if 'name' field is present in the form data to determine if it's a signup request
        if 'name' in request.form:
            name = request.form['name']
            try:
                user = auth.create_user_with_email_and_password(email, password)
                # session["email"] = email
                auth.send_email_verification(user['idToken'])


                ref.child(f'Users/{user["localId"]}').set(
                    {
                        'User Data': {
                            'Email': email,
                            'Name': name
                        }
                    }
                )

                # print("Welcome, you are all set!")
                # print(f"Welcome to cognito, {name} and your email is {email}!")
                # return redirect("/chat")
                signupmsg = "Welcome, please verify your email now!"
                return render_template("login.html", signupmsg=signupmsg)
            except:
                if not email or not password:
                    session['signup_msg'] = "Fill up all the information!"
                elif len(password) < 6:
                    session['signup_msg'] = "Password must be at least 6 characters!"
                else:
                    session['signup_msg'] = "Email is already registered!"
                return render_template('login.html', signup_msg=session['signup_msg'],
                                       msg="right-panel-active")
        else:  # It's a login request
            try:
                user_info = auth.sign_in_with_email_and_password(email, password)
                user = admin_auth.get_user(user_info['localId'])
                if user.email_verified:
                    # print(f"Logged in as {user['email']}")
                    # # session["email"] = email
                    # print(f"Welcome back, {email}")
                    #
                    # name = ref.child(f'Users/{user_info["localId"]}/User Data/Name').get()
                    # session['name'] = name

                    return redirect("/chat")
                else:
                    verifyemail = "Please verify your email first!"
                    return render_template("login.html", verifyemail=verifyemail)

            except:
                session['umessage'] = "Incorrect password or email!"
                return render_template('login.html', umessage=session['umessage'])

    return render_template("login.html")

@app.route('/chat')
def chatURL():
    return render_template('chat.html', name=session['name'])


@app.route('/chat', methods=['POST'])
def chat():
    data = request.json
    pdf_url = data.get('pdf_url', '')
    user_query = data.get('query', '')

    if pdf_url and user_query:
        response = getResponceUrl.getApiResponce(pdf_url, user_query)

        return jsonify({'message': response, 'user': f"Test User"})
    else:
        return jsonify({'message': 'Invalid request parameters.'}), 400


if __name__ == '__main__':
    app.run(debug=True, port=9999)