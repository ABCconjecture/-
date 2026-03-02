document.addEventListener('DOMContentLoaded', () => loadClusterDistribution());

async function loadClusterDistribution() {
    const dom = document.getElementById('clusterDistributionChart');
    if (!dom) return;
    try {
        const res = await api.getClusterCounts();
        const names = ["学术型", "均衡型", "活跃型", "沉迷型", "潜力型"];
        const chartData = names.map((name, i) => ({
            name: name,
            value: res.data['cluster_' + i] || 0
        }));

        const chart = echarts.init(dom);
        chart.setOption({
            xAxis: { type: 'category', data: chartData.map(d => d.name) },
            yAxis: { type: 'value' },
            series: [{ data: chartData.map(d => d.value), type: 'bar', itemStyle: { color: '#667eea' } }]
        });
    } catch (e) { console.error('加载聚类分布失败', e); }
}

async function loadClusterProfile(cid) {
    try {
        const res = await api.request(`/campus/cluster/${cid}/users`);
        const tbody = document.getElementById('clusterUsersTable');
        if (!tbody) return;
        tbody.innerHTML = res.data.map(u => `
            <tr><td>${u.studentId}</td><td>${u.name}</td><td>${u.college}</td><td>${cid+1}</td><td>${u.tags}</td><td>--</td></tr>
        `).join('');
    } catch(e) { alert('加载失败'); }
}