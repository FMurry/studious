var mongoose = require('mongoose');
var Course = require('./course');
//var Assignment = require('./assignment');
var Schema = mongoose.Schema;
var bcrypt = require('bcrypt');
var jwt = require('jsonwebtoken');
var config = require('../../config/database');

//We will set up User Schema
//Has many Courses

var validateEmail = function(email) {
    var regex = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/;
    return regex.test(email)
};

var validateName = function(name) {
	return (name.length >= 3 && name.length <= 48);
};

var validatePassword = function(password) {
	return (password.length >= 8 && password.length <=24);
}

var userSchema = new Schema({
	name: {
		type: String,
		required: [true, 'email required'],
		validate: [validateName, "Please Enter name between 3 and 48 characters"]
	},
	email: {
		type: String,
		unique: true,
		required: true,
		trim: true,
        lowercase: true,
        validate: [validateEmail, 'Please fill a valid email address'],
        match: [/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/, 'Please fill a valid email address']
	},
	password: {
        type: String,
        required: true,
        validate: [validatePassword, 'Please Enter A Password between 8 and 24 characters']
    },
    verified: {
    	type: Boolean,
    	required: true,
    	default: false
    },
    courses: [Course.schema],
    emailVerificationToken: {
    	token: {
    		type: String
    	},
    	expires: {
    		type: Date
    	}
    }
    //Here add assignments: [Assignment.schema]
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


