/**
 * 多维度分析页面脚本
 */

let selectedUserId = null;

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', async () => {
    console.log('📊 多维度分析页面加载中...');

    try {
        // 加载系统统计
        await loadStats();
    } catch (error) {
        console.error('加载数据失败:', error);
    }

    // 绑定搜索框回车事件
    document.getElementById('searchInput').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            searchUser();
        }
    });
});

/**
 * 加载系统统计数据
 */
async function loadStats() {
    try {
        const stats = await api.getStats();
        document.getElementById('totalUsers').textContent = stats.totalUsers || '0';
        document.getElementById('highRiskCount').textContent = stats.highRiskUsers || '0';
        document.getElementById('avgHealth').textContent = (stats.avgHealthScore || 0).toFixed(1);

        console.log('✓ 统计数据加载完成');
    } catch (error) {
        console.error('加载统计数据失败:', error);
    }
}

/**
 * 搜索用户
 */
async function searchUser() {
    const searchInput = document.getElementById('searchInput').value.trim();

    if (!searchInput) {
        alert('请输入学号或姓名');
        return;
    }

    try {
        const users = await api.getUsers();
        const user = users.find(u =>
            u.studentId.toString().includes(searchInput) ||
            u.name.includes(searchInput)
        );

        if (!user) {
            alert('未找到该用户');
            return;
        }

        selectedUserId = user.userId;
        await displayUserAnalysis(user.userId);
    } catch (error) {
        console.error('搜索用户失败:', error);
        alert('搜索失败: ' + error.message);
    }
}

/**
 * 显示用户分析
 */
async function displayUserAnalysis(userId) {
    try {
        // 获取用户信息和分析数据
        const [profileData, analysisData, warningsData, historyData, radarData] = await Promise.all([
            api.getProfile(userId),
            api.getAnalysis(userId),
            api.getUserWarnings(userId),
            api.getAnalysisHistory(userId),
            api.getProfileRadar(userId),
        ]);

        // 显示用户基本信息
        displayUserBasicInfo(profileData.user);

        // 显示多维度指标
        if (analysisData) {
            displayUserMetrics(analysisData);
        }

        // 显示雷达图
        if (radarData && radarData.value) {
            displayRadarChart(radarData);
        }

        // 显示分析历史
        if (historyData) {
            displayAnalysisHistory(historyData);
        }

        // 显示预警信息
        if (warningsData) {
            displayUserWarnings(warningsData);
        }

        // 显示分析容器，隐藏提示
        document.getElementById('userAnalysisContainer').style.display = 'block';
        document.getElementById('noUserSelected').style.display = 'none';

        console.log('✓ 用户分析数据加载完成');
    } catch (error) {
        console.error('加载用户分析失败:', error);
        alert('加载分析数据失败: ' + error.message);
    }
}

/**
 * 显示用户基本信息
 */
function displayUserBasicInfo(user) {
    const html = `
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
            <div>
                <strong>学号：</strong><span>${user.studentId || '-'}</span>
            </div>
            <div>
                <strong>姓名：</strong><span>${user.name || '-'}</span>
            </div>
            <div>
                <strong>性别：</strong><span>${user.gender || '-'}</span>
            </div>
            <div>
                <strong>学院：</strong><span>${user.college || '-'}</span>
            </div>
            <div>
                <strong>专业：</strong><span>${user.major || '-'}</span>
            </div>
            <div>
                <strong>班级：</strong><span>${user.clazz || '-'}</span>
            </div>
        </div>
    `;
    document.getElementById('userBasicInfo').innerHTML = html;
}

/**
 * 显示多维度指标
 */
function displayUserMetrics(analysis) {
    const metrics = [
        {
            label: '日均上网时长',
            value: (analysis.avgDailyOnlineHours || 0).toFixed(1),
            unit: '小时',
            color: '#667eea',
        },
        {
            label: '学习流量占比',
            value: ((analysis.studyTrafficRatio || 0) * 100).toFixed(0),
            unit: '%',
            color: '#764ba2',
        },
        {
            label: '休闲流量占比',
            value: ((analysis.leisureTrafficRatio || 0) * 100).toFixed(0),
            unit: '%',
            color: '#f5576c',
        },
        {
            label: '教学楼进出次数',
            value: analysis.classroomAccessCount || '0',
            unit: '次',
            color: '#4facfe',
        },
        {
            label: '借阅活跃度',
            value: (analysis.borrowActivityScore || 0).toFixed(0),
            unit: '分',
            color: '#6bcf7f',
        },
        {
            label: '晚归次数',
            value: analysis.lateReturnCount || '0',
            unit: '次',
            color: '#ffd93d',
        },
        {
            label: '月均学习时长',
            value: (analysis.monthlystudyHours || 0).toFixed(1),
            unit: '小时',
            color: '#00f2fe',
        },
        {
            label: '综合健康度',
            value: (analysis.overallHealthScore || 0).toFixed(1),
            unit: '/100',
            color: '#667eea',
        },
    ];

    const html = metrics.map(m => `
        <div class="metric-card" style="background: linear-gradient(135deg, ${m.color} 0%, ${adjustColor(m.color, -20)} 100%);">
            <div class="metric-label">${m.label}</div>
            <div class="metric-value">${m.value}</div>
            <div class="metric-unit">${m.unit}</div>
        </div>
    `).join('');

    document.getElementById('userMetrics').innerHTML = html;
}

