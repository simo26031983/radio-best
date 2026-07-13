/**
 * Queries the radio-browser.info community API for all stations of a given
 * country. The API is mirrored across several independent servers with no
 * single stable hostname, so we try a short list of known mirrors in order
 * and use the first one that responds.
 */

const MIRRORS = ["de1.api.radio-browser.info", "de2.api.radio-browser.info", "nl1.api.radio-browser.info"];

const USER_AGENT = "best-radio-curation/1.0 (+https://github.com/simo26031983/radio-best)";
const REQUEST_TIMEOUT_MS = 15_000;

export interface RawStation {
  stationuuid: string;
  name: string;
  countrycode: string;
  url: string;
  url_resolved: string;
  favicon: string;
  tags: string;
  votes: number;
  clickcount: number;
  bitrate: number;
  codec: string;
  lastcheckok: number;
}

export async function fetchStationsByCountry(countryCode: string): Promise<RawStation[]> {
  let lastError: unknown;
  for (const mirror of MIRRORS) {
    try {
      // radio-browser.info defaults to a 1000-result page; pass an explicit
      // high limit since France alone has 2000+ registered stations.
      const url = `https://${mirror}/json/stations/bycountrycodeexact/${countryCode}?hidebroken=true&limit=100000`;
      const response = await fetch(url, {
        headers: { "User-Agent": USER_AGENT },
        signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS),
      });
      if (!response.ok) {
        throw new Error(`${mirror} responded with HTTP ${response.status}`);
      }
      return (await response.json()) as RawStation[];
    } catch (error) {
      lastError = error;
      console.warn(`[fetchStations] mirror ${mirror} failed for ${countryCode}: ${(error as Error).message}`);
    }
  }
  throw new Error(`All radio-browser.info mirrors failed for country ${countryCode}: ${lastError}`);
}
