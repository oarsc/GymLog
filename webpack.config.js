const path = require('path');
const fs = require('fs');

const filesConfiguration = [
// POLYFILLS
{
	in: [
		'core-js',
		'./private/js/custom-front-js-mods/dom-modifications.js',
		'regenerator-runtime/runtime',
	],
	out: "polyfill.min.js"
},

...fs.readdirSync('private/js', { withFileTypes:true })
	.filter(element => element.isFile())
	.map(file => file.name.replace(/\.js$/, ''))
	.map(file => ({
		in: `./private/js/${file}.js`,
		out: `${file}.min.js`
	}))
];


module.exports = filesConfiguration.map(entry => (
	{
		entry: entry.in,
		output: {
			path: path.join(__dirname, 'public/js'),
			publicPath: '/',
			filename: entry.out
		},
		module: {
			rules: [
				{
					test: /\.js$/,
					exclude: /node_modules/,
					use: {
						loader: 'babel-loader',
						options: {
							presets: [
								['@babel/preset-env', {
									//"corejs": 3,
									//"useBuiltIns": "usage"
								}]
							]
						}
					}
				}
			]
		}
	}
));