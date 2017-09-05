#!/usr/bin/env node

module.exports = function(context) 
{
 var fs = require('fs');

 var srcFile = context.opts.projectRoot + '/scripts/debug-signing.properties';
 var dstFile = context.opts.projectRoot + '/platforms/android/debug-signing.properties';

 fs.createReadStream(srcFile).pipe(fs.createWriteStream(dstFile));
}
