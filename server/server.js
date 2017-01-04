//Application will be ran from here

var express = require('express');
var app = express();
var bodyParser = require('body-parser');
var morgan = require('morgan');
var mongoose = require('mongoose');
var passport = require('passport');
var User = require('./app/models/user');
var Course = require('./app/models/course')
var Term = require('./app/models/term')

var port = process.env.PORT || 3000;
var jwt = require('jwt-simple');
require('dotenv').config(); 
var nodemailer = require('nodemailer');

// get our request parameters
app.use(bodyParser.urlencoded({ extended: false}));
app.use(bodyParser.json());

//logging
app.use(morgan('dev'));

// Use the passport package in our application
app.use(passport.initialize());
require('./config/passport').auth(passport);
require('./config/passport').google(passport);
require('./config/passport').googleToken(passport);


// demo Route (GET http://localhost:8080)
app.get('/', function(req, res) {
  res.send('Hello! The API is at http://localhost:' + port + '/api');
});

require('./config/passport');

var apiRoutes = express.Router();

apiRoutes.post('/register', function(req, res){
	//If one of the fields are empty
	if(!req.body.name || !req.body.email || !req.body.password){
		res.json({success: false, msg: 'Missing a required field'});
	}
	//Create the new user
	else{

		var newUser = new User({
			email: req.body.email,
			name: req.body.name,
			password: req.body.password
		});
		var chars = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
		var emailToken = '';
		for (var i = 16; i > 0; --i) {
			emailToken += chars[Math.round(Math.random() * (chars.length - 1))];
		}
		// create expiration date
		var expires = new Date();
		expires.setHours(expires.getHours() + 6);

		newUser.emailVerificationToken = {
			token: emailToken,
			expires: expires
		};

		//Saves the user to db save hashes the password because of pre function
		newUser.save(function(err) {
			if(err){
				//console.log(err);
				//Mongodb unique error, user already exists
				if(err.code == 11000){
					return res.json({ success: false, code: 401,msg: "User already exists"})
				}

				//Hit the validation error
				else if(err.name='ValidatorError') {
					//Validation issue with email
					if(err.errors.email){
						return res.json({ success: false, code: 402,msg: err.errors.email.message})
					}
					//Validation issue with name
					else if(err.errors.name){
						return res.json({ success: false, code: 403,msg: err.errors.name.message})
					}
					//Validation Issue with password
					else if(err.errors.password){
						return res.json({ success: false, code: 404,msg: err.errors.password.message})
					}
					//Unknown error
					return res.json({ success: false, code: 450,msg: err})
				}
				else{
					console.log(err);
					return res.json({ success: false, code: 401,msg: "Internal Server Error"})
				}
			}
			//Handles Email Sending when user signs up
			if(process.env.NODEMAILER){
				if(process.env.NODEMAILER==='true'){
					var transporter = nodemailer.createTransport({
						service: process.env.NODEMAILER_SERVICE,
						auth: {
							user: process.env.NODEMAILER_EMAIL,
							pass: process.env.NODEMAILER_PASS
						}
					});
					var link="http://"+req.get('host')+"/verify?id="+newUser._id+"&tid="+emailToken;
					mailOptions = {
						from: process.env.NODEMAILER_EMAIL, // sender address
						to: newUser.email, // list of receivers
						subject : "Welcome to Studious",
						html : "Hello and welcome to Studious,<br> Please Click on the link to verify your email.<br><a href="+link+">Click here to verify</a>" 
					};

					transporter.sendMail(mailOptions, function(error, info) {
						if(error){
							console.log(error);
						}
						else{
							console.log('Message sent: ' + info.response);
						}
					});
				}
			}
			res.json({success: true, code:200, msg: 'New user created successfully'});
		});

		

		
	}
});

