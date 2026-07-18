# ZBioLock — Publishing & Release Guide

## Pre-publish Checklist

- [ ] Bump `version` in `package.json`
- [ ] Update `CHANGELOG.md` (if present)
- [ ] Ensure all tests pass locally
- [ ] Verify `npm run build` completes without errors
- [ ] Verify `npm run lint` passes

## Local Build Verification

```bash
# 1. Install dependencies
npm install

# 2. Build TypeScript + Rollup bundle
npm run build

# 3. Lint + format check
npm run lint

# 4. Android build (requires Android SDK)
cd android && ./gradlew clean build test && cd ..

# 5. iOS build (requires macOS + Xcode)
xcodebuild test \
  -scheme ZBioLockPlugin \
  -destination 'platform=iOS Simulator,name=iPhone 16,OS=latest' \
  CODE_SIGNING_ALLOWED=NO
```

## NPM Publishing

```bash
# Login to npm (one-time)
npm login

# Publish (prepublishOnly script runs the build automatically)
npm publish --access public
```

## GitHub Release

```bash
git tag -a v0.0.1 -m "Release v0.0.1"
git push origin v0.0.1
```

Then create a GitHub Release from the tag with the changelog notes.

## Capacitor Plugin Registry

To list your plugin on the [Capacitor Plugin Registry](https://capacitorjs.com/docs/plugins), ensure:
- `package.json` contains the `capacitor` key with `ios.src` and `android.src`
- Plugin passes lint and type checks
- README documents the full API

## Security Disclosure

If you find a security vulnerability, please report it via [GitHub Security Advisories](https://github.com/zakirjarir/zbiolock/security/advisories) — **do not open a public issue**.
