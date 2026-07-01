let currentSong = null;
let currentPlaylist = [];

// ─── Library state ───────────────────────────────────────────
let libCurrentPage = 1;
let libTotalPages = 1;
let libSelectedIds = new Set();

document.addEventListener('DOMContentLoaded', () => {
    loadPlaylist();
    loadHistory();
    loadLibrary();
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

    // Library event listeners
    document.getElementById('btn-lib-filter').addEventListener('click', () => {
        libCurrentPage = 1;
        loadLibrary();
    });
    document.getElementById('btn-lib-reset').addEventListener('click', onLibReset);
    document.getElementById('btn-lib-add-selected').addEventListener('click', onLibAddSelected);
    document.getElementById('btn-lib-add-all-filtered').addEventListener('click', onLibAddAllFiltered);
    document.getElementById('lib-page-prev').addEventListener('click', () => {
        if (libCurrentPage > 1) { libCurrentPage--; loadLibrary(); }
    });
    document.getElementById('lib-page-next').addEventListener('click', () => {
        if (libCurrentPage < libTotalPages) { libCurrentPage++; loadLibrary(); }
    });
    document.getElementById('lib-select-all').addEventListener('change', onLibSelectAll);

    // Filter on Enter key
    document.getElementById('lib-filter-title').addEventListener('keydown', e => {
        if (e.key === 'Enter') { libCurrentPage = 1; loadLibrary(); }
    });
}

// ═══════════════════════════════════════════════════════════════
// PLAYLIST
// ═══════════════════════════════════════════════════════════════

async function loadPlaylist() {
    try {
        currentPlaylist = await getPlaylist();
        renderPlaylist(currentPlaylist);
        renderCircularList(currentPlaylist, currentSong ? currentSong.id : null);
        // Update playlist count badge
        const badge = document.getElementById('playlist-count-badge');
        if (badge) badge.textContent = `${currentPlaylist.length} songs`;
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
        tbody.innerHTML = `<tr><td colspan="5" class="empty-state">
            <div class="empty-icon">🎵</div>
            <p>Your playlist is empty</p>
            <p class="empty-hint">Browse the Song Library above and add songs to get started!</p>
        </td></tr>`;
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
        btn.addEventListener('click', () => onDelete(btn.dataset.id));
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
        renderCircularList(currentPlaylist, song ? song.id : null);
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
        renderCircularList(currentPlaylist, song ? song.id : null);
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
    const id = document.getElementById('song-id').value.trim();
    const title = document.getElementById('song-title').value.trim();
    const artist = document.getElementById('song-artist').value.trim();
    const duration = parseInt(document.getElementById('song-duration').value);

    if (!title || !artist) return;

    try {
        await addSong({ id, title, artist, duration });
        document.getElementById('add-song-form').reset();
        await loadPlaylist();
        // Refresh library to update "in playlist" status
        loadLibrary();
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
        // Refresh library to update "in playlist" status
        loadLibrary();
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
        if (song && row.cells[0] && row.cells[0].textContent === String(song.id)) {
            row.classList.add('active-song');
        }
    });
}

// ═══════════════════════════════════════════════════════════════
// SONG LIBRARY (CSV)
// ═══════════════════════════════════════════════════════════════

function getLibFilters() {
    return {
        title: document.getElementById('lib-filter-title').value.trim(),
        artist: document.getElementById('lib-filter-artist').value,
        minDuration: document.getElementById('lib-filter-min-dur').value || undefined,
        maxDuration: document.getElementById('lib-filter-max-dur').value || undefined,
        page: libCurrentPage,
        pageSize: 50,
    };
}

async function loadLibrary() {
    try {
        const filters = getLibFilters();
        const data = await getCsvSongs(filters);

        // Update badge
        const badge = document.getElementById('library-total-badge');
        if (badge) badge.textContent = `${data.total} songs`;

        // Populate artist dropdown (only on first load or reset)
        const artistSelect = document.getElementById('lib-filter-artist');
        if (artistSelect.options.length <= 1 && data.artists) {
            const currentVal = artistSelect.value;
            artistSelect.innerHTML = '<option value="">All Artists</option>';
            data.artists.forEach(a => {
                const opt = document.createElement('option');
                opt.value = a;
                opt.textContent = a;
                artistSelect.appendChild(opt);
            });
            artistSelect.value = currentVal;
        }

        // Render table
        renderLibraryTable(data.songs);

        // Update pagination
        libTotalPages = data.totalPages || 1;
        document.getElementById('lib-page-info').textContent = `Page ${data.page} / ${libTotalPages}`;
        document.getElementById('lib-page-prev').disabled = data.page <= 1;
        document.getElementById('lib-page-next').disabled = data.page >= libTotalPages;

        // Update select all checkbox
        document.getElementById('lib-select-all').checked = false;
        updateSelectedCount();
    } catch (err) {
        console.error('Failed to load library:', err);
    }
}

function renderLibraryTable(songs) {
    const tbody = document.getElementById('library-body');
    if (!songs || songs.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state">
            <div class="empty-icon">🔍</div>
            <p>No songs match your filter criteria</p>
        </td></tr>`;
        return;
    }

    tbody.innerHTML = songs.map(song => {
        const isInPlaylist = song.inPlaylist;
        const isSelected = libSelectedIds.has(song.id);
        return `<tr class="${isInPlaylist ? 'in-playlist' : ''}">
            <td><input type="checkbox" class="lib-checkbox" data-id="${song.id}" ${isInPlaylist ? 'disabled' : ''} ${isSelected ? 'checked' : ''}></td>
            <td>${song.id}</td>
            <td>${escapeHtml(song.title)}</td>
            <td>${escapeHtml(song.artist)}</td>
            <td>${formatDuration(song.duration)}</td>
            <td>${isInPlaylist ? '<span class="status-badge status-added">✓ In Playlist</span>' : '<span class="status-badge status-available">Available</span>'}</td>
            <td>${isInPlaylist ? '<button class="btn btn-sm btn-disabled" disabled>Added</button>' : `<button class="btn btn-sm btn-add-one" data-id="${song.id}">➕ Add</button>`}</td>
        </tr>`;
    }).join('');

    // Individual add buttons
    tbody.querySelectorAll('.btn-add-one').forEach(btn => {
        btn.addEventListener('click', async () => {
            const id = btn.dataset.id;
            btn.disabled = true;
            btn.textContent = '⏳';
            try {
                await addCsvSongsById([id]);
                libSelectedIds.delete(id);
                await loadPlaylist();
                await loadLibrary();
                showToast(`Added song ${id} to playlist!`);
            } catch (err) {
                console.error('Add single song failed:', err);
                btn.disabled = false;
                btn.textContent = '➕ Add';
            }
        });
    });

    // Checkbox handlers
    tbody.querySelectorAll('.lib-checkbox').forEach(cb => {
        cb.addEventListener('change', () => {
            if (cb.checked) {
                libSelectedIds.add(cb.dataset.id);
            } else {
                libSelectedIds.delete(cb.dataset.id);
            }
            updateSelectedCount();
        });
    });
}

function onLibSelectAll(e) {
    const checkboxes = document.querySelectorAll('#library-body .lib-checkbox:not(:disabled)');
    checkboxes.forEach(cb => {
        cb.checked = e.target.checked;
        if (e.target.checked) {
            libSelectedIds.add(cb.dataset.id);
        } else {
            libSelectedIds.delete(cb.dataset.id);
        }
    });
    updateSelectedCount();
}

function updateSelectedCount() {
    const btn = document.getElementById('btn-lib-add-selected');
    btn.textContent = `➕ Add Selected (${libSelectedIds.size})`;
    btn.disabled = libSelectedIds.size === 0;
}

function onLibReset() {
    document.getElementById('lib-filter-title').value = '';
    document.getElementById('lib-filter-artist').value = '';
    document.getElementById('lib-filter-min-dur').value = '';
    document.getElementById('lib-filter-max-dur').value = '';
    libCurrentPage = 1;
    libSelectedIds.clear();
    // Reset artist dropdown to re-populate
    const artistSelect = document.getElementById('lib-filter-artist');
    artistSelect.innerHTML = '<option value="">All Artists</option>';
    loadLibrary();
}

async function onLibAddSelected() {
    if (libSelectedIds.size === 0) return;
    const ids = Array.from(libSelectedIds);
    const btn = document.getElementById('btn-lib-add-selected');
    btn.disabled = true;
    btn.textContent = '⏳ Adding...';

    try {
        const result = await addCsvSongsById(ids);
        libSelectedIds.clear();
        await loadPlaylist();
        await loadLibrary();
        showToast(`Added ${result.added} songs to playlist! (Total: ${result.total})`);
    } catch (err) {
        console.error('Add selected failed:', err);
    }
    updateSelectedCount();
}

async function onLibAddAllFiltered() {
    const filters = getLibFilters();
    const btn = document.getElementById('btn-lib-add-all-filtered');
    btn.disabled = true;
    btn.textContent = '⏳ Adding...';

    try {
        const result = await addCsvSongsFiltered({
            title: filters.title || '',
            artist: filters.artist || '',
            minDuration: parseInt(filters.minDuration) || 0,
            maxDuration: parseInt(filters.maxDuration) || 0,
        });
        await loadPlaylist();
        await loadLibrary();
        showToast(`Added ${result.added} songs to playlist! (Total: ${result.total})`);
    } catch (err) {
        console.error('Add all filtered failed:', err);
    }

    btn.disabled = false;
    btn.textContent = '📥 Add All Filtered';
}

// ─── Toast notification ──────────────────────────────────────
function showToast(message) {
    const existing = document.querySelector('.toast');
    if (existing) existing.remove();

    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = message;
    document.body.appendChild(toast);

    requestAnimationFrame(() => {
        toast.classList.add('toast-show');
    });

    setTimeout(() => {
        toast.classList.remove('toast-show');
        setTimeout(() => toast.remove(), 400);
    }, 3000);
}
