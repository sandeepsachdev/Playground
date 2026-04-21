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

    let articles = [];

    if (dialog) {
        dialogClose.addEventListener('click', function () { dialog.close(); });
        dialog.addEventListener('click', function (e) {
            if (e.target === dialog) {
                dialog.close();
            }
        });
    }

    function matchesTerm(article, term) {
        const haystack = (article.title + ' ' + article.description).toLowerCase();
        return haystack.indexOf(term) !== -1;
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
        const rect = container.getBoundingClientRect();
        const canvas = document.createElement('canvas');
        canvas.width = Math.max(600, rect.width);
        canvas.height = Math.max(480, rect.height);
        container.innerHTML = '';
        container.appendChild(canvas);
        canvas.style.cursor = 'pointer';

        const maxCount = words[0][1];

        WordCloud(canvas, {
            list: words,
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
            click: function (item) {
                if (item && item[0]) {
                    showArticlesFor(item[0]);
                }
            }
        });
    }

    function wireTopList() {
        document.querySelectorAll('.top-list .term').forEach(function (el) {
            el.style.cursor = 'pointer';
            el.addEventListener('click', function () {
                showArticlesFor(el.textContent.trim());
            });
        });
    }

    fetch('/api/trending', { headers: { 'Accept': 'application/json' } })
        .then(function (response) {
            if (!response.ok) {
                throw new Error('Failed to load /api/trending (HTTP ' + response.status + ')');
            }
            return response.json();
        })
        .then(function (snapshot) {
            articles = snapshot.articles || [];
            const words = (snapshot.words || []).map(function (w) {
                return [w.text, w.count];
            });
            if (words.length === 0) {
                container.textContent = 'No trending words yet. Configure feeds and retry.';
                return;
            }

            wireTopList();
            render(words);
            setInterval(function () { render(words); }, REFRESH_MS);
        })
        .catch(function (err) {
            container.textContent = 'Could not load the word cloud: ' + err.message;
        });
})();
