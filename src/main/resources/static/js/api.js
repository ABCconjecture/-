const API_BASE = '/api/campus';

async function request(url, options = {}) {
    const defaultOptions = {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    };
    try {
        const response = await fetch(API_BASE + url, { ...defaultOptions, ...options });
        const json = await response.json();

        // 只要 code 是 200 就返回 data
        if (json.code === 200) {
            return json.data !== undefined ? json.data : json;
        }
        throw new Error(json.message || '请求失败');
    } catch (error) {
        console.error('API 接入异常:', error);
        throw error;
    }
}

const api = {
    getStats: () => request('/stats'),
    getTrend: () => request('/trend'),
    getUsers: (p = 0, s = 50) => request(`/users?page=${p}&size=${s}`),
    // 修正：调用刚刚补全的详情接口
    getProfile: (uid) => request(`/users/${uid}/profile`),
    getAnalysis: (uid) => request(`/analysis/${uid}`),
    getUserWarnings: (uid) => request(`/warning?userId=${uid}`),
    getAnalysisHistory: (uid) => request(`/analysis/${uid}/history`),
    getProfileRadar: (uid) => {
        return Promise.resolve({
            indicator: ["学业活跃", "社交参与", "消费稳定", "出入规律", "网络学习"],
            value: [85, 70, 65, 90, 80]
        });
    },
    triggerAnalysisUpdate: () => request('/analysis/1/trigger', { method: 'POST' }),
    triggerProfileUpdate: () => request('/cluster/trigger', { method: 'POST' }),
    triggerWarningCheck: () => request('/warning/trigger', { method: 'POST' }),
    getWarningStats: () => request('/warning/stats'),
    getClusterCounts: () => request('/cluster/counts')
};