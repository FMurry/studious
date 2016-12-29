# Studious
Studious is a course management application were students can take control of their classes manage assignments

## Installation
Start of by cloning the repository

### Android

Install [Android Studio](https://developer.android.com/studio/index.html)
In android studio select "Open an existing android studio project" and select the android folder

### iOS

Install Xcode through Mac App Store
Open the file in iOS called "Studious.xcodeproj"

### Web
Install [Node](https://nodejs.org/en/)
Within the web directory, in terminal, type: 
```javascript
npm install
```
To start web app type in
```javascript
npm start
```
### Studious API Server
Install [Node](https://nodejs.org/en/)
This server will make it easier for us to unify the data management between
multiple applications like web, iOS, and Android

#### How to install
First there are a couple of things that are required to run this application
We need:
[Nodejs](https://nodejs.org/en/) and Npm to be installed (npm is included with nodejs)

[MongoDB](https://www.mongodb.com/download-center?jmp=nav) will be our database

Optional Tools:

[Postman](https://www.getpostman.com/) - Postman allows us to easily send requests to the server and test the api

[Robomongo](https://robomongo.org/) - Robomongo is a mongodb database management tool that allows us to see the database through GUI

Ready to install:
Go to the main directory of the app and run 
```javacript
npm install
```
Start your mongodb by running mongod in terminal

Example .env file (Inside server folder)

```javascript
APPNAME=Studious
MONGOLAB_URI_DEV=mongodb://localhost/Studious
NODE_ENV=dev
MONGOLAB_URI_PROD=
SECRET="A Secret password Here"
NODEMAILER="True if you want to send email when registering, false otherwise"
NODEMAILER_SERVICE=Gmail
NODEMAILER_EMAIL="Your email here"
NODEMAILER_PASS="Password to email here"
```
To start the server you can run
```javascript
npm start
```