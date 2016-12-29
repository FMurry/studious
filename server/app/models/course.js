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
		required: true
	},
	monday: {
		type: Boolean,
		required: true
	},
	tuesday: {
		type: Boolean,
		required: true
	},
	wednesday: {
		type: Boolean,
		required: true
	},
	thursday: {
		type: Boolean,
		required: true
	},
	friday: {
		type: Boolean,
		required: true
	},
	saturday: {
		type: Boolean,
		required: true
	},
	color: {
		type: String,
		required: true,
		default: "#ff2e7d32"
	}
});


module.exports = mongoose.model('Course', courseSchema);
