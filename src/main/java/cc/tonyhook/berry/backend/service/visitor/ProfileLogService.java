package cc.tonyhook.berry.backend.service.visitor;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.visitor.ProfileLogRepository;
import cc.tonyhook.berry.backend.entity.visitor.ProfileBitmap;
import cc.tonyhook.berry.backend.entity.visitor.ProfileLog;
import jakarta.transaction.Transactional;

@Service
public class ProfileLogService {

    @Autowired
    private ProfileLogRepository profileLogRepository;

    public Page<ProfileLog> getProfileLogList(String openid, String resourceType, Integer resourceId, Integer action, String value, Pageable pageable) {
        Page<ProfileLog> profileLogPage = null;

        if (value == null) {
            if (resourceId == null && action == null) {
                profileLogPage = profileLogRepository.findByOpenidAndResourceTypeOrderByUpdateTimeDesc(openid, resourceType, pageable);
            }
            if (resourceId == null && action != null) {
                profileLogPage = profileLogRepository.findByOpenidAndResourceTypeAndActionOrderByUpdateTimeDesc(openid, resourceType, action, pageable);
            }
            if (resourceId != null && action == null) {
                profileLogPage = profileLogRepository.findByOpenidAndResourceTypeAndResourceIdOrderByUpdateTimeDesc(openid, resourceType, resourceId, pageable);
            }
            if (resourceId != null && action != null) {
                profileLogPage = profileLogRepository.findByOpenidAndResourceTypeAndResourceIdAndActionOrderByUpdateTimeDesc(openid, resourceType, resourceId, action, pageable);
            }
        } else {
            List<ProfileLog> profileLogList = getProfileLogList(openid, resourceType, resourceId, action);

            Iterator<ProfileLog> iterator = profileLogList.iterator();
            while (iterator.hasNext()) {
                ProfileLog profileLog = iterator.next();
                int valueType = profileLog.getValueType();
                Boolean qualified = true;

                if (valueType == ProfileLog.PROFILE_LOG_VALUE_TYPE_BITMAP) {
                    ProfileBitmap historyValue = ProfileBitmap.fromString(profileLog.getValue());
                    ProfileBitmap referenceValue = ProfileBitmap.fromString(value);

                    if (!historyValue.getFinished() && referenceValue.getFinished()) {
                        qualified = false;
                    }
                    if (historyValue.getPv() < referenceValue.getPv()) {
                        qualified = false;
                    }
                    if (historyValue.getTimelen() < referenceValue.getTimelen()) {
                        qualified = false;
                    }
                    if (historyValue.getPercentage() < referenceValue.getPercentage()) {
                        qualified = false;
                    }
                }

                if (valueType == ProfileLog.PROFILE_LOG_VALUE_TYPE_BOOLEAN) {
                    Boolean historyValue = Boolean.parseBoolean(profileLog.getValue());
                    Boolean referenceValue = Boolean.parseBoolean(value);

                    if (!historyValue.equals(referenceValue)) {
                        qualified = false;
                    }
                }

                if (valueType == ProfileLog.PROFILE_LOG_VALUE_TYPE_NUMBER || valueType == ProfileLog.PROFILE_LOG_VALUE_TYPE_NUMBERINCR) {
                    Double historyValue = NumberUtils.isCreatable(profileLog.getValue()) ? NumberUtils.toDouble(profileLog.getValue()) : 0;
                    Double referenceValue = NumberUtils.isCreatable(value) ? NumberUtils.toDouble(value) : 0;

                    if (historyValue < referenceValue) {
                        qualified = false;
                    }
                }

                if (valueType == ProfileLog.PROFILE_LOG_VALUE_TYPE_TIMESTAMP) {
                    Timestamp historyValue = null;
                    Timestamp referenceValue = null;
                    try {
                        historyValue = Timestamp.valueOf(profileLog.getValue());
                    } catch (Exception e) {
                        historyValue = new Timestamp(System.currentTimeMillis());
                    }
                    try {
                        referenceValue = Timestamp.valueOf(value);
                    } catch (Exception e) {
                        referenceValue = new Timestamp(System.currentTimeMillis());
                    }

                    if (historyValue.before(referenceValue)) {
                        qualified = false;
                    }
                }

                if (!qualified) {
                    iterator.remove();
                }
            }

            profileLogPage = new PageImpl<ProfileLog>(profileLogList, pageable, profileLogList.size());
        }

        return profileLogPage;
    }

