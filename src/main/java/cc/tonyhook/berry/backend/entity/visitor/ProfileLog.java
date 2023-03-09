package cc.tonyhook.berry.backend.entity.visitor;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "visitor_profile_log", indexes = {
    @Index(columnList = "openid"),
    @Index(columnList = "resourceType"),
    @Index(columnList = "resourceId"),
    @Index(columnList = "action"),
    @Index(columnList = "updateTime"),
})
public class ProfileLog {

    public static final int PROFILE_LOG_AGGREGATE_LEVEL_LIFETIME  = 0;
    public static final int PROFILE_LOG_AGGREGATE_LEVEL_MONTH     = 1;
    public static final int PROFILE_LOG_AGGREGATE_LEVEL_HALFMONTH = 2;
    public static final int PROFILE_LOG_AGGREGATE_LEVEL_DAY       = 3;
    public static final int PROFILE_LOG_AGGREGATE_LEVEL_HALFDAY   = 4;

    public static final int PROFILE_LOG_VALUE_TYPE_NONE           = 0;
    public static final int PROFILE_LOG_VALUE_TYPE_BITMAP         = 1;
    public static final int PROFILE_LOG_VALUE_TYPE_BOOLEAN        = 2;
    public static final int PROFILE_LOG_VALUE_TYPE_NUMBER         = 3;
    public static final int PROFILE_LOG_VALUE_TYPE_STRING         = 4;
    public static final int PROFILE_LOG_VALUE_TYPE_TIMESTAMP      = 5;
    public static final int PROFILE_LOG_VALUE_TYPE_NUMBERINCR     = 6;

    public static final int PROFILE_LOG_ACTION_TYPE_LOGIN         = 0;
    public static final int PROFILE_LOG_ACTION_TYPE_VIEW          = 1;
    public static final int PROFILE_LOG_ACTION_TYPE_CLICK         = 2;
    public static final int PROFILE_LOG_ACTION_TYPE_DOWNLOAD      = 3;
    public static final int PROFILE_LOG_ACTION_TYPE_ACTION        = 4;
    public static final int PROFILE_LOG_ACTION_TYPE_LIKE          = 5;
    public static final int PROFILE_LOG_ACTION_TYPE_FAVORITE      = 6;
    public static final int PROFILE_LOG_ACTION_TYPE_SHARE         = 7;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String openid;

    private String resourceType;

    private Integer resourceId;

    private Integer aggregateLevel;

    private Integer valueType;

    private Integer action;

    @Column(length = 1024)
    private String value;

    private Timestamp createTime;

    private Timestamp updateTime;

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOpenid() {
        return this.openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getResourceType() {
        return this.resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Integer getResourceId() {
        return this.resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getAggregateLevel() {
        return this.aggregateLevel;
    }

    public void setAggregateLevel(Integer aggregateLevel) {
        this.aggregateLevel = aggregateLevel;
    }

    public Integer getValueType() {
        return this.valueType;
    }

    public void setValueType(Integer valueType) {
        this.valueType = valueType;
    }

    public Integer getAction() {
        return this.action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Timestamp getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

}
