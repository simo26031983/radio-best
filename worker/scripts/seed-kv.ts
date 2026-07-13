/**
 * Seeds the STATIONS_KV Cloudflare KV namespace from the curated station
 * files produced by tools/curate-stations. Run manually after each curation
 * refresh (npm run seed --workspace=worker), requires `wrangler login` to
 * have been run once beforehand.
 *
 * Usage: npm run seed              (seeds the remote/production namespace)
 *        npm run seed -- --local   (seeds wrangler's local dev KV store)
 */
import { execFileSync } from "node:child_process";
import { readFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const CURATED_DIR = path.join(__dirname, "..", "..", "tools", "curate-stations", "output");
const COUNTRIES = ["fr", "ma"] as const;

const isLocal = process.argv.includes("--local");

for (const country of COUNTRIES) {
  const filePath = path.join(CURATED_DIR, `stations-${country}.json`);
  const content = readFileSync(filePath, "utf-8");
  const key = `stations:${country.toUpperCase()}`;

  console.log(`Seeding ${key} from ${filePath} (${content.length} bytes)${isLocal ? " [local]" : ""}...`);
  execFileSync(
    "npx",
    [
      "wrangler",
      "kv",
      "key",
      "put",
      key,
      "--path",
      filePath,
      "--binding",
      "STATIONS_KV",
      ...(isLocal ? ["--local"] : ["--remote"]),
    ],
    { stdio: "inherit", cwd: path.join(__dirname, ".."), shell: true }
  );
}

console.log("Done.");
