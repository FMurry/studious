var JwtStrategy = require('passport-jwt').Strategy;
var ExtractJwt = require('passport-jwt').ExtractJwt;
var User = require('../app/models/user');
var config = require('./database');

module.exports = function(passport){
	var opts = {};
	opts.jwtFromRequest = ExtractJwt.fromAuthHeader();
	opts.secretOrKey = config.secret;
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