var mongoose = require('mongoose');
var Schema = mongoose.Schema;
var Course = require('./course');
//Term model - The semester, Quarter or Trimester

var termSchema = new Schema({
	name: {
		type: String,
		required: true,
	},
	school: {
		type: String,
		required: true
	},
	startDate: {
		type: String,
	},
	endDate: {
		type: String
	},
	type: {
		type: String,
		required: true,
		default: 'Semester',
		enum: ['Semester', 'Trimester', 'Quarter']
	},
	courses: [Course.schema],
	created_at: {
		type: Date,
		default: Date.now()
	},
	updated_at: {
		type: Date,
		default: Date.now()
	}
});

termSchema.pre('save', function(next){
	var now = new Date();
	this.updated_at = now;
	if (!this.created_at) {
		this.created_at = now;
	}
	next();
});

termSchema.pre('update', function(next){
	var now = new Date();
	this.updated_at = now;
	if (!this.created_at) {
		this.created_at = now;
	}
	next();
});

module.exports = mongoose.model('Term', termSchema);