apiRoutes.post('/login', function(req, res){
	User.findOne({
		email: req.body.email
	}, function(err, user){
		if(err){
			throw err;
		}

		//If no user found
		if(!user){
			res.send({success: false, code: 501, msg: 'Authentication failed. No user found'})
		}
		else{
			user.verifyPassword(req.body.password, function(err, isMatch){
				if(isMatch && !err){
					//password matched create token for user
					var token = jwt.encode(user,process.env.SECRET);
					//var token = user.generateToken();
					//console.log(token);

					res.json({success: true, code:200, 
						user: {
						_id:user._id,
						name:user.name,
						email:user.email
					}
					,token: 'JWT '+ token});
				}
				else{
					res.send({success: false, code:502, msg: 'Authentication failed. Wrong password.'});
				}
			});
		}
	});
})

apiRoutes.get('/profile', passport.authenticate('jwt', { session: false}), function(req, res){
	var token = getToken(req.headers);
	if(token){
		var decodedToken = jwt.decode(token, process.env.SECRET);
		User.findOne({
			email: decodedToken.email
		}, function(err, user){
			if(err){
				throw err;
			}

			if(!user){
				return res.status(403).send({success: false, code:501, token: '',msg: 'Authentication failed. User not found.'});
			}
			else{
				// var courses = [];
				// for(var course in user.courses){
				// 	courses.push({CourseID: course.courseID,
				// 		courseName: course.courseName,
				// 		startTime: course.startTime,
				// 		endTime: course.endTime,
				// 		room: course.room,
				// 		sunday: course.sunday,
				// 		monday: course.monday,
				// 		tuesday: course.tuesday,
				// 		wednesday: course.wednesday,
				// 		thursday: course.thursday,
				// 		friday: course.friday,
				// 		saturday: course.saturday,
				// 		color: course.color});
				// }
				res.json({success: true, code: 200, msg:'accessing profile',
					user: {
						_id:user._id,
						name:user.name,
						email:user.email,
						terms: user.terms,
						verified: user.verified,
						pro: user.pro
					}
				});
			}
		});
	}
	else{
		return res.status(403).send({success: false, msg: 'No token provided.'});
	}
});

getToken = function(headers) {
	if(headers && headers.authorization){
		var split = headers.authorization.split(' ');
		if(split.length === 2){
			return split[1];
		}
		else{
			return null;
		}
	}
	else if(headers && headers.access_token){
		return headers.access_token;
	}
	else{
		return null;
	}
}

//Add a Term
apiRoutes.post('/addTerm', passport.authenticate(['jwt', 'google-token'], { session: false}), function(req, res){
	var token = getToken(req.headers);
	if(token.includes("JWT")){
		var decodedToken = jwt.decode(token, process.env.SECRET);
		User.findOne({
			email: decodedToken.email
		}, function(err, user){
			if(err){
				throw err;
			}

			if(!user){
				return res.status(403).send({success: false, code:501, msg: 'Authentication failed. User not found.'});
			}
			else{
				var newTerm = new Term({
					name: req.body.name,
					school: req.body.school,
					startDate: req.body.startDate,
					endDate: req.body.endDate,
					type: req.body.type,
				});
				user.terms.push(newTerm);
				console.log("Password count: "+ user.password.length);
				user.save(function(err) {
					if(err){
						console.log(err);
						return res.json({ success: false, code: 401,msg: 'Term not Saved'})
					}
					res.json({success: true, code:200, msg: 'New Term saved Successfully'});
				});
			}
		});
	}
	else if(!token){
		//Google Portion
		var googleID = req.headers.googleID;
		User.findOne({
			googleID: googleID
		}, function(err, user){
			if(err){
				throw err;
			}

			if(!user){
				return res.status(403).send({success: false, code:501, msg: 'Authentication failed. User not found.'});
			}
			else{
				var newTerm = new Term({
					name: req.body.name,
					school: req.body.school,
					startDate: req.body.startDate,
					endDate: req.body.endDate,
					type: req.body.type,
				});
				user.terms.push(newTerm);
				console.log("Password count: "+ user.password.length);
				user.save(function(err) {
					if(err){
						console.log(err);
						return res.json({ success: false, code: 401,msg: 'Term not Saved'})
					}
					res.json({success: true, code:200, msg: 'New Term saved Successfully'});
				});
			}
		});
	}
	else{
		return res.status(403).send({success: false, msg: 'No token provided.'});
	}
});