    public List<ProfileLog> getProfileLogList(String openid, String resourceType, Integer resourceId, Integer action) {
        List<ProfileLog> profileLogList = new ArrayList<ProfileLog>();

        if (resourceId == null && action == null) {
            profileLogList = profileLogRepository.findByOpenidAndResourceTypeOrderByUpdateTimeDesc(openid, resourceType);
        }
        if (resourceId == null && action != null) {
            profileLogList = profileLogRepository.findByOpenidAndResourceTypeAndActionOrderByUpdateTimeDesc(openid, resourceType, action);
        }
        if (resourceId != null && action == null) {
            profileLogList = profileLogRepository.findByOpenidAndResourceTypeAndResourceIdOrderByUpdateTimeDesc(openid, resourceType, resourceId);
        }
        if (resourceId != null && action != null) {
            profileLogList = profileLogRepository.findByOpenidAndResourceTypeAndResourceIdAndActionOrderByUpdateTimeDesc(openid, resourceType, resourceId, action);
        }

        return profileLogList;
    }

    public ProfileLog getTopProfileLog(String openid, String resourceType, Integer resourceId, Integer action) {
        ProfileLog profileLog = null;

        if (resourceId == null && action == null) {
            profileLog = profileLogRepository.findTopByOpenidAndResourceTypeOrderByUpdateTimeDesc(openid, resourceType);
        }
        if (resourceId == null && action != null) {
            profileLog = profileLogRepository.findTopByOpenidAndResourceTypeAndActionOrderByUpdateTimeDesc(openid, resourceType, action);
        }
        if (resourceId != null && action == null) {
            profileLog = profileLogRepository.findTopByOpenidAndResourceTypeAndResourceIdOrderByUpdateTimeDesc(openid, resourceType, resourceId);
        }
        if (resourceId != null && action != null) {
            profileLog = profileLogRepository.findTopByOpenidAndResourceTypeAndResourceIdAndActionOrderByUpdateTimeDesc(openid, resourceType, resourceId, action);
        }

        return profileLog;
    }

