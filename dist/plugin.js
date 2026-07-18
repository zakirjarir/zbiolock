var capacitorZBioLock = (function (exports, core) {
    'use strict';

    const ZBioLock = core.registerPlugin('ZBioLock', {
        web: () => Promise.resolve().then(function () { return web; }).then((m) => new m.ZBioLockWeb()),
    });

    class ZBioLockWeb extends core.WebPlugin {
        async echo(options) {
            console.log('ECHO', options);
            return options;
        }
    }

    var web = /*#__PURE__*/Object.freeze({
        __proto__: null,
        ZBioLockWeb: ZBioLockWeb
    });

    exports.ZBioLock = ZBioLock;

    return exports;

})({}, capacitorExports);
//# sourceMappingURL=plugin.js.map
