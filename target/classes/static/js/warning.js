/**
 * 预警管理页面脚本
 */

document.addEventListener('DOMContentLoaded', async () => {
    console.log('⚠️ 预警管理页面加载中...');
    await refreshPageData();
});

/**
 * 刷新页面所有数据
 */
async function refreshPageData() {
    try {
        await Promise.all([
            loadWarningStats(),
            loadHighRiskUsers(),
            loadUnhandledWarnings()
        ]);
    } catch (error) {
        console.error('加载数据失败:', error);
    }
}

/**
 * 加载预警统计
 */
async function loadWarningStats() {
    try {
        const stats = await api.getStats();
        const typeStats = await api.getWarningStats();

        document.getElementById('unhandledCount').textContent = stats.unhandledWarnings || 0;
        document.getElementById('highRiskUserCount').textContent = stats.highRiskUsers || 0;

        renderWarningTypeChart(typeStats);
        console.log('✓ 预警统计加载完成');
    } catch (error) {
        console.error('加载预警统计失败:', error);
    }
}

/**
 * 加载高风险用户列表
 */
async function loadHighRiskUsers() {
    try {
        const users = await api.getHighRiskUsers();
        const tbody = document.getElementById('highRiskTable');

        if (!users || users.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-light);">暂无高风险用户</td></tr>';
            return;
        }

        tbody.innerHTML = users.map(user => `
            <tr>
                <td>${user.studentId || '-'}</td>
                <td>${user.name || '-'}</td>
                <td>${user.college || '-'}</td>
                <td><span class="badge badge-danger">${user.warningCount || 0}</span></td>
                <td><span class="badge ${getWarningLevelBadgeClass(user.maxRiskLevel)}">${user.maxRiskLevel || 'HIGH'}</span></td>
                <td>
                    <button class="btn btn-primary btn-small" onclick="location.href='analysis.html#user=${user.userId}'">查看详情</button>
                </td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('加载高风险用户失败:', error);
    }
}

/**
 * 加载未处理预警
 */
async function loadUnhandledWarnings() {
    try {
        // 使用 api.js 中定义的 getUserWarnings (不传 uid 则获取全部)
        const warnings = await api.getUserWarnings();
        const tbody = document.getElementById('warningTable');

        if (!warnings || warnings.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-light);">暂无未处理预警数据</td></tr>';
            return;
        }

        tbody.innerHTML = warnings.map(w => `
            <tr>
                <td>用户 ID: ${w.userId}</td>
                <td>${w.warningType || '综合风险'}</td>
                <td><span class="badge ${getWarningLevelBadgeClass(w.warningLevel)}">${w.warningLevel || 'MEDIUM'}</span></td>
                <td>${w.riskScore || 0}</td>
                <td>${w.riskDescription || '系统自动检测异常'}</td>
                <td>${new Date(w.createTime).toLocaleDateString()}</td>
                <td>
                    <button class="btn btn-success btn-small" onclick="handleWarningClick(${w.warningId})">处理</button>
                </td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('加载未处理预警失败:', error);
    }
}

/**
 * 渲染 ECharts 饼图
 */
function renderWarningTypeChart(data) {
    const chartDom = document.getElementById('warningTypeChart');
    if (!chartDom || !window.echarts) return;

    const chart = echarts.init(chartDom);
    const chartData = (data.types || []).map((type, index) => ({
        name: type,
        value: data.counts[index] || 0
    }));

    chart.setOption({
        tooltip: { trigger: 'item' },
        legend: { orient: 'vertical', right: '10%', top: 'center' },
        series: [{
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
            data: chartData
        }]
    });
    window.addEventListener('resize', () => chart.resize());
}

function handleWarningClick(id) {
    if (confirm('确认已处理该条预警记录？')) {
        alert('演示模式：预警状态已模拟更新');
        refreshPageData();
    }
}

function getWarningLevelBadgeClass(level) {
    if (level === 'HIGH' || level === '3') return 'badge-danger';
    if (level === 'MEDIUM' || level === '2') return 'badge-warning';
    return 'badge-success';
}