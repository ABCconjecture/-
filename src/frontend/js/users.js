/**
 * 用户列表页面脚本
 */

let currentPage = 0;
const pageSize = 50;
let allUsers = [];

// 页面加载
document.addEventListener('DOMContentLoaded', () => {
    loadUsers();
});

/**
 * 加载用户列表
 */
async function loadUsers() {
    try {
        const response = await api.getUsers(0, 1000);
        allUsers = response.data || response;
        displayUsers();
    } catch (error) {
        console.error('加载用户列表失败:', error);
        showError('加载用户列表失败');
    }
}

/**
 * 显示用户列表
 */
function displayUsers() {
    const tbody = document.getElementById('usersTable');

    if (allUsers.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" style="text-align: center; color: var(--text-light);">暂无用户数据</td></tr>';
        return;
    }

    // 获取当前页的用户
    const startIndex = currentPage * pageSize;
    const endIndex = startIndex + pageSize;
    const pageUsers = allUsers.slice(startIndex, endIndex);

    const html = pageUsers.map(user => {
        const riskFlag = user.riskFlag === '⚠️' ? '有异常' : '正常';
        const statusClass = user.riskFlag === '⚠️' ? 'table-status-high' : 'table-status-low';

        return `
            <tr>
                <td>${user.studentId}</td>
                <td>${user.name}</td>
                <td>${user.gender || '-'}</td>
                <td>${user.college}</td>
                <td>${user.major || '-'}</td>
                <td>
                    <span class="badge badge-primary">群体 ${user.cluster}</span>
                </td>
                <td>
                    <span class="badge ${getHealthBadgeClass(parseFloat(user.healthScore))}" 
                          style="font-weight: bold;">
                        ${user.healthScore || '0'}
                    </span>
                </td>
                <td>
                    <span class="${statusClass}">${riskFlag}</span>
                </td>
                <td>
                    <button class="btn btn-primary btn-small" 
                            onclick="viewUserDetail(${user.userId})">
                        详情
                    </button>
                </td>
            </tr>
        `;
    }).join('');

    tbody.innerHTML = html;

    // 更新分页信息
    const totalPages = Math.ceil(allUsers.length / pageSize);
    document.getElementById('pageInfo').textContent = `第 ${currentPage + 1} / ${totalPages} 页`;
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
 * 下一页
 */
function next() {
    const totalPages = Math.ceil(allUsers.length / pageSize);
    if (currentPage < totalPages - 1) {
        currentPage++;
        displayUsers();
        window.scrollTo(0, 0);
    }
}

/**
 * 上一页
 */
function prev() {
    if (currentPage > 0) {
        currentPage--;
        displayUsers();
        window.scrollTo(0, 0);
    }
}

/**
 * 查看用户详情
 */
function viewUserDetail(userId) {
    // 这里可以跳转到详情页面，或者在模态框中显示
    window.location.href = `analysis.html#user=${userId}`;
}

/**
 * 显示错误信息
 */
function showError(message) {
    alert(message);
}