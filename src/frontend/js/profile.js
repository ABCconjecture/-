/**
 * 用户画像页面脚本
 */

let currentClusterId = null;

// 页面加载
document.addEventListener('DOMContentLoaded', async () => {
    console.log('🎯 用户画像页面加载中...');

    try {
        await loadClusterDistribution();
    } catch (error) {
        console.error('加载数据失败:', error);
    }
});

/**
 * 加载聚类分布
 */
async function loadClusterDistribution() {
    try {
        const clusterCounts = await api.getClusterCounts();

        const chartElement = document.getElementById('clusterDistributionChart');
        const chart = echarts.init(chartElement);

        const colors = ['#667eea', '#764ba2', '#f093fb', '#4facfe', '#6bcf7f'];

        const option = {
            tooltip: {
                trigger: 'axis',
                backgroundColor: 'rgba(0, 0, 0, 0.7)',
                borderColor: '#667eea',
            },
            grid: {
                left: '3%',
                right: '3%',
                bottom: '3%',
                top: '5%',
                containLabel: true,
            },
            xAxis: {
                type: 'category',
                data: clusterCounts.map(c => `群体${c.clusterId}`),
                axisLine: { lineStyle: { color: '#e0e6ed' } },
                axisLabel: { color: '#7f8c8d' },
            },
            yAxis: {
                type: 'value',
                axisLine: { show: false },
                splitLine: { lineStyle: { color: '#e0e6ed' } },
                axisLabel: { color: '#7f8c8d' },
            },
            series: [
                {
                    type: 'bar',
                    data: clusterCounts.map((c, i) => ({
                        value: c.count,
                        itemStyle: { color: colors[i % colors.length] },
                    })),
                    barWidth: '60%',
                    borderRadius: [4, 4, 0, 0],
                },
            ],
        };

        chart.setOption(option);
        window.addEventListener('resize', () => chart.resize());

        console.log('✓ 聚类分布加载完成');
    } catch (error) {
        console.error('加载聚类分布失败:', error);
    }
}

/**
 * 加载聚类用户详情
 */
async function loadClusterProfile(clusterId) {
    try {
        currentClusterId = clusterId;
        const users = await api.getClusterUsers(clusterId);

        const html = (users || []).map(user => `
            <tr>
                <td>${user.studentId}</td>
                <td>${user.name}</td>
                <td>${user.college}</td>
                <td>
                    <span class="badge badge-primary">群体${user.clusterId}</span>
                </td>
                <td>
                    ${user.tags ? user.tags.split(',').map(tag =>
            `<span class="badge badge-info">${tag}</span>`
        ).join(' ') : '-'}
                </td>
                <td>
                    <span class="badge ${getHealthBadgeClass(parseFloat(user.healthScore))}">
                        ${user.healthScore || '0'}
                    </span>
                </td>
            </tr>
        `).join('');

        document.getElementById('clusterUsersTable').innerHTML = html ||
            '<tr><td colspan="6" style="text-align: center; color: var(--text-light);">暂无用户数据</td></tr>';

        console.log(`✓ 群体${clusterId}用户加载完成`);
    } catch (error) {
        console.error('加载聚类用户失败:', error);
        alert('加载失败: ' + error.message);
    }
}

/**
 * 获取健康度badge样式
 */
function getHealthBadgeClass(score) {
    if (score >= 70) return 'badge-success';
    if (score >= 50) return 'badge-warning';
    return 'badge-danger';
}