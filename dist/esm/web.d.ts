import { WebPlugin } from '@capacitor/core';
import type { ZBioLockPlugin } from './definitions';
export declare class ZBioLockWeb extends WebPlugin implements ZBioLockPlugin {
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
}