/**
 * 显示雷达图
 */
function displayRadarChart(radarData) {
    const chartElement = document.getElementById('userRadarChart');
    const chart = echarts.init(chartElement);

    const option = {
        radar: {
            indicator: (radarData.indicator || []).map((name, index) => ({
                name,
                max: 100,
            })),
            shape: 'circle',
            splitNumber: 4,
            name: {
                textStyle: {
                    color: '#666',
                    fontSize: 11,
                },
            },
            splitLine: {
                lineStyle: {
                    color: ['#e0e6ed', '#e0e6ed', '#e0e6ed', '#e0e6ed'],
                },
            },
            splitArea: {
                areaStyle: {
                    color: ['rgba(102, 126, 234, 0.05)', 'rgba(102, 126, 234, 0.1)'],
                },
            },
            axisLine: {
                lineStyle: {
                    color: '#ddd',
                },
            },
        },
        series: [
            {
                type: 'radar',
                data: [
                    {
                        value: radarData.value || [],
                        name: '用户数据',
                    },
                ],
                areaStyle: {
                    opacity: 0.4,
                },
                itemStyle: {
                    color: '#667eea',
                },
                lineStyle: {
                    color: '#667eea',
                },
                label: {
                    show: true,
                    color: '#667eea',
                },
            },
        ],
    };

    chart.setOption(option);
    window.addEventListener('resize', () => chart.resize());
}

/**
 * 显示分析历史
 */
function displayAnalysisHistory(historyData) {
    if (!historyData || historyData.length === 0) {
        document.getElementById('analysisHistoryTable').innerHTML =
            '<tr><td colspan="6" style="text-align: center; color: var(--text-light);">暂无历史数据</td></tr>';
        return;
    }

    const html = historyData.map(item => `
        <tr>
            <td>${new Date(item.analysisDate).toLocaleDateString()}</td>
            <td>${(item.avgDailyOnlineHours || 0).toFixed(1)}h</td>
            <td>${((item.studyTrafficRatio || 0) * 100).toFixed(0)}%</td>
            <td>${item.classroomAccessCount || 0}</td>
            <td>${(item.borrowActivityScore || 0).toFixed(0)}</td>
            <td>
                <span class="badge ${getHealthBadgeClass(item.overallHealthScore)}">
                    ${(item.overallHealthScore || 0).toFixed(1)}
                </span>
            </td>
        </tr>
    `).join('');

    document.getElementById('analysisHistoryTable').innerHTML = html;
}

/**
 * 显示用户预警信息
 */
function displayUserWarnings(warnings) {
    if (!warnings || warnings.length === 0) {
        document.getElementById('userWarningsTable').innerHTML =
            '<tr><td colspan="5" style="text-align: center; color: var(--text-light);">暂无预警信息</td></tr>';
        return;
    }

    const html = warnings.map(w => {
        const levelClass = {
            'HIGH': 'table-status-high',
            'MEDIUM': 'table-status-medium',
            'LOW': 'table-status-low',
        }[w.warningLevel] || '';

        return `
            <tr>
                <td>${w.warningType || '-'}</td>
                <td><span class="${levelClass}">${w.warningLevel || '-'}</span></td>
                <td>${(w.riskScore || 0).toFixed(0)}</td>
                <td>${w.riskDescription || '-'}</td>
                <td>${new Date(w.createTime).toLocaleDateString()}</td>
            </tr>
        `;
    }).join('');

    document.getElementById('userWarningsTable').innerHTML = html;
}

/**
 * 获取健康度badge样式
 */
function getHealthBadgeClass(score) {
    if (score >= 70) return 'badge-success';
    if (score >= 50) return 'badge-warning';
    return 'badge-danger';
}

/**
 * 调整颜色亮度（简单的颜色调整）
 */
function adjustColor(color, percent) {
    // 这是一个简化版本，实际项目中可能需要更复杂的颜色处理
    return color;
}