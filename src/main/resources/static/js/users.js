let currentPage = 0;
const pageSize = 15;

document.addEventListener('DOMContentLoaded', () => loadUsers());

async function loadUsers() {
    try {
        console.log('正在请求用户列表...');
        const response = await api.getUsers(currentPage, pageSize);
        console.log('API 返回数据:', response);

        // 确保 data 存在
        const users = response.data || [];
        renderTable(users);

        const pageInfo = document.getElementById('pageInfo');
        if (pageInfo) pageInfo.textContent = `第 ${currentPage + 1} 页`;
    } catch (e) {
        console.error('加载用户列表失败:', e);
        alert('加载列表失败，请查看控制台日志');
    }
}

function renderTable(users) {
    const tbody = document.getElementById('usersTable');
    if (!tbody) return;

    if (!users || users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" style="text-align: center;">暂无用户数据，请检查后端数据库或初始化任务</td></tr>';
        return;
    }

    tbody.innerHTML = users.map(u => `
        <tr>
            <td>${u.studentId || '-'}</td>
            <td>${u.name || '-'}</td>
            <td>${u.gender || '未知'}</td>
            <td>${u.college || '-'}</td>
            <td>${u.major || '未分配'}</td>
            <td>群体 ${u.cluster + 1}</td>
            <td><span class="badge ${u.healthScore < 60 ? 'badge-danger' : 'badge-success'}">${u.healthScore}</span></td>
            <td>${u.status == 1 ? '正常' : '预警'}</td>
            <td><button class="btn btn-primary btn-small" onclick="viewDetail(${u.userId})">分析详情</button></td>
        </tr>`).join('');
}

function viewDetail(id) {
    window.location.href = `analysis.html#user=${id}`;
}

function next() { currentPage++; loadUsers(); }
function prev() { if (currentPage > 0) { currentPage--; loadUsers(); } }