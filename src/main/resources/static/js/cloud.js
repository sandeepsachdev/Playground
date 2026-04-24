(function () {
    const container = document.getElementById('cloud');
    if (!container || typeof WordCloud !== 'function') {
        return;
    }

    const palette = ['#6ee7ff', '#f5b301', '#9ad36b', '#ff8a80', '#c9b1ff', '#ffb870'];
    const REFRESH_MS = 10000;
    const MAX_WORDS = 60;

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
    let renderGen = 0;
    let rendering = false;
    let cycleTimer = null;

    if (dialog) {
        dialogClose.addEventListener('click', function () { dialog.close(); });
        dialog.addEventListener('click', function (e) {
            if (e.target === dialog) {
                dialog.close();
            }
        });
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
        if (!dialog || !term) {
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
        const myGen = ++renderGen;

        if (!words || words.length === 0) {
            container.textContent = 'No trending words yet. Configure feeds and retry.';
            rendering = false;
            return;
        }
        const rect = container.getBoundingClientRect();
        const canvas = document.createElement('canvas');
        canvas.width = Math.max(600, rect.width);
        canvas.height = Math.max(480, rect.height);
        container.innerHTML = '';
        container.appendChild(canvas);
        canvas.style.cursor = 'pointer';

        // Cap list size so a single render stays short on mobile.
        const list = words.length > MAX_WORDS ? words.slice(0, MAX_WORDS) : words;
        const maxCount = list[0][1];

        rendering = true;
        canvas.addEventListener('wordcloudstop', function () {
            if (renderGen === myGen) {
                rendering = false;
            }
        });

        WordCloud(canvas, {
            list: list,
            fontFamily: 'Helvetica, Arial, sans-serif',
            weightFactor: function (count) {
                return Math.max(12, (count / maxCount) * 70);
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
            abort: function () { return renderGen !== myGen; },
            click: function (item) {
                if (item && item[0]) {
                    showArticlesFor(item[0]);
                }
            }
        });
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
        render(toPairs(entry.words));
    }

    function advanceCycle() {
        if (sourceCycle.length <= 1) { return; }
        if (document.hidden) { return; }
        if (dialog && dialog.open) { return; }
        // If the previous render is still drawing, give it this tick to finish.
        if (rendering) { return; }
        cycleIndex = (cycleIndex + 1) % sourceCycle.length;
        showCurrent();
    }

    function scheduleNextCycle() {
        if (cycleTimer) { clearTimeout(cycleTimer); }
        cycleTimer = setTimeout(function () {
            cycleTimer = null;
            advanceCycle();
            scheduleNextCycle();
        }, REFRESH_MS);
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
                sourceCycle = buildSourceCycle(snapshot);
                cycleIndex = 0;
                showCurrent();
            });
    }

    wireTopList();
    loadSnapshot().catch(function (err) {
        container.textContent = 'Could not load the word cloud: ' + err.message;
    });
    scheduleNextCycle();

    document.addEventListener('visibilitychange', function () {
        if (document.hidden) {
            // Drop any in-flight render so wordcloud2's setTimeout chain cannot
            // resume once the tab wakes and flood the UI with queued work.
            renderGen++;
            rendering = false;
            if (cycleTimer) { clearTimeout(cycleTimer); cycleTimer = null; }
        } else {
            scheduleNextCycle();
            if (!rendering) {
                showCurrent();
            }
        }
    });
})();
