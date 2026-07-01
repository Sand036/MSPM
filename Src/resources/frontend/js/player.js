let currentSong = null;
let currentPlaylist = [];
let isPlaying = false;

// ─── Library state ───────────────────────────────────────────
let libCurrentPage = 1;
let libTotalPages = 1;
let libSelectedIds = new Set();

document.addEventListener('DOMContentLoaded', async () => {
    await loadPlaylist();
    await loadCurrentSong();
    loadHistory();
    loadLibrary();
    loadContentRails();
    setupEventListeners();
});

function setupEventListeners() {
    document.getElementById('btn-play').addEventListener('click', onPlayPause);
    document.getElementById('btn-next').addEventListener('click', onNext);
    document.getElementById('btn-prev').addEventListener('click', onPrevious);
    document.getElementById('btn-shuffle').addEventListener('click', onShuffle);
    document.getElementById('repeat-mode').addEventListener('change', onRepeatChange);

    // Search input: show dropdown on focus, filter on keyup
    const searchInput = document.getElementById('search-input');
    searchInput.addEventListener('focus', onSearchFocus);
    searchInput.addEventListener('keyup', onSearchType);

    document.addEventListener('click', e => {
        const dd = document.getElementById('search-dropdown');
        if (!e.target.closest('.search-bar')) dd.classList.remove('search-dropdown-visible');
    });

    // NP Bar controls
    document.getElementById('np-play').addEventListener('click', onPlayPause);
    document.getElementById('np-next').addEventListener('click', onNext);
    document.getElementById('np-prev').addEventListener('click', onPrevious);
    document.getElementById('np-shuffle').addEventListener('click', onShuffle);
    document.getElementById('np-repeat').addEventListener('click', function() {
        const modes = ['OFF', 'ONE', 'ALL'];
        const current = document.getElementById('repeat-mode').value;
        const next = modes[(modes.indexOf(current) + 1) % modes.length];
        document.getElementById('repeat-mode').value = next;
        onRepeatChange();
    });

    // NP Volume bar
    const volBar = document.getElementById('np-volume-bar');
    volBar.addEventListener('click', e => {
        const rect = volBar.getBoundingClientRect();
        const pct = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
        document.getElementById('np-volume-fill').style.width = (pct * 100) + '%';
    });

    // NP Progress bar
    const progBar = document.getElementById('np-progress-bar');
    progBar.addEventListener('click', e => {
        if (!currentSong || !currentSong.duration) return;
        const rect = progBar.getBoundingClientRect();
        const pct = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
        document.getElementById('np-progress-fill').style.width = (pct * 100) + '%';
        const seekTime = Math.floor(pct * currentSong.duration);
        document.getElementById('np-time-current').textContent = formatDuration(seekTime);
    });

    // Library event listeners
    document.getElementById('btn-lib-filter').addEventListener('click', () => {
        libCurrentPage = 1;
        loadLibrary();
    });
    document.getElementById('btn-lib-reset').addEventListener('click', onLibReset);
    document.getElementById('btn-lib-add-selected').addEventListener('click', onLibAddSelected);
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
            <p class="empty-hint">Browse the Song Library and add songs to get started!</p>
        </td></tr>`;
        return;
    }

    tbody.innerHTML = songs.map((song, i) => {
        const isActive = currentSong && song.id === currentSong.id;
        return `<tr class="tracklist-row${isActive ? ' active-song' : ''}" data-song-id="${song.id}">
            <td class="track-col-num">
                <span class="track-num">${i + 1}</span>
                <span class="track-play-icon">
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M8 5v14l11-7L8 5z"/></svg>
                </span>
            </td>
            <td class="track-col-title">
                <span class="track-title${isActive ? ' track-title-active' : ''}">${escapeHtml(song.title)}</span>
            </td>
            <td class="track-col-artist"><span class="track-artist">${escapeHtml(song.artist)}</span></td>
            <td class="track-col-duration"><span class="track-duration">${formatDuration(song.duration)}</span></td>
            <td class="track-col-action">
                <button class="track-more" data-id="${song.id}" title="More">
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><circle cx="5" cy="12" r="2"/><circle cx="12" cy="12" r="2"/><circle cx="19" cy="12" r="2"/></svg>
                </button>
            </td>
        </tr>`;
    }).join('');

    tbody.querySelectorAll('.track-more').forEach(btn => {
        btn.addEventListener('click', e => {
            e.stopPropagation();
            const rect = btn.getBoundingClientRect();
            showContextMenu(rect.right - 180, rect.bottom + 4, btn.dataset.id);
        });
    });

    tbody.querySelectorAll('.tracklist-row').forEach(row => {
        row.addEventListener('dblclick', () => {
            const id = row.dataset.songId;
            const song = currentPlaylist.find(s => String(s.id) === id);
            if (song && currentSong && String(currentSong.id) === id) {
                onPlayPause();
            } else {
                onPlaySongById(id);
            }
        });
    });
}

function updateNowPlayingBar(song) {
    const titleEl = document.getElementById('np-title');
    const artistEl = document.getElementById('np-artist');
    const playBtn = document.getElementById('np-play');

    if (!song) {
        titleEl.textContent = 'No song playing';
        artistEl.textContent = '';
        playBtn.innerHTML = '<svg viewBox="0 0 24 24" width="18" height="18" fill="#000"><path d="M8 5v14l11-7L8 5z"/></svg>';
        playBtn.title = 'Play';
        document.getElementById('np-time-current').textContent = '0:00';
        document.getElementById('np-time-total').textContent = '0:00';
        document.getElementById('np-progress-fill').style.width = '0%';
        return;
    }

    titleEl.textContent = song.title;
    artistEl.textContent = song.artist;
    document.getElementById('np-time-total').textContent = formatDuration(song.duration);

    if (isPlaying) {
        playBtn.innerHTML = '<svg viewBox="0 0 24 24" width="18" height="18" fill="#000"><rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/></svg>';
        playBtn.title = 'Pause';
    } else {
        playBtn.innerHTML = '<svg viewBox="0 0 24 24" width="18" height="18" fill="#000"><path d="M8 5v14l11-7L8 5z"/></svg>';
        playBtn.title = 'Play';
    }
}

function renderCurrentSong(song) {
    const info = document.getElementById('current-song-info');
    currentSong = song;
    updateNowPlayingBar(song);

    if (!song) {
        info.innerHTML = '<p class="no-song">No song playing</p>';
        return;
    }

    const statusIcon = isPlaying
        ? '<span class="playing-indicator">▶ Playing</span>'
        : '<span class="paused-indicator">⏸ Paused</span>';

    info.innerHTML = `
        ${statusIcon}
        <span class="song-tag">ID <span>${song.id}</span></span>
        <span class="song-tag">Title <span>${escapeHtml(song.title)}</span></span>
        <span class="song-tag">Artist <span>${escapeHtml(song.artist)}</span></span>
        <span class="song-tag">Duration <span>${formatDuration(song.duration)}</span></span>
    `;
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

async function loadCurrentSong() {
    try {
        const song = await getCurrentSong();
        if (song) {
            renderCurrentSong(song);
            highlightActiveSong(song);
            renderCircularList(currentPlaylist, song.id);
        } else if (currentPlaylist.length > 0) {
            const song = await playSong();
            if (song) {
                renderCurrentSong(song);
                highlightActiveSong(song);
                renderCircularList(currentPlaylist, song.id);
            }
        }
        updatePlayButton();
    } catch (err) {
        console.error('Failed to load current song:', err);
    }
}

async function onPlayPause() {
    if (isPlaying) {
        isPlaying = false;
        updatePlayButton();
        renderCurrentSong(currentSong);
        return;
    }
    if (!currentSong && currentPlaylist.length > 0) {
        try {
            const song = await playSong();
            renderCurrentSong(song);
            highlightActiveSong(song);
            renderCircularList(currentPlaylist, song ? song.id : null);
        } catch (err) {
            console.error('Play failed:', err);
            return;
        }
    }
    isPlaying = true;
    updatePlayButton();
    renderCurrentSong(currentSong);
}

function updatePlayButton() {
    const btn = document.getElementById('btn-play');
    btn.innerHTML = isPlaying
        ? '<svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/></svg>'
        : '<svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M8 5v14l11-7L8 5z"/></svg>';
    btn.title = isPlaying ? 'Pause' : 'Play';
    updateNowPlayingBar(currentSong);
}

async function onNext() {
    try {
        const song = await playNext();
        renderCurrentSong(song);
        highlightActiveSong(song);
        renderCircularList(currentPlaylist, song ? song.id : null);
        loadHistory();
        if (song) {
            isPlaying = true;
            updatePlayButton();
        }
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
        if (song) {
            isPlaying = true;
            updatePlayButton();
        }
    } catch (err) {
        console.error('Previous failed:', err);
    }
}

async function onShuffle() {
    const shuffleEl = document.getElementById('shuffle-animation');
    const npShuffle = document.getElementById('np-shuffle');
    npShuffle.classList.toggle('active');
    npShuffle.style.color = npShuffle.classList.contains('active') ? '#1ed760' : '';
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
    const npRepeat = document.getElementById('np-repeat');
    npRepeat.classList.toggle('active', mode !== 'OFF');
    npRepeat.style.color = mode !== 'OFF' ? '#1ed760' : '';
    try {
        await setRepeatMode(mode);
    } catch (err) {
        console.error('Repeat mode change failed:', err);
    }
}

let searchDebounceTimer = null;

async function onSearchFocus() {
    const dd = document.getElementById('search-dropdown');
    const val = document.getElementById('search-input').value.trim();
    if (val.length > 0) {
        dd.classList.add('search-dropdown-visible');
    }
}

async function onSearchType() {
    clearTimeout(searchDebounceTimer);
    searchDebounceTimer = setTimeout(async () => {
        const val = document.getElementById('search-input').value.trim();
        const dd = document.getElementById('search-dropdown');
        if (val.length === 0) {
            dd.classList.remove('search-dropdown-visible');
            return;
        }
        try {
            const data = await getCsvSongs({ title: val, page: 1, pageSize: 20 });
            const results = data && data.songs ? data.songs : [];
            if (results.length === 0) {
                dd.innerHTML = '<div class="search-dd-empty">No songs found</div>';
                dd.classList.add('search-dropdown-visible');
                return;
            }
            dd.innerHTML = results.map(song => `
                <div class="search-dd-item" data-song-id="${song.id}" data-song-title="${escapeHtml(song.title)}" data-song-artist="${escapeHtml(song.artist)}" data-song-duration="${song.duration}">
                    <div class="search-dd-info">
                        <div class="search-dd-title">${escapeHtml(song.title)}</div>
                        <div class="search-dd-artist">${escapeHtml(song.artist)} — ${formatDuration(song.duration)}</div>
                    </div>
                    <button class="search-dd-add" data-id="${song.id}" data-title="${escapeHtml(song.title)}" data-artist="${escapeHtml(song.artist)}" data-duration="${song.duration}">Add</button>
                </div>
            `).join('');
            dd.classList.add('search-dropdown-visible');

            dd.querySelectorAll('.search-dd-add').forEach(btn => {
                btn.addEventListener('click', async e => {
                    e.stopPropagation();
                    btn.disabled = true;
                    btn.textContent = '⏳';
                    try {
                        await addSong({
                            id: btn.dataset.id,
                            title: btn.dataset.title,
                            artist: btn.dataset.artist,
                            duration: parseInt(btn.dataset.duration)
                        });
                        await loadPlaylist();
                        loadLibrary();
                        showToast(`Added "${btn.dataset.title}" to playlist`);
                        btn.textContent = '✓';
                    } catch (err) {
                        console.error('Add from search failed:', err);
                        btn.textContent = 'Add';
                        btn.disabled = false;
                    }
                });
            });
        } catch (err) {
            console.error('Search failed:', err);
        }
    }, 250);
}

async function onAddToPlaylist(id) {
    try {
        const song = currentPlaylist.find(s => String(s.id) === String(id));
        if (song) {
            showToast('Song is already in your playlist');
            return;
        }
        const data = await getCsvSongs({ page: 1, pageSize: 1000 });
        if (data && data.songs) {
            const found = data.songs.find(s => String(s.id) === String(id));
            if (found) {
                await addSong({ id: found.id, title: found.title, artist: found.artist, duration: found.duration });
                await loadPlaylist();
                loadLibrary();
                showToast(`Added "${found.title}" to playlist`);
            }
        }
    } catch (err) {
        console.error('Add to playlist failed:', err);
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
        loadLibrary();
    } catch (err) {
        console.error('Delete song failed:', err);
    }
}


function highlightActiveSong(song) {
    const rows = document.querySelectorAll('#playlist-body .tracklist-row');
    rows.forEach(row => {
        row.classList.remove('active-song');
        if (song && row.dataset.songId === String(song.id)) {
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
        minDuration: document.getElementById('lib-filter-min-dur').value || undefined,
        maxDuration: document.getElementById('lib-filter-max-dur').value || undefined,
        page: libCurrentPage,
        pageSize: 10,
    };
}

async function loadLibrary() {
    try {
        const filters = getLibFilters();
        const data = await getCsvSongs(filters);

        // Update badge
        const badge = document.getElementById('library-total-badge');
        if (badge) badge.textContent = `${data.total} songs`;

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
            <td>${isInPlaylist ? '<button class="btn btn-ghost" disabled style="opacity:0.4;padding:6px 16px;font-size:13px;">Added</button>' : `<button class="btn btn-ghost" data-id="${song.id}" style="padding:6px 16px;font-size:13px;">Add</button>`}</td>
        </tr>`;
    }).join('');

    // Individual add buttons
    tbody.querySelectorAll('.btn-ghost[data-id]').forEach(btn => {
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
    document.getElementById('lib-filter-min-dur').value = '';
    document.getElementById('lib-filter-max-dur').value = '';
    libCurrentPage = 1;
    libSelectedIds.clear();
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

// ═══════════════════════════════════════════════════════════════
// CONTENT RAILS
// ═══════════════════════════════════════════════════════════════

async function loadContentRails() {
    try {
        const data = await getCsvSongs({ page: 1, pageSize: 100 });
        if (!data || !data.songs || data.songs.length === 0) return;

        const featuredEl = document.getElementById('rail-featured-content');
        const featured = data.songs.slice(0, 10);
        featuredEl.innerHTML = featured.map(song => `
            <div class="rail-card" data-song-id="${song.id}">
                <div class="rail-card-img">
                    <button class="rail-play-btn" data-id="${song.id}">
                        <svg viewBox="0 0 24 24" width="20" height="20" fill="#000"><path d="M8 5v14l11-7L8 5z"/></svg>
                    </button>
                </div>
                <div class="rail-card-title">${escapeHtml(song.title)}</div>
                <div class="rail-card-sub">${escapeHtml(song.artist)}</div>
            </div>
        `).join('');

        const artistMap = {};
        data.songs.forEach(song => {
            if (!artistMap[song.artist]) artistMap[song.artist] = [];
            if (artistMap[song.artist].length < 6) artistMap[song.artist].push(song);
        });
        const topArtists = Object.keys(artistMap).slice(0, 5);
        const artistEl = document.getElementById('rail-artists-content');
        artistEl.innerHTML = topArtists.flatMap(artist => artistMap[artist]).map(song => `
            <div class="rail-card" data-song-id="${song.id}">
                <div class="rail-card-img">
                    <button class="rail-play-btn" data-id="${song.id}">
                        <svg viewBox="0 0 24 24" width="20" height="20" fill="#000"><path d="M8 5v14l11-7L8 5z"/></svg>
                    </button>
                </div>
                <div class="rail-card-title">${escapeHtml(song.title)}</div>
                <div class="rail-card-sub">${escapeHtml(song.artist)}</div>
            </div>
        `).join('');

        document.querySelectorAll('.rail-play-btn').forEach(btn => {
            btn.addEventListener('click', e => {
                e.stopPropagation();
                onPlaySongById(btn.dataset.id);
            });
        });
        document.querySelectorAll('.rail-card').forEach(card => {
            card.addEventListener('contextmenu', e => {
                e.preventDefault();
                showContextMenu(e.clientX, e.clientY, card.dataset.songId);
            });
        });
    } catch (err) {
        console.error('Failed to load content rails:', err);
    }
}

async function onPlaySongById(id) {
    let song = currentPlaylist.find(s => String(s.id) === String(id));
    if (!song) {
        try {
            const data = await getCsvSongs({ page: 1, pageSize: 1000 });
            if (data && data.songs) {
                song = data.songs.find(s => String(s.id) === String(id));
            }
        } catch (err) {
            console.error('Failed to find song in library:', err);
            return;
        }
    }
    if (!song) return;
    if (currentSong && String(currentSong.id) === String(id)) {
        onPlayPause();
        return;
    }
    try {
        const inPlaylist = currentPlaylist.some(s => String(s.id) === String(id));
        if (!inPlaylist) {
            await addSong({ id: song.id, title: song.title, artist: song.artist, duration: song.duration });
            await loadPlaylist();
        }
        loadHistory();
        // Find song in updated playlist
        const playSong = currentPlaylist.find(s => String(s.id) === String(id));
        if (!playSong) return;

        // We need to set this song as current. The backend /play starts from first song.
        // Navigate to the correct song by playing next/prev until we reach it
        const idx = currentPlaylist.findIndex(s => String(s.id) === String(id));
        if (idx <= 0) {
            const s = await playSong();
            if (s) {
                isPlaying = true;
                renderCurrentSong(s);
                highlightActiveSong(s);
                renderCircularList(currentPlaylist, s.id);
                updatePlayButton();
            }
            return;
        }
        let current = await playSong();
        for (let i = 0; i < idx && current; i++) {
            current = await playNext();
        }
        if (current) {
            isPlaying = true;
            renderCurrentSong(current);
            highlightActiveSong(current);
            renderCircularList(currentPlaylist, current.id);
            updatePlayButton();
        }
    } catch (err) {
        console.error('Play song by ID failed:', err);
    }
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
