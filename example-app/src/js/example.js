/**
 * ZBioLock Demo App — example.js
 *
 * Exercises every plugin API method and renders results in the UI.
 * This file is intentionally verbose for demonstration purposes.
 */

import { ZBioLock } from 'zbiolock';

// ─────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────

function log(msg, type = 'info') {
  const el = document.getElementById('log');
  const entry = document.createElement('div');
  entry.className = `entry ${type}`;
  const time = new Date().toLocaleTimeString();
  entry.textContent = `[${time}] ${msg}`;
  el.prepend(entry);
}

function chip(text, ok) {
  return `<span class="result-chip ${ok ? 'ok' : 'err'}">${ok ? '✔' : '✖'} ${text}</span>`;
}

function setBadge(id, labelId, text, state) {
  const badge = document.getElementById(id);
  badge.className = `badge ${state}`;
  document.getElementById(labelId).textContent = text;
}

// ─────────────────────────────────────────────
// On load — auto check availability
// ─────────────────────────────────────────────

window.addEventListener('load', () => {
  checkAvailability();
});

// ─────────────────────────────────────────────
// Availability
// ─────────────────────────────────────────────

window.checkAvailability = async () => {
  try {
    log('Calling isAvailable()…');
    const result = await ZBioLock.isAvailable();
    log(`isAvailable → ${JSON.stringify(result)}`, 'ok');

    const el = document.getElementById('result-availability');
    el.innerHTML = chip(`Available: ${result.isAvailable}  |  Type: ${result.biometricType}`, result.isAvailable);

    setBadge(
      'badge-available', 'label-available',
      result.isAvailable ? 'Available' : 'Not Available',
      result.isAvailable ? 'ok' : 'warn'
    );
    setBadge('badge-type', 'label-type', result.biometricType, result.biometricType !== 'none' ? 'ok' : 'warn');
  } catch (e) {
    log(`isAvailable error: ${e.message || e}`, 'err');
    setBadge('badge-available', 'label-available', 'Error', 'error');
  }
};

window.checkEnrolled = async () => {
  try {
    log('Calling isBiometricEnrolled()…');
    const result = await ZBioLock.isBiometricEnrolled();
    log(`isBiometricEnrolled → ${JSON.stringify(result)}`, 'ok');
    document.getElementById('result-availability').innerHTML = chip(
      `Biometric enrolled: ${result.enrolled}`, result.enrolled
    );
    setBadge('badge-enrolled', 'label-enrolled', result.enrolled ? 'Enrolled' : 'Not Enrolled', result.enrolled ? 'ok' : 'warn');
  } catch (e) {
    log(`isBiometricEnrolled error: ${e.message || e}`, 'err');
  }
};

window.checkType = async () => {
  try {
    log('Calling getBiometricType()…');
    const result = await ZBioLock.getBiometricType();
    log(`getBiometricType → ${JSON.stringify(result)}`, 'ok');
    document.getElementById('result-availability').innerHTML = chip(`Type: ${result.biometricType}`, true);
    setBadge('badge-type', 'label-type', result.biometricType, result.biometricType !== 'none' ? 'ok' : 'warn');
  } catch (e) {
    log(`getBiometricType error: ${e.message || e}`, 'err');
  }
};

// ─────────────────────────────────────────────
// Authentication
// ─────────────────────────────────────────────

window.doAuthenticate = async (allowDeviceCredential) => {
  const title       = document.getElementById('auth-title').value || 'Verify Identity';
  const description = document.getElementById('auth-desc').value  || undefined;

  try {
    log(`Calling authenticate({ title: "${title}", allowDeviceCredential: ${allowDeviceCredential} })…`);
    const result = await ZBioLock.authenticate({
      title,
      description,
      allowDeviceCredential,
    });
    log(`authenticate → ${JSON.stringify(result)}`, 'ok');
    document.getElementById('result-auth').innerHTML = chip(
      `Authenticated via ${result.biometricType}`, result.success
    );
  } catch (e) {
    const code = e.code || 'UNKNOWN';
    log(`authenticate error [${code}]: ${e.message || e}`, 'err');
    document.getElementById('result-auth').innerHTML = chip(`${code}: ${e.message || e}`, false);
  }
};

// ─────────────────────────────────────────────
// Token Storage
// ─────────────────────────────────────────────

window.doSaveToken = async () => {
  const key   = document.getElementById('save-key').value;
  const token = document.getElementById('save-token').value;

  if (!key || !token) {
    log('Save aborted: key and token are required.', 'err');
    return;
  }

  try {
    log(`Calling saveToken({ key: "${key}", token: "***" })…`);
    await ZBioLock.saveToken({ key, token });
    log(`saveToken → success`, 'ok');
    document.getElementById('result-save').innerHTML = chip(`Token saved under key "${key}"`, true);
  } catch (e) {
    const code = e.code || 'UNKNOWN';
    log(`saveToken error [${code}]: ${e.message || e}`, 'err');
    document.getElementById('result-save').innerHTML = chip(`${code}: ${e.message || e}`, false);
  }
};

window.doGetToken = async () => {
  const key = document.getElementById('get-key').value;

  if (!key) {
    log('Get aborted: key is required.', 'err');
    return;
  }

  try {
    log(`Calling getToken({ key: "${key}" })…`);
    const result = await ZBioLock.getToken({ key });
    const found  = result.token !== null;
    log(`getToken → ${found ? `"${result.token.substring(0, 20)}…"` : 'null (not found)'}`, found ? 'ok' : 'err');
    document.getElementById('result-get').innerHTML = chip(
      found ? `Token found (${result.token.length} chars)` : 'Token not found', found
    );
  } catch (e) {
    const code = e.code || 'UNKNOWN';
    log(`getToken error [${code}]: ${e.message || e}`, 'err');
    document.getElementById('result-get').innerHTML = chip(`${code}: ${e.message || e}`, false);
  }
};

window.doDeleteToken = async () => {
  const key = document.getElementById('get-key').value;

  if (!key) {
    log('Delete aborted: key is required.', 'err');
    return;
  }

  try {
    log(`Calling deleteToken({ key: "${key}" })…`);
    await ZBioLock.deleteToken({ key });
    log(`deleteToken → success`, 'ok');
    document.getElementById('result-get').innerHTML = chip(`Token "${key}" deleted`, true);
  } catch (e) {
    const code = e.code || 'UNKNOWN';
    log(`deleteToken error [${code}]: ${e.message || e}`, 'err');
    document.getElementById('result-get').innerHTML = chip(`${code}: ${e.message || e}`, false);
  }
};

window.doClear = async () => {
  try {
    log('Calling clear()…');
    await ZBioLock.clear();
    log('clear → all tokens wiped', 'ok');
    document.getElementById('result-get').innerHTML = chip('All tokens cleared', true);
  } catch (e) {
    const code = e.code || 'UNKNOWN';
    log(`clear error [${code}]: ${e.message || e}`, 'err');
    document.getElementById('result-get').innerHTML = chip(`${code}: ${e.message || e}`, false);
  }
};
