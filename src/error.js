module.exports = {
	send: function(message, status, funct) {
		funct(this.get(message,status));
	},

	handle: function(error, status, rejectFunction, resolve) {
		if (error)
			this.send(error, status, rejectFunction);
		else
			resolve();
	},

	throw: function(message, status) {
		throw this.get(message,status);
	},

	get: function(message, status) {
		let error = message;
		if (typeof error == 'string')
			error = new Error(message);
		error.status = status;
		return error;
	},
}