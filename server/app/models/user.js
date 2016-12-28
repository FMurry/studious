var mongoose = require('mongoose');
var Schema = mongoose.Schema;
var bcrypt = require('bcrypt');
var jwt = require('jsonwebtoken');
var config = require('../../config/database');


//We will set up User Schema
var userSchema = new Schema({
	name: {
		type: String,
		required: true
	},
	email: {
		type: String,
		unique: true,
		required: true
	},
	password: {
        type: String,
        required: true
    }
});

//Operation to be done before save
userSchema.pre('save', function(next){
	var user = this;
	if(this.isModified('password') || this.isNew) {
		bcrypt.genSalt(10, function(err, salt){
			if(err){
				return next(err);
			}
			bcrypt.hash(user.password, salt, function(err, hash){
				if(err){
					return next(err);
				}
				else{
					user.password = hash;
					next();
				}
			});
		});
	}
	else {
		return next();
	}
});

userSchema.methods.verifyPassword = function(pass, cb) {
	bcrypt.compare(pass, this.password, function(err, isMatch){
		if(err){
			return cb(err);
		}
		cb(null, isMatch);
	});
};


module.exports = mongoose.model('User', userSchema);


