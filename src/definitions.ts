export interface ZBioLockPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
