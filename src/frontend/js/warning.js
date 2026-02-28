/**
 * 预警管理页面脚本
 */

// 页面加载
document.addEventListener('DOMContentLoaded', async () => {
    console.log('⚠️ 预警管理页面加载中...');

    try {
        // 加载预警统计
        await loadWarningStats();

        // 加载高风险用户
        await loadHighRiskUsers();

        // 加载未处理预警
        await loadUnhandledWarnings();
    } catch (error) {
        console.error('加载数据失败:', error);
    }
});

/**
 * 加载预警统计
 */
async function loadWarningStats() {
    try {
        const [stats, typeStats] = await Promise.all([
            api.getStats(),
            api.getWarningStats(),
        ]);

        document.getElementById('unhandledCount').textContent = stats.unhandledWarnings || 0;
        document.getElementById('highRiskUserCount').textContent = stats.highRiskUsers || 0;

        // 加载类型分布图
        loadWarningTypeChart(typeStats);

        console.log('✓ 预警统计加载完成');
    } catch (error) {
        console.error('加载预警统计失败:', error);
    }
}

/**
 * 加载预警类型分布图
 */
function loadWarningTypeChart(data) {
    const chart = echarts.init(document.getElementById('warningTypeChart'));

    const option = {
        tooltip: {
            trigger: 'item',
            backgroundColor: 'rgba(0, 0, 0, 0.7)',
            borderColor: '#667eea',
        },
        legend: {
            orient: 'right',
            right: '10%',
        },
        series: [
            {
                type: 'pie',
                radius: ['40%', '70%'],
                avoidLabelOverlap: false,
                itemStyle: {
                    borderRadius: [10, 10, 10, 10],
                    borderColor: '#fff',
                    borderWidth: 2,
                },
                label: {
                    show: false,
                    position: 'center',
                },
                emphasis: {
                    label: {
                        show: true,
                        fontSize: 16,
                        fontWeight: 'bold',
                    },
                },
                labelLine: {
                    show: false,
                },
                data: (data.types || []).map((type, index) => ({
                    value: data.counts[index] || 0,
                    name: type,
                })),
            },
        ],
    };

    chart.setOption(option);
    window.addEventListener('resize', () => chart.resize());
}

/**
 * 加载高风险用户
 */
async function loadHighRiskUsers() {
    try {
        const highRiskUsers = await api.getHighRiskUsers();

        const html = highRiskUsers.map(user => `
            <tr>
                <td>${user.studentId || 'N/A'}</td>
                <td>${user.name || 'N/A'}</td>
                <td>${user.college || 'N/A'}</td>
                <td>
                    <span class="badge badge-danger">${user.warningCount || 0}</span>
                </td>
                <td>
                    <span class="badge ${getRiskLevelBadgeClass(user.maxRiskLevel)}">
                        ${getRiskLevelName(user.maxRiskLevel)}
                    </span>
                </td>
                <td>
                    <button class="btn btn-primary btn-small" 
                            onclick="location.href='analysis.html#user=${user.userId}'">
                        查看详情
                    </button>
                </td>
            </tr>
        `).join('');

        document.getElementById('highRiskTable').innerHTML = html ||
            '<tr><td colspan="6" style="text-align: center; color: var(--text-light);">暂无高风险用户</td></tr>';

        console.log('✓ 高风险用户加载完成');
    } catch (error) {
        console.error('加载高风险用户失败:', error);
    }
}

/**
 * 加载未处理预警
 */
async function loadUnhandledWarnings() {
    try {
        const warnings = await api.getUnhandledWarnings();

        const html = (warnings || []).map(w => `
            <tr>
                <td>${w.userName || 'N/A'}</td>
                <td>${w.warningType || '-'}</td>
                <td>
                    <span class="badge ${getWarningLevelBadgeClass(w.warningLevel)}">
                        ${w.warningLevel || '-'}
                    </span>
                </td>
                <td>${(w.riskScore || 0).toFixed(0)}</td>
                <td>${w.riskDescription || '-'}</td>
                <td>${new Date(w.createTime).toLocaleDateString()}</td>
                <td>
                    <button class="btn btn-success btn-small" 
                            onclick="handleWarning(${w.id})">
                        处理
                    </button>
                </td>
            </tr>
        `).join('');

        document.getElementById('warningTable').innerHTML = html ||
            '<tr><td colspan="7" style="text-align: center; color: var(--text-light);">暂无未处理预警</td></tr>';

        console.log('✓ 未处理预警加载完成');
    } catch (error) {
        console.error('加载未处理预警失败:', error);
    }
}

/**
 * 处理预警
 */
async function handleWarning(warningId) {
    const remark = prompt('请输入处理备注：');
    if (remark === null) return;

    try {
        await api.handleWarning(warningId, remark);
        alert('预警已处理');
        await loadUnhandledWarnings();
    } catch (error) {
        alert('处理失败: ' + error.message);
    }
}

/**
 * 获取风险等级名称
 */
function getRiskLevelName(level) {
    const names = {
        3: '高',
        2: '中',
        1: '低',
    };
    return names[level] || '-';
}

/**
 * 获取风险等级badge样式
 */
function getRiskLevelBadgeClass(level) {
    const classes = {
        3: 'badge-danger',
        2: 'badge-warning',
        1: 'badge-success',
    };
    return classes[level] || 'badge-info';
}

/**
 * 获取预警等级badge样式
 */
function getWarningLevelBadgeClass(level) {
    if (level === 'HIGH') return 'badge-danger';
    if (level === 'MEDIUM') return 'badge-warning';
    if (level === 'LOW') return 'badge-success';
    return 'badge-info';
}