    public void mergeProfileLog(ProfileLog newProfileLog) {
        String openid = newProfileLog.getOpenid();
        String resourceType = newProfileLog.getResourceType();
        Integer resourceId = newProfileLog.getResourceId();
        Integer aggregateLevel = newProfileLog.getAggregateLevel();
        Integer valueType = newProfileLog.getValueType();
        Integer action = newProfileLog.getAction();

        ProfileLog mergedProfileLog = getTopProfileLog(openid, resourceType, resourceId, action);

        if (mergedProfileLog == null) {
            mergedProfileLog = newProfileLog;
            mergedProfileLog.setCreateTime(new Timestamp(System.currentTimeMillis()));
            mergedProfileLog.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        } else {
            Boolean shouldAggrgate = false;
            if (aggregateLevel == ProfileLog.PROFILE_LOG_AGGREGATE_LEVEL_LIFETIME) {
                shouldAggrgate = true;
            }
            if (aggregateLevel == ProfileLog.PROFILE_LOG_AGGREGATE_LEVEL_MONTH) {
                Timestamp now = new Timestamp(System.currentTimeMillis());
                ZoneId zoneId = ZoneId.of("Asia/Shanghai");
                ZonedDateTime nowDateTime = now.toInstant().atZone(zoneId);
                ZonedDateTime lastDateTime = mergedProfileLog.getUpdateTime().toInstant().atZone(zoneId);

                if (nowDateTime.toString().substring(0, 7).equals(lastDateTime.toString().substring(0, 7))) {
                    shouldAggrgate = true;
                }
            }
            if (aggregateLevel == ProfileLog.PROFILE_LOG_AGGREGATE_LEVEL_HALFMONTH) {
                Timestamp now = new Timestamp(System.currentTimeMillis());
                ZoneId zoneId = ZoneId.of("Asia/Shanghai");
                ZonedDateTime nowDateTime = now.toInstant().atZone(zoneId);
                ZonedDateTime lastDateTime = mergedProfileLog.getUpdateTime().toInstant().atZone(zoneId);

                if (nowDateTime.toString().substring(0, 7).equals(lastDateTime.toString().substring(0, 7))) {
                    int nowDay = nowDateTime.getDayOfMonth();
                    int lastDay = lastDateTime.getDayOfMonth();
                    int days = nowDateTime.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
                    if ((nowDay - 1) / (days / 2) <= (lastDay - 1) / (days / 2)) {
                        shouldAggrgate = true;
                    }
                }
            }
            if (aggregateLevel == ProfileLog.PROFILE_LOG_AGGREGATE_LEVEL_DAY) {
                Timestamp now = new Timestamp(System.currentTimeMillis());
                ZoneId zoneId = ZoneId.of("Asia/Shanghai");
                ZonedDateTime nowDateTime = now.toInstant().atZone(zoneId);
                ZonedDateTime lastDateTime = mergedProfileLog.getUpdateTime().toInstant().atZone(zoneId);

                if (nowDateTime.toString().substring(0, 10).equals(lastDateTime.toString().substring(0, 10))) {
                    shouldAggrgate = true;
                }
            }
            if (aggregateLevel == ProfileLog.PROFILE_LOG_AGGREGATE_LEVEL_HALFDAY) {
                Timestamp now = new Timestamp(System.currentTimeMillis());
                ZoneId zoneId = ZoneId.of("Asia/Shanghai");
                ZonedDateTime nowDateTime = now.toInstant().atZone(zoneId);
                ZonedDateTime lastDateTime = mergedProfileLog.getUpdateTime().toInstant().atZone(zoneId);

                if (nowDateTime.toString().substring(0, 10).equals(lastDateTime.toString().substring(0, 10))) {
                    int nowHour = nowDateTime.getHour();
                    int lastHour = lastDateTime.getHour();
                    if (nowHour / 12 <= lastHour / 12) {
                        shouldAggrgate = true;
                    }
                }
            }

            if (!shouldAggrgate) {
                mergedProfileLog = newProfileLog;
                mergedProfileLog.setCreateTime(new Timestamp(System.currentTimeMillis()));
                mergedProfileLog.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            } else {
                String value = "";
                String newValue = newProfileLog.getValue();
                String oldValue = mergedProfileLog.getValue();

                if (valueType == ProfileLog.PROFILE_LOG_VALUE_TYPE_NONE) {
                    value = null;
                }
                if (valueType == ProfileLog.PROFILE_LOG_VALUE_TYPE_BITMAP) {
                    ProfileBitmap oldBitmap = ProfileBitmap.fromString(oldValue);
                    ProfileBitmap newBitmap = ProfileBitmap.fromString(newValue);
                    newBitmap.merge(oldBitmap);
                    value = newBitmap.toString();
                }
                if (valueType == ProfileLog.PROFILE_LOG_VALUE_TYPE_BOOLEAN) {
                    value = String.valueOf(Boolean.parseBoolean(newValue));
                }
                if (valueType == ProfileLog.PROFILE_LOG_VALUE_TYPE_NUMBER) {
                    value = NumberUtils.isCreatable(newValue) ? newValue : "0";
                }
                if (valueType == ProfileLog.PROFILE_LOG_VALUE_TYPE_STRING) {
                    value = newValue.length() > 1024 ? newValue.substring(0, 1024) : newValue;
                }
                if (valueType == ProfileLog.PROFILE_LOG_VALUE_TYPE_TIMESTAMP) {
                    try {
                        value = Timestamp.valueOf(newValue).toString();
                    } catch (Exception e) {
                        value = new Timestamp(System.currentTimeMillis()).toString();
                    }
                }
                if (valueType == ProfileLog.PROFILE_LOG_VALUE_TYPE_NUMBERINCR) {
                    Double oldNumber = NumberUtils.isCreatable(oldValue) ? NumberUtils.toDouble(oldValue) : 0;
                    Double newNumber = NumberUtils.isCreatable(newValue) ? NumberUtils.toDouble(newValue) : 0;
                    value = String.valueOf(oldNumber + newNumber);
                }

                mergedProfileLog.setValue(value);
                mergedProfileLog.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            }
        }

        profileLogRepository.save(mergedProfileLog);
    }

    public List<ProfileLog> getProfileLogList() {
        List<ProfileLog> profileLogList = profileLogRepository.findAll();

        return profileLogList;
    }

    public ProfileLog getProfileLog(Integer id) {
        ProfileLog profileLog = profileLogRepository.findById(id).orElse(null);

        return profileLog;
    }

    public ProfileLog addProfileLog(ProfileLog newProfileLog) {
        ProfileLog updatedProfileLog = profileLogRepository.save(newProfileLog);

        return updatedProfileLog;
    }

    public void updateProfileLog(Integer id, ProfileLog newProfileLog) {
        profileLogRepository.save(newProfileLog);
    }

    @Transactional
    public void removeProfileLog(Integer id) {
        ProfileLog deletedProfileLog = profileLogRepository.findById(id).orElse(null);

        profileLogRepository.delete(deletedProfileLog);
    }

    @Transactional
    public void removeProfileLogs(List<ProfileLog> profileLogList) {
        profileLogRepository.deleteAll(profileLogList);
    }

}
