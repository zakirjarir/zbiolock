/**
 * ZBioLock - Plugin entry point.
 *
 * Registers the plugin with Capacitor and re-exports the public API surface
 * so consumers can import everything from 'zbiolock'.
 *
 * @author Zakir Jarir
 * @license MIT
 */
import { registerPlugin } from '@capacitor/core';
const ZBioLock = registerPlugin('ZBioLock', {
    web: () => import('./web').then(m => new m.ZBioLockWeb()),
});
export { ZBioLock };
//# sourceMappingURL=index.js.map