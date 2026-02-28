package com.example.bysjdesign.campus.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 分析数据实体
 *
 * 用于存储用户的多维度分析结果，包含：
 * - 网络行为数据
 * - 门禁行为数据
 * - 借阅行为数据
 * - 综合风险评分
 */
@Entity
@Table(name = "analysis_data", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_analysis_date", columnList = "analysis_date"),
        @Index(name = "idx_risk_score", columnList = "risk_score"),
        @Index(name = "idx_user_date", columnList = "user_id,analysis_date")
})
public class AnalysisData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== 基础字段 ====================

    @Column(name = "user_id", nullable = false)
    private Integer userId;  // 类型从 Long 改为 Integer，与系统其他实体保持一致

    @Column(name = "analysis_date", nullable = false)
    private LocalDate analysisDate;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    // ==================== 网络行为字段 ====================

    @Column(name = "avg_online_hours")
    private Double avgOnlineHours; // 日均上网时长（小时）

    @Column(name = "total_online_minutes")
    private Long totalOnlineMinutes; // 总上网时长（分钟）

    @Column(name = "study_traffic_ratio")
    private Double studyTrafficRatio; // 学习流量占比

    @Column(name = "leisure_traffic_ratio")
    private Double leisureTrafficRatio; // 娱乐流量占比

    @Column(name = "abnormal_traffic_flag")
    private Boolean abnormalTrafficFlag; // 异常流量标志

    @Column(name = "network_activity_count")
    private Integer networkActivityCount; // 网络活动次数

    // ==================== 门禁行为字段 ====================

    @Column(name = "library_access_count")
    private Integer libraryAccessCount; // 图书馆进出次数

    @Column(name = "classroom_access_count")
    private Integer classroomAccessCount; // 教室进出次数

    @Column(name = "late_return_count")
    private Integer lateReturnCount; // 晚归次数

    @Column(name = "active_days")
    private Integer activeDays; // 活跃天数

    @Column(name = "avg_access_frequency")
    private Double avgAccessFrequency; // 日均进出频率

    // ==================== 借阅行为字段 ====================

    @Column(name = "borrow_activity_score")
    private Double borrowActivityScore; // 借阅活跃度评分（0-100）

    @Column(name = "borrow_count")
    private Long borrowCount; // 借阅次数

    @Column(name = "avg_borrow_days")
    private Double avgBorrowDays; // 平均借期（天）

    @Column(name = "unreturned_count")
    private Integer unreturnedCount; // 未归还图书数

    // ==================== 风险评分字段 ====================

    @Column(name = "risk_score", nullable = false)
    private Double riskScore; // 风险评分（0-100）

    @Column(name = "health_score")
    private Double healthScore; // 健康度评分（0-100）

    @Column(name = "network_risk")
    private Double networkRisk; // 网络维度风险分值

    @Column(name = "access_risk")
    private Double accessRisk; // 门禁维度风险分值

    @Column(name = "borrow_risk")
    private Double borrowRisk; // 借阅维度风险分值

    // ==================== 构造函数 ====================

    public AnalysisData() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public AnalysisData(Integer userId) {  // 参数类型改为 Integer
        this.userId = userId;
        this.analysisDate = LocalDate.now();
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    // ==================== Getters and Setters ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUserId() {  // 返回类型改为 Integer
        return userId;
    }

    public void setUserId(Integer userId) {  // 参数类型改为 Integer
        this.userId = userId;
    }

    public LocalDate getAnalysisDate() {
        return analysisDate;
    }

    public void setAnalysisDate(LocalDate analysisDate) {
        this.analysisDate = analysisDate;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Double getAvgOnlineHours() {
        return avgOnlineHours;
    }

    public void setAvgOnlineHours(Double avgOnlineHours) {
        this.avgOnlineHours = avgOnlineHours;
    }

    public Long getTotalOnlineMinutes() {
        return totalOnlineMinutes;
    }

    public void setTotalOnlineMinutes(Long totalOnlineMinutes) {
        this.totalOnlineMinutes = totalOnlineMinutes;
    }

    public Double getStudyTrafficRatio() {
        return studyTrafficRatio;
    }

    public void setStudyTrafficRatio(Double studyTrafficRatio) {
        this.studyTrafficRatio = studyTrafficRatio;
    }

    public Double getLeisureTrafficRatio() {
        return leisureTrafficRatio;
    }

    public void setLeisureTrafficRatio(Double leisureTrafficRatio) {
        this.leisureTrafficRatio = leisureTrafficRatio;
    }

    public Boolean getAbnormalTrafficFlag() {
        return abnormalTrafficFlag;
    }

    public void setAbnormalTrafficFlag(Boolean abnormalTrafficFlag) {
        this.abnormalTrafficFlag = abnormalTrafficFlag;
    }

    public Integer getNetworkActivityCount() {
        return networkActivityCount;
    }

    public void setNetworkActivityCount(Integer networkActivityCount) {
        this.networkActivityCount = networkActivityCount;
    }

    public Integer getLibraryAccessCount() {
        return libraryAccessCount;
    }

    public void setLibraryAccessCount(Integer libraryAccessCount) {
        this.libraryAccessCount = libraryAccessCount;
    }

    public Integer getClassroomAccessCount() {
        return classroomAccessCount;
    }

    public void setClassroomAccessCount(Integer classroomAccessCount) {
        this.classroomAccessCount = classroomAccessCount;
    }

    public Integer getLateReturnCount() {
        return lateReturnCount;
    }

    public void setLateReturnCount(Integer lateReturnCount) {
        this.lateReturnCount = lateReturnCount;
    }

    public Integer getActiveDays() {
        return activeDays;
    }

    public void setActiveDays(Integer activeDays) {
        this.activeDays = activeDays;
    }

    public Double getAvgAccessFrequency() {
        return avgAccessFrequency;
    }

    public void setAvgAccessFrequency(Double avgAccessFrequency) {
        this.avgAccessFrequency = avgAccessFrequency;
    }

    public Double getBorrowActivityScore() {
        return borrowActivityScore;
    }

    public void setBorrowActivityScore(Double borrowActivityScore) {
        this.borrowActivityScore = borrowActivityScore;
    }

    public Long getBorrowCount() {
        return borrowCount;
    }

    public void setBorrowCount(Long borrowCount) {
        this.borrowCount = borrowCount;
    }

    public Double getAvgBorrowDays() {
        return avgBorrowDays;
    }

    public void setAvgBorrowDays(Double avgBorrowDays) {
        this.avgBorrowDays = avgBorrowDays;
    }

    public Integer getUnreturnedCount() {
        return unreturnedCount;
    }

    public void setUnreturnedCount(Integer unreturnedCount) {
        this.unreturnedCount = unreturnedCount;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
        this.updateTime = LocalDateTime.now();
    }

    public Double getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(Double healthScore) {
        this.healthScore = healthScore;
    }

    public Double getNetworkRisk() {
        return networkRisk;
    }

    public void setNetworkRisk(Double networkRisk) {
        this.networkRisk = networkRisk;
    }

    public Double getAccessRisk() {
        return accessRisk;
    }

    public void setAccessRisk(Double accessRisk) {
        this.accessRisk = accessRisk;
    }

    public Double getBorrowRisk() {
        return borrowRisk;
    }

    public void setBorrowRisk(Double borrowRisk) {
        this.borrowRisk = borrowRisk;
    }

    // ==================== 辅助方法 ====================

    @Override
    public String toString() {
        return "AnalysisData{" +
                "id=" + id +
                ", userId=" + userId +
                ", analysisDate=" + analysisDate +
                ", riskScore=" + riskScore +
                ", healthScore=" + healthScore +
                '}';
    }

    // 以下方法可能需要在其他服务中实现，此处保留占位
    public Double getOverallHealthScore() {
        // 可根据需要实现综合健康度计算
        return healthScore;
    }

    public boolean getAbsenteeFlag() {
        // 可根据缺勤规则实现
        return false;
    }
}