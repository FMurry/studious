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
	}
});


module.exports = mongoose.model('Course', courseSchema);
