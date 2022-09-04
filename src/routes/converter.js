const express = require('express');
const router = express.Router();
const fs = require('fs');
const path = require('path');
const error = require('../error');

/* GET converter. */
router.get('/', function(req, res, next) {
	fs.readFile(path.join(__dirname, '../../bak/output.json'), 'utf8', (err, content) => {
		error.handle(err, 404, next, () => {
			res.render('converter', { content: content });
		});
	});
});

module.exports = router;
