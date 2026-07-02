const BASE_URL = 'http://localhost:8085';

async function request(method, url, body) {
    const opts = {
        method,
        headers: { 'Content-Type': 'application/json' }
    };
    if (body !== undefined) {
        opts.body = JSON.stringify(body);
    }
    const res = await fetch(BASE_URL + url, opts);
    if (!res.ok) {
        const errText = await res.text().catch(() => '');
        throw new Error(`${res.status} ${res.statusText}: ${errText}`);
    }
    const text = await res.text();
    return text ? JSON.parse(text) : null;
}

function getPlaylist() {
    return request('GET', '/playlist');
}

function addSong(song) {
    return request('POST', '/song', song);
}

function deleteSong(id) {
    return request('DELETE', `/song?id=${id}`);
}

function playNext() {
    return request('POST', '/next');
}

function playPrevious() {
    return request('POST', '/previous');
}

function shufflePlaylist() {
    return request('POST', '/shuffle');
}

function getCurrentSong() {
    return request('GET', '/current-song');
}

function playSong() {
    return request('POST', '/play');
}

function setCurrentSong(id) {
    return request('POST', '/set-current', { id });
}


function setRepeatMode(mode) {
    return request('POST', '/repeat', { mode });
}

function getHistory() {
    return request('GET', '/history');
}

function searchSong(title) {
    return request('GET', `/search?title=${encodeURIComponent(title)}`);
}

function sortPlaylist(type) {
    return request('GET', `/sort?type=${encodeURIComponent(type)}`);
}

// ─── CSV Library API ─────────────────────────────────────────
function getCsvSongs(params = {}) {
    const query = new URLSearchParams();
    if (params.title) query.set('title', params.title);
    if (params.artist) query.set('artist', params.artist);
    if (params.minDuration) query.set('minDuration', params.minDuration);
    if (params.maxDuration) query.set('maxDuration', params.maxDuration);
    if (params.page) query.set('page', params.page);
    if (params.pageSize) query.set('pageSize', params.pageSize);
    const qs = query.toString();
    return request('GET', `/csv-songs${qs ? '?' + qs : ''}`);
}

function addCsvSongsById(ids) {
    return request('POST', '/csv-songs/add', { ids });
}

function addCsvSongsFiltered(filters) {
    return request('POST', '/csv-songs/add-filtered', filters);
}
