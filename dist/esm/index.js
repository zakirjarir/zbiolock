import { registerPlugin } from '@capacitor/core';
const ZBioLock = registerPlugin('ZBioLock', {
    web: () => import('./web').then((m) => new m.ZBioLockWeb()),
});
export * from './definitions';
export { ZBioLock };
//# sourceMappingURL=index.js.map