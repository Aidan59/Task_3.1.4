(function () {
    const csrfToken = () => document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = () => document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';

    window.esc = function (value) {
        const map = {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'};
        return String(value ?? '').replace(/[&<>"']/g, ch => map[ch]);
    };

    window.roleNames = function (roles) {
        return (roles || []).map(r => esc(String(r.name).replace('ROLE_', ''))).join(' ');
    };

    window.api = async function (url, method = 'GET', body) {
        const headers = {'Accept': 'application/json'};
        if (body !== undefined) {
            headers['Content-Type'] = 'application/json';
        }
        if (method !== 'GET') {
            const token = csrfToken();
            if (token) {
                headers[csrfHeader()] = token;
            }
        }

        const res = await fetch(url, {
            method,
            headers,
            body: body === undefined ? undefined : JSON.stringify(body)
        });

        if (res.status === 401 || (res.redirected && res.url.includes('/login'))) {
            window.location.href = '/login';
            throw {message: 'Session expired'};
        }
        if (!res.ok) {
            let err;
            try {
                err = await res.json();
            } catch (e) {
                err = {message: res.status === 403 ? 'Access denied' : 'Request failed (' + res.status + ')'};
            }
            throw err;
        }
        return res.status === 204 ? null : res.json();
    };

    window.errorsHtml = function (err) {
        return Object.values(err || {})
            .filter(v => typeof v === 'string')
            .map(m => '<div>' + esc(m) + '</div>')
            .join('');
    };
})();
