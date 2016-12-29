//Application will be ran from here

var express = require('express');
var app = express();
var bodyParser = require('body-parser');
var morgan = require('morgan');
var mongoose = require('mongoose');
var passport = require('passport');
var User = require('./app/models/user');
var Course = require('./app/models/course')
var port = process.env.PORT || 3000;
var jwt = require('jwt-simple');
require('dotenv').config(); 

// get our request parameters
app.use(bodyParser.urlencoded({ extended: false}));
app.use(bodyParser.json());

//logging
app.use(morgan('dev'));

// Use the passport package in our application
app.use(passport.initialize());
require('./config/passport')(passport);

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

		//Saves the user to db save hashes the password because of pre function
		newUser.save(function(err) {
			if(err){
				//console.log(err);
				if(err.code == 11000){
					return res.json({ success: false, code: 401,msg: "User already exists"})
				}

				else if(err.name='ValidatorError') {
					if(err.errors.email){
						return res.json({ success: false, code: 402,msg: err.errors.email.message})
					}
					else if(err.errors.name){
						return res.json({ success: false, code: 403,msg: err.errors.name.message})
					}
					else if(err.errors.password){
						return res.json({ success: false, code: 404,msg: err.errors.password.message})
					}
					return res.json({ success: false, code: 450,msg: err})
				}
				else{
					console.log(err);
					if(err.message === 'User validation failed'){
						return res.json({ success: false, code: 401,msg: "Not A valid Email"})
					}
					else{
						return res.json({ success: false, code: 401,msg: err})
					}
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

					res.json({success: true, code:200, token: 'JWT '+ token});
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
				return res.status(403).send({success: false, code:501, msg: 'Authentication failed. User not found.'});
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
						courses: user.courses
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
	else{
		return null;
	}
}

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
				user.courses.push(newCourse);
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
app.use('/api', apiRoutes);
 
// Start the server
app.listen(port);
console.log('Studious Listening at http://localhost:' + port);