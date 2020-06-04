const path = require('path');

module.exports = {
    entry: './main.js',
    output: {
        path: path.resolve(__dirname, "../app/src/main/assets/www"),
        filename: 'homework-bundled.js'
    },
    optimization:{
//        minimize: false, // <---- 禁用 uglify.
    }
};