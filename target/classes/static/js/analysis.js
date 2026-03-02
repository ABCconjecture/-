let radarChart = null;

document.addEventListener('DOMContentLoaded', async () => {
    initChart();
    const hash = window.location.hash.substring(1);
    const params = new URLSearchParams(hash);
    const uid = params.get('user');
    if (uid) {
        document.getElementById('searchInput').value = uid;
        setTimeout(() => searchUser(), 500);
    }
});

function initChart() {
    const dom = document.getElementById('userRadarChart');
    if (dom) radarChart = echarts.init(dom);
}

async function searchUser() {
    const input = document.getElementById('searchInput')?.value.trim();
    if (!input) return;
    try {
        const profileData = await api.getProfile(input);
        const analysis = await api.getAnalysis(profileData.user.userId);
        const warnings = await api.getUserWarnings(profileData.user.userId);
        renderAnalysisResult(profileData.user, analysis, warnings);
    } catch (e) {
        alert('未找到数据，请确保已执行数据初始化或预警检查');
    }
}

function renderAnalysisResult(user, analysis, warnings) {
    document.getElementById('noUserSelected').style.display = 'none';
    document.getElementById('userAnalysisContainer').style.display = 'block';

    document.getElementById('userBasicInfo').innerHTML = `
        <p><strong>姓名：</strong>${user.name}</p>
        <p><strong>学号：</strong>${user.studentId}</p>
    `;

    document.getElementById('userMetrics').innerHTML = `
        <div class="metric-card info"><div class="metric-label">日均上网</div><div class="metric-value">${(analysis.avgOnlineHours || 0).toFixed(1)}h</div></div>
        <div class="metric-card pink"><div class="metric-label">学习占比</div><div class="metric-value">${((analysis.studyTrafficRatio || 0)*100).toFixed(0)}%</div></div>
        <div class="metric-card danger"><div class="metric-label">健康度</div><div class="metric-value">${(analysis.healthScore || 0).toFixed(0)}</div></div>
    `;

    renderRadar(analysis);
    const warnTable = document.getElementById('userWarningsTable');
    if (warnTable) {
        warnTable.innerHTML = warnings.length ? warnings.map(w => `
            <tr><td>${w.warningType}</td><td><span class="badge badge-danger">${w.warningLevel}</span></td><td>${w.riskScore}</td><td>${w.riskDescription}</td><td>${new Date(w.createTime).toLocaleDateString()}</td></tr>`).join('') : '<tr><td colspan="5">无预警</td></tr>';
    }
}

function renderRadar(data) {
    if (!radarChart) return;
    const val = (v, def) => (v != null ? v : def);
    const vals = [
        val((data.studyTrafficRatio || 0) * 100, 0),
        val((data.avgOnlineHours || 0) * 10, 0),
        Math.min(val(data.libraryAccessCount, 0) * 10, 100),
        val(data.healthScore || 0, 0),
        Math.min(val(data.borrowCount, 0) * 20, 100)
    ];
    radarChart.setOption({
        radar: { indicator: [{name:'学习'}, {name:'上网'}, {name:'图书馆'}, {name:'健康'}, {name:'借阅'}], shape: 'circle' },
        series: [{ type: 'radar', data: [{ value: vals, name: '特征', areaStyle: { color: 'rgba(102, 126, 234, 0.4)' } }] }]
    }, true);
}