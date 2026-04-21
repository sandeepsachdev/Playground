(function () {
    const container = document.getElementById('cloud');
    if (!container || typeof WordCloud !== 'function') {
        return;
    }

    const palette = ['#6ee7ff', '#f5b301', '#9ad36b', '#ff8a80', '#c9b1ff', '#ffb870'];
    const REFRESH_MS = 10000;

    function newsUrl(term) {
        return 'https://news.google.com/search?q=' + encodeURIComponent(term) + '&hl=en';
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
                    window.open(newsUrl(item[0]), '_blank', 'noopener');
                }
            }
        });
    }

    function wireTopList() {
        document.querySelectorAll('.top-list .term').forEach(function (el) {
            el.style.cursor = 'pointer';
            el.addEventListener('click', function () {
                window.open(newsUrl(el.textContent.trim()), '_blank', 'noopener');
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
