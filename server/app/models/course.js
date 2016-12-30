var mongoose = require('mongoose');
var Schema = mongoose.Schema;

//Course Model
var courseSchema = new Schema({
	courseID: {
		type: String,
	},
	courseName: {
		type: String,
		required: true
	},
	startTime: {
		type: String
	},
	endTime: {
		type: String
	},
	room: {
		type: String,
	},
	type: {
		type: String,
		required: true,
		default: 'Lecture',
		enum: ['Lecture', 'Seminar', 'Lab']
	},
	sunday: {
		type: Boolean,
		required: true,
		default: false
	},
	monday: {
		type: Boolean,
		required: true,
		default: false
	},
	tuesday: {
		type: Boolean,
		required: true,
		default: false
	},
	wednesday: {
		type: Boolean,
		required: true,
		default: false
	},
	thursday: {
		type: Boolean,
		required: true,
		default: false
	},
	friday: {
		type: Boolean,
		required: true,
		default: false
	},
	saturday: {
		type: Boolean,
		required: true,
		default: false
	},
	color: {
		type: String,
		required: true,
		default: "#ff2e7d32"
	},
	isOnline: {
		type: Boolean,
		required: true,
		default: false
	},
	created_at: {
		type: Date,
		default: Date.now()
	},
	updated_at: {
		type: Date,
		default: Date.now()
	}
});

courseSchema.pre('save', function(next){
	var now = new Date();
	this.updated_at = now;
	if (!this.created_at) {
		this.created_at = now;
	}
	next();
});

courseSchema.pre('update', function(next){
	var now = new Date();
	this.updated_at = now;
	if (!this.created_at) {
		this.created_at = now;
	}
	next();
});
module.exports = mongoose.model('Course', courseSchema);
