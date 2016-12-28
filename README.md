<h1>Studious</h1>
<p>Studious is a course management application were students can take control of their classes manage assignments</p>
<h2>Installation</h2>
<p>Start of by cloning the repository</p>
<h3>Android</h3>
<ul><li>Install <a href="https://developer.android.com/studio/index.html">Android Studio</a></li>
<li>In android studio select "Open an existing android studio project" and select the android folder</li>
</ul>
<h3>iOS</h3>
<ul><li>Install Xcode through Mac App Store</li>
<li>Open the file in iOS called "Studious.xcodeproj"</li>
</ul>
<h3>Web</h3>
<ul><li>Install <a href="https://nodejs.org/en/">Node</a></li>
<li>Within the web directory, in terminal, type "npm install"</li>
<li>To start web app type in "npm start" in terminal</li></ul>
<h3>Studious API Server</h3>
<p>This server will make it easier for us to unify the data management between
multiple applications like web, iOS, and Android</p>
<p>This server will make it easier for us to unify the data management between
multiple applications like web, iOS, and Android</p>
<h4>How to install</h4>
<p>First there are a couple of things that are required to run this application
<br>We need:
<li><a href="https://nodejs.org/en/">Nodejs</a> and Npm to be installed (npm is included with nodejs)</li>
<li><a href="https://www.mongodb.com/download-center?jmp=nav">MongoDB</a> will be our database</li>
<br>Optional Tools:
<br>
<li><a href="https://www.getpostman.com/">Postman</a> - Postman allows us to easily send requests to the server and test the api</li>
<li><a href="https://robomongo.org/">Robomongo</a> - Robomongo is a mongodb database management tool that allows us to see the database through GUI</li>
<br>Ready to install:
<li>Go to the main directory of the app and run npm install in terminal</li>
<li>Start your mongodb by running mongod in terminal</li>
```javacript
MONGOLAB_URI_DEV=mongodb://localhost/Studious
NODE_ENV=
MONGOLAB_URI_PROD= 
```
<li>To start the server you can run npm start</li>
</p>

# Studious
Studious is a course management application were students can take control of their classes manage assignments