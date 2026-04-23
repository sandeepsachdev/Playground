(function () {
    const container = document.getElementById('cloud');
    if (!container || typeof WordCloud !== 'function') {
        return;
    }

    const palette = ['#6ee7ff', '#f5b301', '#9ad36b', '#ff8a80', '#c9b1ff', '#ffb870'];
    const REFRESH_MS = 120000;
    const SOURCE_CYCLE_MS = 20000;
    const CLOUD_WORD_LIMIT = 50;

    const dialog = document.getElementById('article-dialog');
    const dialogTerm = dialog ? dialog.querySelector('.term-chip') : null;
    const dialogList = dialog ? dialog.querySelector('.article-list') : null;
    const dialogClose = dialog ? dialog.querySelector('.close') : null;

    const main = document.querySelector('main');
    const toggleBtn = document.getElementById('toggle-top');
    const topListAside = document.getElementById('top-list');
    const sourceLabel = document.getElementById('source-label');

    let allArticles = [];
    let sourceCycle = [];
    let cycleIndex = 0;
    let cycleTimer = null;
    let renderTimer = null;
    let renderGen = 0;

    if (dialog) {
        dialogClose.addEventListener('click', function () { dialog.close(); });
        dialog.addEventListener('click', function (e) {
            if (e.target === dialog) {
                dialog.close();
            }
        });
        // Re-render after dialog closes so the canvas gets a fresh event-handler
        // registration. On mobile, dismissing a modal can leave touch routing in a
        // confused state; a new canvas clears it.
        dialog.addEventListener('close', function () { showCurrent(); });
    }

    if (toggleBtn && main) {
        toggleBtn.addEventListener('click', function () {
            const hidden = main.classList.toggle('hide-top');
            toggleBtn.setAttribute('aria-pressed', hidden ? 'false' : 'true');
            toggleBtn.textContent = hidden ? 'Show top words' : 'Hide top words';
        });
    }

    function escapeHtml(s) {
        return String(s)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function openArticle(article) {
        if (article && article.link) {
            window.open(article.link, '_blank', 'noopener');
        }
    }

    function matchesTerm(article, term) {
        const haystack = (article.title + ' ' + article.description).toLowerCase();
        return haystack.indexOf(term) !== -1;
    }

    function currentEntry() {
        return sourceCycle.length > 0 ? sourceCycle[cycleIndex % sourceCycle.length] : null;
    }

    function showArticlesFor(term) {
        if (!dialog || !term || dialog.open) {
            return;
        }
        const needle = term.toLowerCase();
        const entry = currentEntry();
        const pool = entry && entry.source ?
            allArticles.filter(function (a) { return a.source === entry.source; }) :
            allArticles;
        const matches = pool.filter(function (a) { return matchesTerm(a, needle); });

        dialogTerm.textContent = term;
        dialogList.innerHTML = '';

        if (matches.length === 0) {
            const empty = document.createElement('li');
            empty.className = 'empty';
            empty.textContent = 'No articles currently mention this term.';
            dialogList.appendChild(empty);
        } else {
            matches.forEach(function (a) {
                const li = document.createElement('li');
                li.innerHTML =
                    '<div class="article-meta">' + escapeHtml(a.source || '') + '</div>' +
                    '<div class="article-title">' + escapeHtml(a.title || '') + '</div>' +
                    '<div class="article-desc">' + escapeHtml(a.description || '') + '</div>';
                li.addEventListener('click', function () { openArticle(a); });
                dialogList.appendChild(li);
            });
        }

        if (typeof dialog.showModal === 'function') {
            dialog.showModal();
        } else {
            dialog.setAttribute('open', '');
        }
    }

    function render(words) {
        if (!words || words.length === 0) {
            container.textContent = 'No trending words yet. Configure feeds and retry.';
            return;
        }

        // Cancel any render that hasn't started yet.
        if (renderTimer) {
            clearTimeout(renderTimer);
            renderTimer = null;
        }

        // Signal the active render to stop. wordcloud2 sets an internal escapeTime
        // flag that its next step-callback checks before continuing.
        if (typeof WordCloud.stop === 'function') {
            WordCloud.stop();
        }

        const myGen = ++renderGen;
        const rect = container.getBoundingClientRect();
        const cssW = Math.max(240, rect.width);
        const cssH = Math.max(320, rect.height);
        const canvas = document.createElement('canvas');
        // Keep canvas internal resolution in CSS pixels so wordcloud2's hit-test
        // (which divides offsetX/offsetY by gridSize) stays aligned with taps.
        canvas.width = Math.round(cssW);
        canvas.height = Math.round(cssH);
        canvas.style.width = cssW + 'px';
        canvas.style.height = cssH + 'px';
        container.innerHTML = '';
        container.appendChild(canvas);
        canvas.style.cursor = 'pointer';

        const maxCount = words[0][1];
        // Scale font sizes to the container so words never swamp a phone.
        const maxWord = Math.min(72, Math.max(28, cssW * 0.18));
        const minWord = Math.max(10, Math.min(14, cssW * 0.035));

        // Defer WordCloud() so any pending wait:1 step-callbacks from the stopped
        // render can fire and exit before the new render resets the internal timer
        // state. 50 ms is enough headroom even on throttled mobile timers.
        renderTimer = setTimeout(function () {
            renderTimer = null;
            if (renderGen !== myGen) { return; }
            WordCloud(canvas, {
                list: words,
                fontFamily: 'Helvetica, Arial, sans-serif',
                weightFactor: function (count) {
                    return Math.max(minWord, (count / maxCount) * maxWord);
                },
                color: function () {
                    return palette[Math.floor(Math.random() * palette.length)];
                },
                backgroundColor: 'transparent',
                rotateRatio: 0.25 + Math.random() * 0.3,
                rotationSteps: 2,
                gridSize: 6 + Math.floor(Math.random() * 6),
                shrinkToFit: true,
                shuffle: true,
                // Yield to the browser between word placements so taps stay responsive on mobile.
                wait: 1,
                click: function (item) {
                    if (item && item[0]) {
                        showArticlesFor(item[0]);
                    }
                }
            });
        }, 50);
    }

    function rebuildTopList(words) {
        if (!topListAside) {
            return;
        }
        const ol = topListAside.querySelector('ol');
        if (!ol) {
            return;
        }
        ol.innerHTML = '';
        words.slice(0, 25).forEach(function (w, i) {
            const li = document.createElement('li');
            li.innerHTML =
                '<span class="rank">' + (i + 1) + '</span>' +
                '<span class="term">' + escapeHtml(w.text) + '</span>' +
                '<span class="count">' + w.count + '</span>' +
                (w.phrase ? '<span class="badge">phrase</span>' : '<span></span>') +
                '<button type="button" class="exclude" data-term="' + escapeHtml(w.text) +
                '" data-phrase="' + (w.phrase ? 'true' : 'false') +
                '" title="Exclude this word from the cloud">Exclude</button>';
            ol.appendChild(li);
        });
        wireTopList();
    }

    function wireTopList() {
        document.querySelectorAll('.top-list .term').forEach(function (el) {
            el.style.cursor = 'pointer';
            el.addEventListener('click', function () {
                showArticlesFor(el.textContent.trim());
            });
        });
        document.querySelectorAll('.top-list .exclude').forEach(function (btn) {
            btn.addEventListener('click', function (e) {
                e.stopPropagation();
                excludeTerm(btn);
            });
        });
    }

    function excludeTerm(btn) {
        const term = btn.getAttribute('data-term');
        if (!term) {
            return;
        }
        btn.disabled = true;
        fetch('/api/stopwords', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify({ word: term, fragment: false })
        }).then(function (response) {
            if (!response.ok) {
                throw new Error('HTTP ' + response.status);
            }
            return response.json();
        }).then(function () {
            return loadSnapshot();
        }).catch(function (err) {
            btn.disabled = false;
            alert('Could not exclude "' + term + '": ' + err.message);
        });
    }

    function toPairs(words) {
        return words.map(function (w) { return [w.text, w.count]; });
    }

    function setSourceLabel(text) {
        if (sourceLabel) {
            sourceLabel.textContent = text || '';
        }
    }

    function showCurrent() {
        const entry = currentEntry();
        if (!entry) {
            setSourceLabel('');
            render([]);
            rebuildTopList([]);
            return;
        }
        setSourceLabel(entry.label);
        rebuildTopList(entry.words);
        render(toPairs(entry.words.slice(0, CLOUD_WORD_LIMIT)));
    }

    function advanceCycle() {
        if (sourceCycle.length <= 1) { return; }
        if (document.hidden) { return; }
        if (dialog && dialog.open) { return; }
        cycleIndex = (cycleIndex + 1) % sourceCycle.length;
        showCurrent();
    }

    function startCycle() {
        if (cycleTimer) { clearInterval(cycleTimer); }
        cycleTimer = setInterval(advanceCycle, SOURCE_CYCLE_MS);
    }

    function buildSourceCycle(snapshot) {
        const perSource = snapshot.sourceWords || [];
        const cycle = perSource
            .filter(function (s) { return s.words && s.words.length > 0; })
            .map(function (s) {
                return { source: s.source, label: s.source, words: s.words };
            });
        if (cycle.length === 0) {
            const words = snapshot.words || [];
            if (words.length > 0) {
                cycle.push({ source: null, label: 'All sources', words: words });
            }
        }
        return cycle;
    }

    function loadSnapshot() {
        return fetch('/api/trending', { headers: { 'Accept': 'application/json' } })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('Failed to load /api/trending (HTTP ' + response.status + ')');
                }
                return response.json();
            })
            .then(function (snapshot) {
                allArticles = snapshot.articles || [];
                const previousSource = currentEntry() ? currentEntry().source : null;
                sourceCycle = buildSourceCycle(snapshot);
                cycleIndex = 0;
                if (previousSource) {
                    for (let i = 0; i < sourceCycle.length; i++) {
                        if (sourceCycle[i].source === previousSource) {
                            cycleIndex = i;
                            break;
                        }
                    }
                }
                showCurrent();
            });
    }

    // When the tab is hidden, stop any active render so throttled mobile timers
    // don't pile up and flood the event loop on return. Re-render on visibility
    // restore so the canvas and its touch handlers are always fresh.
    document.addEventListener('visibilitychange', function () {
        if (document.hidden) {
            if (renderTimer) {
                clearTimeout(renderTimer);
                renderTimer = null;
            }
            if (typeof WordCloud.stop === 'function') {
                WordCloud.stop();
            }
        } else {
            showCurrent();
        }
    });

    wireTopList();
    loadSnapshot()
        .then(startCycle)
        .catch(function (err) {
            container.textContent = 'Could not load the word cloud: ' + err.message;
        });
    setInterval(function () {
        if (document.hidden) { return; }
        if (dialog && dialog.open) { return; }
        loadSnapshot().catch(function () { /* keep cycling with prior data */ });
    }, REFRESH_MS);
})();
