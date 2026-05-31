const BASE_URL = 'http://localhost:8080';

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
