/**
 * 数据看板脚本
 */

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', async () => {
    console.log('📊 数据看板加载中...');

    try {
        // 加载统计数据
        await loadStats();

        // 加载趋势图
        await loadTrendChart();

        // 加载预警信息
        await loadWarningInfo();
    } catch (error) {
        console.error('加载数据失败:', error);
        showError('系统加载失败，请刷新页面重试');
    }
});

/**
 * 加载系统统计数据
 */
async function loadStats() {
    try {
        const response = await api.getStats();
        const stats = response.data || response;

        document.getElementById('totalUsers').textContent = stats.totalUsers || '0';
        document.getElementById('unhandledWarnings').textContent = stats.unhandledWarnings || '0';
        document.getElementById('highRiskCount').textContent = stats.highRiskUsers || '0';
        document.getElementById('avgHealth').textContent = (stats.avgHealthScore || 0).toFixed(1);

        console.log('✓ 统计数据加载完成:', stats);
    } catch (error) {
        console.error('加载统计数据失败:', error);
    }
}

/**
 * 加载趋势图
 */
async function loadTrendChart() {
    try {
        const response = await api.getTrend();
        const trendDataList = response.data || response;

        // Extract dates and health scores from analysis data
        const days = trendDataList.map(item => item.date || item.analysisDate || '');
        const healthTrend = trendDataList.map(item => item.healthScore || (100 - (item.riskScore || 0)));

        const chart = echarts.init(document.getElementById('trendChart'));
        const option = {
            tooltip: {
                trigger: 'axis',
                backgroundColor: 'rgba(0, 0, 0, 0.7)',
                borderColor: '#667eea',
            },
            legend: {
                data: ['健康度评分'],
                top: '3%',
            },
            grid: {
                left: '3%',
                right: '3%',
                bottom: '3%',
                top: '15%',
                containLabel: true,
            },
            xAxis: {
                type: 'category',
                data: days,
                boundaryGap: false,
                axisLine: { lineStyle: { color: '#e0e6ed' } },
                axisLabel: { color: '#7f8c8d' },
            },
            yAxis: {
                type: 'value',
                axisLine: { show: false },
                splitLine: { lineStyle: { color: '#e0e6ed' } },
                axisLabel: { color: '#7f8c8d' },
                min: 0,
                max: 100,
            },
            series: [
                {
                    name: '健康度评分',
                    type: 'line',
                    data: healthTrend,
                    smooth: true,
                    lineStyle: {
                        color: '#667eea',
                        width: 2,
                    },
                    areaStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
                            { offset: 1, color: 'rgba(102, 126, 234, 0.05)' },
                        ]),
                    },
                    itemStyle: { color: '#667eea' },
                    symbolSize: 6,
                },
            ],
        };

        chart.setOption(option);
        window.addEventListener('resize', () => chart.resize());

        console.log('✓ 趋势图加载完成');
    } catch (error) {
        console.error('加载趋势图失败:', error);
    }
}

/**
 * 加载预警相关信息
 */
async function loadWarningInfo() {
    try {
        // 加载预警统计
        const warningResponse = await api.getWarningStats();
        const warningStats = warningResponse.data || warningResponse;
        loadWarningTypeChart(warningStats);

        // 加载聚类信息
        const clusterResponse = await api.getClusterCounts();
        const clusterData = clusterResponse.data || clusterResponse;
        loadClusterChart(clusterData);

        console.log('✓ 预警和聚类信息加载完成');
    } catch (error) {
        console.error('加载预警信息失败:', error);
    }
}

/**
 * 加载预警类型图
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
            top: 'center',
            right: '10%',
            orient: 'vertical',
        },
        series: [
            {
                type: 'pie',
                radius: ['40%', '70%'],
                label: {
                    show: false,
                },
                data: (data.types || []).map((type, index) => ({
                    value: data.counts[index] || 0,
                    name: type,
                })) || [],
            },
        ],
    };

    chart.setOption(option);
    window.addEventListener('resize', () => chart.resize());
}

/**
 * 加载聚类图
 */
function loadClusterChart(data) {
    const chart = echarts.init(document.getElementById('clusterChart'));

    const colors = ['#667eea', '#764ba2', '#f093fb', '#4facfe', '#6bcf7f'];

    // Handle both direct array and object with data property
    const clusterList = Array.isArray(data) ? data : (data || []);

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
            data: clusterList.map(d => d.name || `群体${d.clusterId}`) || [],
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
                data: clusterList.map(d => d.count) || [],
                itemStyle: {
                    color: (params) => colors[params.dataIndex % colors.length],
                },
                barWidth: '60%',
                borderRadius: [4, 4, 0, 0],
            },
        ],
    };

    chart.setOption(option);
    window.addEventListener('resize', () => chart.resize());
}

/**
 * 显示错误信息
 */
function showError(message) {
    const alert = document.createElement('div');
    alert.className = 'alert alert-danger';
    alert.textContent = message;
    document.querySelector('.main-concent').insertBefore(alert, document.querySelector('.card'));

    setTimeout(() => alert.remove(), 5000);
}