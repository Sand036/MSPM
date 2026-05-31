let currentSong = null;
let currentPlaylist = [];

document.addEventListener('DOMContentLoaded', () => {
    loadPlaylist();
    loadHistory();
    setupEventListeners();
});

function setupEventListeners() {
    document.getElementById('btn-next').addEventListener('click', onNext);
    document.getElementById('btn-prev').addEventListener('click', onPrevious);
    document.getElementById('btn-shuffle').addEventListener('click', onShuffle);
    document.getElementById('repeat-mode').addEventListener('change', onRepeatChange);
    document.getElementById('btn-search').addEventListener('click', onSearch);
    document.getElementById('search-input').addEventListener('keydown', e => {
        if (e.key === 'Enter') onSearch();
    });
    document.getElementById('add-song-form').addEventListener('submit', onAddSong);
    document.getElementById('btn-sort').addEventListener('click', onSort);
}

async function loadPlaylist() {
    try {
        currentPlaylist = await getPlaylist();
        renderPlaylist(currentPlaylist);
        renderCircularList(currentPlaylist, currentSong ? currentSong.id : null);
    } catch (err) {
        console.error('Failed to load playlist:', err);
    }
}

async function loadHistory() {
    try {
        const history = await getHistory();
        renderHistory(history);
        renderHistoryStack(history);
    } catch (err) {
        console.error('Failed to load history:', err);
    }
}

function renderPlaylist(songs) {
    const tbody = document.getElementById('playlist-body');
    if (!songs || songs.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:#666;padding:20px;">No songs in playlist</td></tr>';
        return;
    }

    tbody.innerHTML = songs.map(song => {
        const active = currentSong && song.id === currentSong.id ? 'class="active-song"' : '';
        return `<tr ${active}>
            <td>${song.id}</td>
            <td>${escapeHtml(song.title)}</td>
            <td>${escapeHtml(song.artist)}</td>
            <td>${formatDuration(song.duration)}</td>
            <td><button class="btn btn-danger" data-id="${song.id}">Delete</button></td>
        </tr>`;
    }).join('');

    tbody.querySelectorAll('.btn-danger').forEach(btn => {
        btn.addEventListener('click', () => onDelete(parseInt(btn.dataset.id)));
    });
}

function renderCurrentSong(song) {
    const info = document.getElementById('current-song-info');
    currentSong = song;

    if (!song) {
        info.innerHTML = '<p class="no-song">No song playing</p>';
        return;
    }

    info.innerHTML = `
        <span class="song-tag">ID <span>${song.id}</span></span>
        <span class="song-tag">Title <span>${escapeHtml(song.title)}</span></span>
        <span class="song-tag">Artist <span>${escapeHtml(song.artist)}</span></span>
        <span class="song-tag">Duration <span>${formatDuration(song.duration)}</span></span>
    `;
}

function renderHistory(history) {
    const list = document.getElementById('history-stack');
    if (!history || history.length === 0) {
        list.innerHTML = '<li style="color:#666;border-left-color:#555;">No recently played songs</li>';
        return;
    }

    list.innerHTML = history.map((song, i) => {
        const label = i === 0 ? 'Top' : '';
        return `<li>${label ? `<span style="color:#1DB954;font-weight:600;">Top</span>` : `<span>${escapeHtml(song.title)}</span>`} <span style="color:#888;">${escapeHtml(song.artist)}</span></li>`;
    }).join('');
}

function formatDuration(seconds) {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
}

function escapeHtml(text) {
    if (typeof text !== 'string') return String(text);
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

async function onNext() {
    try {
        const song = await playNext();
        renderCurrentSong(song);
        highlightActiveSong(song);
        renderCircularList(currentPlaylist, song.id);
        loadHistory();
    } catch (err) {
        console.error('Next failed:', err);
    }
}

async function onPrevious() {
    try {
        const song = await playPrevious();
        renderCurrentSong(song);
        highlightActiveSong(song);
        renderCircularList(currentPlaylist, song.id);
        loadHistory();
    } catch (err) {
        console.error('Previous failed:', err);
    }
}

async function onShuffle() {
    const shuffleEl = document.getElementById('shuffle-animation');
    showShuffleAnimation(async () => {
        try {
            const songs = await shufflePlaylist();
            currentPlaylist = songs;
            renderPlaylist(currentPlaylist);
            if (currentSong) {
                highlightActiveSong(currentSong);
                renderCircularList(currentPlaylist, currentSong.id);
            } else {
                renderCircularList(currentPlaylist, null);
            }
        } catch (err) {
            console.error('Shuffle failed:', err);
        }
    });
}

async function onRepeatChange() {
    const mode = document.getElementById('repeat-mode').value;
    try {
        await setRepeatMode(mode);
    } catch (err) {
        console.error('Repeat mode change failed:', err);
    }
}

async function onSearch() {
    const title = document.getElementById('search-input').value.trim();
    if (!title) return;

    try {
        const results = await searchSong(title);
        const container = document.getElementById('search-results');
        if (!results || results.length === 0) {
            container.innerHTML = '<p style="color:#888;">No results found</p>';
            return;
        }
        container.innerHTML = results.map(song =>
            `<div class="search-result-item">
                <span class="title">${escapeHtml(song.title)}</span>
                <span style="color:#888;">${escapeHtml(song.artist)} — ${formatDuration(song.duration)}</span>
            </div>`
        ).join('');
    } catch (err) {
        console.error('Search failed:', err);
    }
}

async function onAddSong(e) {
    e.preventDefault();
    const id = parseInt(document.getElementById('song-id').value);
    const title = document.getElementById('song-title').value.trim();
    const artist = document.getElementById('song-artist').value.trim();
    const duration = parseInt(document.getElementById('song-duration').value);

    if (!title || !artist) return;

    try {
        await addSong({ id, title, artist, duration });
        document.getElementById('add-song-form').reset();
        await loadPlaylist();
    } catch (err) {
        console.error('Add song failed:', err);
    }
}

async function onDelete(id) {
    if (!confirm('Are you sure you want to delete this song?')) return;

    try {
        await deleteSong(id);
        await loadPlaylist();
        if (currentSong && currentSong.id === id) {
            renderCurrentSong(null);
        }
    } catch (err) {
        console.error('Delete song failed:', err);
    }
}

async function onSort() {
    const type = document.getElementById('sort-type').value;
    try {
        const songs = await sortPlaylist(type);
        currentPlaylist = songs;
        renderPlaylist(currentPlaylist);
        if (currentSong) {
            highlightActiveSong(currentSong);
            renderCircularList(currentPlaylist, currentSong.id);
        } else {
            renderCircularList(currentPlaylist, null);
        }
    } catch (err) {
        console.error('Sort failed:', err);
    }
}

function highlightActiveSong(song) {
    const rows = document.querySelectorAll('#playlist-body tr');
    rows.forEach(row => {
        row.classList.remove('active-song');
        if (row.cells[0] && parseInt(row.cells[0].textContent) === song.id) {
            row.classList.add('active-song');
        }
    });
}
