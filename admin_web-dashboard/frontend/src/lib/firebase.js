import { getApp, getApps, initializeApp } from "firebase/app";
import { getAuth, onAuthStateChanged } from "firebase/auth";
import { getDatabase } from "firebase/database";

// CleanNode's Firebase Web App configuration. The values are supplied through
// CRA environment variables so the project can be moved without changing code.
const firebaseConfig = {
  apiKey: process.env.REACT_APP_FIREBASE_API_KEY,
  authDomain: process.env.REACT_APP_FIREBASE_AUTH_DOMAIN,
  databaseURL: process.env.REACT_APP_FIREBASE_DATABASE_URL,
  projectId: process.env.REACT_APP_FIREBASE_PROJECT_ID,
  storageBucket: process.env.REACT_APP_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: process.env.REACT_APP_FIREBASE_MESSAGING_SENDER_ID,
  appId: process.env.REACT_APP_FIREBASE_APP_ID,
};

const required = [
  "REACT_APP_FIREBASE_API_KEY",
  "REACT_APP_FIREBASE_AUTH_DOMAIN",
  "REACT_APP_FIREBASE_PROJECT_ID",
  "REACT_APP_FIREBASE_DATABASE_URL",
  "REACT_APP_FIREBASE_APP_ID",
];

const missing = required.filter((key) => !process.env[key]);
if (missing.length) {
  console.error(`Missing Firebase environment variable(s): ${missing.join(", ")}`);
}

// initializeApp is idempotent across hot reloads in CRA development.
export const app = getApps().length ? getApp() : initializeApp(firebaseConfig);
export const firebaseAuth = getAuth(app);
export const database = getDatabase(app);

// Keep this export aligned with AuthContext so there is only one auth object.
export { onAuthStateChanged };
