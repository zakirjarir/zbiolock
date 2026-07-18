import { WebPlugin } from '@capacitor/core';

import type { ZBioLockPlugin } from './definitions';

export class ZBioLockWeb extends WebPlugin implements ZBioLockPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