//Add a course
apiRoutes.post('/addCourse', passport.authenticate('jwt', { session: false}), function(req, res){
	var token = getToken(req.headers);
	if(token){
		var decodedToken = jwt.decode(token, process.env.SECRET);
		User.findOne({
			email: decodedToken.email
		}, function(err, user){
			if(err){
				throw err;
			}

			if(!user){
				return res.status(403).send({success: false, code:501, msg: 'Authentication failed. User not found.'});
			}
			else{
				var termID = req.body.termID
				var newCourse = new Course({
					courseID: req.body.courseID,
					courseName: req.body.courseName,
					startTime: req.body.startTime,
					endTime: req.body.endTime,
					room: req.body.room,
					sunday: req.body.sunday,
					monday: req.body.monday,
					tuesday: req.body.tuesday,
					wednesday: req.body.wednesday,
					thursday: req.body.thursday,
					friday: req.body.friday,
					saturday: req.body.saturday,
					color: req.body.color
				});
				user.terms.id(termID).courses.push(newCourse);
				user.save(function(err) {
					if(err){
						return res.json({ success: false, code: 401,msg: 'Course Not Saved'})
					}
					res.json({success: true, code:200, msg: 'New course created successfully'});
				});
			}
		});
	}
	else{
		return res.status(403).send({success: false, msg: 'No token provided.'});
	}
});

//Verify the user email
app.get('/verify', function(req, res){
	console.log("Verify Reached");
	console.log(req.query.id);
	console.log(req.query.tid);
	User.findOne({
		_id: req.query.id
	}, function(err, user){
		if(err){
			throw err;
			res.send('<h1>Error has occured please request another verification email');
		}
		else if(!user){
			console.log("User not found");
			res.send("<h1>Invalid email</h1>")
			res.end();
			return;
		}
		else{
			//Verify Here
			var userEmailToken = user.emailVerificationToken.token;
			if(user.verified == true){
				//User is verified so all links are invalid
				res.end('<h1>Link no longer valid</h1>');
			}
			else if(userEmailToken === req.query.tid) {
				console.log("There is a match");
				var currentDate = new Date();
				if(currentDate < user.emailVerificationToken.expires){
					user.verified = true;
					user.save(function(err){
						if(err){
							console.log(err);
						}
						else{
							res.send("<h1>Email verified successfully");
						}

					});
				}

				
			}
		}
	})
});

//
apiRoutes.get('/', function(req, res) {
  res.send('API HOME');
});
app.get('/fail', function(req, res) {
	res.send('Oauth Auth Failed');
});

//Google Auth Stuff
apiRoutes.get(process.env.GOOGLE_AUTH_URL, passport.authenticate('google', { scope: ['profile', 'email'] }));
apiRoutes.get(process.env.GOOGLE_CALLBACK_URL,
	passport.authenticate('google', { failureRedirect: '/fail' }),
	function(req, res) {
		// Successful authentication, redirect home.
		res.json({success: true, code:200 });

});

//This logs in Users for Mobile Apps
apiRoutes.post('/auth/google/token', passport.authenticate('google-token',{scope: ['https://www.googleapis.com/auth/userinfo.profile',
                                          'https://www.googleapis.com/auth/userinfo.email'],
                                  accessType: 'offline', approvalPrompt: 'force'}),
 function(req, res) {
  res.json({success: true, code:200, message: "Google Auth successful", user:req.user});
});

apiRoutes.post('/google/profile', passport.authenticate('google-token'),
 function(req, res) {
  res.json(req.user);
});
app.use('/api', apiRoutes);

 
// Start the server
app.listen(port);
console.log('Studious Listening at http://localhost:' + port);


