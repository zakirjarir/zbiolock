import { registerPlugin } from '@capacitor/core';

import type { ZBioLockPlugin } from './definitions';

const ZBioLock = registerPlugin<ZBioLockPlugin>('ZBioLock', {
  web: () => import('./web').then((m) => new m.ZBioLockWeb()),
});

export * from './definitions';
export { ZBioLock };
