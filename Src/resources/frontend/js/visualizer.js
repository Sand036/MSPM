function renderCircularList(songs, currentId) {
    const container = document.getElementById('circular-list');
    const repeatMode = document.getElementById('repeat-mode').value;
    if (!songs || songs.length === 0) {
        container.innerHTML = '<span style="color:#666;">Playlist is empty</span>';
        return;
    }

    if (repeatMode === 'ONE') {
        if (!currentId) { container.innerHTML = ''; return; }
        const song = songs.find(s => s.id === currentId);
        if (!song) { container.innerHTML = ''; return; }
        container.innerHTML = `
            <div class="circular-node active-node">
                <div class="node-title">${escapeHtml(song.title)}</div>
                <div class="node-artist">${escapeHtml(song.artist)}</div>
            </div>
            <span class="circular-arrow">→</span>
            <div class="circular-node circular-back-node">↻ back to first</div>
        `;
        return;
    }

    let html = '';
    songs.forEach((song, i) => {
        const active = song.id === currentId ? 'active-node' : '';
        html += `<div class="circular-node ${active}">
            <div class="node-title">${escapeHtml(song.title)}</div>
            <div class="node-artist">${escapeHtml(song.artist)}</div>
        </div>`;
        if (i < songs.length - 1) {
            html += `<span class="circular-arrow">→</span>`;
        }
    });

    if (repeatMode === 'ALL' && songs.length > 1) {
        html += `<span class="circular-arrow">→</span>`;
        html += `<div class="circular-node circular-back-node">↻ back to first</div>`;
    }

    container.innerHTML = html;
}

function renderHistoryStack(history) {
    const container = document.getElementById('stack-display');
    if (!history || history.length === 0) {
        container.innerHTML = '<div style="color:#666;">No history</div>';
        return;
    }

    let html = '<div class="stack-item stack-top">TOP</div>';
    history.forEach(song => {
        html += `<div class="stack-item">${escapeHtml(song.title)} - ${escapeHtml(song.artist)}</div>`;
    });
    container.innerHTML = html;
}

function showShuffleAnimation(callback) {
    const el = document.getElementById('shuffle-animation');
    el.textContent = 'Shuffling playlist...';
    el.className = 'shuffle-visible';

    setTimeout(() => {
        el.className = 'shuffle-hidden';
        if (callback) callback();
    }, 1500);
}

function escapeHtml(text) {
    if (typeof text !== 'string') return String(text);
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
