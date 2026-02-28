/**
 * API客户端 - 与后端通信
 * 基础URL: http://localhost:8080/api/campus
 */

const API_BASE = 'http://localhost:8080/api/campus';
const REQUEST_TIMEOUT = 30000; // 30秒超时

/**
 * HTTP 请求方法
 */
async function request(url, options = {}) {
    const defaultOptions = {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
        timeout: REQUEST_TIMEOUT,
    };

    const finalOptions = { ...defaultOptions, ...options };

    try {
        const response = await fetch(API_BASE + url, finalOptions);

        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            throw new Error(error.message || `HTTP ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('API请求失败:', error);
        throw error;
    }
}

/**
 * API对象 - 包含所有API端点
 */
const api = {
    // ==================== 基础统计接口 ====================

    /**
     * 获取系统统计数据
     */
    getStats: () => {
        console.log('[API] 获取系统统计数据');
        return request('/stats');
    },

    /**
     * 获取健康度趋势
     */
    getTrend: () => {
        console.log('[API] 获取健康度趋势');
        return request('/trend');
    },

    // ==================== 用户管理接口 ====================

    /**
     * 获取用户列表
     * @param {number} page - 页码（从0开始）
     * @param {number} size - 每页记录数
     */
    getUsers: (page = 0, size = 50) => {
        console.log(`[API] 获取用户列表 - 页码:${page}, 每页:${size}`);
        return request(`/users?page=${page}&size=${size}`);
    },

    /**
     * 获取用户详细信息
     * @param {number} userId - 用户ID
     */
    getProfile: (userId) => {
        console.log(`[API] 获取用户详情 - userId:${userId}`);
        return request(`/profile/${userId}`);
    },

    /**
     * 获取用户最新分析数据
     * @param {number} userId - 用户ID
     */
    getAnalysis: (userId) => {
        console.log(`[API] 获取用户分析数据 - userId:${userId}`);
        return request(`/analysis/${userId}`);
    },

    /**
     * 获取用户分析数据历史
     * @param {number} userId - 用户ID
     * @param {number} page - 页码
     * @param {number} size - 每页记录数
     */
    getAnalysisHistory: (userId, page = 0, size = 10) => {
        console.log(`[API] 获取用户分析历史 - userId:${userId}`);
        return request(`/analysis/${userId}/history?page=${page}&size=${size}`);
    },

    /**
     * 获取用户雷达图数据
     * @param {number} userId - 用户ID
     */
    getProfileRadar: (userId) => {
        console.log(`[API] 获取用户雷达图 - userId:${userId}`);
        return request(`/profile/${userId}/radar`);
    },

    // ==================== 预警管理接口 ====================

    /**
     * 获取预警统计
     */
    getWarningStats: () => {
        console.log('[API] 获取预警统计');
        return request('/warning/stats');
    },

    /**
     * 获取未处理预警列表
     */
    getUnhandledWarnings: () => {
        console.log('[API] 获取未处理预警');
        return request('/warning/unhandled');
    },

    /**
     * 获取高风险用户列表
     */
    getHighRiskUsers: () => {
        console.log('[API] 获取高风险用户');
        return request('/warning/high-risk');
    },

    /**
     * 获取特定用户的预警信息
     * @param {number} userId - 用户ID
     */
    getUserWarnings: (userId) => {
        console.log(`[API] 获取用户预警 - userId:${userId}`);
        return request(`/warning?userId=${userId}`);
    },

    /**
     * 处理预警
     * @param {number} warningId - 预警ID
     * @param {string} remark - 处理备注
     */
    handleWarning: (warningId, remark = '') => {
        console.log(`[API] 处理预警 - warningId:${warningId}`);
        return request(`/warning/handle/${warningId}`, {
            method: 'POST',
            body: JSON.stringify({ remark }),
        });
    },

    // ==================== 聚类分析接口 ====================

    /**
     * 获取聚类计数
     */
    getClusterCounts: () => {
        console.log('[API] 获取聚类计数');
        return request('/cluster/counts');
    },

    /**
     * 获取特定聚类的用户列表
     * @param {number} clusterId - 聚类ID
     */
    getClusterUsers: (clusterId) => {
        console.log(`[API] 获取聚类用户 - clusterId:${clusterId}`);
        return request(`/cluster/${clusterId}/users`);
    },

    // ==================== 定时任务接口 ====================

    /**
     * 手动触发多维度分析任务
     */
    triggerAnalysisUpdate: () => {
        console.log('[API] 触发多维度分析任务');
        return request('/task/trigger-analysis', { method: 'POST' });
    },

    /**
     * 手动触发用户画像更新任务
     */
    triggerProfileUpdate: () => {
        console.log('[API] 触发用户画像更新任务');
        return request('/task/trigger-profile', { method: 'POST' });
    },

    /**
     * 手动触发预警检查任务
     */
    triggerWarningCheck: () => {
        console.log('[API] 触发预警检查任务');
        return request('/task/trigger-warning', { method: 'POST' });
    },

    // ==================== 健康检查接口 ====================

    /**
     * 检查系统健康状态
     */
    health: () => {
        console.log('[API] 系统健康检查');
        return request('/health');
    },
};

// 导出API对象供其他文件使用
if (typeof module !== 'undefined' && module.exports) {
    module.exports = api;
}