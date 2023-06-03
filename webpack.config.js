const path = require('path');
const fs = require('fs');

const jsFilesConfiguration = [
  // POLYFILLS
  {
    in: [
      'core-js',
      './private/js/_dom/dom-modifications.js',
      'regenerator-runtime/runtime',
    ],
    out: "polyfill.min.js"
  },

  ...fs.readdirSync('private/js', { withFileTypes:true })
    .filter(element => element.isFile())
    .filter(file => file.name.endsWith('.js'))
    .map(file => file.name.replace(/\.js$/, ''))
    .map(file => ({
      in: `./private/js/${file}.js`,
      out: `${file}.min.js`
  }))

];


const tsFilesConfiguration = [
  ...fs.readdirSync('private/js', { withFileTypes:true })
    .filter(element => element.isFile())
    .filter(file => file.name.endsWith('.ts') || file.name.endsWith('.tsx'))
    .map(file => file.name)
    .map(file => ({
      in: `./private/js/${file}`,
      out: `${file.replace(/\.tsx?$/, '')}.min.js`
    }))
];

module.exports = jsFilesConfiguration.map(entry => ({
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
            presets: ['@babel/preset-env']
          }
        }
      }
    ]
  }
}))
.concat(
  tsFilesConfiguration.map(entry => ({
    entry: entry.in,
    output: {
      path: path.join(__dirname, 'public/js'),
      publicPath: '/',
      filename: entry.out,
    },
    module: {
      rules: [
        {
          test: /\.[tj]sx?$/,
          exclude: /node_modules/,
          use: [
            {
              loader: 'babel-loader',
              options: {
                presets: ['@babel/preset-env', '@babel/react']
              }
            },
            {
              loader: 'ts-loader',
              options: {
                configFile: 'tsconfig.webpack.json'
              }
            }
          ]
        },
        {
          test: /\.scss$/,
          exclude: /node_modules/,
          use: [ 'style-loader', 'css-loader', 'sass-loader' ]
        }
      ],
    },
    resolve: {
      extensions: ['.tsx', '.ts', '.js'],
    },
  }))
);