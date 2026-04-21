(function () {
    const container = document.getElementById('cloud');
    if (!container || typeof WordCloud !== 'function') {
        return;
    }

    const palette = ['#6ee7ff', '#f5b301', '#9ad36b', '#ff8a80', '#c9b1ff', '#ffb870'];
    const REFRESH_MS = 10000;

    const dialog = document.getElementById('article-dialog');
    const dialogTerm = dialog ? dialog.querySelector('.term-chip') : null;
    const dialogList = dialog ? dialog.querySelector('.article-list') : null;
    const dialogClose = dialog ? dialog.querySelector('.close') : null;

    const main = document.querySelector('main');
    const toggleBtn = document.getElementById('toggle-top');
    const topListAside = document.getElementById('top-list');

    let currentWords = [];
    let articles = [];

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

    function showArticlesFor(term) {
        if (!dialog || !term) {
            return;
        }
        const needle = term.toLowerCase();
        const matches = articles.filter(function (a) { return matchesTerm(a, needle); });

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
        const rect = container.getBoundingClientRect();
        const dpr = Math.max(1, window.devicePixelRatio || 1);
        const cssW = Math.max(240, rect.width);
        const cssH = Math.max(320, rect.height);
        const canvas = document.createElement('canvas');
        canvas.width = Math.round(cssW * dpr);
        canvas.height = Math.round(cssH * dpr);
        canvas.style.width = cssW + 'px';
        canvas.style.height = cssH + 'px';
        container.innerHTML = '';
        container.appendChild(canvas);
        canvas.style.cursor = 'pointer';

        const maxCount = words[0][1];
        // Scale font sizes to the container so words never swamp a phone.
        const maxWord = Math.min(72, Math.max(28, cssW * 0.18));
        const minWord = Math.max(10, Math.min(14, cssW * 0.035));

        WordCloud(canvas, {
            list: words,
            fontFamily: 'Helvetica, Arial, sans-serif',
            weightFactor: function (count) {
                return Math.max(minWord, (count / maxCount) * maxWord) * dpr;
            },
            color: function () {
                return palette[Math.floor(Math.random() * palette.length)];
            },
            backgroundColor: 'transparent',
            rotateRatio: 0.25 + Math.random() * 0.3,
            rotationSteps: 2,
            gridSize: Math.round((6 + Math.floor(Math.random() * 6)) * dpr),
            shrinkToFit: true,
            shuffle: true,
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

    function loadSnapshot() {
        return fetch('/api/trending', { headers: { 'Accept': 'application/json' } })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('Failed to load /api/trending (HTTP ' + response.status + ')');
                }
                return response.json();
            })
            .then(function (snapshot) {
                articles = snapshot.articles || [];
                const words = snapshot.words || [];
                currentWords = words.map(function (w) { return [w.text, w.count]; });
                rebuildTopList(words);
                render(currentWords);
            });
    }

    wireTopList();
    loadSnapshot().catch(function (err) {
        container.textContent = 'Could not load the word cloud: ' + err.message;
    });
    setInterval(function () { render(currentWords); }, REFRESH_MS);
})();
