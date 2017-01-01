var JwtStrategy = require('passport-jwt').Strategy;
var GoogleStrategy = require('passport-google-oauth20').Strategy;
var GoogleTokenStrategy = require('passport-google-token').Strategy;
var ExtractJwt = require('passport-jwt').ExtractJwt;
var User = require('../app/models/user');
var passport = require('passport');
require('dotenv').config(); 


passport.serializeUser(function(user, done) {
  done(null, user);
});

passport.deserializeUser(function(user, done) {
  done(null, user);
});

var Auth = function(passport){
	var opts = {};
	opts.jwtFromRequest = ExtractJwt.fromAuthHeader();
	opts.secretOrKey = process.env.SECRET;
	passport.use(new JwtStrategy(opts, function(jwt_payload, done){
		User.findOne({id: jwt_payload.id}, function(err, user){
			//Hit error
			if(err){
				return done(err, false);
			}
			//User found
			if(user){
				done(null, user);
			}
			//User not found
			else{
				done(null, false);
			}
		});
	}));
}

//http://stackoverflow.com/questions/23878577/cant-authenticate-mobile-client-with-node-js-using-passport-js
var googleAuth = function(passport) {
	passport.use(new GoogleStrategy({
		clientID: process.env.GOOGLE_CLIENT_ID,
		clientSecret: process.env.GOOGLE_CLIENT_SECRET,
		callbackURL: '/api'+process.env.GOOGLE_CALLBACK_URL
	},
	function(accessToken, refreshToken, profile, cb) {
		console.log("Access: "+accessToken);
		console.log("Refresh:"+refreshToken);
		User.findOne({ email: profile.emails[0].value}, function(err, user){
			if(err) {
				console.log(err);
				return cb(err,false);
			}

			else if(!user) {
				console.log("Creating User");
				var newUser = new User({
					email: profile.emails[0].value,
					name: profile.displayName,
					googleID: profile.id,
					verified: true,
					imageURL: profile.photos[0].value
				});

				newUser.save(function(err){
					if(err){
						console.log(err);
						cb(null,false);
					}
					else{
						cb(null,newUser);
					}
				})
				
			}
			else{
				//No user create one here
				console.log("User already created");

				cb(null, user);
			}
		});
	} 
	));
};

var googleTokenAuth = function(passport) {
	passport.use(new GoogleTokenStrategy({
    clientID: process.env.GOOGLE_CLIENT_ID,
    clientSecret: process.env.GOOGLE_CLIENT_SECRET
  },
  function(accessToken, refreshToken, profile, done) {
    User.findOne({ googleID: profile.id }, function (err, user) {
    		if(err){
    			console.log(error);
    			return done(err, false);
    		}
    		else if(!user){
    			console.log("Creating Google User");
    			var newUser = new User({
					email: profile.emails[0].value,
					name: profile.displayName,
					googleID: profile.id,
					verified: true,
					imageURL: profile._json.picture
				});
				console.log(newUser);

				newUser.save(function(err){
					if(err){
						console.log(err);
						return done(null,false);
					}
					else{
						return done(null, newUser);
					}
				});


    		}
    		else{
    			console.log("User Found");
    			return done(null, user);
    		}
    		
    });
  }
  ));
};

module.exports = {
   'auth': Auth,
   'google': googleAuth,
   'googleToken': googleTokenAuth
};