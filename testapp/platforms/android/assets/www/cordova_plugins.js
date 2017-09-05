cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
    {
        "id": "cw-swcore-ceruleanplugins.CeruleanPlugins",
        "file": "plugins/cw-swcore-ceruleanplugins/www/CeruleanPlugins.js",
        "pluginId": "cw-swcore-ceruleanplugins",
        "clobbers": [
            "cordova.plugins.CeruleanPlugins"
        ]
    }
];
module.exports.metadata = 
// TOP OF METADATA
{
    "cordova-plugin-whitelist": "1.3.2",
    "cw-swcore-ceruleanplugins": "0.1.2"
};
// BOTTOM OF METADATA
});